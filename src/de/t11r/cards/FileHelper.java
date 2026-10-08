// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

public class FileHelper {

	public static String removeExt(String name) {
		int i = name.lastIndexOf('.');
		return i >= 0 ? name.substring(0, i) : name;
	}
}
