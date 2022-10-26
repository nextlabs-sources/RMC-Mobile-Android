package com.skydrm.rmc.ui.interacor;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.UserProfileInfo;
import com.skydrm.rmc.engine.eventBusMsg.account.UpdateUserAvatarEvent;
import com.skydrm.rmc.engine.eventBusMsg.account.UpdateUserNameEvent;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.user.IRmUser;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;


public class UserProfileHandler {
    public static final String TAG = "UserProfileHandler";
    private static volatile UserProfileHandler mInstance;

    private UserProfileHandler() {

    }

    public static UserProfileHandler getDefault() {
        if (mInstance == null) {
            synchronized (UserProfileHandler.class) {
                if (mInstance == null) {
                    mInstance = new UserProfileHandler();
                }
            }
        }
        return mInstance;
    }

    public void retrieveUserProfileInfo(final Context context, final IRetrieveCallback callback) {
        class UserProfileInfoAsyncTask extends AsyncTask<Void, Void, Boolean> {
            private boolean flag = false;
            private UserProfileInfo info;
            private boolean avartarFlag = false;
            private RmsRestAPIException mRmsRestAPIException;

            private UserProfileInfoAsyncTask() {
                if (info == null) {
                    info = new UserProfileInfo();
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
                    if (rmUser == null) {
                        return false;
                    }
                    JSONObject results = SkyDRMApp.getInstance()
                            .getSession()
                            .getRmsRestAPI()
                            .getUserService(rmUser)
                            .retrieveUserProfile();
                    flag = !TextUtils.isEmpty(results.toString());
                    JSONObject extra = results.getJSONObject("extra");
                    info.setUserId(extra.getString("userId"));
                    info.setTicket(extra.getString("ticket"));
                    info.setTtl(extra.getLong("ttl"));
                    info.setName(extra.getString("name"));
                    info.setEmail(extra.getString("email"));
                    //
                    // by Osmond, as RMS changed, we should ignore profile_picture field
                    // but we need to create a bitmap
                    flag =true;
                    avartarFlag=true;
                    //
//                    if (!extra.has("preferences")) {
//                        info.setUserAvatar(info.getUserAvatar());
//                        avartarFlag = false;
//                        flag = true;
//                    } else {
//                        try {
//                            JSONObject preferences = extra.getJSONObject("preferences");
//                            String profilePicInfo = preferences.getString("profile_picture");
//                            String[] splitProfileInfo = profilePicInfo.split(",");
//                            final String base64Avatar = splitProfileInfo[1];
//                            if (!TextUtils.isEmpty(base64Avatar)) {
//                                byte[] avatarBytes = Base64.decode(base64Avatar, Base64.DEFAULT);
//                                Bitmap avatarBitmap = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.length);
//                                AvatarUtil.getInstance().cacheAvatar(avatarBitmap);
//                                info.setUserAvatar(avatarBitmap);
//                                flag = true;
//                                avartarFlag = true;
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            flag = true;
//                            avartarFlag = false;
//                        }
//                    }
                } catch (RmsRestAPIException e) {
                    flag = false;
                    mRmsRestAPIException = e;
                } catch (Exception e) {
                    flag = false;
                    e.printStackTrace();
                }
                return flag;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    if (callback != null) {
                        callback.onRetrieveSuccess(info);
                    }
                    // notify name
                    if (!TextUtils.isEmpty(info.getName())) {
                        SkyDRMApp.getInstance().getSession().updateRmUserName(info.getName().trim());
                        // send event onto EventBus
                        EventBus.getDefault().post(new UpdateUserNameEvent());
                    }
                    if (avartarFlag) {
                        EventBus.getDefault().post(new UpdateUserAvatarEvent());
                    }
                } else {
                    GenericError.handleCommonException(context, true, mRmsRestAPIException);
                    if (callback != null) {
                        callback.onRetrieveFailed();
                    }
                }
            }
        }
        new UserProfileInfoAsyncTask().executeOnExecutor(ExecutorPools.SelectSmartly(FIRED_BY_UI));
    }


    public interface IRetrieveCallback {
        void onRetrieveSuccess(UserProfileInfo info);

        void onRetrieveFailed();
    }
}
