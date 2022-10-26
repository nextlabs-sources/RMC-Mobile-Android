package com.skydrm.rmc.ui.project.common;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.Utils;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.activity.CreateFolderActivity;
import com.skydrm.rmc.ui.activity.home.PhotographMsg;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.base.BaseContextMenu;
import com.skydrm.rmc.ui.project.feature.configuration.task.GetMetadataTask;
import com.skydrm.rmc.ui.project.feature.member.InvitationMsg;
import com.skydrm.rmc.ui.project.feature.member.task.InviteMemberTask;
import com.skydrm.rmc.ui.project.service.ProjectOperateActivity;
import com.skydrm.rmc.ui.project.service.ViewAllProjectsActivity;
import com.skydrm.rmc.ui.service.contact.ContactActivity;
import com.skydrm.rmc.ui.service.protect.ProtectActivity;
import com.skydrm.rmc.ui.widget.animation.ShakeAnimator;
import com.skydrm.rmc.ui.widget.customcontrol.CommentWidget;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.rms.rest.project.GetProjectMetadataResult;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.skydrm.rmc.domain.Constant.REQUEST_CODE_SELECT_EMAILS;

public class ProjectContextMenu extends BaseContextMenu {
    private Context mCtx;

    private IProject mProject;

    private String mRootPathId = "/";
    private String mCurrentPathId;

    private AlertDialog mInviteDialog;
    private EditText mEtInviteEmailAddress;
    private FlowLayout mFlEmailList;
    private ImageButton mIbDismiss;
    private Button mBtInvite;
    private CommentWidget mCommentWidget;
    private String mInvitationMsg;

    private InviteCallback mInviteCallback;
    private GetProjectMetadataCallback mGetMetadataCallback;
    private boolean hideAddFileMenuItem;
    private boolean hideCreateNewFolderMenuItem;
    private boolean hideScanDocMenuItem;
    private boolean hideGoToAllProjectsMenuItem;

    private ShakeAnimator mShakeAnimator;
    private TextInputLayout mTilEditText;

    private boolean showErrorHint;

    public static ProjectContextMenu newInstance() {
        return new ProjectContextMenu();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mCtx = context;
    }

    public void bindContext(Context ctx) {
        this.mCtx = ctx;
    }

    public void setProject(IProject project) {
        this.mProject = project;
    }

    public void setCurrentPathId(String currentPathId) {
        this.mCurrentPathId = currentPathId;
    }

    public void setHideAddFileMenuItem(boolean hide) {
        this.hideAddFileMenuItem = hide;
    }

    public void setHideCreateNewFolderMenuItem(boolean hide) {
        this.hideCreateNewFolderMenuItem = hide;
    }

    public void setHideScanDocMenuItem(boolean hide) {
        this.hideScanDocMenuItem = hide;
    }

    public void setHideGoToAllProjectsMenuItem(boolean hide) {
        this.hideGoToAllProjectsMenuItem = hide;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_project_context_menu,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View goToAllProjects = view.findViewById(R.id.ll_menu_go_to_all_project);
        View divider = view.findViewById(R.id.ll_divider);
        View addFile = view.findViewById(R.id.ll_menu_add_file);
        View inviteMember = view.findViewById(R.id.ll_menu_invite_members);
        View createNewFolder = view.findViewById(R.id.ll_menu_create_new_folder);
        View scanDoc = view.findViewById(R.id.ll_menu_scan_a_document);

        if (hideGoToAllProjectsMenuItem) {
            goToAllProjects.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
        if (hideAddFileMenuItem) {
            addFile.setVisibility(View.GONE);
        }
        if (hideCreateNewFolderMenuItem) {
            createNewFolder.setVisibility(View.GONE);
        }
        if (hideScanDocMenuItem) {
            scanDoc.setVisibility(View.GONE);
        }

        if (!hideGoToAllProjectsMenuItem) {
            goToAllProjects.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToAllProjectsPage();
                    dismiss();
                }
            });
        }
        if (!hideAddFileMenuItem) {
            addFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lunchAddFilePage();

                    dismiss();
                }
            });
        }

        inviteMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInviteDialog();
            }
        });

        if (!hideCreateNewFolderMenuItem) {
            createNewFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    createNewFolder();
                }
            });
        }

        if (!hideScanDocMenuItem) {
            scanDoc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    scanDoc();
                }
            });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constant.REQUEST_CODE_SELECT_EMAILS) {
            wrapEmails(data);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mInviteCallback != null) {
            mInviteCallback = null;
        }
        if (mGetMetadataCallback != null) {
            mGetMetadataCallback = null;
        }
        if (mInviteDialog != null) {
            mInviteDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mInviteCallback != null) {
            mInviteCallback = null;
        }
        if (mGetMetadataCallback != null) {
            mGetMetadataCallback = null;
        }
        if (mInviteDialog != null) {
            mInviteDialog = null;
        }
    }

    private void goToAllProjectsPage() {
        Intent intent = new Intent(mCtx, ViewAllProjectsActivity.class);
        startActivity(intent);
    }

    private void lunch2RepoSelectPage() {
        Intent i = new Intent(mCtx, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_LUNCH_REPOSITORY_SELECT_FRAGMENT);
        i.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mProject);
        i.putExtra(Constant.NAME_ROOT_PATH_ID, mRootPathId);
        i.putExtra(Constant.NAME_CURRENT_PATH_ID, mCurrentPathId);
        startActivity(i);
    }

    private void lunchAddFilePage() {
        dismiss();

        Intent i = new Intent(mCtx, ProtectActivity.class);
        i.putExtra(Constant.PROTECT_SERVICE, (Parcelable) mProject);
        i.putExtra(Constant.NAME_ROOT_PATH_ID, "/");
        i.putExtra(Constant.NAME_CURRENT_PATH_ID, mCurrentPathId);
        mCtx.startActivity(i);
    }

    public void showInviteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        RelativeLayout root = (RelativeLayout) LayoutInflater.from(mCtx).inflate(R.layout.people_invite_dialog_layout3, null);
        initPeopleInviteDialogView(root);
        initPeopleInviteDialogListener();
        builder.setView(root);
        mInviteDialog = builder.create();
        mInviteDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                boolean b = false;
                if (keyCode == KeyEvent.KEYCODE_ENTER && KeyEvent.ACTION_DOWN == event.getAction()) {
                    if (null != mEtInviteEmailAddress && !TextUtils.isEmpty(mEtInviteEmailAddress.getText().toString())) {
                        mFlEmailList.wrapEmail(mEtInviteEmailAddress.getText().toString(), true);
                    }
                    b = true;
                }
                return b;
            }
        });
        mInviteDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                hindSoftKeyBoard();
                mFlEmailList.clearEmailList();
            }
        });
        //mInviteDialog.setCanceledOnTouchOutside(true);
        mInviteDialog.show();

        new GetMetadataTask(mProject, mGetMetadataCallback).run();
    }

    private void createNewFolder() {
        Intent i = new Intent(mCtx, CreateFolderActivity.class);
        i.setAction(Constant.ACTION_NEW_FOLDER_FROM_PROJECT);
        if (TextUtils.isEmpty(mCurrentPathId)) {
            i.putExtra(Constant.PROJECT_PARENT_PATH_ID, mRootPathId);
        } else {
            i.putExtra(Constant.PROJECT_PARENT_PATH_ID, mCurrentPathId);
        }
        i.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mProject);
        mCtx.startActivity(i);
    }

    private void scanDoc() {
        final BaseActivity launcher = (BaseActivity) mCtx;
        launcher.checkPermission(new BaseActivity.CheckPermissionListener() {
                                     @Override
                                     public void superPermission() {
                                         Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                         String name = System.currentTimeMillis() + ".jpg";
                                         File imageFile = new File(getTmpMountPoint(), name);

                                         EventBus.getDefault().post(new PhotographMsg(imageFile));
                                         cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                                 FileProvider.getUriForFile(launcher,
                                                         mCtx.getPackageName() + ".fileprovider",
                                                         imageFile));
                                         cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                                         launcher.startActivityForResult(cameraIntent, Constant.PROJECT_ALBUM_REQUEST_CODE);
                                     }

                                     @Override
                                     public void onPermissionDenied() {
                                         List<String> permission = new ArrayList<>();
                                         permission.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                                         permission.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                         launcher.checkPermissionNeverAskAgain(null, permission);
                                     }
                                 }, R.string.permission_storage_rationale,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private File getTmpMountPoint() {
        try {
            return Utils.getTmpMountPoint();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void hindSoftKeyBoard() {
        // hidden soft-keyboard
        InputMethodManager imm = (InputMethodManager) mCtx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            // the first para is windowToken, which can be any current view's window token.
            imm.hideSoftInputFromWindow(mBtInvite.getWindowToken(), 0);
        }
    }

    private void initPeopleInviteDialogView(RelativeLayout root) {
        mIbDismiss = root.findViewById(R.id.invited_member_back);
        mBtInvite = root.findViewById(R.id.invite_people);
        mFlEmailList = root.findViewById(R.id.people_invited_flowLayout);
        mTilEditText = root.findViewById(R.id.editText_textInputLayout);
        mEtInviteEmailAddress = root.findViewById(R.id.people_invited_et_email_address);
        mEtInviteEmailAddress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Drawable drawable = mEtInviteEmailAddress.getCompoundDrawables()[2];
                if (drawable == null) return false;
                if (motionEvent.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (motionEvent.getX() > mEtInviteEmailAddress.getWidth() - mEtInviteEmailAddress.getPaddingLeft() - drawable.getIntrinsicWidth()) {
                    mEtInviteEmailAddress.setFocusableInTouchMode(false);
                    mEtInviteEmailAddress.setFocusable(false);
                    lunchContactPageWithResult(mEtInviteEmailAddress.getContext());
                } else {
                    mEtInviteEmailAddress.setFocusableInTouchMode(true);
                    mEtInviteEmailAddress.setFocusable(true);
                }
                return false;
            }
        });
        mCommentWidget = root.findViewById(R.id.comment_widget);
        if (mCommentWidget.isEditTextEqualToEmpty() && !TextUtils.isEmpty(mInvitationMsg)) {
            mCommentWidget.setEditText(mInvitationMsg);
        }
        mFlEmailList.setOnClearInputEmailListener(new FlowLayout.OnClearInputEmailListener() {
            @Override
            public void onClear() {
                mEtInviteEmailAddress.setText("");
            }
        });
        mShakeAnimator = new ShakeAnimator();
        mShakeAnimator.setTarget(mEtInviteEmailAddress);
    }

    protected void lunchContactPageWithResult(Context ctx) {
        Intent i = new Intent(ctx, ContactActivity.class);
        if (isAdded()) {
            startActivityForResult(i, REQUEST_CODE_SELECT_EMAILS);
        } else {
            ((FragmentActivity) ctx).startActivityForResult(i, REQUEST_CODE_SELECT_EMAILS);
        }
    }

    public void wrapEmails(Intent data) {
        mFlEmailList.wrapEmailFromContact(data);
    }

    private void initPeopleInviteDialogListener() {
        if (mInviteCallback == null) {
            mInviteCallback = new InviteCallback();
        }
        if (mGetMetadataCallback == null) {
            mGetMetadataCallback = new GetProjectMetadataCallback();
        }

        mIbDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissInviteDialog();
            }
        });
        mBtInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteMember();
            }
        });
        if (mEtInviteEmailAddress != null) {
            mEtInviteEmailAddress.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (showErrorHint) {
                        mTilEditText.setError("");
                        showErrorHint = false;
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    mFlEmailList.wrapEmail(s.toString(), false);
                }
            });
        }
    }

    private void inviteMember() {
        // get valid emails
        if (!TextUtils.isEmpty(mEtInviteEmailAddress.getText().toString())) {
            mFlEmailList.wrapEmail(mEtInviteEmailAddress.getText().toString(), true);
        }
        List<String> validEmails = mFlEmailList.getEmailList();
        if (null == validEmails || validEmails.size() == 0) {
            hintUserEmptyEmailInput(true, "Empty email");
            return;
        }
        for (String s : validEmails) {
            if (!CommonUtils.checkEmailAddress(s)) {
                hintUserEmptyEmailInput(true, "Email:" + s + " is invalid.");
                return;
            }
        }
        String msg = mCommentWidget.getText().toString();
        new InviteMemberTask(mProject, validEmails, msg, mInviteCallback).run();
    }

    private void dismissInviteDialog() {
        if (mInviteDialog != null && mInviteDialog.isShowing()) {
            mInviteDialog.dismiss();
            mInviteDialog = null;
        }
    }

    public void hintUserEmptyEmailInput(boolean animate, String msg) {
        showErrorHint = true;
        if (mTilEditText == null) {
            return;
        }
        if (mShakeAnimator == null) {
            return;
        }
        mTilEditText.setError(msg);
        if (!mEtInviteEmailAddress.isFocused()) {
            mEtInviteEmailAddress.requestFocus();
        }
        if (animate) {
            mShakeAnimator.startAnimation();
        }
    }

    class InviteCallback implements InviteMemberTask.ITaskCallback<InviteMemberTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(InviteMemberTask.Result results) {
            dismissInviteDialog();
            if (isAdded()) {
                dismiss();
            }
            String msg = results.msg;
            if (msg != null && !msg.isEmpty()) {
                ToastUtil.showToast(mCtx.getApplicationContext(), msg);
            }
            EventBus.getDefault().post(new InvitationMsg());
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            ExceptionHandler.handleException(mCtx, e);
            dismissInviteDialog();
            if (isAdded()) {
                dismiss();
            }
        }
    }

    class GetProjectMetadataCallback implements GetMetadataTask.ITaskCallback<GetMetadataTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(GetMetadataTask.Result results) {
            GetProjectMetadataResult metadata = results.metadata;
            mInvitationMsg = metadata.getResults().getDetail().getInvitationMsg();
            if (null != mCommentWidget
                    && mCommentWidget.isEditTextEqualToEmpty()
                    && !TextUtils.isEmpty(mInvitationMsg)) {
                mCommentWidget.setEditText(mInvitationMsg);
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            ExceptionHandler.handleException(mCtx, e);
        }
    }
}
