package com.skydrm.sdk.rms.rest;

import android.support.annotation.NonNull;

import com.skydrm.sdk.exception.RmsRestAPIException;

import org.json.JSONObject;

public interface IPolicyEvaluationService {
    String performPolicyEvaluation(@NonNull JSONObject parametersObj) throws RmsRestAPIException;
}
