package com.skydrm.rmc.reposystem.types;

import android.text.TextUtils;

import com.skydrm.sdk.rms.types.RmsUserLinkedRepos;

import java.util.ArrayList;
import java.util.List;


public class RmsRepoInfo {

    public static final String TYPE_DROPBOX = "DROPBOX";
    public static final String TYPE_ONEDRIVE = "ONE_DRIVE";
    public static final String TYPE_GOOGLEDRIVE = "GOOGLE_DRIVE";
    public static final String TYPE_SHAREPOINT_ONLINE = "SHAREPOINT_ONLINE";
    public static final String TYPE_NEXTLABS_MYDRIVE = "S3";
    public static final String TYPE_BOX = "BOX";
    public static final String TYPE_SHAREPOINT = "SHAREPOINT_ONPREMISE";

    public String rmsRepoId = "";
    public String rmsName = "";
    public String rmsType = "";
    public boolean rmsIsShared = false;
    public String rmsAccountName = "";
    public String rmsAccountId = "";
    public String rmsToken = "";
    public String rmsPreference = "";
    public long rmsCreationTime = 0;
    //a new boolean value ‘isDefault’ in every repoItem, so for MyDrive this value will be true and for other repositories this value will be false.
    private boolean rmsIsDefault;

    public static boolean matchMyDrive(RmsRepoInfo r) {
//        return TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_NEXTLABS_MYDRIVE);
        return r.rmsIsDefault;
    }

    public static boolean matchDropBox(RmsRepoInfo r) {
        return TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_DROPBOX);
    }

    public static boolean matchOneDrive(RmsRepoInfo r) {
        return TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_ONEDRIVE);
    }

    public static boolean matchSharepointOnline(RmsRepoInfo r) {
        return TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_SHAREPOINT_ONLINE);
    }

    public static boolean matchGoogleDrive(RmsRepoInfo r) {
        return TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_GOOGLEDRIVE);
    }

    public static boolean matchBox(RmsRepoInfo r) {
        return TextUtils.equals(r.rmsType, RmsRepoInfo.TYPE_BOX);
    }

    public static boolean matchSharePoint(RmsRepoInfo info) {
        return TextUtils.equals(info.rmsType, TYPE_SHAREPOINT);
    }

    public static List<RmsRepoInfo> fromResultBean(RmsUserLinkedRepos.ResultsBean bean) {
        List<RmsRepoInfo> ls = new ArrayList<>(bean.getRepoItems().size());
        for (RmsUserLinkedRepos.ResultsBean.RepoItemsBean r : bean.getRepoItems()) {
            RmsRepoInfo item = new RmsRepoInfo();
            item.rmsRepoId = r.getRepoId();
            item.rmsName = r.getName();
            item.rmsType = r.getType();
            item.rmsIsShared = r.isIsShared();
            item.rmsAccountName = r.getAccountName();
            item.rmsAccountId = r.getAccountId();
            item.rmsToken = r.getToken();
            item.rmsPreference = r.getPreference();
            item.rmsCreationTime = r.getCreationTime();
            item.rmsIsDefault = r.isDefault();
            ls.add(item);
        }
        return ls;
    }

}
