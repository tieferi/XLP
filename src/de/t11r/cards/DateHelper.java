// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateHelper {

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static Date getDate(long t) {
        return t != 0 ? new Date(t) : null;
    }

    public static String formatTimestamp(long t) {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date(t));
    }

    public static long parseTimestamp(String s) throws Exception {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).parse(s).getTime();
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat(TextHelper.get("dateFormat")).format(date);
    }

    public static String howLongAgo(Date date) {
        Date today = new Date();
        int[] a = dateToI3(date);
        int[] b = dateToI3(today);
        if (compare(a, b) > 0) {
            return "";
        }
        int d = b[2] - a[2];
        int m = b[1] - a[1];
        int y = b[0] - a[0];
        if (d < 0) {
            d += monthDayCount(a[0], a[1]);
            m--;
        }
        if (m < 0) {
            m += 12;
            y--;
        }
        if (y >= 3) {
            return agoStr("ny", y);
        } else if (m >= 3) {
            return agoStr("nm", m);
        } else if (d >= 21) {
            return agoStr("nw", d / 7);
        } else if (d >= 2) {
            return agoStr("nd", d);
        } else if (d == 1) {
            return agoStr("1d");
        } else {
            return agoStr("0d");
        }
    }

    private static int[] dateToI3(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int i1 = c.get(Calendar.YEAR);
        int i2 = c.get(Calendar.MONTH) + 1;
        int i3 = c.get(Calendar.DAY_OF_MONTH);
        return new int[] { i1, i2, i3 };
    }

    private static int compare(int[] a, int[] b) {
        for (int i = 0; i < 3; i++) {
            if (a[i] != b[i]) {
                return Integer.compare(a[i], b[i]);
            }
        }
        return 0;
    }

    private static int monthDayCount(int y, int m) {
        if (m == 2) {
            return y % 4 == 0 && (y % 100 != 0 || y % 400 == 0) ? 29 : 28;
        } else if (m == 4 || m == 6 || m == 9 || m == 11) {
            return 30;
        } else {
            return 31;
        }
    }

    private static String agoStr(String key, Object... args) {
        return TextHelper.format("agoDate." + key, args);
    }
}
