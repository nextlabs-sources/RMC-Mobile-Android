package com.skydrm.hoops;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ViewerUtils {

    static {
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("hps_core");
        System.loadLibrary("hps_sprk");
        System.loadLibrary("hps_sprk_ops");
        System.loadLibrary("A3DLIBS");
        System.loadLibrary("hps_sprk_exchange");
        System.loadLibrary("hoopsfacade");
    }

    public final static String FONT_DIRECTORY_PATH = "file:///android_asset/fonts";
    public final static String MATERIAL_DIRECTORY_PATH = "file:///android_asset/materials";

    private static boolean USING_EXCHANGE = true;
//    private static boolean mNativeLibsLoaded = false;
//
//    public static void LoadNativeLibs() {
//        if (!mNativeLibsLoaded) {
//            System.loadLibrary("gnustl_shared");
//            System.loadLibrary("hps_core");
//            System.loadLibrary("hps_sprk");
//            System.loadLibrary("hps_sprk_ops");
//            USING_EXCHANGE = true;
//            try {
//                System.loadLibrary("A3DLIBS");
//                System.loadLibrary("hps_sprk_exchange");
//            } catch (UnsatisfiedLinkError e) {
//                USING_EXCHANGE = false;
//            }
//            System.loadLibrary("hoopsfacade");
//            mNativeLibsLoaded = true;
//        }
//    }

    public static boolean canOpenFile(File file) {
        if (file.isDirectory())
            return false;

        String ext = ViewerUtils.fileExtension(file);
        boolean isNormalFormat = ext.compareToIgnoreCase("hsf") == 0 ||
                ext.compareToIgnoreCase("stl") == 0 ||
                ext.compareToIgnoreCase("obj") == 0;

        if (ViewerUtils.USING_EXCHANGE)
            return isNormalFormat ||
                    ext.compareToIgnoreCase("pdf") == 0 ||
                    ext.compareToIgnoreCase("prc") == 0 ||
                    ext.compareToIgnoreCase("u3d") == 0 ||
                    ext.compareToIgnoreCase("step") == 0 ||
                    ext.compareToIgnoreCase("jt") == 0 ||
                    ext.compareToIgnoreCase("iges") == 0 ||
                    ext.compareToIgnoreCase("ifc") == 0 ||
                    ext.compareToIgnoreCase("ifczip") == 0 ||
                    ext.compareToIgnoreCase("x_b") == 0 ||
                    ext.compareToIgnoreCase("x_t") == 0 ||
                    ext.compareToIgnoreCase("x_mt") == 0 ||
                    ext.compareToIgnoreCase("xmt_txt") == 0;
        else
            return isNormalFormat;
    }

    public static String fileExtension(File file) {
        String[] parts = file.getName().split("\\.");
        return parts[parts.length - 1];
    }

    /**
     * Copies file from asset manager to external storage directory
     *
     * @param assetManager      AssetManager containing resources
     * @param fileName          Relative path to file in asset manager
     * @param targetDirName     External storage directory name
     * @param overwriteExisting Pass true to overwrite existing file
     */
    public static void copyAssetFile(AssetManager assetManager, String fileName, String targetDirName, boolean overwriteExisting) {
        File file = new File(targetDirName, fileName);
        if (!overwriteExisting && file.exists())
            return;

        try {
            InputStream istream = assetManager.open(fileName);
            OutputStream ostream = new BufferedOutputStream(new FileOutputStream(file));

            byte[] buffer = new byte[1024];
            int len = istream.read(buffer);
            while (len != -1) {
                ostream.write(buffer, 0, len);
                len = istream.read(buffer);
            }
            ostream.close();
            istream.close();
        } catch (FileNotFoundException e) {
            Log.e("ViewerUtils", "File not found in asset manager: " + fileName, e);
        } catch (IOException e) {
            Log.e("ViewerUtils", "IOException", e);
        }
    }

    /**
     * Copies file or directory from asset manager to external storage directory.
     *
     * @param assetManager      AssetManager containing resources
     * @param path              Relative path to file or directory in asset manager
     * @param targetDirName     External storage directory name
     * @param overwriteExisting Pass true to overwrite existing file
     */
    public static void copyAsset(AssetManager assetManager, String path, String targetDirName, boolean overwriteExisting) {
        try {
            // If path is a file, assets will be null or 0 length.
            String[] assets = assetManager.list(path);

            if (assets == null || assets.length == 0) {
                copyAssetFile(assetManager, path, targetDirName, overwriteExisting);
            } else {
                File targetDir = new File(targetDirName, path);
                targetDir.mkdirs();

                for (String subDirName : assets) {
                    copyAsset(assetManager, path + "/" + subDirName, targetDirName, overwriteExisting);
                }
            }

        } catch (IOException e) {
            Log.e("ViewerUtils", "IOException", e);
        }
    }
}
