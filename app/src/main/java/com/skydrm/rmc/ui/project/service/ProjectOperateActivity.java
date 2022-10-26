package com.skydrm.rmc.ui.project.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.datalayer.repo.project.Project;
import com.skydrm.rmc.datalayer.repo.project.ProjectFile;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceRepo;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.project.feature.service.IMarkCallback;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.protect.ProjectAddFileFragment;
import com.skydrm.rmc.ui.project.feature.service.protect.ProjectMoreOptionsFragment;
import com.skydrm.rmc.ui.project.feature.service.share.ProjectSelectFragment;
import com.skydrm.rmc.ui.repository.feature.LibraryFileFragment;
import com.skydrm.rmc.ui.repository.feature.RepoFileFragment;
import com.skydrm.rmc.ui.repository.feature.RepoSelectFragment;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsFile;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsService;
import com.skydrm.rmc.ui.service.modifyrights.view.ModifyRightsFragment;
import com.skydrm.rmc.ui.widget.LoadingDialog2;

import java.io.Serializable;

public class ProjectOperateActivity extends BaseActivity {
    private MarkCallback mMarkCallback;
    private LoadingDialog2 mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_operate);
        loadByAction();
    }

    public void replaceLoadProjectAddFileFrag(IProject p) {
        ProjectAddFileFragment frag = ProjectAddFileFragment.newInstance();

        String parentPathId = "/";
        INxlFile f = mMarkCallback.getFile();
        if (f == null) {
            return;
        }

        Bundle args = new Bundle();
        args.putString(Constant.KEY_ACTION_BELONG_ADD_FILE_FRAG, Constant.VALUE_ADD_FILE_FROM_PROJECT);
        args.putParcelable(Constant.PROJECT_DETAIL, (Parcelable) p);
        args.putParcelable(Constant.PROJECT_FILE_ENTRY, (NxlDoc) f);
        args.putString(Constant.PROJECT_PARENT_PATH_ID, parentPathId);
        frag.setArguments(args);

        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    public void replaceLoadProjectAddFileFrag(IProject p, INxFile file, String parentPathId) {
        ProjectAddFileFragment frag = ProjectAddFileFragment.newInstance();
        Bundle args = new Bundle();

        args.putString(Constant.KEY_ACTION_BELONG_ADD_FILE_FRAG, Constant.VALUE_ADD_FILE_FROM_WORKSPACE);
        args.putParcelable(Constant.PROJECT_DETAIL, (Parcelable) p);
        args.putString(Constant.PROJECT_PARENT_PATH_ID, parentPathId);
        args.putSerializable(Constant.LIBRARY_FILE_ENTRY, (Serializable) file);
        frag.setArguments(args);

        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    public void replaceLoadProjectAddFileFragment(IProject p, String localPath, String pathId) {
        ProjectAddFileFragment frag = ProjectAddFileFragment.newInstance();
        Bundle args = new Bundle();

        args.putString(Constant.KEY_ACTION_BELONG_ADD_FILE_FRAG, Constant.VALUE_ADD_FILE_FROM_LIBRARY);
        args.putParcelable(Constant.PROJECT_DETAIL, (Parcelable) p);
        args.putString(Constant.PROJECT_PARENT_PATH_ID, pathId);
        args.putString(Constant.LIBRARY_FILE_ENTRY, localPath);
        frag.setArguments(args);

        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    public void replaceLoadRepoFileFragment(final IProject p, BoundService s, final String pathId) {
        RepoFileFragment frag = RepoFileFragment.newInstance();
        frag.setOnRepoFileItemClickListener(new RepoFileFragment.OnRepoFileItemClickListener() {
            @Override
            public void onItemClick(INxFile f) {
                replaceLoadProjectAddFileFrag(p, f, pathId);
            }
        });
        frag.setOnPopupFragmentListener(new RepoFileFragment.OnPopupFragmentListener() {
            @Override
            public void onPopup() {
                popup();
            }
        });
        Bundle args = new Bundle();
        args.putSerializable(Constant.BOUND_SERVICE, s);
        frag.setArguments(args);

        frag.setUserVisibleHint(true);
        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    public void replaceLoadLibraryFileFrag(final IProject p,
                                           String rootPathId,
                                           final String currentPathId) {
        LibraryFileFragment frag = LibraryFileFragment.newInstance();
        frag.setOnItemClickListener(new LibraryFileFragment.OnItemClickListener() {
            @Override
            public void onItemClick(INxlFile f, int pos) {
                replaceLoadProjectAddFileFragment(p, f.getPathId(), currentPathId);
            }
        });
        frag.setOnPopupFragmentListener(new LibraryFileFragment.OnPopupFragmentListener() {
            @Override
            public void onPopup() {
                popup();
            }
        });
        frag.setUserVisibleHint(true);
        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    private void loadByAction() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        String action = intent.getAction();
        if (action == null || action.isEmpty()) {
            finish();
            return;
        }
        switch (action) {
            case Constant.ACTION_LUNCH_REPOSITORY_SELECT_FRAGMENT:
                loadRepoSelectFragAsRoot(intent);
                break;
            case Constant.ACTION_LUNCH_MORE_OPTIONS_FRAGMENT:
                loadMoreOptionsFragAsRoot(intent);
                break;
            case Constant.ACTION_LUNCH_PROJECT_SELECT_FRAGMENT:
                checkThenLoadProjectSelectFragmentAsRoot(intent);
                break;
            case Constant.ACTION_CHECK_THEN_SHARE_TO_PERSON:
                syncTenantPreferences();
                checkThenLoadProjectAddFileFragmentAsRoot(intent);
                break;
            case Constant.ACTION_CHECK_THEN_MODIFY_RIGHTS:
                checkThenLoadModifyRightsFragAsRoot(intent);
                break;
        }
    }

    private void loadRepoSelectFragAsRoot(Intent intent) {
        final IProject project = intent.getParcelableExtra(Constant.PROJECT_DETAIL);
        final String rootPathId = intent.getStringExtra(Constant.NAME_ROOT_PATH_ID);
        final String currentPathId = intent.getStringExtra(Constant.NAME_CURRENT_PATH_ID);

        RepoSelectFragment frag = RepoSelectFragment.newInstance();
        frag.setOnRepoItemClickListener(new RepoSelectFragment.OnRepoItemClickListener() {
            @Override
            public void onLibraryItemClick() {
                replaceLoadLibraryFileFrag(project, rootPathId, currentPathId);
            }

            @Override
            public void onRepoItemClick(BoundService s) {
                replaceLoadRepoFileFragment(project, s, currentPathId);
            }
        });

        loadRootFragment(R.id.fl_container, frag);
    }

    private void loadMoreOptionsFragAsRoot(Intent intent) {
        ProjectMoreOptionsFragment frag = ProjectMoreOptionsFragment.newInstance();
        Bundle args = new Bundle();
        args.putBoolean(Constant.STATE_EXTRACT_SWITCH, intent.getBooleanExtra(Constant.STATE_EXTRACT_SWITCH, false));
        frag.setArguments(args);
        loadRootFragment(R.id.fl_container, frag);
    }

    private void checkThenLoadProjectSelectFragmentAsRoot(Intent i) {
        if (i == null) {
            return;
        }
        INxlFile base = i.getParcelableExtra(Constant.PROJECT_FILE_ENTRY);
        IProject project = i.getParcelableExtra(Constant.PROJECT_DETAIL);
        if (base instanceof ProjectFile) {
            ProjectFile pf = (ProjectFile) base;
            mMarkCallback = new MarkCallback(project, MarkCallback.CALLBACK_ADD_TO_PROJECT, base);
            pf.shareToProject(mMarkCallback);
        }
    }

    private void checkThenLoadProjectAddFileFragmentAsRoot(Intent i) {
        if (i == null) {
            return;
        }
        INxlFile base = i.getParcelableExtra(Constant.PROJECT_FILE_ENTRY);
        IProject project = i.getParcelableExtra(Constant.PROJECT_DETAIL);
        if (base instanceof ProjectFile) {
            ProjectFile pf = (ProjectFile) base;
            mMarkCallback = new MarkCallback(project, MarkCallback.CALLBACK_SHARE_TO_PERSON, base);
            pf.shareToPerson(mMarkCallback);
        }
    }

    private void checkThenLoadModifyRightsFragAsRoot(Intent i) {
        if (i == null) {
            return;
        }
        IModifyRightsFile base = i.getParcelableExtra(Constant.MODIFY_RIGHTS_ENTRY);
        IModifyRightsService service = i.getParcelableExtra(Constant.MODIFY_RIGHTS_SERVICE);
        mMarkCallback = new MarkCallback(service, base, MarkCallback.CALLBACK_MODIFY_RIGHTS);

        if (base != null) {
            base.modifyRights(mMarkCallback);
        }
    }

    private void loadProjectSelectFragmentAsRoot(int id) {
        ProjectSelectFragment frag = ProjectSelectFragment.newInstance();
        frag.setUserVisibleHint(true);
        Bundle args = new Bundle();
        args.putInt(Constant.PROJECT_ID, id);
        frag.setArguments(args);
        loadRootFragment(R.id.fl_container, frag);
    }

    private void loadProjectAddFileFragAsRoot(IProject p, INxlFile file) {
        ProjectAddFileFragment frag = ProjectAddFileFragment.newInstance();

        Bundle args = new Bundle();
        args.putString(Constant.KEY_ACTION_BELONG_ADD_FILE_FRAG, Constant.VALUE_SHARE_TO_PERSON);
        args.putParcelable(Constant.PROJECT_DETAIL, (Parcelable) p);
        args.putParcelable(Constant.PROJECT_FILE_ENTRY, (NxlDoc) file);
        frag.setArguments(args);

        loadRootFragment(R.id.fl_container, frag);
    }

    private void loadModifyRightsFragAsRoot(IModifyRightsService service, IModifyRightsFile file) {
        ModifyRightsFragment frag = ModifyRightsFragment.newInstance();

        Bundle args = new Bundle();
        if (service instanceof Project) {
            args.putParcelable(Constant.MODIFY_RIGHTS_SERVICE, (Project) service);
        }
        if (service instanceof WorkSpaceRepo) {
            args.putParcelable(Constant.MODIFY_RIGHTS_SERVICE, (WorkSpaceRepo) service);
        }
        args.putParcelable(Constant.MODIFY_RIGHTS_ENTRY, file);
        frag.setArguments(args);

        loadRootFragment(R.id.fl_container, frag);
    }

    public void popup() {
        popupFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMarkCallback != null) {
            mMarkCallback = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constant.REQUEST_CODE_SELECT_EMAILS) {
            wrapContactParcel(data);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        if (intent == null) {
            super.onBackPressed();
        } else {
            String action = intent.getAction();
            if (action == null || action.isEmpty()) {
                super.onBackPressed();
            } else {
                switch (action) {
                    case Constant.ACTION_LUNCH_PROJECT_SELECT_FRAGMENT:
                        handleProjectAddFileFragmentBackPressEvent();
                        break;
                    case Constant.ACTION_LUNCH_REPOSITORY_SELECT_FRAGMENT:
                        handleRepoFileFragmentBackPressEvent();
                        break;
                    case Constant.ACTION_CHECK_THEN_MODIFY_RIGHTS:
                        handleModifyRightFragBackPressEvent2();
                        break;
                    default:
                        super.onBackPressed();
                        break;
                }
            }
        }
    }

    private void handleRepoFileFragmentBackPressEvent() {
        ProjectAddFileFragment frag = findChildFragment(ProjectAddFileFragment.class.getSimpleName());
        if (frag == null) {
            RepoFileFragment fileFrag = findChildFragment(RepoFileFragment.class.getSimpleName());
            if (fileFrag == null) {
                NxlBaseFragment nxlBaseFrag = findChildFragment(LibraryFileFragment.class.getSimpleName());
                if (nxlBaseFrag == null) {
                    super.onBackPressed();
                } else {
                    if (nxlBaseFrag.needInterceptBackPress()) {
                        nxlBaseFrag.interceptBackPress();
                    } else {
                        super.onBackPressed();
                    }
                }
            } else {
                if (fileFrag.needIntercept()) {
                    fileFrag.intercept();
                } else {
                    super.onBackPressed();
                }
            }

        } else {
            if (frag.needInterceptBackPress()) {
                frag.interceptBackPress();
            } else {
                super.onBackPressed();
            }
        }
    }

    private void handleModifyRightFragBackPressEvent2() {
        ModifyRightsFragment frag = findChildFragment(ModifyRightsFragment.class.getSimpleName());
        if (frag == null) {
            super.onBackPressed();
        } else {
            if (frag.needInterceptBackPress()) {
                frag.interceptBackPress();
            } else {
                super.onBackPressed();
            }
        }
    }

    private void handleProjectAddFileFragmentBackPressEvent() {
        ProjectAddFileFragment frag = findChildFragment(ProjectAddFileFragment.class.getSimpleName());
        if (frag == null) {
            super.onBackPressed();
        } else {
            if (frag.needInterceptBackPress()) {
                frag.interceptBackPress();
            } else {
                super.onBackPressed();
            }
        }
    }

    public void wrapContactParcel(Intent data) {
        ProjectAddFileFragment addFileFrag = findChildFragment(ProjectAddFileFragment.class.getSimpleName());
        if (addFileFrag != null) {
            addFileFrag.wrapContactParcel(data);
        }
    }

    private class MarkCallback implements IMarkCallback {
        private static final int CALLBACK_ADD_TO_PROJECT = 0;
        private static final int CALLBACK_SHARE_TO_PERSON = 1;
        private static final int CALLBACK_MODIFY_RIGHTS = 2;
        private IModifyRightsService mService;
        private IModifyRightsFile mModifyFile;
        private IProject mProject;
        private INxlFile mBase;

        private int mType;

        MarkCallback(IProject project, int type, INxlFile base) {
            this.mProject = project;
            this.mType = type;
            this.mBase = base;
        }

        MarkCallback(IModifyRightsService service, IModifyRightsFile file, int type) {
            this.mService = service;
            this.mModifyFile = file;
            this.mType = type;
        }

        public INxlFile getFile() {
            return mBase;
        }

        @Override
        public void onMarkStart() {
            showLoadingDialog();
        }

        @Override
        public void onMarkAllow() {
            dismissLoadingDialog();
            loadByType();
        }

        @Override
        public void onMarkFailed(MarkException e) {
            dismissLoadingDialog();
            ExceptionHandler.handleException(ProjectOperateActivity.this, e);
        }

        private void loadByType() {
            if (mType == CALLBACK_ADD_TO_PROJECT) {
                loadProjectSelectFragmentAsRoot(mProject == null ? -1 : mProject.getId());
            }
            if (mType == CALLBACK_SHARE_TO_PERSON) {
                loadProjectAddFileFragAsRoot(mProject, mBase);
            }
            if (mType == CALLBACK_MODIFY_RIGHTS) {
                loadModifyRightsFragAsRoot(mService, mModifyFile);
            }
        }
    }

    private void showLoadingDialog() {
        mLoadingDialog = LoadingDialog2.newInstance();
        mLoadingDialog.showModalDialog(this);
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissDialog();
            mLoadingDialog = null;
        }
    }
}
