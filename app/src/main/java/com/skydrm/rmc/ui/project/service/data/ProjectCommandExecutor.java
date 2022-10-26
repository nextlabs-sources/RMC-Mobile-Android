package com.skydrm.rmc.ui.project.service.data;

import com.skydrm.rmc.ui.project.service.data.tasks.ProjectGetMembershipTask;
import com.skydrm.rmc.ui.myspace.myvault.ICommand;
import com.skydrm.rmc.ui.myspace.myvault.data.Error;

/**
 * Created by hhu on 5/3/2018.
 */

public class ProjectCommandExecutor implements IProjectCommand {

    @Override
    public void getProjectMemberShipId(int projectId, ICommand.ICommandExecuteCallback<Result.GetMembershipResult, Error> callback) {
        ProjectGetMembershipTask getMembershipTask = new ProjectGetMembershipTask(projectId, callback);
        getMembershipTask.run();
    }
}
