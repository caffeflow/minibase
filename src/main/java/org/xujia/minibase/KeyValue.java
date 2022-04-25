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
     *     下述代码中：rawKey和key等价意思，对应着hbase中的key部分，但也有区别的。
     *     区别在于，在hbase中key囊括rowKey,family,qualifier的固定长度字段和变长的值字段，rowKey根据进行排序（横向划分）,
     *     但在本代码中不考虑key的内部字段划分,直接使用key进行排序了。
     *
     *     关于存储空间：字段划分为rawKeyLen+valueLen+rawKey+Op+sequenceId+value的可变字节数组（是序列化的数组）
     *              但是注意除了rawKey和value是可变长度的字段外，其余都是固定长度的字段（这就需要Bytes类来转化为定长了）。
     *
     */
    public static final int RAW_KEY_LEN_SIZE = 4; // 用于保存长度值的字段，并非key的真实长度
    public static final int VAL_LEN_SIZE = 4;
    public static final int OP_SIZE = 1;
    public static final int SEQ_ID_SIZE = 4;

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

    private int getRawKeyLen(){return key.length + SEQ_ID_SIZE + OP_SIZE;} // key是变长

    public byte[] toBytes() throws IOException { // 将KeyValue对象编码到字节数组
        /**
         *         存储空间：字段划分为rawKeyLen+valueLen+rawKey+Op+sequenceId+value的可变字节数组。
         *         但是注意除了rawKey和value是可变长度的字段外，其余都是固定长度的字段（这就需要Bytes类来转化为定长了）。
         */
        int pos = 0;
        byte[] bytes = new byte[getSerializeSize()]; // 创建存储空间
        // encode raw key length
        int rawKeyLen = getRawKeyLen();
        byte[] rowKeyLenBytes = Bytes.toBytes(rawKeyLen); // 将rawKey的长度值保存到固定长度的byte数组中。
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

    public static KeyValue parseFrom(byte[] bytes, int offset) throws IOException {
        /**
         *  // 从字节数组解码到KeyValue对象
         */
        if (bytes == null){
            throw new IOException("buffer is null");
        }
        if (bytes.length <= offset + RAW_KEY_LEN_SIZE + VAL_LEN_SIZE){
            throw new IOException("Invalid offset or bytes.len.  offset:"  + offset + ",len:" + bytes.length);
        }
        // Decode raw key length
        int pos = offset;
        int rawKeyLen = Bytes.toInt(Bytes.slice(bytes,pos,RAW_KEY_LEN_SIZE)); // 解码为int
        pos += RAW_KEY_LEN_SIZE;

        // Decode key


    }

    public static org.apache.minibase.KeyValue parseFrom(byte[] bytes) throws IOException {return null;}

    private static class KeyValueComparator implements Comparator<org.apache.minibase.KeyValue> {
        @Override
        public int compare(org.apache.minibase.KeyValue a, org.apache.minibase.KeyValue b) {
            return 0;
        }
    };





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



}
