package com.skydrm.rmc.ui.myspace.myvault.model.domain;

import com.skydrm.rmc.utils.sort.ILogSortable;

import java.io.Serializable;

/**
 * Created by hhu on 1/23/2017.
 */

public class VaultFileLogImpl implements Serializable, Cloneable, IVaultFileLog, ILogSortable {
    private static final long serialVersionUID = 7067435812395081728L;
    //----------------------------myvault file log info-------------------------
    private String email;
    private String operation;
    private String deviceType;
    private String deviceId;
    private long accessTime;
    private String accessResult;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }

    public void setAccessResult(String accessResult) {
        this.accessResult = accessResult;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getOperation() {
        return operation;
    }

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public long getAccessTime() {
        return accessTime;
    }

    @Override
    public String getAccessResult() {
        return accessResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VaultFileLogImpl that = (VaultFileLogImpl) o;

        return accessTime == that.accessTime
                && email.equals(that.email)
                && operation.equals(that.operation)
                && deviceType.equals(that.deviceType)
                && deviceId.equals(that.deviceId)
                && accessResult.equals(that.accessResult);

    }

    @Override
    public int hashCode() {
        int result = email.hashCode();
        result = 31 * result + operation.hashCode();
        result = 31 * result + deviceType.hashCode();
        result = 31 * result + deviceId.hashCode();
        result = 31 * result + (int) (accessTime ^ (accessTime >>> 32));
        result = 31 * result + accessResult.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "VaultFileLogImpl{" +
                "email='" + email + '\'' +
                ", operation='" + operation + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", accessTime=" + accessTime +
                ", accessResult='" + accessResult + '\'' +
                '}';
    }

    @Override
    public String getSortableOperation() {
        return operation;
    }

    @Override
    public String getSortableResult() {
        return accessResult;
    }

    @Override
    public String getSortableName() {
        return email;
    }

    @Override
    public long getSortableSize() {
        return -1;
    }

    @Override
    public long getSortableTime() {
        return accessTime;
    }

    @Override
    public boolean isFolder() {
        return false;
    }
}
