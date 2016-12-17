package com.pitchedapps.butler.library.icon.request;

import android.graphics.Bitmap;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by Allan Wang on 2016-08-20.
 */
class FileUtil {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static int wipe(File file) {
        if (!file.exists()) return 0;
        int count = 0;
        if (file.isDirectory()) {
            File[] folderContent = file.listFiles();
            if (folderContent != null && folderContent.length > 0) {
                for (File fileInFolder : folderContent) {
                    count += wipe(fileInFolder);
                }
            }
        }
        file.delete();
        return count;
    }

    public static void writeIcon(File file, Bitmap icon) throws Exception {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            icon.compress(Bitmap.CompressFormat.PNG, 100, os);
        } finally {
            FileUtil.closeQuietely(os);
        }
    }

    public static void writeAll(File file, String content) throws Exception {
        writeAll(file, content.getBytes("UTF-8"));
    }

    public static void writeAll(File file, byte[] content) throws Exception {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(content);
            os.flush();
        } finally {
            closeQuietely(os);
        }
    }

    public static void closeQuietely(Closeable c) {
        try {
            c.close();
        } catch (Throwable ignored) {
        }
    }

    private FileUtil() {
    }
}