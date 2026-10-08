// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.ExcelData.Sheet;

import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

public class UrlHelper {

    public interface UrlVarMap {
        String get(char c);
    }

    public static UrlVarMap varMap(Sheet sheet, int row, List<String> delLeft, String cutAt, List<String> meta) {
        return new UrlVarMap() {
            @Override
            public String get(char c) {
                if (c >= 'A' && c <= 'Z') {
                    return sheet.getString(row, c - 'A');
                } else if (c >= 'a' && c <= 'z') {
                    return strip(sheet.getString(row, c - 'a'), delLeft, cutAt, meta);
                } else if (c == '$') {
                    return "$";
                } else {
                    return "";
                }
            }
        };
    }

    private static String strip(String s, List<String> delLeft, String cutAt, List<String> meta) {
        for (String t : delLeft) {
            if (s.startsWith(t)) {
                int l = t.length();
                if (isBoundary(s, l)) {
                    s = s.substring(l);
                    break;
                }
            }
        }
        for (int i = 0; i < s.length(); i++) {
            if (cutAt.indexOf(s.charAt(i)) >= 0) {
                s = s.substring(0, i);
                break;
            }
        }
        for (String t : meta) {
            s = delete(s, t);
        }
        for (String t : new String[] {"()", "[]", "{}"}) {
            char t1 = t.charAt(0);
            char t2 = t.charAt(1);
            if (s.indexOf(t1) >= 0) {
                s = s.replaceAll(String.format("\\%1$c[^\\%2$c]*\\%2$c", t1, t2), "");
            }
        }
        s = s.trim();
        s = s.replaceAll("[ ]{2,}", " ");
        return s;
    }

    private static String delete(String s, String t) {
        int i = 0, l = t.length();
        while (true) {
            i = s.indexOf(t, i);
            if (i < 0) {
                break;
            }
            if (isBoundary(s, i) && isBoundary(s, i + l)) {
                s = s.substring(0, i) + s.substring(i + l);
            } else {
                i += l;
            }
        }
        return s;
    }

    private static boolean isBoundary(String s, int i) {
        return i == 0 || i == s.length() ||  (!isLetter(s, i - 1) || !isLetter(s, i));
    }

    private static boolean isLetter(String s, int i) {
        int t = Character.getType(s.charAt(i));
        return t == Character.LOWERCASE_LETTER || t == Character.UPPERCASE_LETTER;
    }

    public static String buildUrl(String pattern, UrlVarMap varMap) {
        if (pattern.matches("\\$[A-Za-z$]")) {
            return varMap.get(pattern.charAt(1));
        }
        String[] parts = StringHelper.split(pattern, "://", "/", "?");
        if (parts[2] == null) {
            return null;
        }
        if (!parts[0].equalsIgnoreCase("http") && !parts[0].equalsIgnoreCase("https")) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(parts[0]);
        sb.append("://");
        sb.append(parts[1]);
        for (String s : parts[2].split("/")) {
            sb.append("/");
            for (String t : StringHelper.split(s, "\\$[A-Za-z$]")) {
                if (t.matches("\\$[A-Za-z$]")) {
                    t = varMap.get(t.charAt(1));
                }
                sb.append(encode(t));
            }
        }
        if (parts[3] != null) {
            sb.append("?");
            for (String t : StringHelper.split(parts[3], "\\$[A-Z]")) {
                if (t.matches("\\$[A-Za-z$]")) {
                    t = encode(varMap.get(t.charAt(1))).replace("%20", "+");
                }
                sb.append(t);
            }
        }
        String url = sb.toString();
        return isValid(url) ? url : null;
    }

    private static String encode(String s) {
        String t = null;
        try {
            t = URLEncoder.encode(s, "UTF8");
        } catch (Exception e) {
        }
        return t;
    }

    private static boolean isValid(String url) {
        return getUri(url) != null;
    }

    public static void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(getUri(url));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static boolean checkUrlPattern(String pattern) {
        return buildUrl(pattern, testVarMap()) != null;
    }

    private static UrlVarMap testVarMap() {
        return new UrlVarMap() {
            @Override
            public String get(char c) {
                return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '$' ?
                        String.format("%c", c) : "";
            }
        };
    }

    private static URI getUri(String url) {
        try {
            return new URI(url);
        } catch (Exception e) {
            return null;
        }
    }
}
