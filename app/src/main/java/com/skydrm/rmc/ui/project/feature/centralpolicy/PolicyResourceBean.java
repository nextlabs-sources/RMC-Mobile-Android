package com.skydrm.rmc.ui.project.feature.centralpolicy;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by hhu on 4/13/2018.
 */

public class PolicyResourceBean {
    /**
     * dimensionName : from
     * resourceType : fso
     * resourceName : Example1.pdf
     * duid : 12345
     * classification : {"itar":["confidential"],"Applicable Licenses":["Lic1","Lic2"]}
     * attributes : {"file creation date":["10-jan-2018"]}
     */

    private String dimensionName;
    private String resourceType;
    private String resourceName;
    private String duid;
    private ClassificationBean classification;
    private AttributesBean attributes;

    public String getDimensionName() {
        return dimensionName;
    }

    public void setDimensionName(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getDuid() {
        return duid;
    }

    public void setDuid(String duid) {
        this.duid = duid;
    }

    public ClassificationBean getClassification() {
        return classification;
    }

    public void setClassification(ClassificationBean classification) {
        this.classification = classification;
    }

    public AttributesBean getAttributes() {
        return attributes;
    }

    public void setAttributes(AttributesBean attributes) {
        this.attributes = attributes;
    }

    public static class ClassificationBean {
        private List<String> itar;
        @SerializedName("Applicable Licenses")
        private List<String> _$ApplicableLicenses132; // FIXME check this code

        public List<String> getItar() {
            return itar;
        }

        public void setItar(List<String> itar) {
            this.itar = itar;
        }

        public List<String> get_$ApplicableLicenses132() {
            return _$ApplicableLicenses132;
        }

        public void set_$ApplicableLicenses132(List<String> _$ApplicableLicenses132) {
            this._$ApplicableLicenses132 = _$ApplicableLicenses132;
        }
    }

    public static class AttributesBean {
        @SerializedName("file creation date")
        private List<String> _$FileCreationDate200; // FIXME check this code

        public List<String> get_$FileCreationDate200() {
            return _$FileCreationDate200;
        }

        public void set_$FileCreationDate200(List<String> _$FileCreationDate200) {
            this._$FileCreationDate200 = _$FileCreationDate200;
        }
    }
}
