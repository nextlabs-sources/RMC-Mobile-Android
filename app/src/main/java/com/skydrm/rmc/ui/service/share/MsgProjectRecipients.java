package com.skydrm.rmc.ui.service.share;

import java.util.List;
import java.util.Map;

public class MsgProjectRecipients {
    List<Integer> mAllData;
    Map<String, String> mData;

    public MsgProjectRecipients(List<Integer> all, Map<String, String> data) {
        this.mAllData = all;
        this.mData = data;
    }

}
