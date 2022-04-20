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

    public enum Op{
        Put((byte) 0),Delete((byte) 1);
        private byte code;
        Op(byte code){
            this.code = code;
        }
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

    @Override
    public boolean equals(KeyValue kv) {
        return false;
    }

}
