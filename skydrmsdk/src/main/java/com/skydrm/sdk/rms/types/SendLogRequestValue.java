package com.skydrm.sdk.rms.types;

import java.io.Serializable;

/**
 * Created by aning on 12/20/2016.
 */

public class SendLogRequestValue implements Serializable {
    private String mDuid;
    private String mOwnerId;
    private long mUserId;
    private int mOperationId;
    private String mDeviceId;
    private int mDeviceType;
    private String mRepositoryId;
    private String mFilePathId;
    private String mFileName;
    private String mFilePath;
    private String mAppName;
    private String mAppPath;
    private String mAppPublisher;
    private int mAccessResult;
    private long mAccessTime;
    private String mActivityData;
    private int mAccountType;

    public SendLogRequestValue() {
        mDuid = null;
        mOwnerId = null;
        mUserId = -1;
        mOperationId = -1;
        mDeviceId = null;
        mDeviceType = -1;
        mRepositoryId = null;
        mFilePathId = null;
        mFileName = null;
        mFilePath = null;
        mAppName = null;
        mAppPath = null;
        mAppPublisher = null;
        mAccessResult = -1;
        mAccessTime = -1;
        mActivityData = null;
        mAccountType = -1;
    }

    /**
     * note: For backward compatibility, the field "AccountType" don’t specify any value, the default will be ‘0’ (PERSONAL).
     *  @param operationId: {@link OperationType}
     *  @param accessResult: {@link AccessResult}
     */
    public SendLogRequestValue(String duid, String ownerId, long userId, int operationId,
                               String deviceId, int deviceType, String repositoryId,
                               String filePathId,String fileName, String filePath,
                               String appName, String appPath, String appPublisher,
                               int accessResult, long accessTime, String activityData) {
        mDuid = duid;
        mOwnerId = ownerId;
        mUserId = userId;
        mOperationId = operationId;
        mDeviceId = deviceId;
        mDeviceType = deviceType;
        mRepositoryId = repositoryId;
        mFilePathId = filePathId;
        mFileName = fileName;
        mFilePath = filePath;
        mAppName = appName;
        mAppPath = appPath;
        mAppPublisher = appPublisher;
        mAccessResult = accessResult;
        mAccessTime = accessTime;
        mActivityData = activityData;
        mAccountType = AccountType.PERSONAL; // default
    }

    /**
     *  @param operationId: {@link OperationType}
     *  @param accessResult: {@link AccessResult}
     *  @param accountType: {@link AccountType}
     */
    public SendLogRequestValue(String duid, String ownerId, long userId, int operationId, String deviceId, int deviceType, String repositoryId, String filePathId,
                               String fileName, String filePath, String appName, String appPath, String appPublisher, int accessResult, long accessTime, String activityData, int accountType) {
        mDuid = duid;
        mOwnerId = ownerId;
        mUserId = userId;
        mOperationId = operationId;
        mDeviceId = deviceId;
        mDeviceType = deviceType;
        mRepositoryId = repositoryId;
        mFilePathId = filePathId;
        mFileName = fileName;
        mFilePath = filePath;
        mAppName = appName;
        mAppPath = appPath;
        mAppPublisher = appPublisher;
        mAccessResult = accessResult;
        mAccessTime = accessTime;
        mActivityData = activityData;
        mAccountType = accountType;
    }

    public void setmDuid(String duid) {
        mDuid = duid;
    }

    public void setmOwnerId(String ownerId) {
        mOwnerId = ownerId;
    }

    public void setmUserId(long userId) {
        mUserId = userId;
    }

    public void setmOperationId(int operationId) {
        mOperationId = operationId;
    }

    public void setmDeviceId(String deviceId) {
        mDeviceId = deviceId;
    }

    public void setmDeviceType(int deviceType){
        mDeviceType = deviceType;
    }

    public void setmRepositoryId(String repositoryId) {
        mRepositoryId = repositoryId;
    }

    public void setmFilePathId(String filePathId) {
        mFilePathId = filePathId;
    }

    public void setmFileName(String fileName) {
        mFileName = fileName;
    }

    public void setmFilePath(String filePath){
        mFilePath = filePath;
    }

    public void setmAppName(String appName){
        mAppName = appName;
    }

    public void setmAppPath(String appPath){
        mAppPath = appPath;
    }

    public void setmAppPublisher(String appPublisher){
        mAppPublisher = appPublisher;
    }

    public void setmAccessResult(int accessResult){
        mAccessResult = accessResult;
    }

    public void setmAccessTime(long accessTime){
        mAccessTime = accessTime;
    }

    public void setmActivityData(String activityData){
        mActivityData = activityData;
    }

    public void setmAccountType(int mAccountType) {
        this.mAccountType = mAccountType;
    }

    public String getmDuid() {
        return mDuid;
    }

    public String getmOwnerId() {
        return mOwnerId;
    }

    public long getmUserId() {
        return mUserId;
    }

    public int getmOperationId() {
        return mOperationId;
    }

    public String getmDeviceId() {
        return mDeviceId;
    }

    public int getmDeviceType(){
        return mDeviceType;
    }

    public String getmRepositoryId() {
        return mRepositoryId;
    }

    public String getmFilePathId() {
        return mFilePathId;
    }

    public String getmFileName() {
        return mFileName;
    }

    public String getmFilePath(){
        return mFilePath;
    }

    public String getmAppName(){
        return mAppName;
    }

    public String getmAppPath(){
        return mAppPath;
    }

    public String getmAppPublisher(){
        return mAppPublisher;
    }

    public int getmAccessResult(){
        return mAccessResult;
    }

    public long getmAccessTime(){
        return mAccessTime;
    }

    public String getmActivityData(){
        return mActivityData;
    }

    public int getmAccountType() {
        return mAccountType;
    }

    // operation type
    public static class OperationType {
        public static final int PROTECT = 1;
        public static final int SHARE = 2;
        public static final int REMOVE_USER = 3;
        public static final int VIEW = 4;
        public static final int PRINT = 5;
        public static final int DOWNLOAD = 6;
        public static final int EDIT = 7;
        public static final int REVOKE = 8;
        public static final int DECRYPT = 9;
        public static final int COPY = 10;
        public static final int CAPTURE_SCREEN = 11;
        public static final int CLASSIFY = 12;
        public static final int RE_SHARE = 13;
        public static final int DELETE = 14;
    }

    // newly added field, this is used to differentiate the activity logs for documents in personal space and documents in projects.
    // For backward compatibility, if you don’t specify any value, the default will be ‘0’ (PERSONAL).
    // Note: When you protect a local file using a project membership, you will need to log with a value of ‘1’
    public static class AccountType {
        public static final int PERSONAL = 0;
        public static final int PROJECT = 1;
    }

    // access result: denied or allowed.
    public static class AccessResult {
        public static final int Denied = 0;
        public static final int Allowed = 1;
    }
}
