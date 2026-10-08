// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.AppData.Link;
import de.t11r.cards.TraceHelper.TraceFilter;
import de.t11r.cards.View.Type;

import javax.swing.*;
import java.awt.*;
import java.awt.desktop.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;

public class App implements AboutHandler, PreferencesHandler, QuitHandler {

    private File dataFile;
    private AppData data;
    private View view;

    private TraceFilter traceFilter;
    private PrintWriter traceWriter;

    private static App app;

    public static App getInstance() {
        return app;
    }

    public static void setTraceFilter() {
        app.traceFilter = TraceHelper.traceFilter(app.data.getTraceList());
    }

    public static boolean trace(String id, String format, Object... args) {
        if (id == "" || (app.traceFilter != null && app.traceFilter.check(id))) {
            try {
                if (app.traceWriter == null) {
                    app.traceWriter = new PrintWriter(new FileWriter(OSHelper.appFolder() + "/trace.log"));
                }
                String line = String.format("[%s] %s", DateHelper.formatTimestamp(System.currentTimeMillis()),
                    String.format(format, args));
                app.traceWriter.println(line);
                app.traceWriter.flush();
            } catch (Exception e) {
            }
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        //Locale.setDefault(new Locale("en", "US"));
        System.setProperty("apple.awt.application.name", TextHelper.get("app.name"));
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.appearance", "system");

        app = new App();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    app.start();
                } catch (Exception e) {
                    ExceptionHelper.report(null, e);
                    System.exit(1);
                }
            }
        });
    }

    public void start() throws Exception {
        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(this);
        desktop.setPreferencesHandler(this);
        desktop.setQuitHandler(this);

        File appFolder = OSHelper.appFolder();
        if (!appFolder.exists() && !appFolder.mkdirs()) {
            throw new Exception();
        }

        dataFile = new File(String.format("%s/data.json", appFolder));
        if (dataFile.exists()) {
            data = AppDataJson.read(dataFile);
            setTraceFilter();
        } else {
            data = new AppData();
            data.setLinkList(new ArrayList<>());
            data.setDocFolder(OSHelper.docFolder());
            data.setTopicFromSettings(true);
        }

        Type viewType = data.getViewType() != null ? data.getViewType() : Type.LINK_LIST;
        View view = null;
        try {
            view = buildView(viewType);
        } catch (Exception e) {
            view = buildView(Type.LINK_LIST);
        }
        view.setVisible(true);
    }

    private View buildView(Type viewType) throws Exception {
        switch (viewType) {
            case LINK_LIST: return new LinkListView();
            case PROJECT: return new ProjectView(getProject());
            case LEARN: return new LearnView(getProject());
        }
        return null;
    }

    public void shutdown() {
        if (traceWriter != null) {
            traceWriter.close();
        }
        try {
            AppDataJson.write(data, dataFile);
        } catch (Exception e) {
            ExceptionHelper.report(null, e);
        }
        System.exit(0);
    }

    public AppData getData() {
        return data;
    }

    private Project getProject() throws Exception {
        String id = data.getLinkId();
        if (id == null) {
            throw new Exception();
        }
        Link link = data.getLink(id);
        if (link == null) {
            throw new Exception();
        }
        return Project.resolveLink(link);
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
        data.setViewType(view.getViewType());
    }

    @Override
    public void handleAbout(AboutEvent e) {
        AboutDialog dialog = new AboutDialog();
        dialog.setVisible(true);
    }

    @Override
    public void handlePreferences(PreferencesEvent e) {
        PreferencesDialog dialog = new PreferencesDialog(data);
        dialog.setVisible(true);
    }

    @Override
    public void handleQuitRequestWith(QuitEvent quitEvent, QuitResponse response) {
        try {
            if (view != null) {
                view.saveState();
                view.dispose();
            }
        } catch (Exception e) {
            ExceptionHelper.report(view, e);
        }
        shutdown();
    }
}
