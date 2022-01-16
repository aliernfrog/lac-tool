package com.aliernfrog.LacMapTool.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.aliernfrog.LacMapTool.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
    public static String removeExtension(String path) {
        int extensionIndex = path.lastIndexOf(".");
        if (extensionIndex == -1) return path;

        return path.substring(0, extensionIndex);
    }

    public static void copyFile(String source, String destination) throws Exception {
        File src = new File(source);
        InputStream in;
        OutputStream out;
        if (src.isFile()) {
            in =  new FileInputStream(source);
            out = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        } else {
            File[] files = src.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyFile(file.getPath(), destination + "/" + file.getName());
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void copyFile(String source, DocumentFile destination, Context context) throws Exception {
        File src = new File(source);
        ContentResolver resolver = context.getContentResolver();
        InputStream in;
        OutputStream out;
        if (src.isFile()) {
            in = new FileInputStream(source);
            out = resolver.openOutputStream(destination.getUri());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        } else {
            File[] files = src.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyFile(file.getPath(), destination.createFile("", file.getName()), context);
                }
            }
        }
    }

    public static void copyFile(DocumentFile source, String destination, Context context) throws Exception {
        File dst = new File(destination);
        ContentResolver resolver = context.getContentResolver();
        InputStream in;
        OutputStream out;
        if (!source.isDirectory()) {
            in = resolver.openInputStream(source.getUri());
            out = new FileOutputStream(destination);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        } else {
            if (!dst.exists()) dst.mkdirs();
            DocumentFile[] files = source.listFiles();
            for (DocumentFile file : files) {
                copyFile(file, destination + "/" + file.getName(), context);
            }
        }
    }

    public static String readFile(String source) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(new File(source).getPath()));
        String _line;
        StringBuilder _full = new StringBuilder();
        while ((_line = reader.readLine()) != null) {
            if (_full.length() > 0) {
                _full.append("\n").append(_line);
            } else {
                _full.append(_line);
            }
        }
        reader.close();
        return _full.toString();
    }

    public static Intent shareFile(String source, String type, Context context) {
        File file = new File(source);
        Intent intent = null;
        if (file.exists()) {
            String packageName = context.getApplicationContext().getPackageName();
            Uri uri = FileProvider.getUriForFile(context, packageName+".provider", file);
            intent = new Intent(Intent.ACTION_SEND)
                    .setType(type)
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .putExtra(Intent.EXTRA_SUBJECT, R.string.info_sharing)
                    .putExtra(Intent.EXTRA_TEXT, R.string.info_sharing);
        }
        return intent;
    }

    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    delete(file);
                }
            }
        }
        directory.delete();
    }

    public static void deleteDirectoryContent(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                delete(file);
            }
        }
    }

    public static void delete(File file) {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            file.delete();
        }
    }
}
