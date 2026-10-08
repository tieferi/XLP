// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import de.t11r.cards.Card.Side;
import de.t11r.cards.JsonHelper.Parser;

import javax.json.stream.JsonGenerator;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public class ProjectDataJson {

    private static int VERSION = 1;

    public static ProjectData read(File file) throws Exception {
        ProjectData data = new ProjectData();
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
            if (p.key("sheetNameSet")) {
                if (p.startArray()) {
                    SortedSet<String> sheetNameSet = new TreeSet<>();
                    while (!p.endArray()) {
                        sheetNameSet.add(p.stringValue());
                    }
                    data.setSheetNameSet(sheetNameSet);
                } else {
                    p.fail();
                }
            }
            if (p.key("cardTypeName")) {
                if (p.value()) {
                    data.setCardTypeName(p.stringValue());
                } else {
                    p.fail();
                }
            }
            if (p.key("learningProfileNameMap")) {
                SortedMap<String, String> learningProfileNameMap = new TreeMap<>();
                if (p.startArray()) {
                    while (!p.endArray()) {
                        if (!p.startObject()) {
                            p.fail();
                        }
                        if (!p.key("cardTypeName") || !p.value()) {
                            p.fail();
                        }
                        String cardTypeName = p.stringValue();
                        if (!p.key("learningProfileName") || !p.value()) {
                            p.fail();
                        }
                        String learningProfileName = p.stringValue();
                        if (!p.endObject()) {
                            p.fail();
                        }
                        learningProfileNameMap.put(cardTypeName, learningProfileName);
                    }
                } else {
                    p.fail();
                }
                data.setLearningProfileNameMap(learningProfileNameMap);
            }
            if (p.key("cardKey")) {
                if (p.value()) {
                    data.setCardKey(p.stringValue());
                } else {
                    p.fail();
                }
            }
            if (p.key("cardSide")) {
                if (p.value()) {
                    data.setCardSide(Side.valueOf(p.stringValue()));
                } else {
                    p.fail();
                }
            }
            if (p.key("statsMap")) {
                SortedMap<String, String> statsMap = new TreeMap<>();
                if (p.startArray()) {
                    while (!p.endArray()) {
                        if (!p.startObject()) {
                            p.fail();
                        }
                        if (!p.key("key") || !p.value()) {
                            p.fail();
                        }
                        String key = p.stringValue();
                        if (!p.key("value") || !p.value()) {
                            p.fail();
                        }
                        String value = p.stringValue();
                        if (!p.endObject()) {
                            p.fail();
                        }
                        statsMap.put(key, value);
                    }
                } else {
                    p.fail();
                }
                data.setStatsMap(statsMap);
            }
            if (!p.endObject()) {
                p.fail();
            }

        } finally {
            p.close();
        }
        return data;
    }

    public static void write(ProjectData data, File file) throws Exception {
        JsonGenerator g = JsonHelper.generator(file);
        g.writeStartObject();
        g.write("version", VERSION);
        if (data.getSheetNameSet() != null) {
            g.writeStartArray("sheetNameSet");
            for (String sheetName : data.getSheetNameSet()) {
                g.write(sheetName);
            }
            g.writeEnd();
        }
        if (data.getCardTypeName() != null) {
            g.write("cardTypeName", data.getCardTypeName());
        }
        if (data.getStatsMap() != null) {
            g.writeStartArray("learningProfileNameMap");
            for (Entry<String, String> entry : data.getLearningProfileNameMap().entrySet()) {
                g.writeStartObject();
                g.write("cardTypeName", entry.getKey());
                g.write("learningProfileName", entry.getValue());
                g.writeEnd();
            }
            g.writeEnd();
        }
        if (data.getCardKey() != null) {
            g.write("cardKey", data.getCardKey());
        }
        if (data.getCardSide() != null) {
            g.write("cardSide", data.getCardSide().name());
        }
        if (data.getStatsMap() != null) {
            g.writeStartArray("statsMap");
            for (Entry<String, String> entry : data.getStatsMap().entrySet()) {
                g.writeStartObject();
                g.write("key", entry.getKey());
                g.write("value", entry.getValue());
                g.writeEnd();
            }
            g.writeEnd();
        }
        g.writeEnd();
        g.close();
    }
}
