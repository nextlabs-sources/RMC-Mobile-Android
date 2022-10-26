package com.skydrm.rmc.ui.project.feature.summary;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.ui.base.IDataService;
import com.skydrm.rmc.ui.common.DeleteNxlFileTask;
import com.skydrm.rmc.ui.common.GetNxlDataTask;
import com.skydrm.rmc.ui.common.NxlFileItem;
import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.project.feature.member.task.GetMemberTask;

import java.util.ArrayList;
import java.util.List;

class SummaryPresenter implements ISummaryContact.IPresenter {
    private IDataService[] mService;
    private IProject mProject;
    private ISummaryContact.IView mView;
    private GetRecentFileCallback mGetRecentCallback;
    private GetMemberCallback mGetMemberCallback;
    private DeleteFileCallback mDeleteCallback;

    private boolean initialize;
    private boolean refresh;
    private boolean showEmpty;

    SummaryPresenter(IProject p, ISummaryContact.IView v) {
        this.mProject = p;
        this.mService = new IDataService[]{(IDataService) mProject};
        this.mView = v;
        this.mGetRecentCallback = new GetRecentFileCallback();
        this.mGetMemberCallback = new GetMemberCallback();
        this.mDeleteCallback = new DeleteFileCallback();
    }

    @Override
    public void initialize() {
        initialize = true;
        listMember(false);
        getRecentFile(SkyDRMApp.getInstance().isNetworkAvailable());
    }

    @Override
    public void listMemberAndFile() {
        listMember(false);
        getRecentFile(false);
    }

    @Override
    public void refresh() {
        if (initialize) {
            return;
        }
        refresh = true;
        listMember(true);
        getRecentFile(true);
    }

    @Override
    public void delete(INxlFile f) {
        DeleteNxlFileTask task = new DeleteNxlFileTask(f, mDeleteCallback);
        task.run();
    }

    private void listMember(boolean refresh) {
        new GetMemberTask(mProject, refresh, mGetMemberCallback).run();
    }

    private void getRecentFile(boolean sync) {
        GetNxlDataTask task = new GetNxlDataTask(mService,
                "/", sync,
                NxlFileType.RECENT.getValue(),
                mGetRecentCallback);
        task.run();
    }

    @Override
    public void onReleaseResource() {
        if (mView != null) {
            mView = null;
        }
        if (mGetRecentCallback != null) {
            mGetRecentCallback = null;
        }
        if (mGetMemberCallback != null) {
            mGetMemberCallback = null;
        }
        if (mDeleteCallback != null) {
            mDeleteCallback = null;
        }
    }

    private class GetRecentFileCallback implements GetNxlDataTask.ITaskCallback<GetNxlDataTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            if (initialize) {
                if (mView != null) {
                    mView.onInitialize(true);
                }
            }
        }

        @Override
        public void onTaskExecuteSuccess(GetNxlDataTask.Result results) {
            if (initialize) {
                if (mView != null) {
                    mView.onInitialize(false);
                }
                initialize = false;
            }
            if (refresh) {
                if (mView != null) {
                    mView.showLoadingIndicator(false);
                }
                refresh = false;
            }
            showSpace();
            List<INxlFile> recent = results.data;
            if (recent == null || recent.size() == 0) {
                if (mView != null) {
                    mView.showEmpty(true);
                    showEmpty = true;
                }
                return;
            }

            if (showEmpty) {
                if (mView != null) {
                    mView.showEmpty(false);
                }
                showEmpty = false;
            }

            if (mView != null) {
                mView.update(adapt2Item(recent));
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (initialize) {
                if (mView != null) {
                    mView.onInitialize(false);
                }
                initialize = false;
            }
            if (refresh) {
                if (mView != null) {
                    mView.showLoadingIndicator(false);
                }
                refresh = false;
            }
            if (mView != null) {
                mView.showErrorView(e);
            }
        }

        private void showSpace() {
            long quota = mProject.getQuota();
            long usage = mProject.getUsage();
            if (mView != null) {
                mView.showProjectSpace(quota, usage);
            }
        }

        private List<NxlFileItem> adapt2Item(List<INxlFile> files) {
            List<NxlFileItem> ret = new ArrayList<>();
            if (files == null || files.size() == 0) {
                return ret;
            }
            for (INxlFile i : files) {
                ret.add(new NxlFileItem(i, "Recent files"));
            }
            return ret;
        }
    }

    private class GetMemberCallback implements GetMemberTask.ITaskCallback<GetMemberTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(GetMemberTask.Result results) {
            if (mView != null) {
                mView.displayMember(results.mMembers);
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }

    class DeleteFileCallback implements DeleteNxlFileTask.ITaskCallback<DeleteNxlFileTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(DeleteNxlFileTask.Result results) {
            getRecentFile(false);
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            if (mView != null) {
                mView.showErrorView(e);
            }
        }
    }
}
