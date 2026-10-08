// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import java.util.*;

public class TraceHelper {

    public static String REGEX1 = "[A-Za-z0-9]+";
    public static String REGEX2 = "\\.[0-9]+";
    public static String REGEX3 = REGEX1 + REGEX2;

    private static int type(String id) {
        if (id.matches(REGEX1)) {
            return 1;
        } else if (id.matches(REGEX2)) {
            return 2;
        } else if (id.matches(REGEX3)) {
            return 3;
        } else {
            return 0;
        }
    }

    public static boolean checkIdList(String idList) {
        return parseIdList(idList) != null;
    }

    public interface TraceFilter {
        boolean check(String id);
    }

    public static TraceFilter traceFilter(String idList) {
        Set<String> idSet = parseIdList(idList);
        return new TraceFilter() {
            @Override
            public boolean check(String id) {
                switch (type(id)) {
                    case 1: return idSet.contains(id);
                    case 3: return idSet.contains(id) || idSet.contains(id.substring(0, id.indexOf('.')));
                    default: return false;
                }
            }
        };
    }

    private static Set<String> parseIdList(String idList) {
        Set<String> idSet = new HashSet<>();
        if (idList != null && !idList.isEmpty()) {
            String id1 = null;
            for (String id : idList.split("[ ]*,[ ]*")) {
                switch (type(id)) {
                    case 1:
                        idSet.add(id);
                        id1 = null;
                        break;
                    case 2:
                        if (id1 != null) {
                            idSet.add(id1 + id);
                        } else {
                            return null;
                        }
                        break;
                    case 3:
                        idSet.add(id);
                        id1 = id.substring(0, id.indexOf('.'));
                        break;
                    default:
                        return null;
                }
            }
        }
        return idSet;
    }
}
