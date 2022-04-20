package org.apache.minibase;
import java.nio.charset.StandardCharsets;

public class Test {

    public static void main(String[] args) {

        for (int i = 0; i < 10000; i++) {
            int h = i;
            int bitLen = (int)( 100 * Math.random()) + 3;
            int idx1 = (h % bitLen + bitLen) % bitLen;
            int idx2 = h % bitLen;
            if (idx1 != idx2)
                System.out.println(idx1 + " " + idx2);
        }

    }
}
