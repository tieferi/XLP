// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import java.awt.*;
import java.util.List;

public class Settings {

    public enum Size { XS, S, M, L, XL }

    public static Dimension getCardSize(Size size) {
        switch (size) {
            case XS: return new Dimension(320, 240);
            case S:  return new Dimension(360, 270);
            case M:  return new Dimension(400, 300);
            case L : return new Dimension(440, 330);
            case XL: return new Dimension(480, 360);
        }
        return null;
    }

    public static int getFontSize(Size size) {
        switch (size) {
            case XS: return 24;
            case S:  return 28;
            case M:  return 32;
            case L:  return 36;
            case XL: return 40;
        }
        return 0;
    }

    public static class CardType {

        public static class Template {

            private String frontPattern;
            private String backPattern;

            public Template(String frontPattern, String backPattern ) {
                this.frontPattern = frontPattern;
                this.backPattern = backPattern;
            }

            public String getFrontPattern() {
                return frontPattern;
            }

            public String getBackPattern() {
                return backPattern;
            }
        }

        private String name;
        private List<Template> templateList;
        private boolean merge;

        private Color cardColorFront;
        private Color cardColorBack;
        private Color textColorFront;
        private Color textColorBack;

        private String urlPattern;

        public CardType(String name, List<Template> templateList, boolean merge,
                Color cardColorFront, Color cardColorBack,
                Color textColorFront, Color textColorBack,
                String urlPattern) {

            this.name = name;
            this.templateList = templateList;
            this.merge = merge;
            this.cardColorFront = cardColorFront;
            this.cardColorBack = cardColorBack;
            this.textColorFront = textColorFront;
            this.textColorBack = textColorBack;
            this.urlPattern = urlPattern;
        }

        public String getName() {
            return name;
        }

        public List<Template> getTemplateList() {
            return templateList;
        }

        public boolean isMerge() {
            return merge;
        }

        public Color getCardColorFront() {
            return cardColorFront;
        }

        public Color getCardColorBack() {
            return cardColorBack;
        }

        public Color getTextColorFront() {
            return textColorFront;
        }

        public Color getTextColorBack() {
            return textColorBack;
        }

        public String getUrlPattern() {
            return urlPattern;
        }
    }

    public static class LearningProfile {

        public static class Condition {

            private int p;
            private int q;

            public Condition(int p, int q) {
                this.p = p;
                this.q = q;
            }

            public int getP() {
                return p;
            }

            public int getQ() {
                return q;
            }
        }

        private String name;
        private String statsId;
        private List<Condition> conditionList;

        public LearningProfile(String name, String statsId, List<Condition> conditionList) {
            this.name = name;
            this.statsId = statsId;
            this.conditionList = conditionList;
        }

        public String getName() {
            return name;
        }

        public String getStatsId() {
            return statsId;
        }

        public List<Condition> getConditionList() {
            return conditionList;
        }
    }

    private String topic;

    private Size cardSize;
    private Size textSize;

    private List<String> meta;

    private List<String> urlVarDelLeft;
    private String urlVarCutAt;

    private List<CardType> cardTypeList;
    private String defaultCardTypeName;

    private List<LearningProfile> learningProfileList;
    private String defaultLearningProfileName;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Size getCardSize() {
        return cardSize;
    }

    public void setCardSize(Size cardSize) {
        this.cardSize = cardSize;
    }

    public Size getTextSize() {
        return textSize;
    }

    public void setTextSize(Size textSize) {
        this.textSize = textSize;
    }

    public List<String> getUrlVarDelLeft() {
        return urlVarDelLeft;
    }

    public void setUrlVarDelLeft(List<String> urlVarDelLeft) {
        this.urlVarDelLeft = urlVarDelLeft;
    }

    public String getUrlVarCutAt() {
        return urlVarCutAt;
    }

    public void setUrlVarCutAt(String urlVarCutAt) {
        this.urlVarCutAt = urlVarCutAt;
    }

    public List<String> getMeta() {
        return meta;
    }

    public void setMeta(List<String> meta) {
        this.meta = meta;
    }

    public List<CardType> getCardTypeList() {
        return cardTypeList;
    }

    public void setCardTypeList(List<CardType> cardTypeList) {
        this.cardTypeList = cardTypeList;
    }

    public CardType getCardType(String name) {
        for (CardType cardType : cardTypeList) {
            if (cardType.getName().equals(name)) {
                return cardType;
            }
        }
        return null;
    }

    public String getDefaultCardTypeName() {
        return defaultCardTypeName;
    }

    public void setDefaultCardTypeName(String defaultCardTypeName) {
        this.defaultCardTypeName = defaultCardTypeName;
    }

    public List<LearningProfile> getLearningProfileList() {
        return learningProfileList;
    }

    public void setLearningProfileList(List<LearningProfile> learningProfileList) {
        this.learningProfileList = learningProfileList;
    }

    public LearningProfile getLearningProfile(String name) {
        for (LearningProfile learningProfile : learningProfileList) {
            if (learningProfile.getName().equals(name)) {
                return learningProfile;
            }
        }
        return null;
    }

    public String getDefaultLearningProfileName() {
        return defaultLearningProfileName;
    }

    public void setDefaultLearningProfileName(String defaultLearningProfileName) {
        this.defaultLearningProfileName = defaultLearningProfileName;
    }
}
