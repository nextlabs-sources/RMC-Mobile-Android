package com.skydrm.rmc.ui.project.feature.member;

import com.skydrm.rmc.datalayer.repo.project.IMember;

public class MemberItem {
    public String title;
    public IMember member;

    public MemberItem(String title, IMember member) {
        this.title = title;
        this.member = member;
    }
}
