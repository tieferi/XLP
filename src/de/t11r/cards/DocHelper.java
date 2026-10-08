// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DocHelper {

    public static void open(String key) throws Exception {
        String name = TextHelper.get(key);
        File file = new File(OSHelper.appFolder() + name);
        File folder = file.getParentFile();
        if (!folder.exists() && !folder.mkdirs()) {
            throw new Exception();
        }
        copyResource(name, file);
        Desktop.getDesktop().open(file);
    }

    private static void copyResource(String name, File file) throws Exception {
        InputStream in = DocHelper.class.getResourceAsStream(name);
        try {
            OutputStream out = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    int n = in.read(buffer);
                    if (n < 0) {
                        break;
                    }
                    out.write(buffer, 0, n);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
