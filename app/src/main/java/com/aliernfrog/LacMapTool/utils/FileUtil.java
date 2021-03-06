package com.aliernfrog.LacMapTool.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.renderscript.ScriptGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void copyFile(Uri source, Uri destination, Context context) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        InputStream in = resolver.openInputStream(source);
        OutputStream out = resolver.openOutputStream(destination);
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

    public static String getFileNameFromPath(String path) {
        String[] arr = path.split("/");
        return arr[arr.length-1];
    }

    public static String getParentPathFromPath(String path) {
        String[] arr = path.split("/");
        String full = "";
        for (int i = 0; i < arr.length; i++) {
            if (i != arr.length-1) {
                if (i != arr.length-2) {
                    full += arr[i]+"/";
                } else {
                    full += arr[i];
                }
            }
        }
        return full;
    }
}
