package com.skydrm.rmc.ui.project.feature.service.share.core;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.project.feature.centralpolicy.PolicyEvaluation;
import com.skydrm.rmc.ui.project.feature.service.core.MarkerStatus;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.core.RightsCheckHandler;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.INxlRights;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.user.IRmUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AddToProjectCheckHandler extends RightsCheckHandler {

    @Override
    protected void handleExactly(INxlFileFingerPrint fPrint, String name) throws MarkException {
        INxlFileFingerPrint fp = paramCheck(fPrint);
        // 1. Add to project file must be Central policy file.
        if (fp.hasRights() && !fp.hasTags()) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_WRONG_POLICY_TYPE, "This file is encrypted using user-defined rights," +
                    "and cannot be added to a project.");
        }
        // 2. File mush have share rights.
        evaluate(fp, name);
    }

    private void evaluate(INxlFileFingerPrint fPrint, String name) throws MarkException {
        INxlFileFingerPrint fp = paramCheck(fPrint);
        String resourceName = paramCheck(name);
        //"You are not authorized to perform this action."
        //If has tags in nxl file header.Extract it send it to rms to evaluate.
        final int rights = INxlRights.VIEW + INxlRights.EDIT + INxlRights.PRINT
                + INxlRights.SHARE + INxlRights.DOWNLOAD
                + INxlRights.WATERMARK + INxlRights.DECRYPT;

        try {
            IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
            int evalType = 0;//defined by rms api.
            int mUserId = rmUser.getUserId();
            String membershipId = fp.getOwnerID();
            String dimensionName = "from";
            String resourceType = "fso";
            String duid = fp.getDUID();
            Map<String, Set<String>> tags = fp.getAll();
            SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
            String responseStr = session.getRmsRestAPI()
                    .getPolicyEvaluationService(session.getRmUser())
                    .performPolicyEvaluation(PolicyEvaluation.generateParameterObj(membershipId, dimensionName,
                            resourceType, resourceName, duid,
                            tags == null ? new HashMap<String, Set<String>>() : tags,
                            rights, mUserId, evalType));

            if (responseStr == null || responseStr.isEmpty()) {
                throw new MarkException(MarkerStatus.STATUS_FAILED_COMMON, "No response string get from server.");
            }
            JSONObject responseObj = new JSONObject(responseStr);
            JSONObject resultsObj = responseObj.optJSONObject("results");

            int permissions = resultsObj.optInt("rights");
            if (!containExtractRights(permissions)) {
                throw new MarkException(MarkerStatus.STATUS_FAILED_UNAUTHORIZED,
                        "You are not authorized to perform this action.");
            }
        } catch (InvalidRMClientException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_INVALID_RMC_CLIENT,
                    e.getMessage(), e);
        } catch (RmsRestAPIException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_RMS_REST_API_EXCEPTION,
                    e.getMessage(), e);
        } catch (SessionInvalidException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_SESSION_INVALID,
                    e.getMessage(), e);
        } catch (JSONException e) {
            throw new MarkException(MarkerStatus.STATUS_FAILED_COMMON,
                    e.getMessage(), e);
        }
    }

    private boolean containExtractRights(int permissions) {
        return (permissions & INxlRights.DECRYPT) == INxlRights.DECRYPT;
    }
}
