package com.pitchedapps.butler.library.icon.request;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

/**
 * Created by Allan Wang on 2016-08-20.
 */
class ZipUtil {

    public static void zip(File zipFile, File... files) throws Exception {
        ZipOutputStream out = null;
        InputStream is = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(zipFile));
            int i = 0;
            for (File fi : files) {
                out.putNextEntry(new ZipEntry(fi.getName()));
                /*
                try {
                    out.putNextEntry(new ZipEntry(fi.getName()));
                } catch (ZipException e) {
                    i += 1;
                    String fiName = fi.getName();
                    out.putNextEntry(new ZipEntry(fiName.substring(0, fiName.lastIndexOf('.')) + String.valueOf(i) +
                            fiName.substring(fiName.lastIndexOf('.', fiName.length()))));
                }
                */
                is = new FileInputStream(fi);

                int read;
                byte[] buffer = new byte[2048];
                while ((read = is.read(buffer)) != -1)
                    out.write(buffer, 0, read);

                FileUtil.closeQuietely(is);
                out.closeEntry();
            }
        } finally {
            FileUtil.closeQuietely(is);
            FileUtil.closeQuietely(out);
        }
    }

    private ZipUtil() {
    }
}