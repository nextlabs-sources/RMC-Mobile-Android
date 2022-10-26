package com.skydrm.rmc.ui.service;

import android.text.TextUtils;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.project.feature.centralpolicy.PolicyEvaluation;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.ui.project.feature.service.share.IShare;
import com.skydrm.sdk.INxlRights;
import com.skydrm.sdk.rms.user.membership.IMemberShip;
import com.skydrm.sdk.rms.user.membership.SystemBucketMemberShip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Helper {

    public static boolean isFromSystemBucket(String membershipId) {
        if (membershipId == null || membershipId.isEmpty()) {
            return false;
        }
        try {
            List<IMemberShip> memberships = SkyDRMApp.getInstance()
                    .getSession()
                    .getRmUser()
                    .getMemberships();
            if (memberships == null || memberships.isEmpty()) {
                return false;
            }
            for (IMemberShip m : memberships) {
                if (m instanceof SystemBucketMemberShip) {
                    if (TextUtils.equals(m.getId(), membershipId)) {
                        return true;
                    }
                }
            }
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void doPolicyEvaluation(String membershipId, String name, String duid,
                                          Map<String, Set<String>> tags,
                                          final IShare.IPolicyCallback callback) {
        int userId = -1;
        try {
            userId = SkyDRMApp.getInstance().getSession().getRmUser().getUserId();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }

        if (userId == -1) {
            return;
        }

        int evalType = 0;//defined by rms api.
        int rights = INxlRights.VIEW + INxlRights.EDIT + INxlRights.PRINT
                + INxlRights.SHARE + INxlRights.DOWNLOAD
                + INxlRights.WATERMARK + INxlRights.DECRYPT;

        PolicyEvaluation.evaluate(PolicyEvaluation.buildEvalBean(membershipId, userId,
                evalType, rights, name, duid, tags),
                new PolicyEvaluation.IEvaluationCallback() {
                    @Override
                    public void onEvaluated(String result) {
                        if (TextUtils.isEmpty(result)) {
                            return;
                        }
                        try {
                            JSONObject responseObj = new JSONObject(result);
                            if (responseObj.has("results")) {
                                JSONObject resultsObj = responseObj.optJSONObject("results");
                                if (resultsObj != null) {
                                    int rights = resultsObj.optInt("rights");
                                    JSONArray obligations = resultsObj.optJSONArray("obligations");
                                    List<String> rightsArray = NxlDoc.integer2Rights(rights);
                                    if (obligations != null && obligations.length() != 0) {
                                        if (rightsArray != null) {
                                            rightsArray.add("WATERMARK");
                                        }
                                    }
                                    if (callback != null) {
                                        callback.onSuccess(rightsArray, obligations == null ?
                                                "" : obligations.toString());
                                    }
                                } else {
                                    if (callback != null) {
                                        callback.onFailed(new MarkException(MarkerStatus.STATUS_FAILED_COMMON,
                                                "Unknown result."));
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            if (callback != null) {
                                callback.onFailed(new MarkException(MarkerStatus.STATUS_FAILED_COMMON,
                                        e.getMessage(), e));
                            }
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        if (callback != null) {
                            callback.onFailed(new MarkException(MarkerStatus.STATUS_FAILED_COMMON,
                                    e.getMessage(), e));
                        }
                    }
                });
    }
}
