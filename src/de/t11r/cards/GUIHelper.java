// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUIHelper {

	public static void makeEqualWidth(Component... a) {
		double w = 0;
		for (Component c : a) {
			w = Math.max(w, c.getPreferredSize().getWidth());
		}
		for (Component c : a) {
			double h = c.getPreferredSize().getHeight();
			Dimension d = new Dimension((int)w, (int)h);
			c.setMinimumSize(d);
			c.setPreferredSize(d);
			c.setMaximumSize(d);
		}
	}

	public static void setForeground(Color c, Component... a) {
		for (Component x : a) {
			x.setForeground(c);
		}
	}

	public static JPanel rowOf(Component... a) {
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		for (Component x : a) {
			p.add(x);
		}
		return p;
	}

	public static JButton okButton() {
		return new JButton(TextHelper.get("okButton.text"));
	}

	public static JButton closeButton() {
		return new JButton(TextHelper.get("closeButton.text"));
	}

	public static JPanel buildButtonPanel(JButton button) {
		JPanel p = rowOf(Box.createHorizontalGlue(), button, Box.createHorizontalGlue());
		p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return p;
	}

	public static void enableTableHeaderAlignment(JTable table) {
		JTableHeader header = table.getTableHeader();
		TableCellRenderer renderer = header.getDefaultRenderer();
		header.setDefaultRenderer(new TableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
				String s = (String)value;
				int alignment = JLabel.LEFT;
				if (s.startsWith("L:")) {
					alignment = JLabel.LEFT;
					value = s.substring(2);
				} else if (s.startsWith("C:")) {
					alignment = JLabel.CENTER;
					value = s.substring(2);
				} else if (s.startsWith("R:")) {
					alignment = JLabel.RIGHT;
					value = s.substring(2);
				}
				JLabel label = (JLabel)renderer.getTableCellRendererComponent(table, value,
					isSelected, hasFocus, row, column);
				label.setHorizontalAlignment(alignment);
				return label;
			}
		});
	}

	public static void enableTableSorting(JTable table, String sortOrder) {
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
		if (sortOrder != null) {
			sorter.setSortKeys(sortKeysFromStr(sortOrder));
		}
		table.setRowSorter(sorter);
	}

	public static String getTableSortOrder(JTable table) {
		return sortKeysToStr(table.getRowSorter().getSortKeys());
	}

	private static String sortKeysToStr(List<? extends SortKey> list) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(String.format("%d: %s", list.get(i).getColumn(), list.get(i).getSortOrder().name()));
		}
		return sb.toString();
	}

	private static List<SortKey> sortKeysFromStr(String s) {
		List<SortKey> list = new ArrayList<>();
		if (!s.isEmpty()) {
			Pattern pattern = Pattern.compile("([0-9]+)[ ]*:[ ]*([A-Z]+)");
			for (String t : s.split("[ ]*,[ ]*")) {
				Matcher matcher = pattern.matcher(t);
				if (matcher.matches()) {
					int column = Integer.parseInt(matcher.group(1));
					SortOrder order = SortOrder.valueOf(matcher.group(2));
					list.add(new SortKey(column, order));
				}
			}
		}
		return list;
	}

	public abstract static class AppAction extends AbstractAction {

		private String key;
		private JFrame frame;

		public AppAction(String key, JFrame frame) {
			super(TextHelper.get("action." + key + ".name"));
			this.key = key;
			this.frame = frame;
			String accelerator = TextHelper.get("action." + key + ".accelerator");
			if (!accelerator.isEmpty()) {
				putValue(javax.swing.Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator));
			}
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			if (App.trace("AppAction", "AppAction.actionPerformed() ---")) {
				App.trace("", "Key: %s", key);
				App.trace("", "Free memory: %,d", Runtime.getRuntime().freeMemory());
			}
			try {
				execute();
			} catch (Exception e) {
				ExceptionHelper.report(frame, e);
			}
			App.trace("AppAction", "Exit");
		}

		public abstract void execute() throws Exception;
	}
}
