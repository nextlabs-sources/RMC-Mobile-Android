package com.skydrm.sdk.rms.types.favorite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ye on 5/27/2017.
 */

public class FavoriteList {
    public String rmsID;
    public List<Item> lists;

    public FavoriteList(String rmsID) {
        this.rmsID = rmsID;
        lists = new ArrayList<>();
    }

    static public class Item{
        public String pathId;
        public String displayPath;
        public String parentFileId;
        public long fileSize;
        public long lastModifiedTime;
        // used to flag is mark or un-mark.
        public boolean isMark = false;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            return pathId != null ? pathId.equals(item.pathId) : item.pathId == null;

        }

        @Override
        public int hashCode() {
            return pathId != null ? pathId.hashCode() : 0;
        }
    }
}
