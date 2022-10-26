package com.skydrm.rmc.ui.project.service.contact;

import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.project.service.contact.task.NewProjectTask;

import java.util.List;

public class NewProjectPresenter implements INewProjectContact.IPresenter {
    private INewProjectContact.IView mView;

    public NewProjectPresenter(INewProjectContact.IView view) {
        this.mView = view;
    }

    @Override
    public void newProject(String name, String description,
                           List<String> emails, String invitationMsg) {
        new NewProjectTask(name, description, emails, invitationMsg,
                new LoadTask.ITaskCallback<NewProjectTask.Result, Exception>() {
                    @Override
                    public void onTaskPreExecute() {
                        if (mView != null) {
                            mView.onCreatingProject();
                        }
                    }

                    @Override
                    public void onTaskExecuteSuccess(NewProjectTask.Result results) {
                        if (mView != null) {
                            mView.onCreateSucceed(results.project);
                        }
                    }

                    @Override
                    public void onTaskExecuteFailed(Exception e) {
                        if (mView != null) {
                            mView.onCreateFailed(e);
                        }
                    }
                }).run();
    }

    @Override
    public void onDestroy() {
        if (mView != null) {
            mView = null;
        }
    }
}
