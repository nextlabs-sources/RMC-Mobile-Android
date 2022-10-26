package com.skydrm.rmc.ui.activity.server;

import com.skydrm.rmc.domain.UserAccount;

import java.util.List;

public class MsgUpdateUserAccount {
    public List<UserAccount> mUsrAccounts;

    public MsgUpdateUserAccount(List<UserAccount> userAccounts) {
        this.mUsrAccounts = userAccounts;
    }
}
