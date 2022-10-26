package com.skydrm.rmc.utils;

import android.text.TextUtils;
import android.util.Log;

import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.utils.sort.IBaseSortable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * handle file property
 * such file size, modify time etc.
 */
public class FileUtils {
    public static String getLetter(String name) {
        // sanity check
        if (name == null || name.isEmpty()) {
            return "";
        }
        String nameLetter = name.trim().substring(0, 1).toUpperCase();
        if (nameLetter.matches("[A-Z]")) {
            return nameLetter;
        } else {
            return "#";
        }
    }

    public static boolean isSpecificLetter(String name) {
        String nameLetter = name.trim().substring(0, 1).toUpperCase();
        return !nameLetter.matches("[A-Z]");
    }

    public static String transparentFileSize(long fileSize) {
        long sizeInteger = fileSize / 1024;
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        if (1024 > sizeInteger && 0 < sizeInteger) {
            return Double.parseDouble(decimalFormat.format((double) fileSize / 1024)) + " KB";
        } else if (1024 < sizeInteger) {
            return Double.parseDouble(decimalFormat.format((double) fileSize / (1024 * 1024))) + " MB";
        }
        return fileSize + " B";
    }

    // Convert byte to MB
    public static double convertByteToMb(long fileSize) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        return Double.parseDouble(decimalFormat.format((double) fileSize / (1024 * 1024)));
    }

    @Deprecated
    public static String convertTime(INxFile file, boolean isBottomItem) {
        try {
            if (isBottomItem) {
                DateFormat sdBottom = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                sdBottom.setTimeZone(TimeZone.getDefault());
                return sdBottom.format(new Date(file.getLastModifiedTimeLong()));
            } else {
                DateFormat sdTitle = new SimpleDateFormat("MMMM yyyy");
                sdTitle.setTimeZone(TimeZone.getDefault());
                return sdTitle.format(new Date(file.getLastModifiedTimeLong()));
            }
        } catch (Exception e) {
            Log.e("ConvertTime", e.toString());
        }
        return "";
    }

    public static String convertTime(IBaseSortable sortable, boolean isBottomItem) {
        try {
            if (isBottomItem) {
                DateFormat sdBottom = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
                sdBottom.setTimeZone(TimeZone.getDefault());
                return sdBottom.format(new Date(sortable.getSortableTime()));
            } else {
                DateFormat sdTitle = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                sdTitle.setTimeZone(TimeZone.getDefault());
                return sdTitle.format(new Date(sortable.getSortableTime()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * translate INxFile format data to NXFileItem format
     * NXFileItem title is the first letter of name by default
     *
     * @param nxFiles the meta data
     * @return NXFileItem format List
     */
    public static List<NXFileItem> translateINxList(List<INxFile> nxFiles) {
        List<NXFileItem> nxFileItems = new ArrayList<>();
        if (nxFiles == null) {
            return nxFileItems;
        }
        //java.util.ConcurrentModificationException
        List<INxFile> temp = new ArrayList<>(nxFiles);
        //Attempt to invoke interface method 'java.lang.String com.skydrm.rmc.reposystem.types.INxFile.getName()' on a null object reference
        for (INxFile item : temp) {
            if (item != null) {
                nxFileItems.add(new NXFileItem(item, getLetter(item.getName())));
            }
        }
        return nxFileItems;
    }

    public static String getParent(String pathId) {
        if (TextUtils.isEmpty(pathId)) {
            return "/";
        }
        if (pathId.equals("/")) {
            return "/";
        }
        if (pathId.endsWith("/")) {//folder /a/
            String one = pathId.substring(0, pathId.lastIndexOf("/"));
            return one.substring(0, one.lastIndexOf("/") + 1);
        } else { //file /a/b.txt
            return pathId.substring(0, pathId.lastIndexOf("/") + 1);
        }
    }

    public static long getSize(File root) {
        long ret = 0;
        if (root == null) {
            return ret;
        }
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    ret += getSize(f);
                } else {
                    ret += f.length();
                }
            }
        } else {
            ret += root.length();
        }
        return ret;
    }

    public static void deleteRecursively(File root) {
        if (root == null) {
            return;
        }
        if (root.isFile()) {
            root.delete();
            return;
        }
        File[] files = root.listFiles();
        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            } else {
                deleteRecursively(f);
            }
        }
    }

    public static boolean deleteFile(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            return false;
        }
        return file.delete();
    }

    public static void copy(InputStream inputStream, File output) throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(output);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }
    }
}
