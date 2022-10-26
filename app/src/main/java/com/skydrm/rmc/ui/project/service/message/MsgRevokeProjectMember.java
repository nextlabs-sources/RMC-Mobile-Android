package com.skydrm.rmc.ui.project.service.message;

import com.skydrm.rmc.datalayer.repo.project.IMember;

public class MsgRevokeProjectMember {
    public IMember mTarget;

    public MsgRevokeProjectMember(IMember target) {
        this.mTarget = target;
    }
}
