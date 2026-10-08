// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TextPane extends JComponent {

    private String fontName;
    private Set<String> meta;

    private enum Style { T, M }

    private int fontSize;
    private Font[] fontArray;
    private FontMetrics[] fontMetricsArray;

    private int h;
    private int h1;
    private int h2;

    private class Element {

        private String text;
        private Style style;
        private boolean underline;

        private int width;

        private int x;
        private int y;

        public Element(String text, Style style, boolean underline) {
            this.text = text;
            this.style = style;
            this.underline = underline;
            FontMetrics fm = getFontMetrics(style);
            width = fm.stringWidth(text);
        }

        public int getWidth() {
            return width;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void draw(Graphics g) {
            g.setFont(getFont(style));
            g.drawString(text, x, y);
            if (underline) {
                if (h2 >= 6) {
                    g.fillRect(x, y + 4, width, 2);
                } else {
                    g.fillRect(x, y + 2, width, 1);
                }
            }
        }
    }

    private List<Element> elementList = new ArrayList<>();

    private int n;
    private int x;
    private int y;
    private int w;

    private int wText;
    private int hText;

    private boolean err;

    private static final int MIN_FONT_SIZE = 16;

    private static final int WM = 10;
    private static final int HM = 5;

    public TextPane(String fontName, List<String> meta) {
        this.fontName = fontName;
        this.meta = new HashSet<>(meta);
    }

    public boolean setText(String text, int fontSize) {
        if (text != null) {
            while (true) {
                setFontSize(fontSize);
                if (setText(text)) {
                    break;
                }
                fontSize -= 2;
                if (fontSize < MIN_FONT_SIZE) {
                    return false;
                }
            }
        } else {
            clear();
        }
        repaint();
        return true;
    }

    private void setFontSize(int fontSize) {
        if (fontSize != this.fontSize) {
            this.fontSize = fontSize;
            fontArray = new Font[Style.values().length];
            fontMetricsArray = new FontMetrics[Style.values().length];
        }
        FontMetrics fm = getFontMetrics(Style.T);
        h = fm.getHeight();
        h1 = fm.getAscent();
        h2 = fm.getDescent();
    }

    private Font getFont(Style style) {
        Font f = fontArray[style.ordinal()];
        if (f == null) {
            switch (style) {
                case T:
                    f = new Font(fontName, Font.PLAIN, fontSize);
                    break;
                case M:
                    f = new Font(fontName, Font.BOLD | Font.ITALIC, fontSize);
                    break;
            }
            fontArray[style.ordinal()] = f;
        }
        return f;
    }

    private FontMetrics getFontMetrics(Style style) {
        FontMetrics fm = fontMetricsArray[style.ordinal()];
        if (fm == null) {
            fm = getFontMetrics(getFont(style));
            fontMetricsArray[style.ordinal()] = fm;
        }
        return fm;
    }

    private boolean setText(String text) {
        App.trace("TextPane.1", "TextPane.setText() ---");
        clear();
        int i = 0, l = text.length();
        err = false;
        while (!err) {
            if (i < l) {
                int i0 = i;
                if (isLetter(text.charAt(i))) {
                    while (i < l && isLetter(text.charAt(i))) {
                        i++;
                    }
                    if (i < l && text.charAt(i) == '_') {
                        if (i - 1 > i0) {
                            addElement(text.substring(i0, i - 1), Style.T, false);
                        }
                        addElement(text.substring(i - 1, i), Style.T, true);
                        i++;
                    } else {
                        if (i < l && text.charAt(i) == '.' && meta.contains(text.substring(i0, i + 1))) {
                            i++;
                        }
                        String s = text.substring(i0, i);
                        addElement(s, meta.contains(s) ? Style.M : Style.T, false);
                    }

                } else {
                    if (text.charAt(i) == ' ') {
                        while (i < l && text.charAt(i) == ' ') {
                            i++;
                        }
                        addElement(text.substring(i0, i), Style.T, false);
                        layoutElements();
                    } else if (text.charAt(i) == '\n') {
                        i++;
                        layoutElements();
                        newLine();
                    } else {
                        while (i < l && !isLetter(text.charAt(i)) && text.charAt(i) != ' ' && text.charAt(i) != '\n') {
                            i++;
                        }
                        addElement(text.substring(i0, i), Style.T, false);
                    }
                }

            } else {
                layoutElements();
                break;
            }
        }
        if (err) {
            clear();
        }
        App.trace("TextPane.1", "Exit");
        return !err;
    }

    private void clear() {
        elementList.clear();
        n = 0;
        x = 0;
        y = 0;
        w = 0;
        wText = 0;
        hText = 0;
    }

    private void addElement(String text, Style style, boolean underline) {
        Element e = new Element(text, style, underline);
        elementList.add(e);
        w += e.getWidth();
    }

    private void layoutElements() {
        if (y == 0) {
            y += h1;
        } else if (x + w + WM * 2 > getWidth()) {
            x = 0;
            y += h;
        }
        w = 0;
        while (n < elementList.size()) {
            Element e = elementList.get(n++);
            e.setX(x);
            e.setY(y);
            x += e.getWidth();
        }
        wText = Math.max(wText, x);
        hText = Math.max(hText, y + h2);
        if (wText + WM * 2 > getWidth() || hText + HM * 2 > getHeight()) {
            err = true;
        }
    }

    private void newLine() {
        x = 0;
        if (y == 0) {
            y += h1;
        } else {
            y += h;
        }
        hText = Math.max(hText, y + h2);
        if (hText + HM * 2 > getHeight()) {
            err = true;
        }
    }

    private boolean isLetter(char c) {
        int t = Character.getType(c);
        return t == Character.LOWERCASE_LETTER || t == Character.UPPERCASE_LETTER;
    }

    @Override
    public void paintComponent(Graphics g) {
        App.trace("TextPane.2", "TextPane.paintComponent() ---");
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getForeground());
        int x0 = (getWidth() - wText) / 2;
        int y0 = (getHeight() - hText) / 2;
        g2.translate(x0, y0);
        for (Element e : elementList) {
            e.draw(g2);
        }
        g2.dispose();
        App.trace("TextPane.2", "Exit");
    }
}
