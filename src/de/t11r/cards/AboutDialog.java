// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AboutDialog extends JDialog {
	
	public AboutDialog() {
		setModal(true);

		ImageIcon icon = new ImageIcon(getClass().getResource(TextHelper.get("aboutDialog.icon")));
		JLabel l1 = new JLabel(icon);
		l1.setAlignmentX(0.5f);
		
		JLabel l2 = new JLabel(TextHelper.get("aboutDialog.text1"));
		l2.setFont(new Font("Courier", Font.PLAIN, 18));
		l2.setAlignmentX(0.5f);

		JLabel l3 = new JLabel(TextHelper.get("aboutDialog.text2"));
		l3.setFont(new Font("Courier", Font.PLAIN, 12));
		l3.setAlignmentX(0.5f);

		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.add(l1);
		p.add(Box.createRigidArea(new Dimension(200, 10)));
		p.add(l2);
		p.add(l3);
		getContentPane().add(p, BorderLayout.CENTER);

		JButton closeButton = GUIHelper.closeButton();
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AboutDialog.this.dispose();
			}
		});

		getContentPane().add(GUIHelper.buildButtonPanel(closeButton), BorderLayout.SOUTH);
		SwingUtilities.getRootPane(closeButton).setDefaultButton(closeButton);

		pack();
		setLocationRelativeTo(null);
	}
}
