package com.skydrm.rmc.ui.activity.profile;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.Render.RenderHelper;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by hhu on 12/16/2016.
 */

public class PhotoPicker {
    private static final boolean DEBUG = SkyDRMApp.getInstance().isDebug();
    public static final String TAG = "PhotoPicker";
    public static Set<String> mtempCopyedFiles = new HashSet<>();

    public static String getPath(Context context, Uri uri) {
        if (DEBUG) {
            Log.e(TAG, "getPath: " + uri);
        }
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        //Document provider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            //ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return getDataColumn(context, contentUri, null,
                        null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
                //MediaStore (and general)
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                } else {
                    Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

                    if (cursor != null) {
                        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        cursor.moveToFirst();
                        String filename = cursor.getString(nameIndex);
                        cursor.close();
                        if (!TextUtils.isEmpty(filename)) {
                            File copyFile = RenderHelper.copyData(context, uri, filename,
                                    Constant.TMP_PIC_COPY_PATH);
                            if (copyFile != null) {
                                mtempCopyedFiles.add(copyFile.getAbsolutePath());
                                return copyFile.getPath();
                            }
                        }
                    }
                }
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } else {
            if (DEBUG) {
                Log.e(TAG, "getPath:selectImage ");
            }
            return selectImage(context, uri);
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static String selectImage(Context context, Uri selectedImage) {
        if (selectedImage != null) {
            if (DEBUG) {
                Log.e(TAG, selectedImage.toString());
            }
            String uriStr = selectedImage.toString();
            String path = uriStr.substring(10, uriStr.length());
            if (path.startsWith("com.sec.android.gallery3d")) {
                if (DEBUG) {
                    Log.e(TAG, "It's auto backup pic path:" + selectedImage.toString());
                }
                return null;
            }
            String[] filePathColumn = {MediaStore.Images.Media.DISPLAY_NAME};
            Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null,
                    null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String fileName = cursor.getString(columnIndex);
                cursor.close();
                File copyFile = RenderHelper.copyData(context, selectedImage, fileName,
                        Constant.TMP_PIC_COPY_PATH);
                if (copyFile != null) {
                    mtempCopyedFiles.add(copyFile.getAbsolutePath());
                    return copyFile.getPath();
                }
            } else {
                File copyFile = RenderHelper.copyData(context, selectedImage, Constant.TMP_NAME,
                        Constant.TMP_PIC_COPY_PATH);
                if (copyFile != null) {
                    mtempCopyedFiles.add(copyFile.getAbsolutePath());
                    return copyFile.getPath();
                }
            }
        }
        return "";
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
