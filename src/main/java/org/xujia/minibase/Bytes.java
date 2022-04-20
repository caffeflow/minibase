package org.xujia.minibase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
        return null;
    }

    public static String toHex(byte[] buf, int offset, int len) {
        return null;
    }

    public static byte[] toBytes(byte[] a, byte[] b) {
        return null;

    }

    public static int toInt(byte[] a) {
        return 0;

    }

    public static long toLong(byte[] a) {
        return 0;
    }

    public static byte[] slice(byte[] buf, int offset, int len) throws IOException {
        return null;

    }

    public static int hash(byte[] key) {
        return 0;

    }

    public static int compare(byte[] a, byte[] b) {
        return 0;

    }
}
