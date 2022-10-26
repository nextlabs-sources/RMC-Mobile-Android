package com.skydrm.sdk.rms.rest.project;

import android.os.Parcel;
import android.os.Parcelable;

public class ProjectMetaDataResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1484538182215
     * results : {"detail":{"id":14,"name":"Project SkyDRM","description":"Share your documents securely","displayName":"Project SkyDRM","creationTime":1483606243712,"totalMembers":1,"totalFiles":0,"ownedByMe":true,"owner":{"userId":2,"name":"admin@nextlabs.com","email":"admin@nextlabs.com"},"accountType":"PROJECT_TRIAL","trialEndTime":148706243714}}
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
         * detail : {"id":14,"name":"Project SkyDRM","description":"Share your documents securely","displayName":"Project SkyDRM","creationTime":1483606243712,"totalMembers":1,"totalFiles":0,"ownedByMe":true,"owner":{"userId":2,"name":"admin@nextlabs.com","email":"admin@nextlabs.com"},"accountType":"PROJECT_TRIAL","trialEndTime":148706243714}
         */

        private DetailBean detail;

        public DetailBean getDetail() {
            return detail;
        }

        public void setDetail(DetailBean detail) {
            this.detail = detail;
        }

        public static class DetailBean {
            /**
             * id : 14
             * name : Project SkyDRM
             * description : Share your documents securely
             * displayName : Project SkyDRM
             * creationTime : 1483606243712
             * invitationMsg  : This is the default message
             * totalMembers : 1
             * totalFiles : 0
             * ownedByMe : true
             * owner : {"userId":2,"name":"admin@nextlabs.com","email":"admin@nextlabs.com"}
             * accountType : PROJECT_TRIAL
             * trialEndTime : 148706243714
             */

            private int id;
            private String name;
            private String description;
            private String displayName;
            private long creationTime;
            private String invitationMsg;
            private int totalMembers;
            private int totalFiles;
            private boolean ownedByMe;
            private OwnerBean owner;
            private String accountType;
            private long trialEndTime;

            public String getInvitationMsg() {
                return invitationMsg;
            }

            public void setInvitationMsg(String invitationMsg) {
                this.invitationMsg = invitationMsg;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public String getDisplayName() {
                return displayName;
            }

            public void setDisplayName(String displayName) {
                this.displayName = displayName;
            }

            public long getCreationTime() {
                return creationTime;
            }

            public void setCreationTime(long creationTime) {
                this.creationTime = creationTime;
            }

            public int getTotalMembers() {
                return totalMembers;
            }

            public void setTotalMembers(int totalMembers) {
                this.totalMembers = totalMembers;
            }

            public int getTotalFiles() {
                return totalFiles;
            }

            public void setTotalFiles(int totalFiles) {
                this.totalFiles = totalFiles;
            }

            public boolean isOwnedByMe() {
                return ownedByMe;
            }

            public void setOwnedByMe(boolean ownedByMe) {
                this.ownedByMe = ownedByMe;
            }

            public OwnerBean getOwner() {
                return owner;
            }

            public void setOwner(OwnerBean owner) {
                this.owner = owner;
            }

            public String getAccountType() {
                return accountType;
            }

            public void setAccountType(String accountType) {
                this.accountType = accountType;
            }

            public long getTrialEndTime() {
                return trialEndTime;
            }

            public void setTrialEndTime(long trialEndTime) {
                this.trialEndTime = trialEndTime;
            }

            public static class OwnerBean implements Parcelable {
                /**
                 * userId : 2
                 * name : admin@nextlabs.com
                 * email : admin@nextlabs.com
                 */

                private int userId;
                private String name;
                private String email;


                public int getUserId() {
                    return userId;
                }

                public void setUserId(int userId) {
                    this.userId = userId;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getEmail() {
                    return email;
                }

                public void setEmail(String email) {
                    this.email = email;
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeInt(this.userId);
                    dest.writeString(this.name);
                    dest.writeString(this.email);
                }

                public OwnerBean() {
                }

                protected OwnerBean(Parcel in) {
                    this.userId = in.readInt();
                    this.name = in.readString();
                    this.email = in.readString();
                }

                public static final Creator<OwnerBean> CREATOR = new Creator<OwnerBean>() {
                    @Override
                    public OwnerBean createFromParcel(Parcel source) {
                        return new OwnerBean(source);
                    }

                    @Override
                    public OwnerBean[] newArray(int size) {
                        return new OwnerBean[size];
                    }
                };
            }
        }
    }
}
