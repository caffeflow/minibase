package org.xujia.minibase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * @author xj
 * @create 2022-04-20 20:08
 *
 * KeyValue： key,value字节数组，增删改操作类型，自增序列号.
 **/
public class KeyValue implements Comparable<KeyValue>{
    /**
     *     （1）在hbase的存储字节空间构成为：
     *          keyLen(4B) + ValueLen(4B)
     *          + RowKeyLen(2B) + RowKeyBytes(变长)
     *          + FamilyLen(1B) + FamilyBytes(变长)
     *          + QualifierBytes(变长)
     *          + TimeStamp(8B)
     *          + Type(1B)
     *          + value
     *
     *     key = rowKeyBytes + familyBytes + qualifierBytes + TimeStamp + Type
     *     QualifierBytes变长，但是其长度可以被推断出来。
     *     KeyValue排序依据是以ASCII码为比较，rowKeyBytes升序->familyBytes升序-> qualifierBytes升序 -> timeStamp降序 -> type升序
     *
     *
     *     （2）本代码中存储空间的构成：
     *          rawKeyLen(4B) + ValueKeyLen(4B)
     *          + Key(变长)
     *          + Op(1B)
     *          + sequenceId(4B)
     *          + Value(变长)
     *     区别1：相比Hbase的列簇存储,这里简化了key部分，没有family和qualifier设计，使之成为列式存储。
     *     区别2：字段排序有些不同,本代码中Op在sequenceId之前。
     *     区别3：字段命名差异。本代码中rawKey表示[key+sequenceId+Op]部分,注意此处rawKey对应hbase中key,而此处key对应hbase中rowKey。
     *     对keyValue排序时以ASCII码为比较，排序为: rawKey升序 -> sequenceId降序 -> Op升序
     *
     *     一些补充：我们设计的Bytes类提供了编码与解码功能，可以用于KeyValue对象的反/序列化。
     *
     */
    public static final int RAW_KEY_LEN_SIZE = 4; // 用于保存长度值的字段，并非key的真实长度
    public static final int VAL_LEN_SIZE = 4;
    public static final int OP_SIZE = 1;
    public static final int SEQ_ID_SIZE = 8;

    public static final KeyValueComparator KV_CMP = new KeyValueComparator();

    private byte[] key;
    private byte[] value;
    private Op op;
    private long sequenceId;

    public KeyValue(byte[] key,byte[] value,Op op,long sequenceId){
        assert key != null;
        assert value != null;
        assert op != null;
        assert sequenceId > 0;
        this.key = key;
        this.value = value;
        this.op = op;
        this.sequenceId = sequenceId;
    }

    public enum Op{
        Put((byte) 0),Delete((byte) 1);
        private byte code;
        Op(byte code){
            this.code = code;
        }
        public static Op code2Op(byte code){
            switch (code){
                case 0 : return Put;
                case 1 : return Delete;
                default:
                    throw new IllegalArgumentException("unknow code:" + code);
            }
        }

        public byte getCode(){
            return this.code;
        }
    }

    public static KeyValue create(byte[] key,byte[] value,Op op,long sequenceId){
        return new KeyValue(key,value,op,sequenceId);
    }

    public static KeyValue createPut(byte[] key,byte[] value,long sequenceId){
        return KeyValue.create(key,value,Op.Put,sequenceId);
    }

    public static KeyValue createDelete(byte[] key,long sequenceId){
        return KeyValue.create(key,Bytes.EMPTY_BYTES,Op.Delete,sequenceId);
    }

    public byte[] getKey(){return key;}

    public byte[] getValue(){return value;}

    public Op getOp(){return op;}

    public long getSequenceId(){return this.sequenceId;}

    private int getRawKeyLen(){return key.length + SEQ_ID_SIZE + OP_SIZE;}

    public byte[] toBytes() throws IOException { // 将KeyValue对象编码到字节数组
        /**
         *         存储空间：字段划分为rawKeyLen+valueLen+rawKey+Op+sequenceId+value的可变字节数组。
         *         但是注意除了rawKey和value是可变长度的字段外，其余都是固定长度的字段（这就需要Bytes类来转化为定长了）。
         */
        int pos = 0;
        byte[] bytes = new byte[getSerializeSize()]; // 创建存储空间

        // encode raw key length
        int rawKeyLen = getRawKeyLen();
        byte[] rowKeyLenBytes = Bytes.toBytes(rawKeyLen);
        System.arraycopy(rowKeyLenBytes,0,bytes,pos,RAW_KEY_LEN_SIZE);
        pos += RAW_KEY_LEN_SIZE;

        // encode value length
        byte[] valueLenBytes = Bytes.toBytes(value.length);
        System.arraycopy(valueLenBytes,0,bytes,pos,VAL_LEN_SIZE);
        pos += VAL_LEN_SIZE;

        // encode key
        System.arraycopy(key,0,bytes,pos,key.length);
        pos += key.length;

        // encode Op
        bytes[pos] = op.getCode();
        pos+=1;

        // encode sequenceId
        byte[] sequenceIdBytes = Bytes.toBytes(sequenceId);
        System.arraycopy(sequenceIdBytes,0,bytes,pos,sequenceIdBytes.length);
        pos += sequenceIdBytes.length;

        // encode value
        System.arraycopy(value,0,bytes,pos,value.length);
        pos += value.length;
        return bytes;
    }

    @Override
    public boolean equals(Object kv) {
        if (kv == null) return false;
        if (!(kv instanceof KeyValue) ) return false;
        return this.compareTo((KeyValue) kv) == 0;
    }

    public int getSerializeSize(){return RAW_KEY_LEN_SIZE + VAL_LEN_SIZE + getRawKeyLen() + value.length;}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("key=").append(Bytes.toHex(this.key))
                .append("/op=").append(this.op)
                .append("/sequenceId=").append(this.sequenceId)
                        .append("/value=").append((Bytes.toHex(this.value)));
        return sb.toString();
    }

    public static KeyValue parseFrom(byte[] bytes, int offset) throws IOException { // 从字节数组反序列化到KeyValue对象

        if (bytes == null){
            throw new IOException("buffer is null");
        }
        if (bytes.length <= offset + RAW_KEY_LEN_SIZE + VAL_LEN_SIZE){
            throw new IOException("Invalid offset or bytes.len.  offset:"  + offset + ",len:" + bytes.length);
        }
        int pos = offset;

        // Decode raw key length
        int rawKeyLen = Bytes.toInt(Bytes.slice(bytes,pos,RAW_KEY_LEN_SIZE)); // 解码为int,以确定rawKey的长度。
        pos += RAW_KEY_LEN_SIZE;

        // Decode value length
        int valueLen = Bytes.toInt(Bytes.slice(bytes,pos,VAL_LEN_SIZE));
        pos += VAL_LEN_SIZE;

        // Decode key
        int keyLen = rawKeyLen - OP_SIZE - SEQ_ID_SIZE;  // -- 推断key的长度
        byte[] key = Bytes.slice(bytes, pos, keyLen);

        // Decode Op
        Op op = Op.code2Op(bytes[pos]);
        pos+=1;

        // Decode SEQ_Id
        long sequenceId = Bytes.toLong(Bytes.slice(bytes,pos,SEQ_ID_SIZE));
        pos += SEQ_ID_SIZE;

        // Decode Value
        byte[] value = Bytes.slice(bytes, pos, valueLen);

        // 构建对象
        return create(key,value,op,sequenceId);
    }

    public static KeyValue parseFrom(byte[] bytes) throws IOException {
        return parseFrom(bytes,0);
    }


    @Override
    public int compareTo(KeyValue kv) {
        if (kv == null)
            throw new IllegalArgumentException("kv to compare should be null");
        int res = Bytes.compare(this.key,kv.key);
        if (res != 0) return res;
        if (this.sequenceId != kv.sequenceId) return this.sequenceId >= kv.sequenceId ? -1 : 1;
        if (this.op != kv.op) return this.op.getCode() > op.getCode() ? -1 : 1;
        return 0;
    }

    private static class KeyValueComparator implements Comparator<KeyValue> {
        @Override
        public int compare(KeyValue o1, KeyValue o2) {
            if (o1 == o2) return 0;
            if (o1 == null ) return -1;
            if (o2 == null)  return 1;
            return o1.compareTo(o2);
        }
    };


}
