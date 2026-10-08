// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.AppData.Link;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;
import java.util.*;

public class LinkListViewHelper {

    public static void updateTopicCB(JComboBox<String> topicCB) {
        SortedSet<String> topicSet = new TreeSet<>();
        for (Link link : appData().getLinkList()) {
            String topic = TopicHelper.getTopic(link);
            if (topic != null) {
                topicSet.add(topic);
            }
        }
        topicCB.removeAllItems();
        topicCB.addItem(TextHelper.get("linkListView.topicCB.noTopic"));
        for (String topic : topicSet) {
            topicCB.addItem(topic);
        }
        String topic = appData().getTopic();
        if (topic != null && topicSet.contains(topic)) {
            topicCB.setSelectedItem(topic);
        } else {
            topicCB.setSelectedIndex(0);
            appData().setTopic(null);
        }
    }

    public static String getSelTopic(JComboBox<String> topicCB) {
        int i = topicCB.getSelectedIndex();
        if (i > 0) {
            return topicCB.getItemAt(i);
        } else {
            return null;
        }
    }

    public static JTable buildLinkTable(JFrame frame) {
        JTable table = new JTable(new LinkTableModel());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ENTER"), "none");
        setLinkTableRenderer(table);
        setLinkTableColWidths(table, frame);
        GUIHelper.enableTableSorting(table, appData().getLinkTableSortOrder());
        updateLinkTable(table);
        return table;
    }

    private static class LinkTableModel extends AbstractTableModel {

        private List<String> idList = Collections.emptyList();
        private Map<String, Link> linkMap = Collections.emptyMap();

        public void update() {
            idList = new ArrayList<>();
            linkMap = new HashMap<>();
            String topic = appData().getTopic();
            for (Link link : appData().getLinkList()) {
                String topic2 = TopicHelper.getTopic(link);
                if (Objects.equals(topic2, topic)) {
                    idList.add(link.getId());
                    linkMap.put(link.getId(), link);
                }
            }
            fireTableDataChanged();
        }

        public List<String> getIdList() {
            return idList;
        }

        private Link link(int i) {
            return linkMap.get(idList.get(i));
        }

        @Override
        public int getRowCount() {
            return idList.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int j) {
            return TextHelper.get(String.format("linkListView.linkTable.col%d.name", j));
        }

        @Override
        public Class<?> getColumnClass(int j) {
            switch (j) {
                case 0: return String.class;
                case 1: return Date.class;
                case 2: return String.class;
            }
            return null;
        }

        @Override
        public Object getValueAt(int i, int j ) {
            switch (j) {
                case 0: return link(i).getId();
                case 1: return DateHelper.getDate(link(i).getLearnTimestamp());
                case 2: return link(i).getName();
            }
            return null;
        }
    }

    private static void setLinkTableColWidths(JTable table, JFrame frame) {
        TableColumnModel colModel = table.getColumnModel();
        FontMetrics fm = table.getFontMetrics(table.getFont());
        Graphics g = frame.getGraphics();

        int w0 = (int)fm.getStringBounds("X".repeat(10), g).getWidth() + 10;
        colModel.getColumn(0).setPreferredWidth(w0);

        int w1 = (int)fm.getStringBounds("X".repeat(15), g).getWidth() + 10;
        colModel.getColumn(1).setPreferredWidth(w1);

        int w2 = (int)fm.getStringBounds("X".repeat(40), g).getWidth() + 10;
        colModel.getColumn(2).setPreferredWidth(w2);
    }

    private static void setLinkTableRenderer(JTable table) {
        TableCellRenderer renderer0 = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value != null) {
                    value = DateHelper.formatDate(Project.getIdDate((String)value));
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        table.getColumnModel().getColumn(0).setCellRenderer(renderer0);

        TableCellRenderer renderer1 = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value != null) {
                    value = DateHelper.howLongAgo((Date)value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };
        table.getColumnModel().getColumn(1).setCellRenderer(renderer1);
    }

    public static void updateLinkTable(JTable linkTable) {
        LinkTableModel model = (LinkTableModel)(linkTable.getModel());
        model.update();
        String id = appData().getLinkId();
        List<String> idList = model.getIdList();
        if (!idList.isEmpty()) {
            if (id == null || !idList.contains(id)) {
                id = idList.get(0);
            }
        } else {
            id = null;
        }
        appData().setLinkId(id);
        if (id != null) {
            int i = idList.indexOf(id);
            int j = linkTable.convertRowIndexToView(i);
            linkTable.setRowSelectionInterval(j, j);
        } else {
            linkTable.clearSelection();
        }
    }

    public static String getSelLinkId(JTable linkTable) {
        int i = linkTable.getSelectedRow();
        if (i >= 0) {
            LinkTableModel model = (LinkTableModel)(linkTable.getModel());
            int j = linkTable.convertRowIndexToModel(i);
            return model.getIdList().get(j);
        } else {
            return null;
        }
    }

    private static AppData appData() {
        return App.getInstance().getData();
    }
}
