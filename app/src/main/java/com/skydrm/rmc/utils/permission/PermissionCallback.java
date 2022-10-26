package com.skydrm.rmc.utils.permission;

import android.support.v4.app.ActivityCompat;

import java.util.List;

/**
 * Created by hhu on 8/9/2017.
 */

public interface PermissionCallback extends ActivityCompat.OnRequestPermissionsResultCallback {

    void onPermissionGranted(int requestCode, List<String> permissions);

    void onPermissionDenied(int requestCode, List<String> permissions);

    void onPermissionAllGranted();
}
