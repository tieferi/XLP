// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.Card.Side;

import java.util.SortedMap;
import java.util.SortedSet;

public class ProjectData {

    private SortedSet<String> sheetNameSet;
    private String cardTypeName;
    private SortedMap<String, String> learningProfileNameMap;

    private String cardKey;
    private Side cardSide;

    private SortedMap<String, String> statsMap;

    public SortedSet<String> getSheetNameSet() {
        return sheetNameSet;
    }

    public void setSheetNameSet(SortedSet<String> sheetNameSet) {
        this.sheetNameSet = sheetNameSet;
    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public void setCardTypeName(String cardTypeName) {
        this.cardTypeName = cardTypeName;
    }

    public SortedMap<String, String> getLearningProfileNameMap() {
        return learningProfileNameMap;
    }

    public void setLearningProfileNameMap(SortedMap<String, String> learningProfileNameMap) {
        this.learningProfileNameMap = learningProfileNameMap;
    }

    public String getCardKey() {
        return cardKey;
    }

    public void setCardKey(String cardKey) {
        this.cardKey = cardKey;
    }

    public Side getCardSide() {
        return cardSide;
    }

    public void setCardSide(Side cardSide) {
        this.cardSide = cardSide;
    }

    public SortedMap<String, String> getStatsMap() {
        return statsMap;
    }

    public void setStatsMap(SortedMap<String, String> statsMap) {
        this.statsMap = statsMap;
    }
}
