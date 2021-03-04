package com.aliernfrog.LacMapTool.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
    public static void copyFile(String source, String destination) throws Exception {
        InputStream in = new FileInputStream(new File(source));
        OutputStream out = new FileOutputStream(new File(destination));
        try {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    public static String readFile(String source) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(new File(source).getPath()));
        String _line;
        String _full = "";
        while ((_line = reader.readLine()) != null) {
            if (_full.length() > 0) {
                _full += "\n"+_line;
            } else {
                _full += _line;
            }
        }
        reader.close();
        return _full;
    }
}
