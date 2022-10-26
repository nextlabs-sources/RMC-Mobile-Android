package com.skydrm.rmc.presenter.impl;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.presenter.IUserDataPresenter;
import com.skydrm.rmc.ui.widget.avatar.AvatarView;
import com.skydrm.rmc.utils.commonUtils.AvatarUtil;
import com.skydrm.sdk.rms.user.IRmUser;

/**
 * Created by hhu on 5/8/2017.
 */

/**
 * This class used to set the global user info(user name and user avatar.)
 */
public class UserDataPresenterImpl implements IUserDataPresenter {
    private static DevLog log = new DevLog(UserDataPresenterImpl.class.getSimpleName());
    private Context mContext;

    public UserDataPresenterImpl(Context context) {
        this.mContext = context;
    }

    @Override
    public void setDisplayName(TextView nameComponent) {
        try {
            if (null == nameComponent) {
                log.e("error:namePresenter is null.");
                return;
            }
            IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
            if (null == rmUser) {
                log.e("error:rmUser is null.");
                return;
            }
            nameComponent.setText(rmUser.getName());
        } catch (InvalidRMClientException e) {
            log.e(e);
        }
    }

    @Override
    public void setDisplayEmail(TextView emailComponent) {
        try {
            if (null == emailComponent) {
                log.e("error:emailComponent is null.");
                return;
            }
            IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
            if (null == rmUser) {
                log.e("error:rmUser is null");
                return;
            }
            emailComponent.setText(rmUser.getEmail());
        } catch (InvalidRMClientException e) {
            log.e(e);
        }
    }

    @Override
    public void setUserAvatar(AvatarView avatarComponent) {
        AvatarUtil.getInstance().setUserAvatar((Activity) mContext, avatarComponent);
    }

    @Override
    public void setSession(TextView sessionComponent) {
        if (null == sessionComponent) {
            log.e("error:sessionComponent is null");
            return;
        }
        sessionComponent.setText(SkyDRMApp.getInstance().getSession().getExpiredTimeFriendly());
    }
}
