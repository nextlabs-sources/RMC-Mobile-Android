package com.skydrm.rmc.ui.myspace.myvault.model.domain;

/**
 * Created by hhu on 1/23/2017.
 */

public interface IVaultFileLog {
    //------------------myvault file log info-------------
    String getEmail();

    String getOperation();

    String getDeviceType();

    String getDeviceId();

    long getAccessTime();

    String getAccessResult();
}
