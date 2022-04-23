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
     */
    public static final int RAW_KEY_LEN_SIZE = 4;
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

    public byte[] toBytes() throws IOException {
        // 字节数组：rawKeyLen+valueLen+rawKey+Op+sequenceId+value
        int rawKeyLen = getRawKeyLen();
        int pos = 0;
        byte[] bytes = new byte[getSerializeSize()];
        // encode raw key length
        byte[] rowKeyLenBytes = Bytes.toBytes(rawKeyLen);
        System.arraycopy(rowKeyLenBytes,0,bytes,pos,RAW_KEY_LEN_SIZE); // 只去raw key部门
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
    public boolean equals(Object kv) {return true;}

    public int getSerializeSize(){return 0;}

    @Override
    public String toString() {return null;}

    public static org.apache.minibase.KeyValue parseFrom(byte[] bytes, int offset) throws IOException {return null;}

    public static org.apache.minibase.KeyValue parseFrom(byte[] bytes) throws IOException {return null;}

    private static class KeyValueComparator implements Comparator<org.apache.minibase.KeyValue> {
        @Override
        public int compare(org.apache.minibase.KeyValue a, org.apache.minibase.KeyValue b) {}
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
