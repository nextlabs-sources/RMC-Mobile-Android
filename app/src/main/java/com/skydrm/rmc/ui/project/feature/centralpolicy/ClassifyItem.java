package com.skydrm.rmc.ui.project.feature.centralpolicy;


import java.util.List;

/**
 * Created by hhu on 4/3/2018.
 */

public class ClassifyItem {
    private String categoryName;
    private boolean multiSelect;
    private boolean mandatory;
    private List<LabelsBean> labels;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public List<LabelsBean> getLabels() {
        return labels;
    }

    public void setLabels(List<LabelsBean> labels) {
        this.labels = labels;
    }

    public static class LabelsBean {
        String name;
        boolean defaultX;

        public LabelsBean(String name, boolean defaultX) {
            this.name = name;
            this.defaultX = defaultX;
        }

        @Override
        public String toString() {
            return "LabelsBean{" +
                    "name='" + name + '\'' +
                    ", defaultX=" + defaultX +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ClassifyItem{" +
                "categoryName='" + categoryName + '\'' +
                ", multiSelect=" + multiSelect +
                ", mandatory=" + mandatory +
                ", labels=" + labels +
                '}';
    }
}
