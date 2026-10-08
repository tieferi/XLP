// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

public class PreferencesDialog extends JDialog {

	private AppData data;

	private JTextField docFolderText;
	private JTextField traceListText;
	private JCheckBox topicFromSettingsCheck;
	private JCheckBox topicFromPathCheck;

	public PreferencesDialog(AppData data) {
		setModal(true);

		this.data = data;

		docFolderText = new JTextField(data.getDocFolder().getPath(), 30);
		docFolderText.setEditable(false);
		docFolderText.setFocusable(false);

		JButton selectDocFolderButton = new JButton(getText("selectDocFolderButton.text"));
		selectDocFolderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectDocFolder();
			}
		});

		topicFromSettingsCheck = new JCheckBox(getText("topicFromSettingsCheck.text"), data.isTopicFromSettings());
		topicFromPathCheck = new JCheckBox(getText("topicFromPathCheck.text"), data.isTopicFromPath());

		traceListText = new JTextField(data.getTraceList(), 35);

		JPanel p = new JPanel();

		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		p.setLayout(new GridLayout(0, 1));

		p.add(new JLabel(getText("docFolder.label")));
		p.add(GUIHelper.rowOf(docFolderText, selectDocFolderButton));

		p.add(new JLabel(getText("topic.label")));
		p.add(GUIHelper.rowOf(topicFromSettingsCheck, topicFromPathCheck));

		p.add(new JLabel(getText("traceList.label")));
		p.add(traceListText);

		getContentPane().add(p, BorderLayout.CENTER);

		JButton okButton = GUIHelper.okButton();
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});

		getContentPane().add(GUIHelper.buildButtonPanel(okButton), BorderLayout.SOUTH);
		SwingUtilities.getRootPane(okButton).setDefaultButton(okButton);

		pack();
		setLocationRelativeTo(null);
	}

	private String getText(String key) {
		return TextHelper.get("preferencesDialog." + key);
	}

	private void selectDocFolder() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File(docFolderText.getText()));
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			docFolderText.setText(chooser.getSelectedFile().getPath());
		}
	}
	
	private void ok() {
		data.setDocFolder(new File(docFolderText.getText()));
		data.setTopicFromPath(topicFromPathCheck.isSelected());
		data.setTopicFromSettings(topicFromSettingsCheck.isSelected());
		data.setTraceList(traceListText.getText());
		App.setTraceFilter();
		View view = App.getInstance().getView();
		if (view.getViewType() == View.Type.LINK_LIST) {
			((LinkListView)view).update();
		}
		dispose();
	}
}
