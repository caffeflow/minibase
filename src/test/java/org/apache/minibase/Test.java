package org.apache.minibase;

import java.nio.charset.StandardCharsets;

public class Test {
    public final static String HEX_TMP = "0123456789ABCDEF";
    public static String toHex(byte[] buf, int offset, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + len; i++) {
            int x = buf[i];
            if (x > 32 && x < 127) {
                sb.append((char) x);
            } else {sb.append("\\x").append(HEX_TMP.charAt((x >> 4) & 0x0F)).append(HEX_TMP.charAt(x & 0x0F));

            }
        }
        return sb.toString();
    }
    public static void main(String[] args) {

    }
}
