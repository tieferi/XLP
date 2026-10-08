// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.AppData.Link;
import de.t11r.cards.AppData.ScreenPos;
import de.t11r.cards.JsonHelper.Parser;
import de.t11r.cards.View.Type;

import javax.json.stream.JsonGenerator;
import java.io.File;
import java.util.*;

public class AppDataJson {

    private static int VERSION = 1;

    public static AppData read(File file) throws Exception {
        AppData data = new AppData();
        Parser p = JsonHelper.parser(file);
        try {
            if (!p.startObject()) {
                p.fail();
            }
            int version = 0;
            if (p.key("version")) {
                if (p.value()) {
                    version = p.integerValue();
                } else {
                    p.fail();
                }
            }
            if (version != VERSION) {
                ExceptionHelper.fail("dataFormatError");
            }
            if (p.key("linkList")) {
                List<Link> linkList = new ArrayList<>();
                if (p.startArray()) {
                    while (!p.endArray()) {
                        Link link = new Link();
                        if (!p.startObject()) {
                            p.fail();
                        }
                        if (p.key("file")) {
                            if (p.value()) {
                                link.setFile(new File(p.stringValue()));
                            } else {
                                p.fail();
                            }
                        }
                        if (p.key("id")) {
                            if (p.value()) {
                                link.setId(p.stringValue());
                            } else {
                                p.fail();
                            }
                        }
                        if (p.key("name")) {
                            if (p.value()) {
                                link.setName(p.stringValue());
                            } else {
                                p.fail();
                            }
                        }
                        if (p.key("topic")) {
                            if (p.value()) {
                                link.setTopic(p.stringValue());
                            } else {
                                p.fail();
                            }
                        }
                        if (p.key("fileTimestamp")) {
                            if (p.value()) {
                                link.setFileTimestamp(DateHelper.parseTimestamp(p.stringValue()));
                            } else {
                                p.fail();
                            }
                        }
                        if (p.key("learnTimestamp")) {
                            if (p.value()) {
                                link.setLearnTimestamp(DateHelper.parseTimestamp(p.stringValue()));
                            } else {
                                p.fail();
                            }
                        }
                        if (!p.endObject()) {
                            p.fail();
                        }
                        linkList.add(link);
                    }
                } else {
                    p.fail();
                }
                data.setLinkList(linkList);
            }
            if (p.key("linkId")) {
                if (p.value()) {
                    data.setLinkId(p.stringValue());
                } else {
                    p.fail();
                }
            }
            if (p.key("idPrefix")) {
                if (p.value()) {
                    data.setIdPrefix(p.stringValue());
                } else {
                    p.fail();
                }
            }
            if (p.key("idCount")) {
                if (p.value()) {
                    data.setIdCount(p.integerValue());
                } else {
                    p.fail();
                }
            }
            if (p.key("viewType")) {
                if (p.value()) {
                    data.setViewType(Type.valueOf(p.stringValue()));
                } else {
                    p.fail();
                }
            }
            if (p.key("screenPos")) {
                if (!p.startObject()) {
                    p.fail();
                }
                if (!p.key("x") || !p.value()) {
                    p.fail();
                }
                int x = p.integerValue();
                if (!p.key("y") || !p.value()) {
                    p.fail();
                }
                int y = p.integerValue();
                if (!p.endObject()) {
                    p.fail();
                }
                data.setScreenPos(new ScreenPos(x, y));
            }
            if (p.key("topic")) {
                if (p.value()) {
                    data.setTopic(p.stringValue());
                } else {
                    p.fail();
                }
            }
            if (p.key("linkTableSortOrder")) {
                if (p.value()) {
                    data.setLinkTableSortOrder(p.stringValue());
                } else {
                    p.fail();
                }
            }
            if (p.key("docFolder")) {
                if (p.value()) {
                    data.setDocFolder(new File(p.stringValue()));
                } else {
                    p.fail();
                }
            }
            if (p.key("topicFromSettings")) {
                if (p.value()) {
                    data.setTopicFromSettings(p.booleanValue());
                } else {
                    p.fail();
                }
            }
            if (p.key("topicFromPath")) {
                if (p.value()) {
                    data.setTopicFromPath(p.booleanValue());
                } else {
                    p.fail();
                }
            }
            if (p.key("traceList")) {
                if (p.value()) {
                    data.setTraceList(p.stringValue());
                } else {
                    p.fail();
                }
            }
            if (!p.endObject()) {
                p.fail();
            }

        } finally {
            p.close();
        }
        return data;
    }

    public static void write(AppData data, File file) throws Exception {
        JsonGenerator g = JsonHelper.generator(file);
        g.writeStartObject();
        g.write("version", VERSION);
        if (data.getLinkList() != null) {
            g.writeStartArray("linkList");
            for (Link link : data.getLinkList()) {
                g.writeStartObject();
                if (link.getFile() != null) {
                    g.write("file", link.getFile().getPath());
                }
                if (link.getId() != null) {
                    g.write("id", link.getId());
                }
                if (link.getName() != null) {
                    g.write("name", link.getName());
                }
                if (link.getTopic() != null) {
                    g.write("topic", link.getTopic());
                }
                if (link.getFileTimestamp() != 0) {
                    g.write("fileTimestamp", DateHelper.formatTimestamp(link.getFileTimestamp()));
                }
                if (link.getLearnTimestamp() != 0) {
                    g.write("learnTimestamp", DateHelper.formatTimestamp(link.getLearnTimestamp()));
                }
                g.writeEnd();
            }
            g.writeEnd();
        }
        if (data.getLinkId() != null) {
            g.write("linkId", data.getLinkId());
        }
        if (data.getIdPrefix() != null) {
            g.write("idPrefix", data.getIdPrefix());
        }
        if (data.getIdCount() != 0) {
            g.write("idCount", data.getIdCount());
        }
        if (data.getViewType() != null) {
            g.write("viewType", data.getViewType().name());
        }
        if (data.getScreenPos() != null) {
            g.writeKey("screenPos");
            g.writeStartObject();
            g.write("x", data.getScreenPos().getX());
            g.write("y", data.getScreenPos().getY());
            g.writeEnd();
        }
        if (data.getTopic() != null) {
            g.write("topic", data.getTopic());
        }
        if (data.getLinkTableSortOrder() != null) {
            g.write("linkTableSortOrder", data.getLinkTableSortOrder());
        }
        g.write("docFolder", data.getDocFolder().getPath());
        g.write("topicFromSettings", data.isTopicFromSettings());
        g.write("topicFromPath", data.isTopicFromPath());
        if (data.getTraceList() != null) {
            g.write("traceList", data.getTraceList());
        }
        g.writeEnd();
        g.close();
    }
}
