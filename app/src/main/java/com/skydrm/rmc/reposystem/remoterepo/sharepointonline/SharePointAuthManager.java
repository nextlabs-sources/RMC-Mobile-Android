package com.skydrm.rmc.reposystem.remoterepo.sharepointonline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.reposystem.types.BoundService;

public class SharePointAuthManager {
    private static Account mAccount;
    private static boolean mAuthStatus;

    public static void reAuth(Activity activity, BoundService service) {
        Intent intent = new Intent();
        intent.setClass(activity, SharePointAuthActivity.class);
        intent.setAction(Constant.RE_AUTHENTICATION);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.BOUND_SERVICE, service);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    public static void startAuth(String domain, String username, String password, IAuthCallback callback) {
        AuthTask task = new AuthTask(callback);
        task.setServerUrl(domain);
        task.setUsername(username);
        task.setPassword(password);
        task.run();
    }

    public static void setAccount(Account account) {
        mAccount = account;
    }

    public static Account getAccount() {
        return mAccount;
    }

    public static boolean isAuthorized() {
        return mAuthStatus;
    }

    public static void setAuthStatus(boolean authStatus) {
        mAuthStatus = authStatus;
    }

    public static void resetAuthStatus() {
        mAuthStatus = false;
    }

    public static class Account {
        public String domain;
        public String username;
        public String password;
        public String nickName;

        public Account(String domain, String username, String password, String nickName) {
            this.domain = domain;
            this.username = username;
            this.password = password;
            this.nickName = nickName;
        }
    }

    public interface IAuthCallback {
        void onSuccess();

        void onFailed(String msg);
    }

}
