package com.skydrm.sdk;

import java.util.Map;
import java.util.Set;

// this interface is designed for get tags which a nxl file hold can be used by Central Policy
public interface INxlTags {
    boolean isEmpty();

    boolean isContain(String key);

    Set<String> find(String key);

    Map<String, Set<String>> getAll();

    String toJsonFormat();
}
