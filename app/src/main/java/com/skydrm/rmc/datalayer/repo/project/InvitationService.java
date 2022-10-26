package com.skydrm.rmc.datalayer.repo.project;

import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;

import org.json.JSONException;
import org.json.JSONObject;

public class InvitationService implements Parcelable {

    InvitationService() {

    }

    private InvitationService(Parcel in) {
    }

    public static final Creator<InvitationService> CREATOR = new Creator<InvitationService>() {
        @Override
        public InvitationService createFromParcel(Parcel in) {
            return new InvitationService(in);
        }

        @Override
        public InvitationService[] newArray(int size) {
            return new InvitationService[size];
        }
    };

    boolean accept(int invitationId, String invitationCode)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        String response = session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .acceptInvitation(invitationId, invitationCode);
        try {
            JSONObject responseObj = new JSONObject(response);
            return responseObj.optInt("statusCode") == 200;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean deny(int invitationId, String invitationCode, String reason)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        return session.getRmsRestAPI()
                .getProjectService(session.getRmUser())
                .denyInvitation(invitationId, invitationCode, reason);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
