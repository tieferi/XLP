// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.AppData.Link;
import de.t11r.cards.Settings.LearningProfile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class Project {

    private static final String ID_DATE_FORMAT = "yyyyMMdd";

    public static Link createLink(File file) throws Exception {
        if (!file.exists()) {
            ExceptionHelper.fail("fileNotFound", file.getPath());
        }
        Link link = new Link();
        link.setFile(file);
        link.setId(nextId());
        link.setName(FileHelper.removeExt(file.getName()));
        return link;
    }

    private static String nextId() throws Exception {
        AppData appData = App.getInstance().getData();
        String idPrefix = new SimpleDateFormat(ID_DATE_FORMAT).format(new Date());
        int idCount = idPrefix.equals(appData.getIdPrefix()) ? appData.getIdCount() : 0;
        if (idCount >= 100) {
            ExceptionHelper.fail("linkCountOverflow");
        }
        String id = String.format("%s%02d", idPrefix, idCount++);
        appData.setIdPrefix(idPrefix);
        appData.setIdCount(idCount);
        return id;
    }

    public static Project resolveLink(Link link) throws Exception {
        return new Project(link);
    }

    public static Date getIdDate(String id) {
        Date d = null;
        try {
            d = new SimpleDateFormat(ID_DATE_FORMAT).parse(id.substring(0, 8));
        } catch (Exception e) {
        }
        return d;
    }

    public static File getDataFile(String id) {
        return new File(String.format("%s/%s.json", OSHelper.appFolder().getPath(), id));
    }

    private Link link;

    private ExcelData content;
    private long contentTimestamp;

    private Settings settings;

    private ProjectData data;

    public Project(Link link) throws Exception {
        this.link = link;
        init();
    }

    private void init() throws Exception {
        updateContent();
        settings = SettingsHelper.build(content);
        File dataFile = getDataFile(link().getId());
        if (dataFile.exists()) {
            data = ProjectDataJson.read(dataFile);
            data.getSheetNameSet().removeIf(name -> content.getSheet(name) == null);
            if (settings.getCardType(data.getCardTypeName()) == null) {
                data.setCardTypeName(settings.getDefaultCardTypeName());
            }
            data.getLearningProfileNameMap().entrySet().removeIf(entry -> settings.getCardType(entry.getKey()) == null
                || settings.getLearningProfile(entry.getValue()) == null);
            if (!data.getLearningProfileNameMap().containsKey(data.getCardTypeName())) {
                data.getLearningProfileNameMap().put(data.getCardTypeName(), settings.getDefaultLearningProfileName());
            }

        } else {
            data = new ProjectData();
            data.setSheetNameSet(new TreeSet<>());
            data.setCardTypeName(settings.getDefaultCardTypeName());
            data.setLearningProfileNameMap(new TreeMap<>());
            data.getLearningProfileNameMap().put(data.getCardTypeName(), settings.getDefaultLearningProfileName());
            data.setStatsMap(new TreeMap<>());
        }
    }

    public boolean updateContent() throws Exception {
        File file = link.getFile();
        long t = file.lastModified();
        if (t > contentTimestamp) {
            content = ExcelHelper.getData(ExcelHelper.read(file));
            contentTimestamp = t;
            return true;
        } else {
            return false;
        }
    }

    public List<Card> buildCardList() {
        return CardListHelper.buildCardList(content, settings, data.getSheetNameSet(), data.getCardTypeName());
    }

    public void saveData() throws Exception {
        ProjectDataJson.write(data, getDataFile(link.getId()));
    }

    public Link link() {
        return link;
    }

    public ExcelData content() {
        return content;
    }

    public Settings settings() {
        return settings;
    }

    public ProjectData data() {
        return data;
    }

    public LearningProfile learningProfile() {
        return settings.getLearningProfile(data.getLearningProfileNameMap().get(data.getCardTypeName()));
    }
}
