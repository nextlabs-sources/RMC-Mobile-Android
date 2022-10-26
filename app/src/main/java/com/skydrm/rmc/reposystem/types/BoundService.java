package com.skydrm.rmc.reposystem.types;

import android.database.Cursor;
import android.text.TextUtils;

import java.io.Serializable;


/**
 * ----------- account  accountId   accountToken
 * oneDrive    email       id         refershToken
 * dropbox     email       uid        oAuth2Token
 * google      email       email       email
 */
public class BoundService implements Serializable {
    public static final String DROPBOX = "Dropbox";
    public static final String ONEDRIVE = "OneDrive";
    public static final String SHAREPOINT = "SharePoint";
    public static final String SHAREPOINT_ONLINE = "SharePoint Online";
    public static final String GOOGLEDRIVE = "Google Drive";
    public static final String MYDRIVE = "MyDrive";
    public static final String BOX = "Box";
    public static final String SHAREPOINT_ONPREMISE = "SharePoint OnPremise";

    public ServiceType type;
    public String alias;
    public String account;
    public String accountID;
    public String accountToken;
    public String accountName = "stupid code"; // special for one drive, RMS can not get one drive account's mail stupid
    public int selected;
    // RMS side fields
    public String rmsRepoId;    // only can get this value after successfully adding repo into RMS
    public String rmsNickName;
    public boolean rmsIsShared = false;
    public String rmsToken;     //may be this token is same as accountToken, maybe not
    public String rmsPreference = "no use";
    public long rmsCreationTime = 0;
    public long rmsUpdatedTime = 0;

    public BoundService(ServiceType type, String alias, String account,
                        String accountID, String accountToken, int selected) {
        this.type = type;
        this.alias = alias;
        this.account = account;
        this.accountID = accountID;
        this.accountToken = accountToken;
        this.selected = selected;
    }

    public BoundService(ServiceType type, String alias,
                        String account, String accountID, String accountToken,
                        int selected, String rmsRepoId, String rmsNickName, boolean rmsIsShared,
                        String rmsToken, String rmsPreference,
                        long rmsCreationTime) {
        this.type = type;
        this.alias = alias;
        this.account = account;
        this.accountID = accountID;
        this.accountToken = accountToken;
        this.selected = selected;
        this.rmsRepoId = rmsRepoId;
        this.rmsNickName = rmsNickName;
        this.rmsIsShared = rmsIsShared;
        this.rmsToken = rmsToken;
        this.rmsPreference = rmsPreference;
        this.rmsCreationTime = rmsCreationTime;
    }

    public static boolean matchDropbox(BoundService s) {
        return s.type == ServiceType.DROPBOX;
    }

    public static boolean matchOnedrive(BoundService s) {
        return s.type == ServiceType.ONEDRIVE;
    }

    public static boolean matchSharepointOnline(BoundService s) {
        return s.type == ServiceType.SHAREPOINT_ONLINE;
    }

    public static boolean matchGoogleDrive(BoundService s) {
        return s.type == ServiceType.GOOGLEDRIVE;
    }

    public static boolean matchBox(BoundService s) {
        return s.type == ServiceType.BOX;
    }

    public static boolean matchSharePoint(BoundService s) {
        return s.type == ServiceType.SHAREPOINT;
    }

    public static BoundService Builder(Cursor c) {
        try {
            BoundService i = new BoundService(
                    BoundService.ServiceType.valueOf(c.getInt(c.getColumnIndex("service_type"))),
                    c.getString(c.getColumnIndex("service_alias")),
                    c.getString(c.getColumnIndex("service_account")),
                    c.getString(c.getColumnIndex("service_account_id")),
                    c.getString(c.getColumnIndex("service_account_token")),
                    c.getInt(c.getColumnIndex("selected")),
                    // rms added
                    c.getString(c.getColumnIndex("rms_repo_id")),
                    c.getString(c.getColumnIndex("rms_nick_name")),
                    c.getInt(c.getColumnIndex("rms_is_shared")) != 0,  // bug-prone point
                    c.getString(c.getColumnIndex("rms_token")),
                    c.getString(c.getColumnIndex("rms_is_preference")),
                    c.getLong(c.getColumnIndex("rms_creation_time")));
            return i;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append("Repo:[")
                .append(type + ":" + alias + " ")
                .append(account + " ")
                .append("select:" + selected + " ")
                .append("]");
        return sb.toString();

    }

    public String getDisplayName() {
        String rt = alias;
        if (rmsNickName != null && !rmsNickName.isEmpty()) {
            rt += " - " + rmsNickName;
        }
        return rt;
    }

    /**
     * if repo is synced from web, it Token is null ,or empty,
     */
    public boolean isValidRepo() {
        return accountToken != null && !accountToken.isEmpty() && accountToken.length() >= 5;
    }

    public boolean isSameOneInLocal(BoundService rh) {
        if (rh == null) {
            return false;
        }
        if (this.type != rh.type) {
            return false;
        }
        // by OneDrive, account may be userName or email, That because RMS only accept userName
        // so if OneDrive here, Ignored to compare account
        // also accountToken(accessToken) may also changed,
        if (!(this.type == ServiceType.ONEDRIVE)) {
            if (!TextUtils.equals(this.account, rh.account)) {
                return false;
            }
            // we do not need to check this, since token may be changed
//            if (!TextUtils.equals(this.accountToken, rh.accountToken)) {
//                return false;
//            }
        }
        if (!TextUtils.equals(this.accountID, rh.accountID)) {
            return false;
        }
        return true;
    }

    public boolean isSelected() {
        return 1 == selected;
    }

    public enum ServiceType {
        DROPBOX(0),
        SHAREPOINT_ONLINE(1),
        SHAREPOINT(2),
        ONEDRIVE(3),
        RECENT(4),
        GOOGLEDRIVE(5),
        MYDRIVE(6),
        BOX(7);

        private int value = 0;

        ServiceType(int type) {
            value = type;
        }

        public static ServiceType valueOf(int value) {
            switch (value) {
                case 0:
                    return DROPBOX;
                case 1:
                    return SHAREPOINT_ONLINE;
                case 2:
                    return SHAREPOINT;
                case 3:
                    return ONEDRIVE;
                case 4:
                    return RECENT;
                case 5:
                    return GOOGLEDRIVE;
                case 6:
                    return MYDRIVE;
                case 7:
                    return BOX;
                default:
                    throw new IllegalArgumentException("value" + value + " is not a legal value to convert to ServiceType");
            }
        }

        public int value() {
            return this.value;
        }

        public String toRMSType() {
            switch (this) {
                case DROPBOX:
                    return RmsRepoInfo.TYPE_DROPBOX;
                case ONEDRIVE:
                    return RmsRepoInfo.TYPE_ONEDRIVE;
                case MYDRIVE:
                    return RmsRepoInfo.TYPE_NEXTLABS_MYDRIVE;
                case GOOGLEDRIVE:
                    return RmsRepoInfo.TYPE_GOOGLEDRIVE;
                case BOX:
                    return RmsRepoInfo.TYPE_BOX;
                case SHAREPOINT:
                    return RmsRepoInfo.TYPE_SHAREPOINT;
                case SHAREPOINT_ONLINE:
                    return RmsRepoInfo.TYPE_SHAREPOINT_ONLINE;
                default:
                    throw new RuntimeException("SHOULD never reach here");
            }
        }
    }


}
