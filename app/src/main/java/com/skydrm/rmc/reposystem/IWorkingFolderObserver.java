package com.skydrm.rmc.reposystem;

import com.skydrm.rmc.reposystem.types.INxFile;

/**
 * Created by oye on 12/23/2016.
 */

public interface IWorkingFolderObserver {
    void onChildrenChanged(INxFile workingFolder);
}
