// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import java.util.ResourceBundle;

public class TextHelper {

	private static ResourceBundle rb = ResourceBundle.getBundle("resources/text");
	
	public static String get(String key) {
		return rb.getString(key);
	}
	
	public static String format(String key, Object... args) {
		return String.format(get(key), args);
	}
}
