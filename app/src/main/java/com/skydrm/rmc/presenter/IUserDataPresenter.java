package com.skydrm.rmc.presenter;

import android.graphics.Bitmap;
import android.widget.TextView;

import com.skydrm.rmc.ui.widget.avatar.AvatarView;

/**
 * Created by hhu on 5/8/2017.
 */

public interface IUserDataPresenter {
    void setDisplayName(TextView nameComponent);

    void setDisplayEmail(TextView emailComponent);

    void setUserAvatar(AvatarView avatarComponent);

    void setSession(TextView sessionComponent);
}
