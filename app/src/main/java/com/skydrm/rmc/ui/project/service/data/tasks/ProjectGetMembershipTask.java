package com.skydrm.rmc.ui.project.service.data.tasks;

import android.text.TextUtils;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.project.service.data.Result;
import com.skydrm.rmc.ui.myspace.myvault.ICommand;
import com.skydrm.rmc.ui.myspace.myvault.data.Error;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.user.membership.ProjectMemberShip;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hhu on 5/3/2018.
 */

public class ProjectGetMembershipTask extends LoadTask<Void, Boolean> {
    private int mProjectId;
    private ICommand.ICommandExecuteCallback<Result.GetMembershipResult, Error> mCallback;
    private RmsRestAPIException mRmsRestAPIException;

    public ProjectGetMembershipTask(int projectId,
                                    ICommand.ICommandExecuteCallback<Result.GetMembershipResult, Error> callback) {
        this.mProjectId = projectId;
        this.mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
            String response = session.getRmsRestAPI()
                    .getProjectService(session.getRmUser())
                    .getProjectMembershipId(mProjectId);
            return storeIntoMembershipCaches(response, session);
        } catch (RmsRestAPIException e) {
            mRmsRestAPIException = e;
        } catch (SessionInvalidException e) {
            mRmsRestAPIException = new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.AuthenticationFailed);
        } catch (InvalidRMClientException e) {
            mRmsRestAPIException = new RmsRestAPIException(e.getMessage(),
                    RmsRestAPIException.ExceptionDomain.Common);
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result) {
            if (mCallback != null) {
                mCallback.onInvoked(new Result.GetMembershipResult());
            }
        } else {
            if (mRmsRestAPIException != null) {
                if (mCallback != null) {
                    mCallback.onFailed(new Error(mRmsRestAPIException,
                            mRmsRestAPIException.getMessage()));
                }
            }
        }
    }

    private boolean storeIntoMembershipCaches(String response, SkyDRMApp.Session2 session) {
        if (TextUtils.isEmpty(response)) {
            return false;
        }
        try {
            JSONObject responseObj = new JSONObject(response);
            if (responseObj.has("results")) {
                JSONObject resultsObj = responseObj.getJSONObject("results");
                if (resultsObj.has("membership")) {
                    JSONObject membershipObj = resultsObj.getJSONObject("membership");
                    String id = membershipObj.getString("id");
                    int type = membershipObj.getInt("type");
                    String tokenGroupName = membershipObj.getString("tokenGroupName");
                    int projectId = membershipObj.getInt("projectId");

                    session.getRmUser().updateOrInsertMembershipItem(new ProjectMemberShip(id, type,
                            tokenGroupName, projectId));
                    return true;
                }
                return false;
            }
            return false;
        } catch (JSONException | InvalidRMClientException e) {
            e.printStackTrace();
        }
        return false;
    }
}
