// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.Card.Side;

import java.util.*;

public class Session {

    private Project project;
    private Algo algo;
    private List<Card> cardList;

    private Card card;

    private Map<Card, Integer> lastTestedMap;
    private int testCount;

    private static class State {

        private Card card;
        private Side cardSide;
        private String stats;
        private int lastTested;
        private int testCount;

        public State(Card card, Side cardSide, String stats, int lastTested, int testCount) {
            this.card = card;
            this.cardSide = cardSide;
            this.stats = stats;
            this.lastTested = lastTested;
            this.testCount = testCount;
        }

        public Card getCard() {
            return card;
        }

        public Side getCardSide() {
            return cardSide;
        }

        public String getStats() {
            return stats;
        }

        public int getLastTested() {
            return lastTested;
        }

        public int getTestCount() {
            return testCount;
        }
    }
    private List<State> history;
    private int historyIndex;

    public Session(Project project) {
        this.project = project;
        init();
    }

    private void init() {
        algo = new Algo(project.learningProfile());
        cardList = project.buildCardList();

        lastTestedMap = new HashMap<>();
        history = new ArrayList<>();

        if (data().getCardKey() != null && data().getCardSide() != null) {
            for (Card card : cardList) {
                if (card.getKey().equals(data().getCardKey())) {
                    this.card = card;
                    break;
                }
            }
        }
        if (card == null) {
            select();
        }
    }

    private ProjectData data() {
        return project.data();
    }

    private void setCard(Card card, Side side) {
        this.card = card;
        if (card != null) {
            data().setCardKey(card.getKey());
            data().setCardSide(side);
        } else {
            data().setCardKey(null);
            data().setCardSide(null);
        }
    }

    private void setCard(String key, String info, String front, String back, Side side) {
        Card card1 = null, card2 = null, card3 = null;
        boolean more1 = false, more2 = false, more3 = false;
        for (Card card : cardList) {
            if (card.getKey().equals(key)) {
                if (card1 == null) {
                    card1 = card;
                } else {
                    more1 = true;
                }
            }
            if (card.getInfo().equals(info)) {
                if (card.getFront().equals(front)) {
                    if (card2 == null) {
                        card2 = card;
                    } else {
                        more2 = true;
                    }
                }
                if (card.getBack().equals(back)) {
                    if (card3 == null) {
                        card3 = card;
                    } else {
                        more3 = true;
                    }
                }
            }
        }
        if (card1 != null && !more1) {
            setCard(card1, side);
        } else if (card2 != null && !more2) {
            setCard(card2, side);
        } else if (card3 != null && !more3) {
            setCard(card3, side);
        } else {
            setCard(null, null);
        }
    }

    private State getState() {
        String cardStats = card != null ? algo.getStats(card, data().getStatsMap()) : null;
        int lastTested = card != null && lastTestedMap.containsKey(card) ? lastTestedMap.get(card) : -1;
        return new State(card, data().getCardSide(), cardStats, lastTested, testCount);
    }

    private void setState(State state) {
        card = state.getCard();
        data().setCardKey(card != null ? card.getKey() : null);
        data().setCardSide(state.getCardSide());
        if (card != null) {
            algo.setStats(card, data().getStatsMap(), state.getStats());
            if (state.getLastTested() >= 0) {
                lastTestedMap.put(card, state.getLastTested());
            } else {
                lastTestedMap.remove(card);
            }
        }
        testCount = state.getTestCount();
    }

    private void select() {
        card = algo.select(cardList, data().getStatsMap(), lastTestedMap);
        if (card != null) {
            setCard(card, Side.FRONT);
        } else {
            setCard(null, null);
        }
    }

    public void update() throws Exception {
        if (project.updateContent()) {
            cardList = project.buildCardList();
            if (card != null) {
                setCard(card.getKey(), card.getInfo(), card.getFront(), card.getBack(), data().getCardSide());
            }
            history.clear();
            historyIndex = 0;
        }
    }

    public Side getCardSide() {
        return data().getCardSide();
    }

    public String getCardInfo() {
        return card != null ? card.getInfo() : "";
    }

    public String getCardStats() {
        if (card != null) {
            String stats = algo.getStats(card, data().getStatsMap());
            return "[" + "*".repeat(stats.charAt(0) - '0') + "/" + stats.substring(1).replaceAll("0", "") + "]";
        } else {
            return "";
        }
    }

    public String getCardUrl() {
        return card != null ? card.getUrl() : null;
    }

    public String getCardText() {
        return card != null && data().getCardSide() != null ? card.getText(data().getCardSide()) : "";
    }

    public int[] getInfo() {
        return algo.getInfo(cardList, data().getStatsMap());
    }

    public void next(boolean ok) {
        if (historyIndex < history.size()) {
            history = new ArrayList<>(history.subList(0, historyIndex));
        }
        history.add(getState());
        historyIndex++;
        if (card != null) {
            if (data().getCardSide() == Side.FRONT && !ok) {
                setCard(card, Side.BACK);
            } else {
                algo.updateStats(data().getStatsMap(), card, ok);
                lastTestedMap.put(card, testCount++);
                select();
            }
        } else {
            select();
        }
    }

    public void turn() {
        if (historyIndex < history.size()) {
            history = new ArrayList<>(history.subList(0, historyIndex));
        }
        history.add(getState());
        historyIndex++;
        data().setCardSide(Card.otherSide(data().getCardSide()));
    }

    public boolean canUndo() {
        return historyIndex > 0;
    }

    public void undo() {
        setState(history.get(--historyIndex));
    }

    public boolean canRedo() {
        return historyIndex < history.size();
    }

    public void redo() {
        setState(history.get(historyIndex++));
    }
}
