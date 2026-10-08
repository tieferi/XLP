// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

public class Card {

	public enum Side { FRONT, BACK }

	public static Side otherSide(Side side) {
		switch (side) {
			case FRONT: return Side.BACK;
			case BACK: return Side.FRONT;
		}
		return null;
	}

	private String info;
	private String front;
	private String back;
	private String url;
	private String key;

	public Card(String info, String front, String back, String url) {
		this.info = info;
		this.front = front;
		this.back = back;
		this.url = url;
		key = HashHelper.hash(front, back);
	}

	public String getInfo() {
		return info;
	}

	public String getFront() {
		return front;
	}

	public String getBack() {
		return back;
	}

	public String getText(Side side) {
		switch (side) {
			case FRONT : return front;
			case BACK: return back;
		}
		return null;
	}

	public String getUrl() {
		return url;
	}

	public String getKey() {
		return key;
	}
}
