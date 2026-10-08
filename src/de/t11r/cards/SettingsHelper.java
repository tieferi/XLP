// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.ExcelData.Sheet;
import de.t11r.cards.Settings.*;
import de.t11r.cards.Settings.CardType.Template;
import de.t11r.cards.Settings.LearningProfile.Condition;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsHelper {

    private static final String SHEET_NAME = TextHelper.get("settingsSheet.name");

    private static final String KEY_REGEX = "[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*";

    private static final String ID_REGEX = "[A-Za-z0-9]+";
    private static final String XID_REGEX = "\\$?" + ID_REGEX;

    private static final String META_REGEX = "[A-Za-z]+[.]?";
    private static final String URL_VAR_DEL_LEFT_REGEX = "[^, ]+";
    private static final String COND_REGEX = "([1-9])/([1-9])";
    private static final String SIZE_REGEX = "XS|S|M|L|XL";
    private static final String COLOR_REGEX = "#[0-9A-F]{6}|\\$([0-9]+)/([0-9]+)/([0-9]+)";

    public static Settings build(ExcelData content) throws Exception {
        Map<String, Object> map = buildMap(content);
        return buildSettings(map);
    }

    private static Map<String, Object> buildMap(ExcelData content) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Sheet sheet = content.getSheet(SHEET_NAME);
        if (sheet != null) {
            Set<String> set = new HashSet<>();
            for (int i = sheet.getFixedRowCount(); i < sheet.getRowCount(); i++) {
                String x = sheet.getString(i, 0);
                if (x == null) {
                    fail("formatError");
                }
                if (x.isEmpty()) {
                    continue;
                }
                if (!x.matches(KEY_REGEX)) {
                    fail("invalidKey", x);
                }
                if (set.contains(x)) {
                    fail("duplicateKey", x);
                }
                set.add(x);
                Object y = sheet.getValue(i, 1);
                if (y != null && y.equals("-")) {
                    continue;
                }
                map.put(x, y);
            }
        }
        return map;
    }

    private static Settings buildSettings(Map<String, Object> map) throws Exception {
        Settings settings = new Settings();

        settings.setTopic(getName(map, "topic", null));

        settings.setCardSize(getSize(map, "cardSize", Size.M));
        settings.setTextSize(getSize(map, "textSize", Size.M));

        settings.setMeta(getStringList(map, "meta", META_REGEX, true, Collections.emptyList()));

        settings.setUrlVarDelLeft(getStringList(map, "urlVarDelLeft", URL_VAR_DEL_LEFT_REGEX, true,
                Collections.emptyList()));

        settings.setUrlVarCutAt(getString(map, "urlVarCutAt", ".*", ""));

        List<CardType> cardTypeList = new ArrayList<>();
        List<String> cardTypeIdList = getStringList(map, "cardTypes", XID_REGEX, true,
                List.of("$AB", "$BA"));
        Set<String> cardTypeNameSet = new HashSet<>();
        for (String id : cardTypeIdList) {
            if (id.startsWith("$")) {
                addCardType(cardTypeList, cardTypeNameSet, getStdCardType(id));
            } else {
                addCardType(cardTypeList, cardTypeNameSet, getCardType(map, id));
            }
        }
        settings.setCardTypeList(cardTypeList);

        String defaultCardTypeId = getString(map, "defaultCardType", XID_REGEX,
                cardTypeIdList.contains("$BA") ? "$BA" : null);
        int defaultCardTypeIndex = 0;
        if (defaultCardTypeId != null) {
            defaultCardTypeIndex = cardTypeIdList.indexOf(defaultCardTypeId);
            if (defaultCardTypeIndex < 0) {
                fail("invalidValue", "defaultCardType");
            }
        }
        settings.setDefaultCardTypeName(cardTypeList.get(defaultCardTypeIndex).getName());

        List<LearningProfile> learningProfileList = new ArrayList<>();
        List<String> learningProfileIdList = getStringList(map, "learningProfiles", XID_REGEX, true,
                List.of("$A", "$B", "$C", "$R1", "$R2", "$R3"));
        Set<String> learningProfileNameSet = new HashSet<>();
        for (String id : learningProfileIdList) {
            if (id.startsWith("$")) {
                addLearningProfile(learningProfileList, learningProfileNameSet, getStdLearningProfile(id));
            } else {
                addLearningProfile(learningProfileList, learningProfileNameSet, getLearningProfile(map, id));
            }
        }
        settings.setLearningProfileList(learningProfileList);

        String defaultLearningProfileId = getString(map, "defaultLearningProfile", XID_REGEX,
                learningProfileIdList.contains("$B") ? "$B" : null);
        int defaultLearningProfileIndex = 0;
        if (defaultLearningProfileId != null) {
            defaultLearningProfileIndex = learningProfileIdList.indexOf(defaultLearningProfileId);
            if (defaultLearningProfileIndex < 0) {
                fail("invalidValue", "defaultLearningProfile");
            }
        }
        settings.setDefaultLearningProfileName(learningProfileList.get(defaultLearningProfileIndex).getName());

        return settings;
    }

    private static void addCardType(List<CardType> list, Set<String> nameSet, CardType cardType) throws Exception {
        String name = cardType.getName();
        if (nameSet.contains(name)) {
            fail("duplicateCardTypeName", name);
        }
        nameSet.add(name);
        list.add(cardType);
    }

    private static CardType getStdCardType(String id) throws Exception {
        if (!id.matches("\\$[A-Z]{2}")) {
            fail("unknownSysObject", id);
        }
        char c1 = id.charAt(1);
        char c2 = id.charAt(2);

        String name = String.format("%c/%c", c1, c2);
        String frontPattern = String.format("$%c", c1);
        String backPattern = String.format("$%c", c2);

        Template template = new Template(frontPattern, backPattern);

        return new CardType(name, List.of(template), true,
                Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK,
                null);
    }

    private static CardType getCardType(Map<String, Object> map, String id) throws Exception {
        String prefix = "cardType." + id;

        String name = getName(map, prefix + ".name", null);
        if (name == null) {
            fail("valueMissing", prefix + ".name");
        }

        boolean canMerge = true;
        List<Template> templList = new ArrayList<>();
        List<String> templIdList = getStringList(map, prefix + ".templates", ID_REGEX, true, null);
        if (templIdList == null) {
            fail("valueMissing", prefix + ".templates");
        }
        if (templIdList.size() > 1) {
            canMerge = false;
        }
        for (String templId : templIdList) {
            String templPrefix = "template." + templId;

            String frontPattern = getPattern(map, templPrefix + ".frontPattern", null);
            if (frontPattern == null) {
                fail("valueMissing", templPrefix + ".frontPattern");
            }
            if (!frontPattern.matches("\\$[A-Z]")) {
                canMerge = false;
            }

            String backPattern = getPattern(map, templPrefix + ".backPattern", null);
            if (backPattern == null) {
                fail("valueMissing", templPrefix + ".backPattern");
            }
            if (!backPattern.matches("\\$[A-Z]")) {
                canMerge = false;
            }

            templList.add(new Template(frontPattern, backPattern));
        }

        boolean merge = getBoolean(map, prefix + ".merge", false);
        if (merge && !canMerge) {
            fail("invalidValue", prefix + ".merge");
        }

        Color cardColorFront = getColor(map, prefix + ".cardColorFront", Color.WHITE);
        Color cardColorBack = getColor(map, prefix + ".cardColorBack", Color.WHITE);
        Color textColorFront = getColor(map, prefix + ".textColorFront", Color.BLACK);
        Color textColorBack = getColor(map, prefix + ".textColorBack", Color.BLACK);

        String urlPattern = getUrlPattern(map, prefix + ".urlPattern", null);

        return new CardType(name, templList, merge,
                cardColorFront, cardColorBack, textColorFront, textColorBack,
                urlPattern);
    }

    private static void addLearningProfile(List<LearningProfile> list, Set<String> nameSet,
            LearningProfile learningProfile) throws Exception {
        String name = learningProfile.getName();
        if (nameSet.contains(name)) {
            fail("duplicateLearningProfileName", name);
        }
        nameSet.add(name);
        list.add(learningProfile);
    }

    private static LearningProfile getStdLearningProfile(String id) throws Exception {
        if (id.equals("$A")) {
            return getLearningProfile("A", 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        } else if (id.equals("$B")) {
            return getLearningProfile("B", 1, 1, 1, 1, 1, 1, 2, 2, 3, 3);
        } else if (id.equals("$C")) {
            return getLearningProfile("C", 1, 1, 2, 2, 3, 3, 4, 4, 5, 5);
        } else if (id.equals("$R1")) {
            return getLearningProfile("R1", 1, 1, 1, 1, 1, 1);
        } else if (id.equals("$R2")) {
            return getLearningProfile("R2", 1, 1, 1, 1, 1, 1);
        } else if (id.equals("$R3")) {
            return getLearningProfile("R3", 1, 1, 1, 1, 1, 1);
        } else {
            fail("unknownSysObject", id);
        }
        return null;
    }

    private static LearningProfile getLearningProfile(String name, int... args) {
        List<Condition> conditionList = new ArrayList<>();
        for (int i = 0; i < args.length; i += 2) {
            conditionList.add(new Condition(args[i], args[i + 1]));
        }
        return new LearningProfile(name, getStatsId(name), conditionList);
    }

    private static LearningProfile getLearningProfile(Map<String, Object> map, String id) throws Exception {
        String prefix = "learningProfile." + id;

        String name = getString(map, prefix + ".name", "[A-Z][1-9]?", null);
        if (name == null) {
            fail("valueMissing", prefix + ".name");
        }

        List<String> conditions = getStringList(map, prefix + ".conditions", COND_REGEX, false, null);
        if (conditions == null) {
            fail("valueMissing", prefix + ".conditions");
        }
        if (conditions.size() > 9) {
            fail("invalidValue", prefix + ".conditions");
        }
        List<Condition> conditionList = new ArrayList<>();
        Pattern conditionPattern = Pattern.compile(COND_REGEX);
        for (String condition : conditions) {
            Matcher matcher = conditionPattern.matcher(condition);
            if (matcher.matches()) {
                int p = Integer.parseInt(matcher.group(1));
                int q = Integer.parseInt(matcher.group(2));
                conditionList.add(new Condition(p, q));
            }
        }

        return new LearningProfile(name, getStatsId(name), conditionList);
    }

    private static String getStatsId(String name) {
        if (name.matches("[A-Z]")) {
            return name + "0";
        } else if (name.matches("[A-Z][1-9]")) {
            return name;
        } else {
            return null;
        }
    }

    private static String getString(Map<String, Object> map, String key, String regex, String defaultValue)
            throws Exception {
        if (map.containsKey(key)) {
            Object x = map.get(key);
            if (!(x instanceof String)) {
                fail("wrongType", key);
            }
            String s = (String)x;
            if (regex != null && !s.matches(regex)) {
                fail("invalidValue", key);
            }
            return s;
        } else {
            return defaultValue;
        }
    }

    private static List<String> getStringList(Map<String, Object> map, String key, String regex, boolean distinct,
            List<String> defaultValue) throws Exception {
        if (map.containsKey(key)) {
            String s = getString(map, key, String.format("%1$s([ ]*,[ ]*%1$s)*", regex), null);
            String[] a = s.split("[ ]*,[ ]*");
            if (distinct && Set.of(a).size() != a.length) {
                fail("invalidValue", key);
            }
            return List.of(a);
        } else {
            return defaultValue;
        }
    }

    private static int getInt(Map<String, Object> map, String key, int minValue, int maxValue, int defaultValue)
            throws Exception {
        if (map.containsKey(key)) {
            Object x = map.get(key);
            if (!(x instanceof Double)) {
                fail("wrongType", key);
            }
            double d = (Double)x;
            int n = (int)d;
            if (n != d || n < minValue || n > maxValue) {
                fail("invalidValue", key);
            }
            return n;
        } else {
            return defaultValue;
        }
    }

    private static boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) throws Exception {
        if (map.containsKey(key)) {
            Object x = map.get(key);
            if (x instanceof Boolean) {
                return (Boolean)x;
            } else if (x instanceof Double) {
                return (Double)x != 0;
            } else if (x instanceof String) {
                return ((String)x).toLowerCase().matches("y|yes|on");
            } else {
                fail("wrongType", key);
                return false;
            }
        } else {
            return defaultValue;
        }
    }

    private static String getName(Map<String, Object> map, String key, String defaultValue)
            throws Exception {
        if (map.containsKey(key)) {
            String s = getString(map, key, null, null);
            if (!isName(s)) {
                fail("invalidValue", key);
            }
            return s;
        } else {
            return defaultValue;
        }
    }

    private static boolean isName(String s) {
        if (s.isEmpty() || s.startsWith(" ") || s.endsWith(" ") || s.indexOf("  ") > 0) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!isNameChar(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNameChar(char c) {
        return ((c >= 0x20 && c <= 0x7E) || (c >= 0xA0 && c <= 0xFF)) && c != 0xA0 && c != 0xAD;
    }

    private static String getPattern(Map<String, Object> map, String key, String defaultValue)
            throws Exception {
        if (map.containsKey(key)) {
            String s = getString(map, key, null, null);
            if (!checkPattern(s)) {
                fail("invalidValue", key);
            }
            return s;
        } else {
            return defaultValue;
        }
    }

    private static boolean checkPattern(String s) {
        int i = 0, l = s.length();
        boolean x = false;
        int y = 0;
        while (i < l) {
            int c = s.charAt(i++);
            if (c == '$') {
                int c2 = s.charAt(i++);
                if (c2 >= 'A' && c2 <= 'Z') {
                    x = true;
                    continue;
                } else if (c2 >= 'a' && c2 <= 'z') {
                    continue;
                } else if (c2 == '-' || c2 == ';') {
                    continue;
                } else if (c2 == '[') {
                    y++;
                    continue;
                } else if (c2 == ']') {
                    y--;
                    continue;
                } else if (c2 == '$') {
                    continue;
                } else {
                    break;
                }
            }
        }
        return i == l && x && y == 0;
    }

    private static String getUrlPattern(Map<String, Object> map, String key, String defaultValue)
            throws Exception {
        if (map.containsKey(key)) {
            String s = getString(map, key, null, null);
            if (!UrlHelper.checkUrlPattern(s)) {
                fail("invalidValue", key);
            }
            return s;
        } else {
            return defaultValue;
        }
    }

    private static Size getSize(Map<String, Object> map, String key, Size defaultValue) throws Exception {
        if (map.containsKey(key)) {
            String s = getString(map, key, SIZE_REGEX, null);
            return Size.valueOf(s);
        } else {
            return defaultValue;
        }
    }

    private static Color getColor(Map<String, Object> map, String key, Color defaultValue) throws Exception {
        if (map.containsKey(key)) {
            String s = getString(map, key, COLOR_REGEX, null);
            Color c = null;
            if (s.startsWith("#")) {
                int r = Integer.parseInt(s.substring(1, 3), 16);
                int g = Integer.parseInt(s.substring(3, 5), 16);
                int b = Integer.parseInt(s.substring(5, 7), 16);
                c = new Color(r, g, b);
            } else {
                String[] a = s.substring(1).split("/");
                int n1 = Integer.parseInt(a[0]);
                int n2 = Integer.parseInt(a[1]);
                int n3 = Integer.parseInt(a[2]);
                if (n1 >= 360 || n2 > 100 || n3 > 100) {
                    fail("invalidValue", key);
                }
                c = Color.getHSBColor(n1 / 360f, n2 / 100f, n3 / 100f);
            }
            return c;
        } else {
            return defaultValue;
        }
    }

    private static void fail(String key, Object... args) throws Exception {
        ExceptionHelper.fail("settingsError", SHEET_NAME, TextHelper.format("error.settings." + key, args));
    }
}
