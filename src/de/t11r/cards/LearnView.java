// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.Card.Side;
import de.t11r.cards.GUIHelper.AppAction;
import de.t11r.cards.Settings.CardType;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LearnView extends View {
	
	private Project project;
	private Session session;

	private JPanel cardPanel;

	private JLabel header;
	private TextPane textPane;
	private JLabel footer1;
	private JLabel footer2;
	private JLabel footer3;

	private Color cardColorFront;
	private Color cardColorBack;
	private Color textColorFront;
	private Color textColorBack;

	private Action openWebpageAction;

	private Action nextAction;
	private Action okAction;
	private Action turnAction;

	private Action undoAction;
	private Action redoAction;

	private Action showStatsAction;

	public LearnView(Project project) {
		super(Type.LEARN);
		this.project = project;
		session = new Session(project);
		buildFrame();
		buildMenu();
		setLocation();
	}
	
	private void buildFrame() {
		setTitle(project.link().getName());
		setSize(Settings.getCardSize(project.settings().getCardSize()));
		setResizable(false);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent event) {
				try {
					LearnView.this.session.update();
					LearnView.this.update();
				} catch (Exception e) {
					ExceptionHelper.report(LearnView.this, e);
				}
			}
			@Override
			public void windowClosing(WindowEvent event) {
				try {
					LearnView.this.openProjectView();
				} catch (Exception e) {
					ExceptionHelper.report(LearnView.this, e);
				}
			}
		});

		header = new JLabel();

		textPane = new TextPane("Arial", project.settings().getMeta());
		textPane.setFocusTraversalKeysEnabled(false);
		textPane.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("TAB"), "turn");
		textPane.getActionMap().put("turn", turnAction);
		textPane.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "close");
		textPane.getActionMap().put("close", getCloseAction());

		footer1 = new JLabel();
		footer2 = new JLabel();
		footer3 = new JLabel();

		cardPanel = new JPanel();
		Border b1 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border b2 = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
		Border b3 = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border b = BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(b1, b2), b3);
		cardPanel.setBorder(b);
		cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.PAGE_AXIS));

		JPanel p1 = new JPanel();
		p1.setOpaque(false);
		p1.setLayout(new BoxLayout(p1, BoxLayout.LINE_AXIS));
		p1.add(Box.createHorizontalGlue());
		p1.add(header);
		p1.add(Box.createHorizontalGlue());
		cardPanel.add(p1);

		JPanel p2 = new JPanel();
		p2.setOpaque(false);
		p2.setLayout(new BoxLayout(p2, BoxLayout.LINE_AXIS));
		p2.add(textPane);
		cardPanel.add(p2);

		JPanel p3 = new JPanel() {
			@Override
			public Dimension getMaximumSize() {
				int w = (int)super.getMaximumSize().getWidth();
				int h = (int)getPreferredSize().getHeight();
				return new Dimension(w, h);
			}
		};
		p3.setOpaque(false);
		p3.setLayout(new GridLayout(1, 3));
		p3.add(GUIHelper.rowOf(footer1, Box.createHorizontalGlue()));
		p3.add(GUIHelper.rowOf(Box.createHorizontalGlue(), footer2, Box.createHorizontalGlue()));
		p3.add(GUIHelper.rowOf(Box.createHorizontalGlue(), footer3));
		cardPanel.add(p3);

		getContentPane().add(cardPanel, BorderLayout.CENTER);

		CardType cardType = project.settings().getCardType(project.data().getCardTypeName());
		cardColorFront = cardType != null ? cardType.getCardColorFront() : Color.WHITE;
		cardColorBack = cardType != null ? cardType.getCardColorBack() : Color.WHITE;
		textColorFront = cardType != null ? cardType.getTextColorFront() : Color.BLACK;
		textColorBack = cardType != null ? cardType.getTextColorBack() : Color.BLACK;
	}
	
	private void buildMenu() {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = getFileMenu();

		openWebpageAction = new AppAction("openWebpage", this) {
			@Override
			public void execute() {
				String url = session.getCardUrl();
				if (url != null) {
					UrlHelper.openUrl(url);
				}
			}
		};
		fileMenu.add(openWebpageAction);

		menuBar.add(fileMenu);

		JMenu learnMenu = new JMenu(TextHelper.get("menu.learn.name"));

		nextAction = new AppAction("next", this) {
			@Override
			public void execute() {
				session.next(false);
				update();
			}
		};
		learnMenu.add(nextAction);
		
		okAction = new AppAction("ok", this) {
			@Override
			public void execute() {
				session.next(true);
				update();
			}
		};
		learnMenu.add(okAction);
		
		turnAction = new AppAction("turn", this) {
			@Override
			public void execute() {
				session.turn();
				update();
			}
		};
		learnMenu.add(turnAction);
		
		learnMenu.addSeparator();
		
		undoAction = new AppAction("undo", this) {
			@Override
			public void execute() {
				session.undo();
				update();
			}
		};
		learnMenu.add(undoAction);
		
		redoAction = new AppAction("redo", this) {
			@Override
			public void execute() {
				session.redo();
				update();
			}
		};
		learnMenu.add(redoAction);

		learnMenu.addSeparator();

		showStatsAction = new AppAction("showStats", this) {
			@Override
			public void execute() {
				header.setText(session.getCardStats());
				showStatsAction.setEnabled(false);
			}
		};
		learnMenu.add(showStatsAction);

		menuBar.add(learnMenu);

		menuBar.add(getHelpMenu());

		setJMenuBar(menuBar);
	}
	
	private void update() {
		header.setText(session.getCardInfo());

		if (!textPane.setText(session.getCardText(), Settings.getFontSize(project.settings().getTextSize()))) {
			header.setText("!");
		}

		int[] status = session.getInfo();
		footer1.setText(status[0] > 0 ? Integer.toString(status[0]) : "");
		footer2.setText("*".repeat(status[1]));
		footer3.setText(status[2] > 0 ? Integer.toString(status[2]) : "");

		Side side = session.getCardSide();
		Color fg = side == Side.FRONT ? textColorFront : textColorBack;
		Color bg = side == Side.FRONT ? cardColorFront : cardColorBack;
		GUIHelper.setForeground(fg, header, textPane, footer1, footer2, footer3);
		cardPanel.setBackground(bg);

		openWebpageAction.setEnabled(session.getCardUrl() != null);

		undoAction.setEnabled(session.canUndo());
		redoAction.setEnabled(session.canRedo());

		showStatsAction.setEnabled(true);

		revalidate();
	}

	@Override
	protected void saveState() throws Exception {
		project.link().setLearnTimestamp(System.currentTimeMillis());
		project.saveData();
		saveLocation();
	}

	private void openProjectView() throws Exception {
		saveState();
		View view = new ProjectView(project);
		view.setVisible(true);
		dispose();
	}
}
