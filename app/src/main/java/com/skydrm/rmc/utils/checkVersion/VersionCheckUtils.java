package com.skydrm.rmc.utils.checkVersion;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.sdk.utils.CheckVersion;

/**
 * Created by aning on 8/23/2017.
 */

public class VersionCheckUtils {
    // google play app url site
    private static final String VERSION_URL = "https://play.google.com/store/apps/details?id=";

    /**
     *  Used to get current latest version
     *  @param packageName  the app package name
     *  @param callback  complete callback, means succeed when result is not empty.
     */
    public static void GetLatestVersion(final String packageName, final IGetVersionComplete callback) {

        class Task extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... params) {
               return CheckVersion.getLatestVersion(VERSION_URL + packageName);
            }

            @Override
            protected void onPostExecute(String result) {
                callback.onGetVersionFinish(result);
            }
        }
        new Task().executeOnExecutor(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.NETWORK_TASK));
    }

    /**
     * Judge current version if have updated
     * @param oldVersion user current using version
     * @param newVersion getting the new version from google play.
     */
    public static boolean isNeedUpdate(@NonNull final String oldVersion, @NonNull final String newVersion) {
        // compare
        if (!oldVersion.contains(".") && !newVersion.contains(".")) {
            return Integer.valueOf(oldVersion) < Integer.valueOf(newVersion);
        }

        if (!oldVersion.contains(".") && newVersion.contains(".")) {
            String[] newVersionArray = newVersion.split("\\.");
            if (Integer.valueOf(oldVersion) < Integer.valueOf(newVersionArray[0])) {
                return true;
            } else if (Integer.valueOf(oldVersion).equals(Integer.valueOf(newVersionArray[0]))){
                if (Integer.valueOf(newVersionArray[1]) > 0) {
                    return true;
                }
            } else {
                return false;
            }
        }

        if (oldVersion.contains(".") && !newVersion.contains(".")) {
            String[] oldVersionArray = oldVersion.split("\\.");
            return Integer.valueOf(oldVersionArray[0]) < Integer.valueOf(newVersion);
        }

        if (oldVersion.contains(".") && newVersion.contains(".")) {
            String[] oldVersionArray = oldVersion.split("\\.");
            String[] newVersionArray = newVersion.split("\\.");
            // Normally, the array length should be same
            for (int i = 0; i<oldVersionArray.length && i< newVersionArray.length; i++) {
                if (Integer.valueOf(oldVersionArray[i]) < Integer.valueOf(newVersionArray[i])) {
                    return true;
                } else if (Integer.valueOf(oldVersionArray[i]) > Integer.valueOf(newVersionArray[i])) {
                    return false;
                }
            }

            // If the length is not same, since previous values are all the same when execute here,
            // so for the case that oldVersion.length() > newVersion.length() will return false obviously.
            if (oldVersion.length() < newVersion.length()) {
                return true;
            }

        }

        return false;
    }

    /**
     * Used to update version dialog
     * @param context  context
     * @param currentVersion  current version
     * @param newVersion  found have new version
     */
    public static void updateDialog(final Context context, final String currentVersion, final String newVersion) {
        String message = "Current version: " + currentVersion + ", and find new version: " + newVersion + ", do you update now?";
        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.app_name))
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(VERSION_URL + context.getPackageName()));
                        context.startActivity(intent);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                    }
                }).create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public interface IGetVersionComplete {
        void onGetVersionFinish(String latestVersion);
    }

}
