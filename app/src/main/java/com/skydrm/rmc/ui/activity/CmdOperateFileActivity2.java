package com.skydrm.rmc.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.Utils;
import com.skydrm.rmc.datalayer.repo.library.DownloadTask;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.eventBusMsg.CommandOperateEvent;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.fragment.CmdAddFragment;
import com.skydrm.rmc.ui.fragment.CmdProtectFragment;
import com.skydrm.rmc.ui.fragment.CmdShareFragment;
import com.skydrm.rmc.ui.project.feature.service.protect.ProjectAddFileFragment;
import com.skydrm.rmc.ui.repository.feature.LibraryFileFragment;
import com.skydrm.rmc.ui.repository.feature.RepoFileFragment;
import com.skydrm.rmc.ui.service.protect.IProtectFile;
import com.skydrm.rmc.ui.service.protect.IProtectService;
import com.skydrm.rmc.ui.service.protect.ProtectFragment;
import com.skydrm.rmc.ui.service.protect.file.LocalProtectFile;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

public class CmdOperateFileActivity2 extends BaseActivity {
    private static final int REQUEST_CODE_SELECT_FILE = 0x100;
    // selected repo file
    private INxFile mClickFileItem;
    private IProject mProject;
    private String mParentPathId;
    private CmdOperate mCmdOperate;

    private String mAction;

    private boolean isLoadProtectFragment;
    private boolean isLoadShareFragment;
    private File mTmpDest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmdoperatefile2);
        EventBus.getDefault().register(this);
        if (!resolveIntent()) {
            finish();
            return;
        }

        if (isProtectFromLibrary() || isShareFromLibrary() || isMySpaceAddFile()) {
            openMediaStore();
        } else if (isProtectFromRepo() || isShareFromRepo()) {
            loadRepositoryFileFragmentAsRoot(getIntent());
        } else if (isProtectFile()) {
            loadCmdProtectFragmentAsRoot(getIntent());
        } else if (isShareFile()) {
            loadCmdShareFragmentAsRoot(getIntent());
        } else if (isMySpaceProtectByScanDoc()) {
            loadCmdAddFileFragmentAsRoot(getIntent());
            ///loadCmdProtectFragmentAsRootByScan(getIntent());
        } else if (isProjectProtectByScanDoc()) {
            loadProjectAddFileFragmentByScan(getIntent());
            syncTenantPreferences();
        } else if (isWorkSpaceProtectByScanDoc()) {
            loadProtectFragmentAsRoot(getIntent());
        } else if (isMySpaceCreateFolderSelectPath()) {
            loadRepositoryFileFragmentAsRoot(getIntent());
        } else {
            loadProjectAddFileFragment();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.REQUEST_CODE_SELECT_EMAILS) {
                updateContactParcel(data);
            }
            if (requestCode == REQUEST_CODE_SELECT_FILE) {
                if (data != null) {
                    copyFile(data.getData());
                }
            }
        }
    }

    private void copyFile(Uri uri) {
        try {
            String displayName = "";
            try (Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null, null)) {
                // moveToFirst() returns false if the cursor has 0 rows. Very handy for
                // "if there's anything to look at, look at it" conditionals.
                if (cursor != null && cursor.moveToFirst()) {
                    // Note it's called "Display Name". This is
                    // provider-specific, and might not necessarily be the file name.
                    displayName = cursor.getString(
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }

            InputStream is = getContentResolver().openInputStream(uri);
            mTmpDest = new File(getDownloadMountPoint(), displayName);

            DownloadTask task = new DownloadTask(is, mTmpDest, new IProtectFile.ICallBack() {
                @Override
                public void onPreDownload() {

                }

                @Override
                public void onDownloadFinished(String localPath) {
                    if (isProtectFromLibrary()) {
                        loadCmdProtectFragmentAsRoot(localPath);
                    }
                    if (isShareFromLibrary()) {
                        loadCmdShareFragmentAsRoot(localPath);
                    }
                    if (isMySpaceAddFile()) {
                        loadCmdAddFileFragmentAsRoot(localPath);
                    }
                }

                @Override
                public void onDownloadProgress(long value) {

                }

                @Override
                public void onDownloadFailed(FileDownloadException e) {

                }
            });
            task.run();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private File getDownloadMountPoint() {
        try {
            return Utils.getTmpMountPoint();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * eventBus message handler for command project add from three party
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onCommandOperateEventHandler(CommandOperateEvent.CommandProjectAddFrom3D eventMsg) {
        mClickFileItem = eventMsg.getNxFile();
        mProject = eventMsg.project;
        mParentPathId = eventMsg.getCurrentPathId();
        mCmdOperate = eventMsg.getCmdOperate();
    }

    public String getWatermarkValue() {
        return mProject == null ? "" : mProject.getWatermark();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTmpDest != null) {
            mTmpDest.delete();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (isShareFromLibrary()) {
            CmdShareFragment shareFrag = findChildFragment(CmdShareFragment.class);
            if (shareFrag != null) {
//                popup();
                super.onBackPressed();
            } else {
                NxlBaseFragment frag = findChildFragment(LibraryFileFragment.class.getSimpleName());
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
        } else if (isShareFromRepo()) {
            CmdShareFragment shareFrag = findChildFragment(CmdShareFragment.class);
            if (shareFrag != null) {
                popup();
            } else {
                RepoFileFragment fileFrag = findChildFragment(RepoFileFragment.class.getSimpleName());
                if (fileFrag == null) {
                    super.onBackPressed();
                } else {
                    if (fileFrag.needIntercept()) {
                        fileFrag.intercept();
                    } else {
                        super.onBackPressed();
                    }
                }
            }
        } else if (isProtectFromRepo()) {
            CmdProtectFragment protectFrag = findChildFragment(CmdProtectFragment.class);
            if (protectFrag != null) {
                popup();
            } else {
                RepoFileFragment fileFrag = findChildFragment(RepoFileFragment.class.getSimpleName());
                if (fileFrag == null) {
                    super.onBackPressed();
                } else {
                    if (fileFrag.needIntercept()) {
                        fileFrag.intercept();
                    } else {
                        super.onBackPressed();
                    }
                }
            }
        } else if (isProtectFromLibrary()) {
            CmdProtectFragment protectFrag = findChildFragment(CmdProtectFragment.class);
            if (protectFrag != null) {
//                popup();
                super.onBackPressed();
            } else {
                NxlBaseFragment frag = findChildFragment(LibraryFileFragment.class.getSimpleName());
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
        } else if (isMySpaceAddFile()) {
            if (isLoadProtectFragment) {
                CmdProtectFragment protectFrag = findChildFragment(CmdProtectFragment.class);
                if (protectFrag != null) {
                    popup();
                } else {
                    handleCmdAddFileFragBackPressEvent();
                }
            } else if (isLoadShareFragment) {
                CmdShareFragment shareFrag = findChildFragment(CmdShareFragment.class);
                if (shareFrag != null) {
                    popup();
                } else {
                    handleCmdAddFileFragBackPressEvent();
                }
            } else {
                handleCmdAddFileFragBackPressEvent();
            }
        } else if (isMySpaceCreateFolderSelectPath()) {
            RepoFileFragment fileFrag = findChildFragment(RepoFileFragment.class.getSimpleName());
            if (fileFrag == null) {
                super.onBackPressed();
            } else {
                if (fileFrag.needIntercept()) {
                    fileFrag.intercept();
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    private void handleCmdAddFileFragBackPressEvent() {
        CmdAddFragment addFrag = findChildFragment(CmdAddFragment.class);
        if (addFrag != null) {
//            popup();
            super.onBackPressed();
        } else {
            NxlBaseFragment frag = findChildFragment(LibraryFileFragment.class.getSimpleName());
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
    }

    public void popup() {
        popupFragment();
    }

    public void replaceLoadCmdProtectFragmentAsRoot(String filePath) {
        isLoadProtectFragment = true;
        isLoadShareFragment = false;
        CmdProtectFragment frag = CmdProtectFragment.newInstance();
        Bundle args = new Bundle();

        args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.COMMAND_PROTECT_FROM_LIB);
        args.putString(Constant.LIBRARY_FILE_ENTRY, filePath);

        frag.setArguments(args);
        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    public void loadCmdProtectFragmentAsRoot(String filePath) {
        isLoadProtectFragment = true;
        isLoadShareFragment = false;
        CmdProtectFragment frag = CmdProtectFragment.newInstance();
        Bundle args = new Bundle();

        args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.COMMAND_PROTECT_FROM_LIB);
        args.putString(Constant.LIBRARY_FILE_ENTRY, filePath);

        frag.setArguments(args);
        loadRootFragment(R.id.fl_container, frag);
    }

    public void replaceLoadCmdProtectFragmentAsRoot(BoundService boundService, INxFile destFolder, String filePath) {
        isLoadProtectFragment = true;
        isLoadShareFragment = false;
        CmdProtectFragment frag = CmdProtectFragment.newInstance();
        Bundle args = new Bundle();

        args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.COMMAND_PROTECT_THEN_ADD);
        args.putSerializable(Constant.BOUND_SERVICE, boundService);
        args.putSerializable(Constant.DEST_FOLDER, (Serializable) destFolder);
        args.putString(Constant.LIBRARY_FILE_ENTRY, filePath);

        frag.setArguments(args);
        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    public void replaceLoadCmdProtectFragmentAsRoot(INxFile file) {
        CmdProtectFragment frag = CmdProtectFragment.newInstance();
        Bundle args = new Bundle();

        args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.COMMAND_PROTECT_FROM_REPO);
        args.putSerializable(Constant.LIBRARY_FILE_ENTRY, (Serializable) file);

        frag.setArguments(args);
        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    public void replaceLoadCmdShareFragmentAsRoot(String filePath) {
        isLoadProtectFragment = false;
        isLoadShareFragment = true;
        CmdShareFragment frag = CmdShareFragment.newInstance();
        Bundle args = new Bundle();

        args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.COMMAND_SHARE_FROM_LIB);
        args.putString(Constant.LIBRARY_FILE_ENTRY, filePath);

        frag.setArguments(args);
        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    public void loadCmdShareFragmentAsRoot(String filePath) {
        isLoadProtectFragment = false;
        isLoadShareFragment = true;
        CmdShareFragment frag = CmdShareFragment.newInstance();
        Bundle args = new Bundle();

        args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.COMMAND_SHARE_FROM_LIB);
        args.putString(Constant.LIBRARY_FILE_ENTRY, filePath);

        frag.setArguments(args);
        loadRootFragment(R.id.fl_container, frag);
    }

    public void replaceLoadCmdShareFragmentAsRoot(BoundService boundService, INxFile destFolder, String filePath) {
        isLoadProtectFragment = false;
        isLoadShareFragment = true;
        CmdShareFragment frag = CmdShareFragment.newInstance();
        Bundle args = new Bundle();

        args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.COMMAND_SHARE_THEN_ADD);
        args.putSerializable(Constant.BOUND_SERVICE, boundService);
        args.putSerializable(Constant.DEST_FOLDER, (Serializable) destFolder);
        args.putString(Constant.LIBRARY_FILE_ENTRY, filePath);

        frag.setArguments(args);
        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    public void replaceLoadCmdShareFragmentAsRoot(INxFile file) {
        CmdShareFragment frag = CmdShareFragment.newInstance();
        Bundle args = new Bundle();

        args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.COMMAND_SHARE_FROM_REPO);
        args.putSerializable(Constant.LIBRARY_FILE_ENTRY, (Serializable) file);

        frag.setArguments(args);
        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    public void replaceLoadCmdAddFileFragmentAsRoot(String filePath) {
        CmdAddFragment frag = CmdAddFragment.newInstance();

        Bundle args = new Bundle();
        args.putString(Constant.LIBRARY_FILE_ENTRY, filePath);
        frag.setArguments(args);
        frag.setUserVisibleHint(true);

        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    public void loadCmdAddFileFragmentAsRoot(String filePath) {
        CmdAddFragment frag = CmdAddFragment.newInstance();

        Bundle args = new Bundle();
        args.putString(Constant.LIBRARY_FILE_ENTRY, filePath);
        frag.setArguments(args);
        frag.setUserVisibleHint(true);

        loadRootFragment(R.id.fl_container, frag);
    }

    private boolean resolveIntent() {
        Intent i = getIntent();
        if (i == null) {
            return false;
        }
        mAction = i.getAction();

        return true;
    }

    private boolean isShareFromLibrary() {
        return Constant.ACTION_SHARE_FROM_LIBRARY.equals(mAction);
    }

    private boolean isProtectFromLibrary() {
        return Constant.ACTION_PROTECT_FROM_LIBRARY.equals(mAction);
    }

    private boolean isShareFromRepo() {
        return Constant.ACTION_SHARE_FROM_REPO.equals(mAction);
    }

    private boolean isProtectFromRepo() {
        return Constant.ACTION_PROTECT_FROM_REPO.equals(mAction);
    }

    private boolean isSelectPathFromRepo() {
        return Constant.ACTION_SELECT_PATH_FROM_REPO.equals(mAction);
    }

    private boolean isMySpaceAddFile() {
        return Constant.ACTION_MYSPACE_ADD_FILE.equals(mAction);
    }

    private boolean isProtectFile() {
        return Constant.ACTION_PROTECT.equals(mAction);
    }

    private boolean isShareFile() {
        return Constant.ACTION_SHARE.equals(mAction);
    }

    private boolean isMySpaceProtectByScanDoc() {
        return Constant.ACTION_MYSPACE_PROTECT_FROM_SCAN_DOC.equals(mAction);
    }

    private boolean isProjectProtectByScanDoc() {
        return Constant.ACTION_PROJECT_PROTECT_FROM_SCAN_DOC.equals(mAction);
    }

    private boolean isWorkSpaceProtectByScanDoc() {
        return Constant.ACTION_WORKSPACE_PROTECT_FROM_SCAN_DOC.equals(mAction);
    }

    private boolean isMySpaceCreateFolderSelectPath() {
        return Constant.ACTION_MYSPACE_CREATE_FOLDER_SELECT_PATH.equals(mAction);
    }

    private void loadProjectAddFileFragment() {
        ProjectAddFileFragment addFileFrag = ProjectAddFileFragment.newInstance();
        Bundle args = new Bundle();

        args.putString(Constant.KEY_ACTION_BELONG_ADD_FILE_FRAG, Constant.VALUE_ADD_FILE_FROM_WORKSPACE);
        args.putString(Constant.PROJECT_PARENT_PATH_ID, mParentPathId);
        args.putParcelable(Constant.PROJECT_DETAIL, (Parcelable) mProject);
        args.putSerializable(Constant.LIBRARY_FILE_ENTRY, (Serializable) mClickFileItem);
        addFileFrag.setArguments(args);

        loadRootFragment(R.id.fl_container, addFileFrag);
    }

    private void loadProjectAddFileFragmentByScan(Intent i) {
        if (i == null) {
            return;
        }
        ProjectAddFileFragment addFileFrag = ProjectAddFileFragment.newInstance();
        Bundle args = new Bundle();

        String parentPathId = i.getStringExtra(Constant.PROJECT_PARENT_PATH_ID);
        IProject project = i.getParcelableExtra(Constant.PROJECT_DETAIL);
        String filePath = i.getStringExtra(Constant.LIBRARY_FILE_ENTRY);

        args.putString(Constant.KEY_ACTION_BELONG_ADD_FILE_FRAG, Constant.VALUE_ADD_FILE_FROM_SCAN_DOC);
        args.putString(Constant.PROJECT_PARENT_PATH_ID, parentPathId);
        args.putParcelable(Constant.PROJECT_DETAIL, (Parcelable) project);
        args.putString(Constant.LIBRARY_FILE_ENTRY, filePath);
        addFileFrag.setArguments(args);

        loadRootFragment(R.id.fl_container, addFileFrag);
    }

    private void loadProtectFragmentAsRoot(Intent i) {
        if (i == null) {
            return;
        }
        String filePath = i.getStringExtra(Constant.LIBRARY_FILE_ENTRY);
        String pathId = i.getStringExtra(Constant.NAME_CURRENT_PATH_ID);
        IProtectService service = i.getParcelableExtra(Constant.PROTECT_SERVICE);

        ProtectFragment frag = ProtectFragment.newInstance();

        Bundle args = new Bundle();
        args.putString(Constant.KEY_ACTION_BELONG_ADD_FILE_FRAG, Constant.ACTION_WORKSPACE_PROTECT_FROM_SCAN_DOC);
        args.putParcelable(Constant.PROTECT_SERVICE, (Parcelable) service);
        args.putParcelable(Constant.PROTECT_FILE_ENTRY, (Parcelable) makeFile(new File(filePath)));
        args.putString(Constant.NAME_CURRENT_PATH_ID, pathId);
        frag.setArguments(args);

        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

//    @Deprecated
//    private void loadLibraryFileFragmentAsRoot() {
//        final LibraryFileFragment frag = LibraryFileFragment.newInstance();
//        frag.setOnItemClickListener(new LibraryFileFragment.OnItemClickListener() {
//            @Override
//            public void onItemClick(INxlFile f, int pos) {
//                if (isProtectFromLibrary()) {
//                    replaceLoadCmdProtectFragmentAsRoot(f.getPathId());
//                }
//                if (isShareFromLibrary()) {
//                    replaceLoadCmdShareFragmentAsRoot(f.getPathId());
//                }
//                if (isMySpaceAddFile()) {
//                    replaceLoadCmdAddFileFragmentAsRoot(f.getPathId());
//                }
//            }
//        });
//        frag.setOnPopupFragmentListener(new LibraryFileFragment.OnPopupFragmentListener() {
//            @Override
//            public void onPopup() {
//                finish();
//            }
//        });
//        frag.setUserVisibleHint(true);
//        loadRootFragment(R.id.fl_container, frag);
//    }

    public void openMediaStore() {
        Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getRootDirectory().getPath());
        chooser.addCategory(Intent.CATEGORY_OPENABLE);
        chooser.setDataAndType(uri, "*/*");
        try {
            startActivityForResult(chooser, REQUEST_CODE_SELECT_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            ToastUtil.showToast(this, "Please install a File Manager.");
        }
    }

    private void loadRepositoryFileFragmentAsRoot(Intent i) {
        RepoFileFragment frag = RepoFileFragment.newInstance();

        frag.setOnRepoFileItemClickListener(new RepoFileFragment.OnRepoFileItemClickListener() {
            @Override
            public void onItemClick(INxFile f) {
                if (isProtectFromRepo()) {
                    replaceLoadCmdProtectFragmentAsRoot(f);
                }
                if (isShareFromRepo()) {
                    replaceLoadCmdShareFragmentAsRoot(f);
                }
            }
        });
        frag.setOnPopupFragmentListener(new RepoFileFragment.OnPopupFragmentListener() {
            @Override
            public void onPopup() {
                finish();
            }
        });
        Bundle args = new Bundle();
        BoundService boundService = (BoundService) i.getSerializableExtra(Constant.BOUND_SERVICE);
        args.putSerializable(Constant.BOUND_SERVICE, boundService);
        if (isSelectPathFromRepo() || isMySpaceCreateFolderSelectPath()) {
            args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.COMMAND_SELECT_PATH);
        }
        frag.setUserVisibleHint(true);
        frag.setArguments(args);

        loadRootFragment(R.id.fl_container, frag);
    }

    private void loadCmdProtectFragmentAsRoot(Intent i) {
        if (i == null) {
            return;
        }
        INxFile file = (INxFile) i.getSerializableExtra(Constant.LIBRARY_FILE_ENTRY);
        CmdProtectFragment frag = CmdProtectFragment.newInstance();
        Bundle args = new Bundle();

        args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.PROTECT);
        args.putSerializable(Constant.LIBRARY_FILE_ENTRY, (Serializable) file);

        frag.setArguments(args);
        loadRootFragment(R.id.fl_container, frag);
    }

    private void loadCmdProtectFragmentAsRootByScan(Intent i) {
        if (i == null) {
            return;
        }
        String filePath = i.getStringExtra(Constant.LIBRARY_FILE_ENTRY);
        CmdProtectFragment frag = CmdProtectFragment.newInstance();
        Bundle args = new Bundle();

        args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.COMMAND_SCAN);
        args.putString(Constant.LIBRARY_FILE_ENTRY, filePath);

        frag.setArguments(args);
        loadRootFragment(R.id.fl_container, frag);
    }

    private void loadCmdShareFragmentAsRoot(Intent i) {
        if (i == null) {
            return;
        }
        INxFile file = (INxFile) i.getSerializableExtra(Constant.LIBRARY_FILE_ENTRY);
        CmdShareFragment frag = CmdShareFragment.newInstance();
        Bundle args = new Bundle();

        args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.SHARE);
        args.putSerializable(Constant.LIBRARY_FILE_ENTRY, (Serializable) file);

        frag.setArguments(args);
        loadRootFragment(R.id.fl_container, frag);
    }

    public void loadCmdAddFileFragmentAsRoot(Intent i) {
        if (i == null) {
            return;
        }
        String filePath = i.getStringExtra(Constant.LIBRARY_FILE_ENTRY);
        CmdAddFragment frag = CmdAddFragment.newInstance();

        Bundle args = new Bundle();
        args.putString(Constant.LIBRARY_FILE_ENTRY, filePath);
        args.putSerializable(Constant.CMD_OPERATE_TYPE, CmdOperate.COMMAND_SCAN);
        frag.setArguments(args);
        frag.setUserVisibleHint(true);

        loadRootFragment(R.id.fl_container, frag);
    }

    private void updateContactParcel(Intent data) {
        CmdShareFragment shareFrag = findChildFragment(CmdShareFragment.class);
        if (shareFrag != null) {
            shareFrag.updateContactParcel(data);
        }
    }

    private IProtectFile makeFile(File f) {
        return new LocalProtectFile(f);
    }
}
