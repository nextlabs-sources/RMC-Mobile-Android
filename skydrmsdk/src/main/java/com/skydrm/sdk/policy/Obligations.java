package com.skydrm.sdk.policy;

import android.support.annotation.Nullable;

import com.skydrm.sdk.INxlObligations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//  change it's impl and make it suitable for Adhoc policy
//  currently, we only need to support watermark obl
public class Obligations implements INxlObligations {
    // map<ob_name,  ob_value>
    private Map<String, String> mapObligation = null;

    public Obligations() {
    }

    public void setObligation(Map<String, String> mapObligation) {
        this.mapObligation = mapObligation;
    }

    public Map<String, String> getObligation() {
        return mapObligation;
    }


    @Override
    public
    @Nullable
    Iterator<Map.Entry<String, String>> getIterator() {
        // sanity check
        if (mapObligation == null) {
            return null;
        }
        return mapObligation.entrySet().iterator();
    }

    /**
     * Judge whether have watermark, only used to the latest version(Add edit watermark & expiry feature),
     * can't be compatible with the older(for older, need to use {@link Rights} hasWatermark()), need to refactor later.
     */
    @Override
    public boolean hasWatermark() {
        if (mapObligation != null) {
            Set set = mapObligation.keySet();
            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if ("WATERMARK".equals(key) && mapObligation.get("WATERMARK") != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getDisplayWatermark() {
        if (mapObligation == null) {
            return "";
        }
        return mapObligation.get("WATERMARK");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("Obligations:[");
        if (hasWatermark()) {
            sb.append("WATERMARK:");
            sb.append(mapObligation.get("WATERMARK"));
        }
        sb.append("]");
        return sb.toString();
    }
}
