package com.skydrm.rmc.ui.project.feature.centralpolicy;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.user.IRmUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by hhu on 4/12/2018.
 */

public class PolicyEvaluation {
    private static DevLog log = new DevLog(PolicyEvaluation.class.getSimpleName());

    public static void evaluate(EvaluationBean evaluationBean, IEvaluationCallback callback) {
        new EvaluationTask(evaluationBean.mMembershipId, evaluationBean.mUserId, evaluationBean.mEvalType,
                evaluationBean.mRights, evaluationBean.mResourceName,
                evaluationBean.mDuid, evaluationBean.mTags, callback).executeOnExecutor(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
    }

    public static String evaluate(INxlFileFingerPrint fp, String fileName, int rights, int evalType)
            throws SessionInvalidException, InvalidRMClientException, RmsRestAPIException {
        EvaluationBean evalBean = buildEvalBean(fp, fileName, rights, evalType);
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        String membershipId = evalBean.mMembershipId;
        String dimensionName = "from";
        String resourceType = "fso";
        String resourceName = evalBean.mResourceName;
        String duid = evalBean.mDuid;
        Map<String, Set<String>> tags = evalBean.mTags;
        int evalRights = evalBean.mRights;
        int userId = evalBean.mUserId;
        int evalTypeVal = evalBean.mEvalType;
        return session.getRmsRestAPI()
                .getPolicyEvaluationService(session.getRmUser())
                .performPolicyEvaluation(generateParameterObj(membershipId, dimensionName,
                        resourceType, resourceName, duid, tags, evalRights, userId, evalTypeVal));
    }

    public static void parseResult(String result, int rightsDetected, IParseEvalResultCallback callback) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject responseObj = new JSONObject(result);
                if (responseObj.has("results")) {
                    JSONObject resultsObj = responseObj.getJSONObject("results");
                    int rights = resultsObj.getInt("rights");
                    //View rights represent 1.
                    if (rights == rightsDetected) {
                        JSONArray adhocObligations = null;
                        if (resultsObj.has("adhocObligations")
                                && !resultsObj.isNull("adhocObligations")) {
                            adhocObligations = resultsObj.getJSONArray("adhocObligations");
                        }
                        JSONArray obligations = null;
                        if (resultsObj.has("obligations")
                                && !resultsObj.isNull("obligations")) {
                            obligations = resultsObj.getJSONArray("obligations");
                        }
                        callback.onAccessAllow(adhocObligations, obligations);
                    } else {
                        //means user does not have view rights.
                        // send deny view log
                        callback.onAccessDenied();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static EvaluationBean buildEvalBean(INxlFileFingerPrint fileFingerPrint, String fileName, int rights, int evalType) {
        String duid = fileFingerPrint.getDUID();
        Map<String, Set<String>> tags = fileFingerPrint.getAll();
        String ownerID = fileFingerPrint.getOwnerID();
        int userId = 0;
        try {
            IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
            userId = rmUser.getUserId();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        return new PolicyEvaluation.EvaluationBean(ownerID, userId,
                evalType, rights, fileName, duid, tags);
    }

    public static EvaluationBean buildEvalBean(String membershipId, int userId,
                                               int evalType, int rights,
                                               String fileName, String duid,
                                               Map<String, Set<String>> tags) {
        return new PolicyEvaluation.EvaluationBean(membershipId, userId,
                evalType, rights, fileName, duid, tags);
    }

    public static class EvaluationBean {
        public String mMembershipId;
        public int mUserId;
        public int mEvalType;
        public int mRights;
        public String mResourceName;
        public String mDuid;
        public Map<String, Set<String>> mTags;

        EvaluationBean(String membershipId, int userId, int evalType,
                       int rights, String resourceName,
                       String duid, Map<String, Set<String>> tags) {
            this.mMembershipId = membershipId;
            this.mUserId = userId;
            this.mEvalType = evalType;
            this.mRights = rights;
            this.mResourceName = resourceName;
            this.mDuid = duid;
            this.mTags = tags == null ? new HashMap<String, Set<String>>() : tags;
        }
    }

    public static JSONObject generateParameterObj(@NonNull String membershipId, @NonNull String dimensionName,
                                                  @NonNull String resourceType, @NonNull String resourceName,
                                                  @NonNull String duid, @NonNull Map<String, Set<String>> tags,
                                                  int rights, int uerId, int evalType) {
        JSONObject paramsObj = new JSONObject();
        JSONObject evalRequestObj = new JSONObject();
        try {
            evalRequestObj.put("membershipId", membershipId);
            JSONArray resourceJArray = new JSONArray();
            JSONObject resourcesContentObj = new JSONObject();
            resourcesContentObj.put("dimensionName", dimensionName);
            resourcesContentObj.put("resourceType", resourceType);
            resourcesContentObj.put("resourceName", resourceName);
            resourcesContentObj.put("duid", duid);
            JSONObject classificationObj = new JSONObject();
            Set<String> keys = tags.keySet();
            for (String key : keys) {
                JSONArray valueJArray = new JSONArray();
                Set<String> values = tags.get(key);
                if (values != null) {
                    for (String value : values) {
                        valueJArray.put(value);
                    }
                }
                classificationObj.put(key, valueJArray);
            }
            resourcesContentObj.put("classification", classificationObj);
            resourceJArray.put(resourcesContentObj);
            evalRequestObj.put("resources", resourceJArray);
            evalRequestObj.put("rights", rights);
            JSONObject userObj = new JSONObject();
            userObj.put("id", uerId);
            evalRequestObj.put("user", userObj);
            evalRequestObj.put("evalType", evalType);
            paramsObj.put("evalRequest", evalRequestObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return paramsObj;
    }

    static class EvaluationTask extends AsyncTask<Void, Void, String> {
        private String mMembershipId;
        private int mUserId;
        private int mEvalType;
        private int mRights;
        private static final String mDimensionName = "from";
        private static final String mResourceType = "fso";
        private String mResourceName;
        private String mDuid;
        private Map<String, Set<String>> mTags;
        private IEvaluationCallback mCallback;
        private Exception mException;

        EvaluationTask(String membershipId, int userId, int evalType,
                       int rights, String resourceName,
                       String duid, Map<String, Set<String>> tags, IEvaluationCallback callback) {
            this.mMembershipId = membershipId;
            this.mUserId = userId;
            this.mEvalType = evalType;
            this.mRights = rights;
            this.mResourceName = resourceName;
            this.mDuid = duid;
            this.mTags = tags;
            this.mCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                return session.getRmsRestAPI()
                        .getPolicyEvaluationService(session.getRmUser())
                        .performPolicyEvaluation(generateParameterObj(mMembershipId, mDimensionName,
                                mResourceType, mResourceName, mDuid, mTags, mRights, mUserId, mEvalType));
            } catch (SessionInvalidException | InvalidRMClientException | RmsRestAPIException e) {
                e.printStackTrace();
                mException = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (mCallback != null) {
                if (result != null) {
                    mCallback.onEvaluated(result);
                } else {
                    mCallback.onFailed(mException != null ? mException : new Exception("Unknown error."));
                }
            }
        }
    }

    public interface IEvaluationCallback {
        void onEvaluated(String result);

        void onFailed(Exception e);
    }

    public interface IParseEvalResultCallback {
        void onAccessAllow(@Nullable JSONArray adhocObligations, @Nullable JSONArray obligations);

        void onAccessDenied();
    }
}
