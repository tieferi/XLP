// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.AppData.Link;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class LinkListView extends View {

    private JComboBox<String> topicCB;

    private JTable linkTable;

    private JButton addButton;
    private JButton removeButton;
    private JButton openButton;

    private boolean updating;

    private static AppData appData() {
        return App.getInstance().getData();
    }

    public LinkListView() {
        super(Type.LINK_LIST);
        buildFrame();
        buildMenu();
        update();
        setLocation();
    }

    private void buildFrame() {
        setTitle(TextHelper.get("linkListView.title"));
        setSize(new Dimension(600, 450));
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                try {
                    LinkListView.this.saveState();
                } catch (Exception e) {
                    ExceptionHelper.report(LinkListView.this, e);
                }
                App.getInstance().shutdown();
            }
        });

        topicCB = new JComboBox<>();
        LinkListViewHelper.updateTopicCB(topicCB);
        topicCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!updating) {
                    appData().setTopic(LinkListViewHelper.getSelTopic(topicCB));
                    updateLinks();
                    updateButtons();
                }
            }
        });
        topicCB.setFocusable(false);

        getContentPane().add(topicCB, BorderLayout.NORTH);

        linkTable = LinkListViewHelper.buildLinkTable(this);
        linkTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!updating) {
                    appData().setLinkId(LinkListViewHelper.getSelLinkId(linkTable));
                    updateButtons();
                }
            }
        });
        linkTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && appData().getLinkId() != null) {
                    openLink();
                }
            }
        });
        linkTable.getInputMap(JTable.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "close");
        linkTable.getActionMap().put("close", getCloseAction());

        JScrollPane sp = new JScrollPane(linkTable);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        getContentPane().add(sp, BorderLayout.CENTER);

        addButton = new JButton(TextHelper.get("linkListView.addButton.text"));
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addLink();
                update();
            }
        });
        addButton.setFocusable(false);

        removeButton = new JButton(TextHelper.get("linkListView.removeButton.text"));
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeLink();
                update();
            }
        });
        removeButton.setFocusable(false);

        openButton = new JButton(TextHelper.get("linkListView.openButton.text"));
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openLink();
            }
        });
        openButton.setFocusable(false);

        GUIHelper.makeEqualWidth(addButton, removeButton, openButton);
        JPanel p = GUIHelper.rowOf(addButton, removeButton, Box.createHorizontalGlue(), openButton);
        p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        getContentPane().add(p, BorderLayout.SOUTH);

        SwingUtilities.getRootPane(openButton).setDefaultButton(openButton);
    }

    private void buildMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getFileMenu());
        menuBar.add(getHelpMenu());
        setJMenuBar(menuBar);
    }

    public void update() {
        updateTopics();
        updateLinks();
        updateButtons();
    }

    private void updateTopics() {
        updating = true;
        try {
            LinkListViewHelper.updateTopicCB(topicCB);
        } finally {
            updating = false;
        }
    }

    private void updateLinks() {
        updating = true;
        try {
            LinkListViewHelper.updateLinkTable(linkTable);
        } finally {
            updating = false;
        }
    }

    private void updateButtons() {
        removeButton.setEnabled(appData().getLinkId() != null);
        openButton.setEnabled(appData().getLinkId() != null);
    }

    @Override
    protected void saveState() {
        appData().setLinkTableSortOrder(GUIHelper.getTableSortOrder(linkTable));
        saveLocation();
    }

    private void addLink() {
        try {
            JFileChooser chooser = getFileChooser();
            chooser.setMultiSelectionEnabled(true);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                Set<File> fileSet = new HashSet<>();
                for (Link link : appData().getLinkList()) {
                    fileSet.add(link.getFile());
                }
                boolean sel = false;
                for (File file : chooser.getSelectedFiles()) {
                    if (fileSet.contains(file)) {
                        continue;
                    }
                    Link link = Project.createLink(file);
                    appData().getLinkList().add(link);
                    if (!sel) {
                        appData().setLinkId(link.getId());
                        appData().setTopic(TopicHelper.getTopic(link));
                        sel = true;
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHelper.report(this, e);
        }
    }

    private void removeLink() {
        String id = appData().getLinkId();
        Link link = appData().getLink(id);
        appData().getLinkList().remove(link);
        appData().setLinkId(null);
        File dataFile = Project.getDataFile(id);
        if (dataFile.exists()) {
            dataFile.delete();
        }
    }

    private void openLink() {
        try {
            saveState();
            String id = appData().getLinkId();
            Link link = appData().getLink(id);
            File file = link.getFile();
            if (!file.exists()) {
                JFileChooser chooser = getFileChooser();
                chooser.setMultiSelectionEnabled(false);
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    link.setFile(file);
                    link.setName(FileHelper.removeExt(file.getName()));
                } else {
                    return;
                }
            }
            Project project = Project.resolveLink(link);
            View view = new ProjectView(project);
            view.setVisible(true);
            dispose();
        } catch (Exception e) {
            ExceptionHelper.report(this, e);
        }
    }

    private JFileChooser getFileChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(
                new FileNameExtensionFilter(TextHelper.get("fileChooser.xlsx.description"), "xlsx"));
        chooser.addChoosableFileFilter(
                new FileNameExtensionFilter(TextHelper.get("fileChooser.xlsm.description"), "xlsm"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setCurrentDirectory(appData().getDocFolder());
        return chooser;
    }
}
