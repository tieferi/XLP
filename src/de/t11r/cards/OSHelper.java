// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import java.io.File;

public class OSHelper {

	public static final String APP_FOLDER_NAME = "de.t11r.reflex";

	public static boolean isMacOS() {
		String osName = System.getProperty("os.name");
		return osName.toLowerCase().contains("mac");
	}
	
	public static File appFolder() {
		String path = System.getProperty("appFolder");
		if (path != null) {
			return new File(path);
		}
		if (isMacOS()) {
			String home = System.getProperty("user.home");
			return new File(String.format("%s/Library/Application Support/%s", home, APP_FOLDER_NAME));
		} else {
			throw new RuntimeException();
		}
	}
	
	public static File docFolder() {
		if (isMacOS()) {
			String home = System.getProperty("user.home");
			return new File(String.format("%s/Documents", home));
		} else {
			throw new RuntimeException();
		}
	}
}
