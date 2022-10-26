package com.skydrm.sdk.policy;

import android.text.TextUtils;

import com.skydrm.sdk.INxlExpiry;
import com.skydrm.sdk.INxlObligations;
import com.skydrm.sdk.INxlRights;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.skydrm.sdk.INxlRights.RIGHT_DECRYPT;
import static com.skydrm.sdk.INxlRights.RIGHT_DOWNLOAD;
import static com.skydrm.sdk.INxlRights.RIGHT_EDIT;
import static com.skydrm.sdk.INxlRights.RIGHT_PRINT;
import static com.skydrm.sdk.INxlRights.RIGHT_SHARE;
import static com.skydrm.sdk.INxlRights.RIGHT_VIEW;


public final class AdhocPolicy {
    private static final String POLICY_NAME = "Ad-hoc";

    private String issuer;
    private Rights rights;
    private Obligations obligations;
    private Expiry expiry;

    public AdhocPolicy(String issuer, Rights rights, Obligations obligations, Expiry expiry) {
        this.issuer = issuer;
        this.rights = rights;
        this.obligations = obligations;
        if (expiry == null) {
            this.expiry = new Expiry.Builder().never().build();
        } else {
            this.expiry = expiry;
        }
    }

    public String getIssuer() {
        return issuer;
    }

    public INxlRights getRights() {
        return rights;
    }

    public INxlObligations getObligations() {
        return obligations;
    }

    public INxlExpiry getExpiry() {
        return expiry;
    }

    public static AdhocPolicy parseAdhocPolicyFromJSON(String rawData) {
        String issuer = null;
        Rights rights = new Rights();
        Obligations obligations = new Obligations();
        Expiry expiry = new Expiry.Builder().never().build();//  by default as Never expired
        try {
            JSONObject adhocPolicy = new JSONObject(rawData);
            // extract issuer
            if (adhocPolicy.has("issuer") && !adhocPolicy.isNull("issuer")) {
                issuer = adhocPolicy.getString("issuer");
            }
            // extact policies
            if (adhocPolicy.has("policies") && !adhocPolicy.isNull("policies")) {
                JSONArray policies = adhocPolicy.getJSONArray("policies");
                for (int i = 0; i < policies.length(); ++i) {
                    JSONObject policy = (JSONObject) policies.get(i);
                    // for rights
                    JSONArray rightsSection = policy.getJSONArray("rights");
                    for (int j = 0; j < rightsSection.length(); ++j) {
                        String right = (String) rightsSection.get(j);
                        if (RIGHT_VIEW.equals(right)) {
                            rights.setView(true);
                        } else if (RIGHT_EDIT.equals(right)) {
                            rights.setEdit(true);
                        } else if (RIGHT_PRINT.equals(right)) {
                            rights.setPrint(true);
                        } else if (RIGHT_SHARE.equals(right)) {
                            rights.setShare(true);
                        } else if (RIGHT_DOWNLOAD.equals(right)) {
                            rights.setDownload(true);
                        } else if (RIGHT_DECRYPT.equals(right)) {
                            rights.setDecrypt(true);
                        }
                        // TBD, whether to extratct watermark?
                    }
                    // for obligations
                    JSONArray obligationsSection = policy.getJSONArray("obligations");
                    for (int z = 0; z < obligationsSection.length(); ++z) {
                        JSONObject obligation = (JSONObject) obligationsSection.get(z);
                        String ob = obligation.getString("name");
                        Map<String, String> map = new HashMap<>();
                        if ("WATERMARK".equals(ob)) {
                            if (obligation.has("value")) { // watermark value
                                JSONObject jo = obligation.getJSONObject("value");
                                String text = jo.getString("text");
                                map.put("WATERMARK", text);
                            }

                            obligations.setObligation(map);
                            // update
                            //  if watermark exists at Obligation, set it at rights obj
                            rights.setWatermark(true);  // used to compatible with older.
                        }
                    }
                    // for expiry
                    if (policy.has("conditions")) {
                        JSONObject con = policy.getJSONObject("conditions");
                        if (con.has("environment")) {
                            try {
                                JSONObject env = con.getJSONObject("environment");
                                {
                                    int type = env.getInt("type");
                                    if (type == 1) {  // for PropertyExpression
                                        if (env.isNull("name")) {
                                            throw new JSONException("can find name in conditons.environment");
                                        }
                                        String name = env.getString("name");
                                        // hint for that this is for expiry
                                        if (TextUtils.equals(name.toLowerCase(), "environment.date")) {
                                            long val = env.getLong("value");
                                            expiry = new Expiry.Builder().absolute().setEndDate(val).build();
                                        }
                                    } else if (type == 0) { // for LogicExpression
                                        // special for Range type;
                                        // assert operator is &&
                                        String op = env.getString("operator");
                                        // assert expressions  exist
                                        JSONArray exprs = env.getJSONArray("expressions");
                                        if (!TextUtils.equals(op, "&&") || exprs == null || exprs.length() != 2) {
                                            throw new JSONException("parse expire failed");
                                        }
                                        JSONObject e1 = exprs.getJSONObject(0);
                                        JSONObject e2 = exprs.getJSONObject(1);
                                        long v1 = e1.getLong("value");
                                        long v2 = e2.getLong("value");
                                        if (v1 >= v2) {
                                            long v3 = v2;
                                            v2 = v1;
                                            v1 = v3;
                                        }
                                        expiry = new Expiry.Builder().range().setStartDate(v1).setEndDate(v2).build();
                                    } else {
                                        // stupid
                                    }
                                }
                            } catch (JSONException e) {
                            }
                        }
                    }
                }
                return new AdhocPolicy(issuer, rights, obligations, expiry);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new AdhocPolicy(issuer, null, null, null);
    }

    public String generateJSON() {
        // build a Adhoc JSON String and follows https://bitbucket.org/nxtlbs-devops/rightsmanagement-wiki/wiki/RMD/policy.format.md
        String result = null;
        try {
            // - Adhoc common, something lie:
            //      "version": "1.0",
            //      "issuer": "m39@skydrm.com",
            //      "issueTime": "2017-11-02T07:57:58Z",
            JSONObject adHocPolicyObj = new JSONObject();
            adHocPolicyObj.put("version", "1.0"); // how get
            adHocPolicyObj.put("issuer", issuer);
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = sDateFormat.format(new java.util.Date());
            SimpleDateFormat sTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String time = sTimeFormat.format(new java.util.Date());
            adHocPolicyObj.put("issueTime", date + "T" + time + "Z");

            // policy array
            JSONArray policiesJsonArray = new JSONArray();
            // one policy
            JSONObject policyJson = new JSONObject();
            policyJson.put("id", 0);
            policyJson.put("name", POLICY_NAME);
            policyJson.put("action", 1); // Grant
            // rights
            JSONArray rightsJsonArray = new JSONArray();
            INxlRights rights = getRights();

            if (rights.hasView()) {
                rightsJsonArray.put(RIGHT_VIEW);
            }
            if (rights.hasEdit()) {
                rightsJsonArray.put(RIGHT_EDIT);
            }
            if (rights.hasPrint()) {
                rightsJsonArray.put(RIGHT_PRINT);
            }
            if (rights.hasShare()) {
                rightsJsonArray.put(RIGHT_SHARE);
            }
            if (rights.hasDownload()) {
                rightsJsonArray.put(RIGHT_DOWNLOAD);
            }
            if (rights.hasDecrypt()) {
                rightsJsonArray.put(RIGHT_DECRYPT);
            }
            // TBD:  log: whether to input keyword Watermark into rights-secion

            policyJson.put("rights", rightsJsonArray);
            // conditions
            JSONObject conditionJson = new JSONObject();
            {   // conditons.subject, (const values each time, maybe god knows what's it used for?)
                JSONObject subjectJson = new JSONObject();
                subjectJson.put("type", 1);
                subjectJson.put("operator", "=");
                subjectJson.put("name", "application.is_associated_app");
                subjectJson.put("value", true);
                conditionJson.put("subject", subjectJson);
            }
            {   // conditions.environment
                if (expiry != null) {
                    JSONObject env = expiry.toAdHocExpiry();
                    if (env != null) {
                        conditionJson.put("environment", env);
                    }
                }
            }
            policyJson.put("conditions", conditionJson);

            // obligations
            JSONArray obligationsJsonArray = new JSONArray();
            if (obligations.getObligation() != null && obligations.getObligation().size() > 0) {

                Set set = obligations.getObligation().keySet();
                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    if (obligations.getObligation().get(key) != null) {
                        JSONObject obligationJsonOb = new JSONObject();
                        obligationJsonOb.put("name", key); // key: watermark

                        JSONObject joValue = new JSONObject();
                        joValue.put("text", obligations.getObligation().get(key)); // put watermark value
                        obligationJsonOb.put("value", joValue);

                        obligationsJsonArray.put(obligationJsonOb);
                    }
                }
            }
            policyJson.put("obligations", obligationsJsonArray);

            policiesJsonArray.put(policyJson);
            adHocPolicyObj.put("policies", policiesJsonArray);

            result = adHocPolicyObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AdhocPolicy:[");
        if (issuer != null) {
            sb.append(issuer + " ");
        }
        if (rights != null) {
            sb.append(rights.toString() + " ");
        }
        if (obligations != null) {
            sb.append(obligations.toString() + " ");
        }
        if (expiry != null) {
            sb.append(expiry.toString() + " ");
        }
        sb.append("]");
        return sb.toString();
    }
}
