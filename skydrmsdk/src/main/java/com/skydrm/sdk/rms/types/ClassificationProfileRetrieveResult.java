package com.skydrm.sdk.rms.types;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by hhu on 4/9/2018.
 */
@Deprecated
public class ClassificationProfileRetrieveResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1484060079827
     * results : {"maxCategoryNum":5,"maxLabelNum":10,"categories":[{"name":"Sensitivity","multiSelect":true,"mandatory":true,"labels":[{"name":"Non-Business","default":true},{"name":"General Business"},{"name":"Confidential"}]},{"name":"Project","multiSelect":false,"mandatory":true,"labels":[{"name":"Project"}]}]}
     */

    private int statusCode;
    private String message;
    private long serverTime;
    private ResultsBean results;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public ResultsBean getResults() {
        return results;
    }

    public void setResults(ResultsBean results) {
        this.results = results;
    }

    public static class ResultsBean {
        /**
         * maxCategoryNum : 5
         * maxLabelNum : 10
         * categories : [{"name":"Sensitivity","multiSelect":true,"mandatory":true,"labels":[{"name":"Non-Business","default":true},{"name":"General Business"},{"name":"Confidential"}]},{"name":"Project","multiSelect":false,"mandatory":true,"labels":[{"name":"Project"}]}]
         */

        private int maxCategoryNum;
        private int maxLabelNum;
        private List<CategoriesBean> categories;

        public int getMaxCategoryNum() {
            return maxCategoryNum;
        }

        public void setMaxCategoryNum(int maxCategoryNum) {
            this.maxCategoryNum = maxCategoryNum;
        }

        public int getMaxLabelNum() {
            return maxLabelNum;
        }

        public void setMaxLabelNum(int maxLabelNum) {
            this.maxLabelNum = maxLabelNum;
        }

        public List<CategoriesBean> getCategories() {
            return categories;
        }

        public void setCategories(List<CategoriesBean> categories) {
            this.categories = categories;
        }

        public static class CategoriesBean {
            /**
             * name : Sensitivity
             * multiSelect : true
             * mandatory : true
             * labels : [{"name":"Non-Business","default":true},{"name":"General Business"},{"name":"Confidential"}]
             */

            private String name;
            private boolean multiSelect;
            private boolean mandatory;
            private List<LabelsBean> labels;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public boolean isMultiSelect() {
                return multiSelect;
            }

            public void setMultiSelect(boolean multiSelect) {
                this.multiSelect = multiSelect;
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
                /**
                 * name : Non-Business
                 * default : true
                 */

                private String name;
                @SerializedName("default")
                private boolean defaultX;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public boolean isDefaultX() {
                    return defaultX;
                }

                public void setDefaultX(boolean defaultX) {
                    this.defaultX = defaultX;
                }

                @Override
                public String toString() {
                    return "LabelsBean{" +
                            "name='" + name + '\'' +
                            ", defaultX=" + defaultX +
                            '}';
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;

                    LabelsBean that = (LabelsBean) o;

                    return defaultX == that.defaultX && (name != null ? name.equals(that.name) : that.name == null);
                }

                @Override
                public int hashCode() {
                    int result = name != null ? name.hashCode() : 0;
                    result = 31 * result + (defaultX ? 1 : 0);
                    return result;
                }
            }

            @Override
            public String toString() {
                return "CategoriesBean{" +
                        "name='" + name + '\'' +
                        ", multiSelect=" + multiSelect +
                        ", mandatory=" + mandatory +
                        ", labels=" + labels +
                        '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                CategoriesBean that = (CategoriesBean) o;

                if (multiSelect != that.multiSelect) return false;
                if (mandatory != that.mandatory) return false;
                if (!name.equals(that.name)) return false;
                return labels.equals(that.labels);
            }

            @Override
            public int hashCode() {
                int result = name.hashCode();
                result = 31 * result + (multiSelect ? 1 : 0);
                result = 31 * result + (mandatory ? 1 : 0);
                result = 31 * result + labels.hashCode();
                return result;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ResultsBean that = (ResultsBean) o;

            if (maxCategoryNum != that.maxCategoryNum) return false;
            if (maxLabelNum != that.maxLabelNum) return false;
            return categories.equals(that.categories);
        }

        @Override
        public int hashCode() {
            int result = maxCategoryNum;
            result = 31 * result + maxLabelNum;
            result = 31 * result + categories.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ResultsBean{" +
                    "maxCategoryNum=" + maxCategoryNum +
                    ", maxLabelNum=" + maxLabelNum +
                    ", categories=" + categories +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ClassificationProfileRetrieveResult{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", serverTime=" + serverTime +
                ", results=" + results +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassificationProfileRetrieveResult that = (ClassificationProfileRetrieveResult) o;

        if (statusCode != that.statusCode) return false;
        if (!message.equals(that.message)) return false;
        return results.equals(that.results);
    }

    @Override
    public int hashCode() {
        int result = statusCode;
        result = 31 * result + message.hashCode();
        result = 31 * result + results.hashCode();
        return result;
    }
}
