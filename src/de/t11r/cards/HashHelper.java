// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import java.security.MessageDigest;

public class HashHelper {

    private static final String MD_ALGO = "MD5";
    private static final String HEX = "0123456789abcdef";

    private static MessageDigest md;

    public static String hash(String s) {
        byte[] a = md().digest(s.getBytes());
        char[] b = new char[a.length * 2];
        for (int i = 0, j = 0; i < a.length; i++) {
            b[j++] = HEX.charAt((a[i] & 0xf0) >> 4);
            b[j++] = HEX.charAt(a[i] & 0xf);
        }
        return new String(b);
    }

    public static String hash(String... a) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            if (a[i] != null) {
                sb.append('"');
                sb.append(a[i].replace("\"", "\"\""));
                sb.append('"');
            }
        }
        return hash(sb.toString());
    }

    private static MessageDigest md() {
        if (md == null) {
            try {
                md = MessageDigest.getInstance(MD_ALGO);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return md;
    }
}
