package com.skydrm.sdk.rms.types;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by hhu on 4/10/2018.
 */

public class PolicyEvaluationParams {

    /**
     * parameters : {"evalRequest":{"adhocPolicy":"Policy body","membershipId":"member1@skydrm","resources":[{"dimensionName":"from","resourceType":"fso","resourceName":"Example1.pdf","duid":"12345","classification":{"itar":["confidential"],"Applicable Licenses":["Lic1","Lic2"]},"attributes":{"file creation date":["10-jan-2018"]}}],"rights":255,"user":{"id":9,"attributes":{"Job Function":["engineer"],"Age":["14"]}},"application":{"name":"RMS","path":"/path/to/application","pid":"12345","attributes":{"publisher":["nextlabs","v1"],"licensed":["yes"]}},"host":{"ipAddress":"118.189.77.114"},"environments":[{"name":"environment","attributes":{"connection_type":["console"]}}],"evalType":0}}
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
         * evalRequest : {"adhocPolicy":"Policy body","membershipId":"member1@skydrm","resources":[{"dimensionName":"from","resourceType":"fso","resourceName":"Example1.pdf","duid":"12345","classification":{"itar":["confidential"],"Applicable Licenses":["Lic1","Lic2"]},"attributes":{"file creation date":["10-jan-2018"]}}],"rights":255,"user":{"id":9,"attributes":{"Job Function":["engineer"],"Age":["14"]}},"application":{"name":"RMS","path":"/path/to/application","pid":"12345","attributes":{"publisher":["nextlabs","v1"],"licensed":["yes"]}},"host":{"ipAddress":"118.189.77.114"},"environments":[{"name":"environment","attributes":{"connection_type":["console"]}}],"evalType":0}
         */

        private EvalRequestBean evalRequest;

        public EvalRequestBean getEvalRequest() {
            return evalRequest;
        }

        public void setEvalRequest(EvalRequestBean evalRequest) {
            this.evalRequest = evalRequest;
        }

        public static class EvalRequestBean {
            /**
             * adhocPolicy : Policy body
             * membershipId : member1@skydrm
             * resources : [{"dimensionName":"from","resourceType":"fso","resourceName":"Example1.pdf","duid":"12345","classification":{"itar":["confidential"],"Applicable Licenses":["Lic1","Lic2"]},"attributes":{"file creation date":["10-jan-2018"]}}]
             * rights : 255
             * user : {"id":9,"attributes":{"Job Function":["engineer"],"Age":["14"]}}
             * application : {"name":"RMS","path":"/path/to/application","pid":"12345","attributes":{"publisher":["nextlabs","v1"],"licensed":["yes"]}}
             * host : {"ipAddress":"118.189.77.114"}
             * environments : [{"name":"environment","attributes":{"connection_type":["console"]}}]
             * evalType : 0
             */

            private String adhocPolicy;
            private String membershipId;
            private int rights;
            private UserBean user;
            private ApplicationBean application;
            private HostBean host;
            private int evalType;
            private List<ResourcesBean> resources;
            private List<EnvironmentsBean> environments;

            public String getAdhocPolicy() {
                return adhocPolicy;
            }

            public void setAdhocPolicy(String adhocPolicy) {
                this.adhocPolicy = adhocPolicy;
            }

            public String getMembershipId() {
                return membershipId;
            }

            public void setMembershipId(String membershipId) {
                this.membershipId = membershipId;
            }

            public int getRights() {
                return rights;
            }

            public void setRights(int rights) {
                this.rights = rights;
            }

            public UserBean getUser() {
                return user;
            }

            public void setUser(UserBean user) {
                this.user = user;
            }

            public ApplicationBean getApplication() {
                return application;
            }

            public void setApplication(ApplicationBean application) {
                this.application = application;
            }

            public HostBean getHost() {
                return host;
            }

            public void setHost(HostBean host) {
                this.host = host;
            }

            public int getEvalType() {
                return evalType;
            }

            public void setEvalType(int evalType) {
                this.evalType = evalType;
            }

            public List<ResourcesBean> getResources() {
                return resources;
            }

            public void setResources(List<ResourcesBean> resources) {
                this.resources = resources;
            }

            public List<EnvironmentsBean> getEnvironments() {
                return environments;
            }

            public void setEnvironments(List<EnvironmentsBean> environments) {
                this.environments = environments;
            }

            public static class UserBean {
                /**
                 * id : 9
                 * attributes : {"Job Function":["engineer"],"Age":["14"]}
                 */

                private int id;
                private AttributesBean attributes;

                public int getId() {
                    return id;
                }

                public void setId(int id) {
                    this.id = id;
                }

                public AttributesBean getAttributes() {
                    return attributes;
                }

                public void setAttributes(AttributesBean attributes) {
                    this.attributes = attributes;
                }

                public static class AttributesBean {
                    @SerializedName("Job Function")
                    private List<String> _$JobFunction272; // FIXME check this code
                    private List<String> Age;

                    public List<String> get_$JobFunction272() {
                        return _$JobFunction272;
                    }

                    public void set_$JobFunction272(List<String> _$JobFunction272) {
                        this._$JobFunction272 = _$JobFunction272;
                    }

                    public List<String> getAge() {
                        return Age;
                    }

                    public void setAge(List<String> Age) {
                        this.Age = Age;
                    }
                }
            }

            public static class ApplicationBean {
                /**
                 * name : RMS
                 * path : /path/to/application
                 * pid : 12345
                 * attributes : {"publisher":["nextlabs","v1"],"licensed":["yes"]}
                 */

                private String name;
                private String path;
                private String pid;
                private AttributesBeanX attributes;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getPath() {
                    return path;
                }

                public void setPath(String path) {
                    this.path = path;
                }

                public String getPid() {
                    return pid;
                }

                public void setPid(String pid) {
                    this.pid = pid;
                }

                public AttributesBeanX getAttributes() {
                    return attributes;
                }

                public void setAttributes(AttributesBeanX attributes) {
                    this.attributes = attributes;
                }

                public static class AttributesBeanX {
                    private List<String> publisher;
                    private List<String> licensed;

                    public List<String> getPublisher() {
                        return publisher;
                    }

                    public void setPublisher(List<String> publisher) {
                        this.publisher = publisher;
                    }

                    public List<String> getLicensed() {
                        return licensed;
                    }

                    public void setLicensed(List<String> licensed) {
                        this.licensed = licensed;
                    }
                }
            }

            public static class HostBean {
                /**
                 * ipAddress : 118.189.77.114
                 */

                private String ipAddress;

                public String getIpAddress() {
                    return ipAddress;
                }

                public void setIpAddress(String ipAddress) {
                    this.ipAddress = ipAddress;
                }
            }

            public static class ResourcesBean {
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
                private AttributesBeanXX attributes;

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

                public AttributesBeanXX getAttributes() {
                    return attributes;
                }

                public void setAttributes(AttributesBeanXX attributes) {
                    this.attributes = attributes;
                }

                public static class ClassificationBean {
                    private List<String> itar;
                    @SerializedName("Applicable Licenses")
                    private List<String> _$ApplicableLicenses200; // FIXME check this code

                    public List<String> getItar() {
                        return itar;
                    }

                    public void setItar(List<String> itar) {
                        this.itar = itar;
                    }

                    public List<String> get_$ApplicableLicenses200() {
                        return _$ApplicableLicenses200;
                    }

                    public void set_$ApplicableLicenses200(List<String> _$ApplicableLicenses200) {
                        this._$ApplicableLicenses200 = _$ApplicableLicenses200;
                    }
                }

                public static class AttributesBeanXX {
                    @SerializedName("file creation date")
                    private List<String> _$FileCreationDate182; // FIXME check this code

                    public List<String> get_$FileCreationDate182() {
                        return _$FileCreationDate182;
                    }

                    public void set_$FileCreationDate182(List<String> _$FileCreationDate182) {
                        this._$FileCreationDate182 = _$FileCreationDate182;
                    }
                }
            }

            public static class EnvironmentsBean {
                /**
                 * name : environment
                 * attributes : {"connection_type":["console"]}
                 */

                private String name;
                private AttributesBeanXXX attributes;

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public AttributesBeanXXX getAttributes() {
                    return attributes;
                }

                public void setAttributes(AttributesBeanXXX attributes) {
                    this.attributes = attributes;
                }

                public static class AttributesBeanXXX {
                    private List<String> connection_type;

                    public List<String> getConnection_type() {
                        return connection_type;
                    }

                    public void setConnection_type(List<String> connection_type) {
                        this.connection_type = connection_type;
                    }
                }
            }
        }
    }
}
