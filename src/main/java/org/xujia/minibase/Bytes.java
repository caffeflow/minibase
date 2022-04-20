package org.xujia.minibase;

import com.sun.org.apache.bcel.internal.generic.NEW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.ResolverStyle;

public class Bytes {

    public final static byte[] EMPTY_BYTES = new byte[0];
    public final static String HEX_TMP = "0123456789ABCDEF";

    public static byte[] toBytes(byte b) {
        return new byte[] {b};
    }

    public static byte[] toBytes(String s) throws IOException {
        if (s == null) return new byte[0];
        return s.getBytes("UTF-8");
    }

    public static byte[] toBytes(int x) {
        byte[] bytes = new byte[4];
        for (int i=4;i>=0;i--){
            int j = (4-i) << 3;
            bytes[i] = (byte) ((x >> j) & 0xFF);
        }
        return bytes;
    }


    public static byte[] toBytes(long x) {
        byte[] bytes = new byte[8];
        for (int i=7;i>=0;i--){
            int j = (7-i) << 3;
            bytes[i] = (byte) ((x >> j) & 0xFF);
        }
        return bytes;
    }

    public static String toHex(byte[] buf){
        return toHex(buf,0,buf.length);
    }

    public static String toHex(byte[] buf, int offset, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i=offset;i < offset+len;i++){
            int x = buf[i];
            if (32 < x && x < 127){
                sb.append((char)x);
            }else{
                sb.append("\\x").append(HEX_TMP.charAt((x >> 4) & 0x0F)).append(HEX_TMP.charAt(x & 0x0F));
            }
        }
        return sb.toString();
    }

    public static byte[] toBytes(byte[] a, byte[] b) {
        if (a == null) return b;
        if (b == null) return a;
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a,0,result,0,a.length);
        System.arraycopy(b,0,result,a.length,b.length);
        return result;
    }

    public static int toInt(byte[] a) {
        int x = 0;
        for (int i=0;i<4;i++){
            int j = (3-i) << 3;
            x |= (a[i] << j) & (0xFF << j);
        }
        return x;
    }

    public static long toLong(byte[] a) {
        long x = 0;
        for (int i = 0; i < 8; i++) {
            int j = (7-i) << 3;
            x |= ((long)a[i] << j) & (0xFFL << j);
        }
        return x;
    }

    public static byte[] slice(byte[] buf, int offset, int len) throws IOException {
        if (buf == null){
            throw new IOException("buffer is null");
        }
        if (offset < 0 || len < 0){
            throw new IOException("Invalid offset:"  + offset  + "Invalid len:" + len);
        }
        if (offset + len > buf.length){
            throw new IOException("Buffer overflow, offset:" + offset + "len:" + len + "buf.length:" + buf.length);
        }
        byte[] bytes = new byte[len];
        System.arraycopy(buf,offset,bytes,0,len);
        return bytes;
    }

    public static int hash(byte[] key) {
        if (key == null){
            return 0;
        }
        int h = 1;
        for (int i = 0; i < key.length; i++) {
            h = (h << 5) + h + key[i];
        }
        return h;
    }

    public static int compare(byte[] a, byte[] b) {
        if (a == b) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        for (int i=0,j=0;i<a.length && j < b.length;i++,j++){
            int x = a[i] & 0xFF;
            int y = a[i] & 0xFF;
            if (x != y){
                return x - y;
            }
        }
        return a.length - b.length;
    }
}
