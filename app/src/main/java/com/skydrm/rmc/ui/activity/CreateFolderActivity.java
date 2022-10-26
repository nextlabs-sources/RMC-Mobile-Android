package com.skydrm.rmc.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.datalayer.repo.project.Project;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.FolderCreateEvent;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.exception.FolderCreateException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.widget.animation.ShakeAnimator;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.workspace.WorkSpaceLibraryActivity;
import com.skydrm.rmc.ui.project.feature.files.view.ProjectLibraryActivity;
import com.skydrm.rmc.ui.service.createfolder.CreateFolderTask;
import com.skydrm.rmc.ui.service.createfolder.ICreateFolderService;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;

/**
 * Created by hhu on 5/15/2017.
 */

public class CreateFolderActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_save)
    TextView mTvSave;

    @BindView(R.id.root_layout)
    LinearLayout mRootLayout;

    @BindView(R.id.textInputLayout)
    TextInputLayout mTextInputLayout;
    @BindView(R.id.folder_name)
    EditText mEtInputName;
    @BindView(R.id.tv_tip)
    TextView tv_tip;
    @BindView(R.id.tv_path)
    TextView mTvFileLocation;
    @BindView(R.id.tv_change)
    TextView mTvChange;


    private static int MYSPACE_CHANGE_PATH_REQUEST_CODE = 0x01;
    private static int PROJECT_CHANGE_PATH_REQUEST_CODE = 0x02;
    private static int WORKSPACE_CHANGE_PATH_REQUEST_CODE = 0x03;

    private BoundService mBoundService;
    private INxFile mParentFolder;
    private ShakeAnimator mShakeAnimator;
    private String mAction;

    private IProject mProject;
    private String mParentPathId;

    private ProgressDialog mProgressDialog;

    private CreateProjectFolderCallback mCreateProjectFolderCallback;
    private ICreateFolderService mService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_folder);
        ButterKnife.bind(this);
        if (resolveIntent()) {
            initViewAndEvents();
        } else {
            finish();
        }
    }

    private void selectProjectFolderPath() {
        Intent intent = new Intent(this, ProjectLibraryActivity.class);
        intent.setAction(Constant.ACTION_NEW_FOLDER_FROM_PROJECT);
        intent.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mProject);
        startActivityForResult(intent, PROJECT_CHANGE_PATH_REQUEST_CODE);
    }

    private void selectMySpaceFolderPath2() {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_MYSPACE_CREATE_FOLDER_SELECT_PATH);
        intent.putExtra(Constant.BOUND_SERVICE, CommonUtils.getDefaultUploadedBoundService());
        intent.setClass(CreateFolderActivity.this, CmdOperateFileActivity2.class);
        startActivityForResult(intent, MYSPACE_CHANGE_PATH_REQUEST_CODE);
    }

    private void reSelectWorkSpaceParenFolder() {
        Intent intent = new Intent(this, WorkSpaceLibraryActivity.class);
        intent.setAction(Constant.ACTION_NEW_FOLDER_FROM_WORKSPACE);
        startActivityForResult(intent, WORKSPACE_CHANGE_PATH_REQUEST_CODE);
    }

    public void showLoading() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(CreateFolderActivity.this, "", getString(R.string.c_Loading_with3dots));
        }
    }

    public void dismissLoading() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public void setUpFolderCreateLocation(BoundService boundService, INxFile parentFolder) {
        if (boundService == null || parentFolder == null) {
            return;
        }
        mTvFileLocation.setText(String.format(Locale.getDefault(), "%s %s", boundService.getDisplayName(),
                parentFolder.getDisplayPath()));
    }

    public void setUpFolderCreateLocation(String projectName, String parentPathId) {
        if (parentPathId == null || parentPathId.isEmpty()) {
            return;
        }
        mTvFileLocation.setText(String.format(Locale.getDefault(),
                "%s%s", projectName, parentPathId));
    }

    public void setupWorkSpaceFolderCreateDisplayPath(String parentPathId) {
        mTvFileLocation.setText(parentPathId);
    }

    private void saveImpl() {
        //
        // as qa required, folderName should be trimmed
        //
        final String folderName = mEtInputName.getText().toString().trim();
        mEtInputName.setText(folderName);

        if (TextUtils.isEmpty(folderName)) {
            //error hint when folder name is empty.
            mShakeAnimator.startAnimation();
            mTextInputLayout.setError(getString(R.string.folder_name_is_empty));
            return;
        }

        if (!isValidFolderName(folderName)) {
            mShakeAnimator.startAnimation();
            return;
        }

        if (isProjectCreateFolderIntent()) {
            createFolder((Project) mProject, folderName);
        }

        if (isWorkSpaceCreateFolderIntent()) {
            createFolder(mService, folderName);
        }

        if (isMySpaceCreateFolderIntent()) {
            if (mBoundService == null || mParentFolder == null) {
                log.e("Error happened in CreateFolderActivity:boundService == null");
                return;
            }
            ExecutorPools.SelectSmartly(FIRED_BY_UI).execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        SkyDRMApp.getInstance()
                                .getRepoSystem()
                                .createFolder(mBoundService, mParentFolder, folderName);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                EventBus.getDefault().post(new FolderCreateEvent(SkyDRMApp.getInstance()
                                        .getRepoSystem()
                                        .isInSyntheticRoot(), folderName));
                                dismissLoading();
                                finish();
                            }
                        });
                    } catch (final FolderCreateException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissLoading();
                                switch (e.getErrorCode()) {
                                    default:
                                        ToastUtil.showToast(getApplicationContext(), getString(R.string.hing_msg_create_folder_failed));
                                        break;
                                    case NamingCollided:
                                        // prompt user to change another name and try again
                                        ToastUtil.showToast(getApplicationContext(), e.getMessage());
                                    case NamingViolation:
                                        // name invalied,
                                        ToastUtil.showToast(getApplicationContext(), e.getMessage());
                                        break;
                                    case AuthenticationFailed:
                                        SkyDRMApp.getInstance().getSession().sessionExpiredHandler(CreateFolderActivity.this);
                                        break;
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void createFolder(ICreateFolderService service, String name) {
        new CreateFolderTask(service, mParentPathId, name, false,
                mCreateProjectFolderCallback = new CreateProjectFolderCallback()).run();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // mySpace change path results
        if (requestCode == MYSPACE_CHANGE_PATH_REQUEST_CODE && resultCode == RESULT_OK) {
            parseResultFromMySpaceSelectPath(data);
        }
        if (requestCode == PROJECT_CHANGE_PATH_REQUEST_CODE && resultCode == RESULT_OK) {
            parseResultFromProjectSelectPath(data);
        }
        if (requestCode == WORKSPACE_CHANGE_PATH_REQUEST_CODE && resultCode == RESULT_OK) {
            parseResultFromWorkSpaceSelectPath(data);
        }
    }

    private void parseResultFromMySpaceSelectPath(Intent data) {
        Bundle extras = data.getExtras();
        if (extras == null) {
            return;
        }
        mBoundService = (BoundService) extras.getSerializable(Constant.BOUND_SERVICE);
        mParentFolder = (INxFile) extras.getSerializable(Constant.DEST_FOLDER);
        setUpFolderCreateLocation(mBoundService, mParentFolder);
    }

    private void parseResultFromProjectSelectPath(Intent data) {
        if (data == null) {
            return;
        }
        mParentPathId = data.getStringExtra(Constant.PROJECT_PARENT_PATH_ID);
        setUpFolderCreateLocation(mProject == null ? getResources().getString(R.string.Project) :
                mProject.getName(), mParentPathId);
    }

    private void parseResultFromWorkSpaceSelectPath(Intent data) {
        mParentPathId = data.getStringExtra(Constant.CREATE_FOLDER_PARENT_PATH_ID);
        setupWorkSpaceFolderCreateDisplayPath(mParentPathId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCreateProjectFolderCallback != null) {
            mCreateProjectFolderCallback = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (mEtInputName == null) {
            return;
        }
        mEtInputName.clearFocus();
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mEtInputName.getWindowToken(), 0);
        }
    }

    public boolean isValidFolderName(String folderName) {
        String regularExpression = "^[\\u00C0-\\u1FFF\\u2C00-\\uD7FF\\w \\x22\\x23\\x27\\x2C\\x2D]+$";
        Pattern pattern = Pattern.compile(regularExpression);
        Matcher matcher = pattern.matcher(folderName);
        return matcher.matches();
    }

    private boolean resolveIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return false;
        }
        String action = intent.getAction();
        if (action == null || action.isEmpty()) {
            return false;
        }
        this.mAction = action;
        if (action.equals(Constant.ACTION_NEW_FOLDER_FROM_HOME)) {
            return resolveHomeBundle(intent);
        }
        if (action.equals(Constant.ACTION_NEW_FOLDER_FROM_PROJECT)) {
            return resolveProjectBundle(intent);
        }
        if (action.equals(Constant.ACTION_NEW_FOLDER_FROM_WORKSPACE)) {
            return resolveWorkSpaceBundle(intent);
        }
        return false;
    }

    private boolean resolveHomeBundle(Intent i) {
        if (i == null) {
            return false;
        }
        Bundle extras = i.getExtras();
        if (extras == null) {
            return false;
        }
        mBoundService = (BoundService) extras.getSerializable(Constant.BOUND_SERVICE);
        mParentFolder = (INxFile) extras.getSerializable(Constant.DEST_FOLDER);
        return true;
    }

    private boolean resolveProjectBundle(Intent i) {
        if (i == null) {
            return false;
        }
        Bundle extras = i.getExtras();
        if (extras == null) {
            return false;
        }
        mParentPathId = extras.getString(Constant.PROJECT_PARENT_PATH_ID);
        mProject = extras.getParcelable(Constant.PROJECT_DETAIL);
        return true;
    }

    private boolean resolveWorkSpaceBundle(Intent i) {
        if (i == null) {
            return false;
        }
        Bundle extras = i.getExtras();
        if (extras == null) {
            return false;
        }
        mService = extras.getParcelable(Constant.CREATE_FOLDER_SERVICE);
        mParentPathId = extras.getString(Constant.CREATE_FOLDER_PARENT_PATH_ID);
        return true;
    }

    private boolean isProjectCreateFolderIntent() {
        return Constant.ACTION_NEW_FOLDER_FROM_PROJECT.equals(mAction);
    }

    private boolean isMySpaceCreateFolderIntent() {
        return Constant.ACTION_NEW_FOLDER_FROM_HOME.equals(mAction);
    }

    private boolean isWorkSpaceCreateFolderIntent() {
        return Constant.ACTION_NEW_FOLDER_FROM_WORKSPACE.equals(mAction);
    }

    private void initViewAndEvents() {
        //init the hint animator.
        if (mShakeAnimator == null) {
            mShakeAnimator = new ShakeAnimator();
            mShakeAnimator.setTarget(mEtInputName);
        }
        //init the display text for folder change
        tv_tip.setText(getString(R.string.folder_create_tip));
        if (isProjectCreateFolderIntent()) {
            setUpFolderCreateLocation(mProject == null ? getResources().getString(R.string.Project) :
                    mProject.getName(), mParentPathId);
        }
        if (isMySpaceCreateFolderIntent()) {
            setUpFolderCreateLocation(mBoundService, mParentFolder);
        }
        if (isWorkSpaceCreateFolderIntent()) {
            setupWorkSpaceFolderCreateDisplayPath(mParentPathId);
        }
        mEtInputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isValidFolderName(s.toString())) {
                    //error hint when folder name does not match the regex
                    mTextInputLayout.setError(getString(R.string.folder_name_rules));
                } else {
                    mTextInputLayout.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mTvChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isProjectCreateFolderIntent()) {
                    selectProjectFolderPath();
                }
                if (isMySpaceCreateFolderIntent()) {
                    //Create folder function only supported in myDrive.
                    selectMySpaceFolderPath2();
                }
                if (isWorkSpaceCreateFolderIntent()) {
                    reSelectWorkSpaceParenFolder();
                }
            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEtInputName != null) {
                    hideSoftInput(mEtInputName);
                }
                saveImpl();
            }
        });
    }

    class CreateProjectFolderCallback implements CreateFolderTask.ITaskCallback<CreateFolderTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            showLoading();
        }

        @Override
        public void onTaskExecuteSuccess(CreateFolderTask.Result results) {
            ToastUtil.showToast(getApplicationContext(), "Create folder success.");
            dismissLoading();
            finish();
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            dismissLoading();
            ExceptionHandler.handleException(CreateFolderActivity.this, e);
        }
    }
}
