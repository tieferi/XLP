// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.Settings.LearningProfile;
import de.t11r.cards.Settings.LearningProfile.Condition;

import java.util.*;

public class Algo {

    private LearningProfile profile ;
    private Random random;

    private static final String UNTESTED = "0000000000";

    public static int numSteps(LearningProfile profile) {
        return profile.getConditionList().size();
    }

    public static String getStatus(LearningProfile profile, List<Card> list, Map<String, String> statsMap) {
        int x = 0, n = numSteps(profile);
        boolean started = false;
        for (Card card : list) {
            String stats = statsMap.get(profile.getStatsId() + card.getKey());
            if (stats != null) {
                x += Math.min(value(stats, 0), n);
                started = true;
            }
        }
        if (started) {
            int y = x / list.size();
            return y < n ? String.format("%s.%d", profile.getName(), y) : profile.getName();
        } else {
            return null;
        }
    }

    public static void reset(LearningProfile profile, List<Card> list, Map<String, String> statsMap) {
        for (Card card : list) {
            String key = profile.getStatsId() + card.getKey();
            statsMap.remove(key);
        }
    }

    public Algo(LearningProfile profile) {
        this.profile = profile;
        random = new Random();
    }

    public String getStats(Card card, Map<String, String> statsMap) {
        String key = profile.getStatsId() + card.getKey();
        String value = statsMap.get(key);
        return value != null ? value : UNTESTED;
    }

    public void setStats(Card card, Map<String, String> statsMap, String stats) {
        String key = profile.getStatsId() + card.getKey();
        if (stats.equals(UNTESTED)) {
            statsMap.remove(key);
        } else {
            statsMap.put(key, stats);
        }
    }

    public int[] getInfo(List<Card> list, Map<String, String> statsMap) {
        if (!list.isEmpty()) {
            int n = list.size(), x = 0;
            for (Card card : list) {
                x += getStep(getStats(card, statsMap));
            }
            int x1 = x / n;
            int x2 = x1 < numSteps(profile) ? n - x % n : 0;
            return new int[] { n, x1, x2 };
        } else {
            return new int[] { 0, 0, 0 };
        }
    }

    public Card select(List<Card> list, Map<String, String> statsMap, Map<Card, Integer> lastTestedMap) {
        if (list.isEmpty()) {
            return null;
        }
        List<Card> list2 = list;
        if (list2.size() > 1) {
            list2 = exclude(list, lastTestedMap, Math.min(list2.size() / 2, 5));
        }
        List<Card> list3 = list2;
        if (list3.size() > 5) {
            list3 = keep(list2, statsMap, 5);
        }
        Card card = list3.get(random.nextInt(list3.size()));
        if (App.trace("Algo.1", "Algo.select() ---")) {
            Map<Card, String> info = infoMap(this, list, statsMap);
            markCards(info, list2);
            markCards(info, list3);
            markCards(info, List.of(card));
            trace(list, info);
        }
        return card;
    }

    private List<Card> exclude(List<Card> list, Map<Card, Integer> lastTestedMap, int count) {
        Set<Card> set = new HashSet<>();
        while (set.size() < count) {
            Card card = null;
            for (Card card2 : list) {
                if (!set.contains(card2) && lastTestedMap.containsKey(card2) &&
                        (card == null || lastTestedMap.get(card2) > lastTestedMap.get(card))) {
                    card = card2;
                }
            }
            if (card == null) {
                break;
            }
            set.add(card);
        }
        List<Card> list2 = new ArrayList<>();
        for (Card card : list) {
            if (!set.contains(card)) {
                list2.add(card);
            }
        }
        return list2;
    }

    private List<Card> keep(List<Card> list, Map<String, String> statsMap, int count) {
        Set<Card> set = new HashSet<>();
        while (set.size() < count) {
            Card card = null;
            for (Card card2 : list) {
                if (!set.contains(card2) && (card == null || compare(card2, card, statsMap) < 0))  {
                    card = card2;
                }
            }
            set.add(card);
        }
        List<Card> list2 = new ArrayList<>();
        for (Card card : list) {
            if (set.contains(card)) {
                list2.add(card);
            }
        }
        return list2;
    }

    public int compare(Card card1, Card card2, Map<String, String> statsMap) {
        String stats1 = getStats(card1, statsMap);
        String stats2 = getStats(card2, statsMap);
        for (int i = 0; i < 10; i++) {
            int x1 = value(stats1, i);
            int x2 = value(stats2, i);
            if (x1 != x2) {
                return Integer.compare(x1, x2);
            }
        }
        return 0;
    }

    public void updateStats(Map<String, String> statsMap, Card card, boolean result) {
        String stats0 = getStats(card, statsMap);
        String stats = stats0;
        stats = addResult(stats, result);
        if (result) {
            int step = getStep(stats);
            if (step < numSteps(profile)) {
                if (checkCondition(stats, profile.getConditionList().get(step))) {
                    stats = nextStep(stats);
                }
            }
        }
        statsMap.put(profile.getStatsId() + card.getKey(), stats);
        if (App.trace("Algo.2", "Algo.updateStats() ---")) {
            App.trace("", "%s -> %s - %s", stats0, stats, card.getFront());
        }
    }

    private boolean checkCondition(String stats, Condition cond) {
        int n = 0;
        for (int i = 0; i < cond.getQ(); i++) {
            if (getResult(stats, i)) {
                n++;
            }
        }
        return n >= cond.getP();
    }

    private static String nextStep(String stats) {
        return String.format("%c%s", stats.charAt(0) + 1, stats.substring(1));
    }

    private static String addResult(String stats, boolean result) {
        return String.format("%c%c%s", stats.charAt(0), result ? '+' : '-', stats.substring(1, 9));
    }

    private int getStep(String stats) {
        return Math.min(value(stats, 0), numSteps(profile));
    }

    private boolean getResult(String stats, int i) {
        return value(stats, i + 1) > 0;
    }

    private static int value(String stats, int i) {
        int c = stats.charAt(i);
        if (i == 0) {
            return c - '0';
        } else {
            if (c == '+') {
                return 1;
            } else if (c == '-') {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private static Map<Card, String> infoMap(Algo algo, List<Card> list, Map<String, String> statsMap) {
        Map<Card, String> map = new HashMap<>();
        for (Card card : list) {
            String stats = algo.getStats(card, statsMap);
            map.put(card, String.format("%s - %s", stats, card.getFront()));
        }
        return map;
    }

    private static void markCards(Map<Card, String> map, List<Card> list) {
        for (Card card : map.keySet()) {
            map.put(card, (list.contains(card) ? "X " : "  ") + map.get(card));
        }
    }

    private static void trace(List<Card> list, Map<Card, String> map) {
        for (Card card : map.keySet()) {
            App.trace("", map.get(card));
        }
    }
}
