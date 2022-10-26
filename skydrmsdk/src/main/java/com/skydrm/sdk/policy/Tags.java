package com.skydrm.sdk.policy;

import com.skydrm.sdk.INxlTags;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Tags implements INxlTags {

    private Map<String, Set<String>> tags;

    // use static Builder class to build
    private Tags(Map<String, Set<String>> tags) {
        this.tags = tags;
    }

    @Override
    public boolean isEmpty() {
        return tags == null || tags.isEmpty();
    }

    @Override
    public boolean isContain(String key) {
        return !isEmpty() && tags.containsKey(key);
    }

    @Override
    public Set<String> find(String key) {
        if (isEmpty()) {
            return null;
        }
        return tags.get(key);
    }

    @Override
    public Map<String, Set<String>> getAll() {
        if (isEmpty()) {
            return null;
        }
        return tags;
    }

    @Override
    public String toJsonFormat() {
        if (isEmpty()) {
            return "{}";
        }
        JSONObject jo = new JSONObject();
        for (String key : tags.keySet()) {
            JSONArray values = new JSONArray();
            Set<String> tagValue = tags.get(key);
            if (tagValue == null || tagValue.isEmpty()) {
                continue;
            }
            for (String v : tagValue) {
                values.put(v);
            }
            try {
                jo.put(key, values);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jo.toString();
    }

    static public class Builder {
        private Map<String, Set<String>> tags;

        public Builder() {
            tags = new HashMap<>();
        }

        public Builder addTag(String key, Set<String> value) {
            // check if key has exist
            if (tags.containsKey(key)) {
                Set<String> v = tags.get(key);
                // enlarge v
                v.addAll(value);
                return this;
            }
            tags.put(key, value);
            return this;
        }

        public Builder addTag(String key, String value) {
            Set<String> vs = new HashSet<>();
            vs.add(value);
            return addTag(key, vs);
        }

        public Builder addTags(Map<String, Set<String>> maps) {
            if (maps == null || maps.isEmpty()) {
                return this;
            }
            for (String key : maps.keySet()) {
                addTag(key, maps.get(key));
            }
            return this;
        }

        // special used for nxl handler, to parese the tags in a json string
        /*
            {
	            "itar": ["itar01"],
	            "ear": ["ear01t"],
	            "test": ["test01","test02"]
            }
         */
        public Builder addFromJson(String json) {
            try {
                JSONObject jo = new JSONObject(json);
                Iterator<String> iter = jo.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    // get Set<String> tagValues;
                    Set<String> values = new HashSet<>();
                    JSONArray array = jo.getJSONArray(key);
                    int len = array.length();
                    for (int i = 0; i < len; i++) {
                        String value = array.getString(i);
                        values.add(value);
                    }
                    // add this <Key,Set<String>> into build
                    addTag(key, values);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public Tags build() {
            return new Tags(tags);
        }
    }

}
