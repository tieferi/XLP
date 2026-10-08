// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ProjectView extends View {

    private Project project;

    private JComboBox<String> cardTypeCB;

    private JTable sheetTable;
    private JLabel infoLabel;

    private JButton resetButton;
    private JComboBox<String> learningProfileCB;
    private JButton learnButton;

    public ProjectView(Project project) {
        super(Type.PROJECT);
        this.project = project;
        buildFrame();
        buildMenu();
        update();
        setLocation();
    }

    private void buildFrame() {
        setTitle(project.link().getName());
        setSize(new Dimension(600, 450));
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                try {
                    ProjectView.this.openLinkListView();
                } catch (Exception e) {
                    ExceptionHelper.report(ProjectView.this, e);
                }
            }
        });

        cardTypeCB = ProjectViewHelper.buildCardTypeCB(project);
        cardTypeCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cardTypeName = (String)cardTypeCB.getSelectedItem();
                project.data().setCardTypeName(cardTypeName);
                String learningProfileName = project.data().getLearningProfileNameMap().get(cardTypeName);
                if (learningProfileName == null) {
                    learningProfileName = project.settings().getDefaultLearningProfileName();
                    project.data().getLearningProfileNameMap().put(cardTypeName, learningProfileName);
                }
                learningProfileCB.setSelectedItem(learningProfileName);
                update();
            }
        });
        cardTypeCB.setFocusable(false);

        getContentPane().add(cardTypeCB, BorderLayout.NORTH);

        sheetTable = ProjectViewHelper.buildSheetTable(project, this);
        sheetTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getColumn() == 0) {
                    update();
                }
            }
        });
        sheetTable.getInputMap(JTable.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        sheetTable.getActionMap().put("close", getCloseAction());

        JScrollPane sp = new JScrollPane(sheetTable);

        infoLabel = new JLabel();
        infoLabel.setAlignmentX(0.5f);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));
        p1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 0, 10),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)));
        p1.add(sp);
        p1.add(infoLabel);
        getContentPane().add(p1, BorderLayout.CENTER);

        resetButton = new JButton(TextHelper.get("projectView.resetButton.text"));
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
                update();
            }
        });
        resetButton.setFocusable(false);

        learningProfileCB = ProjectViewHelper.buildLearningProfileCB(project);
        learningProfileCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String cardTypeName = (String)cardTypeCB.getSelectedItem();
                String learningProfileName = (String) learningProfileCB.getSelectedItem();
                project.data().getLearningProfileNameMap().put(cardTypeName, learningProfileName);
                update();
            }
        });
        learningProfileCB.setFocusable(false);

        learnButton = new JButton(TextHelper.get("projectView.learnButton.text"));
        learnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                learn();
            }
        });
        learnButton.setFocusable(false);

        GUIHelper.makeEqualWidth(resetButton, learnButton);
        JPanel p2 = GUIHelper.rowOf(resetButton, Box.createHorizontalGlue(), learningProfileCB, learnButton);
        p2.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        getContentPane().add(p2, BorderLayout.SOUTH);

        SwingUtilities.getRootPane(learnButton).setDefaultButton(learnButton);
    }

    private void buildMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getFileMenu());
        menuBar.add(getHelpMenu());
        setJMenuBar(menuBar);
    }

    private void update() {
        List<Card> list = project.buildCardList();
        int n = list.size();

        ProjectViewHelper.updateSheetTable(sheetTable);
        infoLabel.setText(TextHelper.format("projectView.count.text." + (n == 0 ? "0" : n == 1 ? "1" : "2"), n));

        resetButton.setEnabled(Algo.getStatus(project.learningProfile(), list, project.data().getStatsMap()) != null);
        learnButton.setEnabled(n > 0);
    }

    @Override
    protected void saveState() throws Exception {
        project.saveData();
        saveLocation();
    }

    private void openLinkListView() throws Exception {
        saveState();
        View view = new LinkListView();
        view.setVisible(true);
        dispose();
    }

    private void reset() {
        List<Card> list = project.buildCardList();
        Algo.reset(project.learningProfile(), list, project.data().getStatsMap());
        project.data().setCardKey(null);
        project.data().setCardSide(null);
    }

    private void learn() {
        try {
            saveState();
            View view = new LearnView(project);
            view.setVisible(true);
            dispose();
        } catch (Exception e) {
            ExceptionHelper.report(this, e);
        }
    }
}
