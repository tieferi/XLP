// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.View.Type;

import java.io.File;
import java.util.List;

public class AppData {

    public static class Link {

        private File file;

        private String id;
        private String name;
        private String topic;

        private long fileTimestamp;
        private long learnTimestamp;

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public long getFileTimestamp() {
            return fileTimestamp;
        }

        public void setFileTimestamp(long fileTimestamp) {
            this.fileTimestamp = fileTimestamp;
        }

        public long getLearnTimestamp() {
            return learnTimestamp;
        }

        public void setLearnTimestamp(long learnTimestamp) {
            this.learnTimestamp = learnTimestamp;
        }
    }

    public static class ScreenPos {

        private int x;
        private int y;

        public ScreenPos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    private List<Link> linkList;
    private String linkId;

    private String idPrefix;
    private int idCount;

    private Type viewType;
    private ScreenPos screenPos;

    private String topic;
    private String linkTableSortOrder;

    private File docFolder;
    private boolean topicFromSettings;
    private boolean topicFromPath;
    public String traceList;

    public List<Link> getLinkList() {
        return linkList;
    }

    public void setLinkList(List<Link> linkList) {
        this.linkList = linkList;
    }

    public Link getLink(String id) {
        for (Link link : linkList) {
            if (link.getId().equals(id)) {
                return link;
            }
        }
        return null;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getIdPrefix() {
        return idPrefix;
    }

    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public int getIdCount() {
        return idCount;
    }

    public void setIdCount(int idCount) {
        this.idCount = idCount;
    }

    public Type getViewType() {
        return viewType;
    }

    public void setViewType(Type viewType) {
        this.viewType = viewType;
    }

    public ScreenPos getScreenPos() {
        return screenPos;
    }

    public void setScreenPos(ScreenPos screenPos) {
        this.screenPos = screenPos;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getLinkTableSortOrder() {
        return linkTableSortOrder;
    }

    public void setLinkTableSortOrder(String linkTableSortOrder) {
        this.linkTableSortOrder = linkTableSortOrder;
    }

    public File getDocFolder() {
        return docFolder;
    }

    public void setDocFolder(File docFolder) {
        this.docFolder = docFolder;
    }

    public boolean isTopicFromSettings() {
        return topicFromSettings;
    }

    public void setTopicFromSettings(boolean topicFromSettings) {
        this.topicFromSettings = topicFromSettings;
    }

    public boolean isTopicFromPath() {
        return topicFromPath;
    }

    public void setTopicFromPath(boolean topicFromPath) {
        this.topicFromPath = topicFromPath;
    }

    public String getTraceList() {
        return traceList;
    }

    public void setTraceList(String traceList) {
        this.traceList = traceList;
    }
}
