package com.skydrm.rmc.ui.activity.profile;

/**
 * Created by hhu on 11/9/2017.
 */

public interface IUserPreferenceView {
    void loading(boolean done);

    void onUpdatePreference(String result);

    void onRetrievePreference(String result);

    void onUpdateFailed(Exception e);

}
