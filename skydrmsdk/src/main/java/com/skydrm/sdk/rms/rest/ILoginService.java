package com.skydrm.sdk.rms.rest;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.user.IRmUser;

public interface ILoginService {
    String getLoginURLbyTenant(String tenant) throws RmsRestAPIException;

    IRmUser basicLogin(String user, String password) throws RmsRestAPIException;
}
