// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import java.awt.*;

public class ColorList {

    public static void main(String[] args) {
        System.out.println(String.format("$C00\t255\t255\t255"));
        for (int i = 1; i <= 12; i++) {
            Color c = Color.getHSBColor(i / 12.0f, 0.1f, 1.0f);
            System.out.println(String.format("$C%02d\t%d\t%d\t%d", i, c.getRed(), c.getGreen(), c.getBlue()));
        }
    }
}
