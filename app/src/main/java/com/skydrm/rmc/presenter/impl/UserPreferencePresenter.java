package com.skydrm.rmc.presenter.impl;

import android.os.AsyncTask;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.presenter.IUserPreferencePresenter;
import com.skydrm.rmc.ui.activity.profile.IUserPreferenceView;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.user.User;
import com.skydrm.sdk.rms.user.IRmUser;

/**
 * Created by hhu on 11/9/2017.
 */

public class UserPreferencePresenter implements IUserPreferencePresenter {
    private IUserPreferenceView preferenceView;

    public UserPreferencePresenter(IUserPreferenceView preferenceView) {
        this.preferenceView = preferenceView;
    }

    @Override
    public void updateUserPreference(final String watermark, final User.IExpiry expiry) {
        new UpdatePreferenceAsyncTask(preferenceView, watermark, expiry).
                executeOnExecutor(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
    }

    @Override
    public void retrieveUserPreference() {
        new RetrieveUserPreferenceAsyncTask(preferenceView).executeOnExecutor(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
    }

    private static class UpdatePreferenceAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private IUserPreferenceView preferenceView;
        private String watermark;
        private User.IExpiry expiry;
        private String userPreferenceResult;
        private boolean flag;
        private Exception exception;

        private UpdatePreferenceAsyncTask(IUserPreferenceView preferenceView, String watermark, User.IExpiry expiry) {
            this.preferenceView = preferenceView;
            this.watermark = watermark;
            this.expiry = expiry;
        }

        @Override
        protected void onPreExecute() {
            preferenceView.loading(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
                SkyDRMApp.getInstance().getSession().getRmsRestAPI().getUserService(rmUser).updateUserPreference(watermark, expiry,
                        new RestAPI.IRequestCallBack<String>() {
                            @Override
                            public void onSuccess(String result) {
                                SkyDRMApp.getInstance().getSession().savePreference(expiry, watermark);
                                flag = result != null;
                                userPreferenceResult = result;
                            }

                            @Override
                            public void onFailed(int statusCode, String errorMsg) {
                                flag = false;
                            }
                        });
            } catch (SessionInvalidException e) {
                this.exception = e;
                e.printStackTrace();
            } catch (InvalidRMClientException e) {
                this.exception = e;
                e.printStackTrace();
            } catch (RmsRestAPIException e) {
                this.exception = e;
                e.printStackTrace();
            } catch (Exception e) {
                this.exception = e;
            }
            return flag;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            preferenceView.loading(true);
            if (result) {
                preferenceView.onUpdatePreference(userPreferenceResult);
            } else {
                preferenceView.onUpdateFailed(exception);
            }
        }
    }

    private static class RetrieveUserPreferenceAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private IUserPreferenceView preferenceView;
        private String retrieveResult;
        private boolean flag;
        private Exception exception;

        private RetrieveUserPreferenceAsyncTask(IUserPreferenceView preferenceView) {
            this.preferenceView = preferenceView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (preferenceView != null) {
                preferenceView.loading(false);
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
                SkyDRMApp.getInstance().getSession().getRmsRestAPI().getUserService(rmUser).retrieveUserPreference(
                        new RestAPI.IRequestCallBack<String>() {
                            @Override
                            public void onSuccess(String result) {
                                SkyDRMApp.getInstance().getSession().savePreference(result);
                                flag = result != null;
                                retrieveResult = result;
                            }

                            @Override
                            public void onFailed(int statusCode, String errorMsg) {
                                flag = false;
                            }
                        });
            } catch (InvalidRMClientException e) {
                exception = e;
                e.printStackTrace();
            } catch (SessionInvalidException e) {
                exception = e;
                e.printStackTrace();
            } catch (RmsRestAPIException e) {
                exception = e;
                e.printStackTrace();
            }
            return flag;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (preferenceView != null) {
                preferenceView.loading(true);
                if (result) {
                    preferenceView.onRetrievePreference(retrieveResult);
                } else {
                    preferenceView.onUpdateFailed(exception);
                }
            }
        }
    }
}
