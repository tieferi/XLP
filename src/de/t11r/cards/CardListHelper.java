// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.ExcelData.Sheet;
import de.t11r.cards.Settings.CardType;
import de.t11r.cards.Settings.CardType.Template;

import java.util.*;

public class CardListHelper {

	private static Random random = new Random();

	public static List<Card> buildCardList(ExcelData content, Settings settings,
			Set<String> sheetNameSet, String typeName) {
		List<Card> list = new ArrayList<>();
		CardType cardType = settings.getCardType(typeName);
		if (cardType != null) {
			for (Sheet sheet : content.getSheets()) {
				if (sheetNameSet.contains(sheet.getName())) {
					addCards(list, sheet, cardType,
						settings.getUrlVarDelLeft(), settings.getUrlVarCutAt(), settings.getMeta());
				}
			}
			if (cardType.isMerge()) {
				List<Card> xlist = new ArrayList<>();
				for (Sheet sheet : content.getSheets()) {
					if (!sheet.getName().startsWith(".")) {
						addCards(xlist, sheet, cardType,
							settings.getUrlVarDelLeft(), settings.getUrlVarCutAt(), settings.getMeta());
					}
				}
				list = matchAndMerge(list, xlist);
			}
			list = shuffle(list);
		}
		return list;
	}
		
	private static void addCards(List<Card> list, Sheet sheet, CardType type,
			List<String> urlVarDelLeft, String urlVarCutAt, List<String> meta) {
		for (int rowIndex = sheet.getFixedRowCount(); rowIndex < sheet.getRowCount(); rowIndex++) {
			for (Template template : type.getTemplateList()) {
				addCard(list, sheet, rowIndex, template,
						type.getUrlPattern(), urlVarDelLeft, urlVarCutAt, meta);
			}
		}
	}
	
	private static void addCard(List<Card> list, Sheet sheet, int rowIndex, Template template,
			String urlPattern, List<String> urlVarDelLeft, String urlVarCutAt, List<String> meta) {
		VarMap varMap = varMap(sheet, rowIndex);
		String front = buildText(template.getFrontPattern(), varMap);
		String back = buildText(template.getBackPattern(), varMap);
		if (!front.isEmpty() && !back.isEmpty()) {
			String info = sheet.getName();
			String url = null;
			if (urlPattern != null) {
				url = UrlHelper.buildUrl(urlPattern, UrlHelper.varMap(sheet, rowIndex,
						urlVarDelLeft, urlVarCutAt, meta));
			}
			list.add(new Card(info, front, back, url));
		}
	}
	
	private interface VarMap {
		String get(char c);
	}
	
	private static VarMap varMap(Sheet sheet, int rowIndex) {
		return new VarMap() {
			@Override
			public String get(char c) {
				if (c >= 'A' && c <= 'Z') {
					return sheet.getString(rowIndex, c - 'A');
				} else if (sheet.getFixedRowCount() > 0 && c >= 'a' && c <= 'z') {
					return sheet.getString(sheet.getFixedRowCount() - 1, c - 'a');
				} else {
					return null;
				}
			}
		};
	}

	private static String buildText(String pattern, VarMap varMap) {
		StringBuilder sb = new StringBuilder();
		int i = buildText(pattern, varMap, 0, sb);
		return i == pattern.length() ? sb.toString() : "";
	}
	
	private static int buildText(String pattern, VarMap varMap, int i0, StringBuilder sb) {
		int i = i0;
		boolean valueMissing = false;
		while (i < pattern.length()) {
			char c = pattern.charAt(i++);
			if (c == '$') {
				if (i < pattern.length()) {
					char c2 = pattern.charAt(i++);
					if (c2 == '$') {
						sb.append('$');
					} else if (c2 == '-') {
						sb.append('\u25CF');
					} else if (c2 == ';') {
						sb.append('\n');
					} else if (c2 == '[') {
						StringBuilder sb2 = new StringBuilder();
						i = buildText(pattern, varMap, i, sb2);
						if (i < 0) {
							return -1;
						}
						sb.append(sb2.toString());
					} else if (c2 == ']' && i0 > 0) {
						break;
					} else {
						String value = varMap.get(c2);
						if (value != null && !value.isEmpty()) {
							sb.append(value);
						} else {
							valueMissing = true;
						}
					}
				} else {
					return -1;
				}
			} else {
				sb.append(c);
			}
		}
		if (valueMissing) {
			sb.setLength(0);
		}
		return i;
	}

	private static List<Card> matchAndMerge(List<Card> list, List<Card> xlist) {
		Set<String> set = new HashSet<>();
		Map<String, List<Card>> map = new HashMap<>();
		for (Card card : xlist) {
			if (!set.contains(card.getFront())) {
				set.add(card.getFront());
			} else {
				map.put(card.getFront(), new ArrayList<>());
			}
		}
		if (!map.isEmpty()) {
			for (Card card : xlist) {
				if (map.containsKey(card.getFront())) {
					map.get(card.getFront()).add(card);
				}
			}
			List<Card> list2 = new ArrayList<>();
			Set<String> set2 = new HashSet<>();
			for (Card card : list) {
				if (map.containsKey(card.getFront())) {
					if (!set2.contains(card.getFront())) {
						set2.add(card.getFront());
						list2.add(merge(map.get(card.getFront())));
					}
				} else {
					list2.add(card);
				}
			}
			return list2;
		} else {
			return list;
		}
	}

	private static Card merge(List<Card> list) {
		List<String> list1 = new ArrayList<>();
		List<String> list2 = new ArrayList<>();
		Set<String> set1 = new HashSet<>();
		Set<String> set2 = new HashSet<>();
		for (Card card : list) {
			String info = card.getInfo();
			if (!set1.contains(info)) {
				list1.add(info);
				set1.add(info);
			}
			String back = card.getBack();
			if (!set2.contains(back)) {
				list2.add(back);
				set2.add(back);
			}
		}
		String info = String.join(", ", list1);
		String front = list.get(0).getFront();
		if (list2.size() == 1) {
			String back = list.get(0).getBack();
			String url = list.get(0).getUrl();
			return new Card(info, front, back, url);
		} else {
			String back = "\u25CF " + String.join("\n\u25CF ", list2);
			return new Card(info, front, back, null);
		}
	}

	private static List<Card> shuffle(List<Card> list)  {
		List<Card> list1 = new LinkedList<>(list);
		List<Card> list2 = new ArrayList<>();
		while (!list1.isEmpty()) {
			int i = random.nextInt(list1.size());
			list2.add(list1.get(i));
			list1.remove(i);
		}
		return list2;
	}
}

