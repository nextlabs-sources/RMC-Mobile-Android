package com.skydrm.sdk.rms.rest.project.file;

public class UploadForNXLFileParam {


    /**
     * parameters : {"name":"test.js.nxl","parentPathId":"/new3/","type":1}
     */

    private ParametersBean parameters;

    public ParametersBean getParameters() {
        return parameters;
    }

    public void setParameters(ParametersBean parameters) {
        this.parameters = parameters;
    }

    public static class ParametersBean {
        /**
         * name : test.js.nxl
         * parentPathId : /new3/
         * type : 1
         */

        private String name;
        private String parentPathId;
        private int type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getParentPathId() {
            return parentPathId;
        }

        public void setParentPathId(String parentPathId) {
            this.parentPathId = parentPathId;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}
