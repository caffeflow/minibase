package org.xujia.minibase;

import org.xujia.minibase.Bytes;
/**
 * @author xj
 * @create 2022-04-20 14:13
 *
 * 一个block对应一个BloomFilter
 **/
public class BloomFilter {
    private int k;
    private int bitsPerKey;
    private int bitLen;
    private byte[] result;

    public BloomFilter(int k,int bitsPerKey){
        this.k = k; // k是hash次数。 最佳误判率时,N = k*元素个数/ln2, N是字节数组长度
        this.bitsPerKey = bitsPerKey;
    }
    public byte[] generate(byte[][] keys) {
        assert keys != null;
        bitLen = keys.length * this.bitsPerKey;
        bitLen = ((bitLen + 7) / 8) << 3; // 对齐bit位
        bitLen = bitLen < 64 ? 64 : bitLen;
        result = new byte[bitLen >> 3];// 8bit/byte
        for (int i = 0; i < keys.length; i++) { // 每个key进行多次hash,hash的结果存到字节数组中
            assert keys[i] != null;
            int h = Bytes.hash(keys[i]);
            for (int j = 0; j < k; j++) {
                int idx = (h % bitLen + bitLen) % bitLen;
                result[idx / 8] |= (1 << (idx % 8));
                int delta = (h >> 17) | (h << 15);
                h += delta;
            }
        }
        return result;

    }

    public boolean contains(byte[] key) {
        assert key != null;
        int h = Bytes.hash(key);
        for (int j = 0; j < k; j++) {
            int idx = (h % bitLen + bitLen) % bitLen;
            if (result[idx / 8] != (1 << (idx % 8)))
                return false;
            int delta = (h >> 17) | (h << 15);
            h += delta;
        }
        return true;
    }
}
