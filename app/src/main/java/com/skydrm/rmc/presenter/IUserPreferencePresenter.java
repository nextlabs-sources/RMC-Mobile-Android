package com.skydrm.rmc.presenter;

import com.skydrm.sdk.rms.rest.user.User;

/**
 * Created by hhu on 11/9/2017.
 */

public interface IUserPreferencePresenter {
    void updateUserPreference(final String watermark, final User.IExpiry expiry);

    void retrieveUserPreference();
}
