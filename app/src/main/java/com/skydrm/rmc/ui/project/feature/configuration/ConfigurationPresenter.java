package com.skydrm.rmc.ui.project.feature.configuration;

import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.ui.project.feature.configuration.task.GetMetadataTask;
import com.skydrm.rmc.ui.project.feature.configuration.task.UpdateProjectTask;

public class ConfigurationPresenter implements IConfigurationContact.IPresenter {
    private IProject mProject;
    private IConfigurationContact.IView mView;
    private GetMetadataCallback mGetMetadataCallback;
    private UpdateProjectCallback mUpdateProjectCallback;

    ConfigurationPresenter(IProject p, IConfigurationContact.IView v) {
        this.mProject = p;
        this.mView = v;
        this.mGetMetadataCallback = new GetMetadataCallback();
        this.mUpdateProjectCallback = new UpdateProjectCallback();
    }

    @Override
    public void getMetadata() {
        new GetMetadataTask(mProject, mGetMetadataCallback).run();
    }

    @Override
    public void updateProject(String name, String description, String invitationMsg) {
        new UpdateProjectTask(mProject, name, description, invitationMsg, mUpdateProjectCallback).run();
    }

    @Override
    public void onDestroy() {
        if (mView != null) {
            mView = null;
        }
        if (mGetMetadataCallback != null) {
            mGetMetadataCallback = null;
        }
        if (mUpdateProjectCallback != null) {
            mUpdateProjectCallback = null;
        }
    }

    class GetMetadataCallback implements GetMetadataTask.ITaskCallback<GetMetadataTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(GetMetadataTask.Result results) {
            if (mView != null) {
                mView.updateMetadata(results.metadata.getResults().getDetail().getInvitationMsg());
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }

    class UpdateProjectCallback implements UpdateProjectTask.ITaskCallback<UpdateProjectTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            if (mView != null) {
                mView.showLoadingIndicator(true);
            }
        }

        @Override
        public void onTaskExecuteSuccess(UpdateProjectTask.Result results) {
            if (mView != null) {
                mView.showLoadingIndicator(false);
            }
            if (mView != null) {
                mView.showSuccessView("ProjectMemberShip has been updated successfully.");
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (mView != null) {
                mView.showLoadingIndicator(false);
            }
        }
    }
}
