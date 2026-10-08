// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringHelper {

    public static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static String[] split(String s, String... a) {
        int n = a.length, i = 0, j = 0;
        String[] b = new String[n + 1];
        for (; i < n; i++) {
            int k = s.indexOf(a[i], j);
            if (k >= 0) {
                b[i] = s.substring(j, k);
                j = k + a[i].length();
            } else {
                b[i] = s.substring(j);
                break;
            }
        }
        if (i == n) {
            b[i] = s.substring(j);
        }
        return b;
    }

    public static List<String> split(String s, String regex) {
        List<String> list = new ArrayList<>();
        Matcher matcher = Pattern.compile(regex).matcher(s);
        int i = 0;
        while (matcher.find()) {
            int j = matcher.start();
            int k = matcher.end();
            if (i < j) {
                list.add(s.substring(i, j));
            }
            list.add(s.substring(j, k));
            i = k;
        }
        if (i < s.length()) {
            list.add(s.substring(i));
        }
        return list;
    }
}
