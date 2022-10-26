package com.skydrm.sdk.rms.rest;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.rest.user.User;

import org.json.JSONObject;

public interface IUserService {
    JSONObject getCaptcha() throws Exception;

    JSONObject sendCaptcha(String email, String nonce, String captcha) throws RmsRestAPIException;

    void changePassword(String oldPassword, String newPassword) throws RmsRestAPIException;

    /**
     * this method is used to retrieve user profile info
     *
     * @throws RmsRestAPIException
     * @return{Base64.encodeToString(byteFile, Base64.DEFAULT) }
     */
    JSONObject retrieveUserProfile() throws RmsRestAPIException;

    /**
     * @param byteFile turn file to byte[] in order to base64
     * @return response
     * @throws RmsRestAPIException
     */
    void updateUserProfile(byte[] byteFile, RestAPI.IRequestCallBack<String> callBack) throws RmsRestAPIException;

    /**
     * @return response
     * @throws RmsRestAPIException
     */
    void updateUserDisplayName(String displayName, RestAPI.IRequestCallBack<String> callBack) throws RmsRestAPIException;

    void updateUserPreference(String watermark, User.IExpiry expiry, RestAPI.IRequestCallBack<String> callBack) throws RmsRestAPIException;

    void retrieveUserPreference(RestAPI.IRequestCallBack<String> callBack) throws RmsRestAPIException;
}
