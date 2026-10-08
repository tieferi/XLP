// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.AppData.Link;

import java.io.File;

public class TopicHelper {

    public static String getTopic(Link link) {
        AppData appData = App.getInstance().getData();
        String topic1 = appData.isTopicFromSettings() ? getTopicFromSettings(link) : null;
        String topic2 = appData.isTopicFromPath() ? getTopicFromPath(link) : null;
        return topic1 != null ? topic1 : topic2;
    }

    private static String getTopicFromSettings(Link link) {
        File file = link.getFile();
        long t = file.lastModified();
        if (t > link.getFileTimestamp()) {
            try {
                ExcelData content = ExcelHelper.getData(ExcelHelper.read(file));
                Settings settings = SettingsHelper.build(content);
                link.setTopic(settings.getTopic());
                link.setFileTimestamp(t);
            } catch (Exception e) {
            }
        }
        return link.getTopic();
    }

    private static String getTopicFromPath(Link link) {
        String topic = null;
        try {
            String path = link.getFile().getParentFile().getCanonicalPath();
            String base = App.getInstance().getData().getDocFolder().getCanonicalPath() + "/";
            if (path.startsWith(base)) {
                topic = path.substring(base.length()).replace("/", " - ");
            }
        } catch (Exception e) {
        }
        return topic;
    }
}
