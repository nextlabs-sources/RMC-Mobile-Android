package com.skydrm.rmc.ui.myspace.myvault.data;

import com.skydrm.rmc.ui.myspace.myvault.ICommand;
import com.skydrm.sdk.exception.RmsRestAPIException;

/**
 * Created by hhu on 4/28/2018.
 */

public class Error implements ICommand.IError {
    public String msg;
    public RmsRestAPIException mException;

    public Error(RmsRestAPIException mException, String msg) {
        this.mException = mException;
        this.msg = msg;
    }

    public Error(RmsRestAPIException mException) {
        this.mException = mException;
    }

    public Error(String msg) {
        this.msg = msg;
    }
}
