package com.skydrm.rmc.engine.enumData;

/**
 * Created by aning on 5/10/2017.
 */

public enum CmdOperate {
    // normal protect
    PROTECT,
    // normal share
    SHARE,
    // command Add
    COMMAND_ADD,
    // project add file from three party.
    COMMAND_PROJECT_ADD_FROM_3D,
    // command Add(Scan) -> Protect
    COMMAND_ADD_PROTECT,
    // command Add(Scan) -> Share
    COMMAND_ADD_SHARE,
    // command Protect from Repo
    COMMAND_PROTECT_FROM_REPO,
    // command Share from Repo
    COMMAND_SHARE_FROM_REPO,
    // command Protect from lib
    COMMAND_PROTECT_FROM_LIB,
    // command Share from lib
    COMMAND_SHARE_FROM_LIB,
    // command Scan
    COMMAND_SCAN,
    COMMAND_SELECT_PATH,
    COMMAND_PROTECT_THEN_ADD,
    COMMAND_SHARE_THEN_ADD
}
