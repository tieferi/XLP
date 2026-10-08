// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.ExcelData.Sheet;
import de.t11r.cards.Settings.LearningProfile;
import de.t11r.cards.Settings.CardType;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class ProjectViewHelper {

    public static JComboBox<String> buildCardTypeCB(Project project) {
        JComboBox<String> cardTypeCB = new JComboBox<>();
        for (CardType cardType : project.settings().getCardTypeList()) {
            cardTypeCB.addItem(cardType.getName());
        }
        String cardTypeName = project.data().getCardTypeName();
        cardTypeCB.setSelectedItem(cardTypeName);
        return cardTypeCB;
    }

    public static JComboBox<String> buildLearningProfileCB(Project project) {
        JComboBox<String> learningProfileCB = new JComboBox<>();
        for (LearningProfile learningProfile : project.settings().getLearningProfileList()) {
            learningProfileCB.addItem(learningProfile.getName());
        }
        String cardTypeName = project.data().getCardTypeName();
        String learningProfileName = project.data().getLearningProfileNameMap().get(cardTypeName);
        learningProfileCB.setSelectedItem(learningProfileName);
        return learningProfileCB;
    }

    public static JTable buildSheetTable(Project project, JFrame frame) {
        JTable table = new JTable(new SheetTableModel(project));
        table.setRowSelectionAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ENTER"), "none");
        table.getInputMap(JTable.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("shift DOWN"), "extendDown");
        table.getActionMap().put("extendDown", extendAction(table, +1));
        table.getInputMap(JTable.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("shift UP"), "extendUp");
        table.getActionMap().put("extendUp", extendAction(table, -1));

        setSheetTableColWidths(table, frame);
        GUIHelper.enableTableHeaderAlignment(table);
        return table;
    }

    private static class SheetTableModel extends AbstractTableModel {

        private Project project;

        private List<String> sheetNameList;
        private List<String> sheetTitleList;
        private int sheetCount;

        private int[] cardCounts;
        private String[] status;

        public SheetTableModel(Project project) {
            this.project = project;
            sheetNameList = new ArrayList<>();
            sheetTitleList = new ArrayList<>();
            for (Sheet sheet : project.content().getSheets()) {
                String name = sheet.getName();
                if (!name.startsWith(".")) {
                    sheetNameList.add(name);
                    sheetTitleList.add(getSheetTitle(sheet));
                }
            }
            sheetCount = sheetNameList.size();
            cardCounts = new int[sheetCount];
            status = new String[sheetCount];
            update();
        }

        public void update() {
            for (int i = 0; i < sheetCount; i++) {
                cardCounts[i] = 0;
                status[i] = "";
                String cardTypeName = project.data().getCardTypeName();
                if (cardTypeName != null) {
                    List<Card> list = CardListHelper.buildCardList(project.content(), project.settings(),
                            Set.of(sheetNameList.get(i)), cardTypeName);
                    cardCounts[i] = list.size();
                    status[i] = statusText(project, list);
                }
            }
            fireTableChanged(new TableModelEvent(this, 0, sheetCount - 1, 1));
            fireTableChanged(new TableModelEvent(this, 0, sheetCount - 1, 2));
        }

        public boolean isSelected(int i) {
            String sheetName = sheetNameList.get(i);
            return  project.data().getSheetNameSet().contains(sheetName);
        }

        public void setSelected(int i, boolean selected) {
            String sheetName = sheetNameList.get(i);
            if (selected) {
                project.data().getSheetNameSet().add(sheetName);
            } else {
                project.data().getSheetNameSet().remove(sheetName);
            }
        }

        @Override
        public int getRowCount() {
            return sheetCount;
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int j) {
            return TextHelper.get(String.format("projectView.sheetTable.col%d.name", j));
        }

        @Override
        public Class<?> getColumnClass(int j) {
            switch (j) {
                case 0: return Boolean.class;
                case 1: return Integer.class;
                case 2: return String.class;
                case 3: return String.class;
            }
            return null;
        }

        @Override
        public Object getValueAt(int i, int j) {
            switch (j) {
                case 0: return isSelected(i);
                case 1: return cardCounts[i];
                case 2: return status[i];
                case 3: return sheetTitleList.get(i);
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int i, int j) {
            return j == 0;
        }

        @Override
        public void setValueAt(Object value, int i, int j) {
            if (j == 0) {
                setSelected(i, (boolean)value);
                fireTableCellUpdated(i, j);
            }
        }
    }

    private static String getSheetTitle(Sheet sheet) {
        if (sheet.getFixedRowCount() > 0 && !StringHelper.nullOrEmpty(sheet.getString(0, 0))) {
            return sheet.getName() + " - " + sheet.getString(0, 0);
        } else {
            return sheet.getName();
        }
    }

    private static String statusText(Project project, List<Card> list) {
        List<String> list2 = new ArrayList<>();
        for (LearningProfile learningProfile : project.settings().getLearningProfileList()) {
            String status = Algo.getStatus(learningProfile, list, project.data().getStatsMap());
            if (status != null) {
                list2.add(status);
            }
        }
        return String.join(", ", list2);
    }

    private static void setSheetTableColWidths(JTable sheetTable, JFrame frame) {
        TableColumnModel colModel = sheetTable.getColumnModel();
        FontMetrics fm = sheetTable.getFontMetrics(sheetTable.getFont());
        Graphics g = frame.getGraphics();

        int w0 = colModel.getColumn(0).getMinWidth() + 10;
        colModel.getColumn(0).setPreferredWidth(w0);

        int w1 = (int)fm.getStringBounds("0".repeat(5), g).getWidth() + 10;
        colModel.getColumn(1).setPreferredWidth(w1);

        int w2 = (int)fm.getStringBounds("X".repeat(15), g).getWidth() + 10;
        colModel.getColumn(2).setPreferredWidth(w2);

        int w3 = (int)fm.getStringBounds("X".repeat(40), g).getWidth() + 10;
        colModel.getColumn(3).setPreferredWidth(w3);
    }

    public static void updateSheetTable(JTable sheetTable) {
        ((SheetTableModel)sheetTable.getModel()).update();
    }

    public static Action extendAction(JTable table, int d) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = table.getSelectedRow(), j = i + d;
                int n = table.getRowCount();
                if (j >= 0 && j < n) {
                    SheetTableModel model = (SheetTableModel)table.getModel();
                    model.setSelected(j, model.isSelected(i));
                    table.setRowSelectionInterval(j, j);
                    model.fireTableChanged(new TableModelEvent(model, j, j, 0));
                }
            }
        };
    }
}
