package com.skydrm.rmc.ui.myspace.myvault.model.domain;

/**
 * Created by hhu on 11/21/2017.
 */

public class RightsBean {
    private int rights_iconID;
    private String rights_text;

    public RightsBean(int rights_iconID, String rights_text) {
        this.rights_iconID = rights_iconID;
        this.rights_text = rights_text;
    }

    public int getRights_iconID() {
        return rights_iconID;
    }

    public String getRights_text() {
        return rights_text;
    }
}
