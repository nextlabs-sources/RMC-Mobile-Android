package com.skydrm.sdk.rms.types.favorite;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ye on 5/27/2017.
 */

@Deprecated
public class UnfavoriteList {
    public String rmsId;
    public List<Item> lists;

    public UnfavoriteList(String rmsId) {
        this.rmsId = rmsId;
        lists = new ArrayList<>();
    }

    static public class Item {
        public String pathId;
        public String displayPath;
    }
}
