// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.AppData.Link;
import de.t11r.cards.AppData.ScreenPos;
import de.t11r.cards.GUIHelper.AppAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;

public abstract class View extends JFrame {

    public enum Type { LINK_LIST, PROJECT, LEARN }
    private Type viewType;

    public View(Type viewType) {
        this.viewType = viewType;
        App.getInstance().setView(this);
    }

    public Type getViewType() {
        return viewType;
    }

    public void setLocation() {
        ScreenPos screenPos = App.getInstance().getData().getScreenPos();
        if (screenPos != null) {
            int x = screenPos.getX() - getWidth() / 2;
            int y = screenPos.getY() - getHeight() / 2;
            setLocation(x, y);
        } else {
            setLocationRelativeTo(null);
        }
    }

    public void saveLocation() {
        int x = getX() + getWidth() / 2;
        int y = getY() + getHeight() / 2;
        App.getInstance().getData().setScreenPos(new ScreenPos(x, y));
    }

    protected abstract void saveState() throws Exception;

    public JMenu getFileMenu() {
        JMenu helpMenu = new JMenu(TextHelper.get("menu.file.name"));
        helpMenu.add(new AppAction("openExcel", this) {
            @Override
            public void execute() throws Exception {
                openExcel();
            }
        });
        return helpMenu;
    }

    private void openExcel() {
        try {
            AppData data = App.getInstance().getData();
            String id = data.getLinkId();
            if (id != null) {
                Desktop.getDesktop().open(data.getLink(id).getFile());
            }
        } catch (Exception e) {
            ExceptionHelper.report(this, e);
        }
    }

    public JMenu getHelpMenu() {
        JMenu helpMenu = new JMenu(TextHelper.get("menu.help.name") + " ");
        helpMenu.add(new AppAction("gettingStarted", this) {
            @Override
            public void execute() throws Exception {
                DocHelper.open("help.gettingStarted");
            }
        });
        return helpMenu;
    }

    public Action getCloseAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispatchEvent(new WindowEvent(View.this, WindowEvent.WINDOW_CLOSING));
            }
        };
    }
}
