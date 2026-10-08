// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ExceptionHelper {

	private interface UserMessage {
		String getUserMessage();
	}

	private static abstract class ExceptionWithUserMessage extends Exception implements UserMessage {
	}

	public static void fail(String key, Object... args) throws Exception {
		String userMessage = TextHelper.format("error." + key, args);
		throw new ExceptionWithUserMessage() {
			@Override
			public String getUserMessage() {
				return userMessage;
			}
		};
	}

	public static void report(Component parent, Throwable e) {
		File file = new File(String.format("%s/error.log", OSHelper.appFolder().getPath()));
		try {
			logError(e, file);
		} catch (Exception e2) {
		}
		String title = TextHelper.get("errorDialog.title");
		String message = null;
		if (e instanceof UserMessage) {
			message = ((UserMessage)e).getUserMessage();
		} else {
			message = TextHelper.format("error.other", file.toString());
		}
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
	}

	private static void logError(Throwable e, File file) throws Exception {
		PrintWriter w = new PrintWriter(new FileWriter(file, true));
		try {
			long t = System.currentTimeMillis();
			w.println(DateHelper.formatTimestamp(t) + " " + "-".repeat(31));
			e.printStackTrace(w);
			w.println();
		} finally {
			w.close();
		}
	}
}
