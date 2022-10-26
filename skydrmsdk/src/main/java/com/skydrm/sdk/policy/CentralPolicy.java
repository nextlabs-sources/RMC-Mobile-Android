package com.skydrm.sdk.policy;

import com.skydrm.sdk.INxlTags;

import java.util.Map;
import java.util.Set;

/**
 * Centroal Policy now only contain Tags
 */

public class CentralPolicy {

    private INxlTags tags;
    private String membershipId;

    private CentralPolicy(INxlTags tags, String membershipId) {
        this.tags = tags;
        this.membershipId = membershipId;
    }

    public String generateJSON() {
        return tags.toJsonFormat();
    }

    public INxlTags getTags() {
        return tags;
    }

    public String getMembershipId() {
        return membershipId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("CentralPolicy:[");
        sb.append("membership:" + membershipId + ",");
        sb.append(generateJSON());
        sb.append("]");
        return sb.toString();
    }

    // for now CentralPolicy only contains Tags
    public static class Builder {
        Tags.Builder builder;
        String membershipId;

        public Builder() {
            builder = new Tags.Builder();
            membershipId = null;
        }

        public Builder addTag(String key, String value) {
            builder.addTag(key, value);
            return this;
        }

        public Builder addTag(String key, Set<String> values) {
            builder.addTag(key, values);
            return this;
        }

        public Builder addTags(Map<String, Set<String>> maps) {
            builder.addTags(maps);
            return this;
        }

        public Builder addTagFromJSON(String json) {
            builder.addFromJson(json);
            return this;
        }

        public Builder setMembershipId(String membershipId) {
            this.membershipId = membershipId;
            return this;
        }

        public CentralPolicy build() {
            // sanity check
            if (membershipId == null || membershipId.isEmpty()) {
                throw new RuntimeException("owner id is null or empty when construct Central Policy");
            }
            return new CentralPolicy(builder.build(), membershipId);
        }
    }


}
