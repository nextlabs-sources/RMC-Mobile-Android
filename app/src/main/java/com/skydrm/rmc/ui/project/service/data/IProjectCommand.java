package com.skydrm.rmc.ui.project.service.data;

import com.skydrm.rmc.ui.myspace.myvault.ICommand;
import com.skydrm.rmc.ui.myspace.myvault.data.Error;

/**
 * Created by hhu on 5/3/2018.
 */

public interface IProjectCommand extends ICommand {
    void getProjectMemberShipId(int projectId, ICommand.ICommandExecuteCallback<Result.GetMembershipResult, Error> callback);
}
