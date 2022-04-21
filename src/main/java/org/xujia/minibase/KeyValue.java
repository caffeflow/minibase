package org.xujia.minibase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author xj
 * @create 2022-04-20 20:08
 *
 * KeyValue： key,value字节数组，增删改操作类型，自增序列号.
 **/
public class KeyValue implements Comparable<KeyValue>{

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

    private int getRawKeyLen(){return key.length + OP_SIZE + SEQ_ID_SIZE;} // NOTE



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
