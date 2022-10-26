package com.skydrm.rmc.ui.service.share;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.project.feature.centralpolicy.CentralRightsView;
import com.skydrm.rmc.ui.service.fileinfo.FileInfoTask;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.service.share.task.RevokingTask;
import com.skydrm.rmc.ui.service.share.task.SharingToProjectTask;
import com.skydrm.rmc.ui.service.share.task.UpdatingTask;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;
import com.skydrm.rmc.ui.widget.customcontrol.rights.ADHocRightsDisplayView;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.DensityHelper;
import com.skydrm.rmc.utils.commonUtils.IconHelper;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;

public class ShareFragment extends BaseFragment {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_size_value)
    TextView mTvFileSize;
    @BindView(R.id.tv_last_modified_time_value)
    TextView mTvLastModifyTime;
    @BindView(R.id.rl_place_holder)
    RelativeLayout mRlRightsPlaceHolder;
    @BindView(R.id.tv_revoke_rights)
    TextView mTvRevokeRights;
    @BindView(R.id.fl_sharing_container)
    FlowLayout mFlSharingContainer;
    @BindView(R.id.tv_add_more)
    TextView mTvAddMore;
    @BindView(R.id.tv_sharing_history)
    TextView mTvSharingHistory;
    @BindView(R.id.bt_update_sharing)
    Button mBtUpdateSharing;

    private ISharingService mService;
    private ISharingFile mFile;
    private Map<String, String> mRecipients = new HashMap<>();
    private Map<String, String> mNewRecipients = new HashMap<>();
    private Map<String, String> mRemovedRecipients = new HashMap<>();

    private GetFingerPrintCallback mGetFingerPrintCallback;
    private SharingCallback mSharingCallback;
    private UpdatingCallback mUpdatingCallback;
    private RevokingCallback mRevokingCallback;

    private LoadingDialog2 mLoadingDialog;
    private CentralRightsView mCentralRightsView;

    public static ShareFragment newInstance() {
        return new ShareFragment();
    }

    @Override
    protected void onUserFirstVisible() {

    }

    @Override
    protected void onUserVisible() {

    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected void initViewAndEvents() {
        if (!resolveData()) {
            finishParent();
            return;
        }
        mToolbar.setLogo(IconHelper.getNxlIconResourceIdByExtension(mFile.getName()));
        mToolbar.setTitle(mFile.getPathDisplay());
        mToolbar.setSubtitle(mFile.getName());
        mTvFileSize.setText(FileUtils.transparentFileSize(mFile.getFileSize()));
        mTvLastModifyTime.setText(TimeUtil.formatData(mFile.getLastModifiedTime()));

        mRecipients.putAll(getFilteredShareWithRecipients());
        mFlSharingContainer.wrapStringWithDrawable(getProjectDisplayRecipients());
        try {
            if (!mFile.isRevokeable() || mFile.isRevoked()) {
                mTvRevokeRights.setVisibility(View.GONE);
            }
        } catch (RmsRestAPIException
                | InvalidRMClientException
                | SessionInvalidException e) {
            e.printStackTrace();
            mTvRevokeRights.setVisibility(View.GONE);
        }
        if (!mFile.isShared()) {
            mTvRevokeRights.setVisibility(View.GONE);
        }
        tryReadNxlRights();
        initEvents();
    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_share;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveProjectRecipientsMsg(MsgProjectRecipients msg) {
        if (msg.mData == null) {
            return;
        }
        if (mFile.isShared()) {
            for (String name : msg.mData.keySet()) {
                if (mRecipients.containsKey(name)) {
                    continue;
                }
                if (mRemovedRecipients.containsKey(name)) {
                    mRemovedRecipients.remove(name);
                    continue;
                }
                String id = msg.mData.get(name);
                if (id == null || id.isEmpty()) {
                    continue;
                }
                mNewRecipients.put(name, id);
                mRecipients.put(name, id);
            }
            Iterator<String> it = mRecipients.keySet().iterator();
            while (it.hasNext()) {
                String name = it.next();
                if (msg.mData.containsKey(name)) {
                    continue;
                }
                String id = mRecipients.get(name);
                if (id == null || id.isEmpty()) {
                    continue;
                }
                if (mService.getId() == Integer.valueOf(id)
                        || !msg.mAllData.contains(Integer.valueOf(id))) {
                    continue;
                }
                mRemovedRecipients.put(name, id);
                it.remove();
            }
        } else {
            mRecipients.clear();
            mRecipients.putAll(msg.mData);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGetFingerPrintCallback != null) {
            mGetFingerPrintCallback = null;
        }
        if (mSharingCallback != null) {
            mSharingCallback = null;
        }
        if (mUpdatingCallback != null) {
            mUpdatingCallback = null;
        }
        if (mRevokingCallback != null) {
            mRevokingCallback = null;
        }
    }

    private boolean resolveData() {
        Bundle args = getArguments();
        if (args == null) {
            return false;
        }
        mService = args.getParcelable(Constant.SHARING_SERVICE);
        mFile = args.getParcelable(Constant.SHARING_ENTRY);
        return mService != null && mFile != null;
    }

    // read nxl file rights
    private void tryReadNxlRights() {
        FileInfoTask task = new FileInfoTask((NxlDoc) mFile, mGetFingerPrintCallback = new GetFingerPrintCallback());
        task.run();
    }

    private void initEvents() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });
        mTvRevokeRights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRevokeDialog();
            }
        });
        mTvAddMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // load shared with select fragment.
                if (_activity instanceof ShareActivity) {
                    ShareActivity activity = (ShareActivity) _activity;
                    List<String> defaultSelected = new ArrayList<>(mRecipients.values());
                    activity.replaceLoadProjectListFragAsRoot(mService.getId(), defaultSelected);
                }
            }
        });
        mBtUpdateSharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSharing();
            }
        });
        mFlSharingContainer.setOnEmailChangeListener(new FlowLayout.OnEmailChangeListener() {
            @Override
            public void onEmailAdded(String email) {

            }

            @Override
            public void onEmailRemoved(String email) {
                removeRecipients(email);
            }

            @Override
            public void onEmailAlreadyExists(String email) {

            }
        });
    }

    private void updateSharing() {
        if (mFile.isShared()) {
            // update recipients.
            List<String> newRecipients = getNewRecipients();
            List<String> removedRecipients = getRemovedRecipients();
            if (newRecipients.isEmpty() && removedRecipients.isEmpty()) {
                ToastUtil.showToast(_activity, getString(R.string.msg_hint_recipient_required));
                return;
            }
            UpdatingTask task = new UpdatingTask(mService, mFile,
                    newRecipients, removedRecipients, "",
                    mUpdatingCallback = new UpdatingCallback());
            task.run();
        } else {
            // share.
            List<Integer> recipients = getProjectRecipientsId();
            if (recipients.isEmpty()) {
                ToastUtil.showToast(_activity, getString(R.string.msg_hint_recipient_required));
                return;
            }
            SharingToProjectTask task = new SharingToProjectTask(mService, mFile,
                    recipients, "",
                    mSharingCallback = new SharingCallback());
            task.run();
        }
    }

    private void policyEvaluation(final INxlFileFingerPrint fingerPrint) {
        loading(true);
        if (mFile instanceof NxlDoc) {
            NxlDoc doc = (NxlDoc) mFile;
            doc.doPolicyEvaluation(fingerPrint, new IFileInfo.IPolicyCallback() {
                @Override
                public void onSuccess(List<String> rights, String obligations) {
                    loading(false);
                    if (mCentralRightsView != null) {
                        mCentralRightsView.paddingRights(rights, false);
                        mCentralRightsView.invalidate();
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    loading(false);
                    if (e instanceof RmsRestAPIException) {
                        RmsRestAPIException rmsRestAPIException = (RmsRestAPIException) e;
                        if (rmsRestAPIException.getDomain()
                                == RmsRestAPIException.ExceptionDomain.NOPOLICY_TO_EVALUATE) {
                            if (mCentralRightsView != null) {
                                mCentralRightsView.showNoPolicyTips();
                            }
                        } else {
                            if (mCentralRightsView != null) {
                                mCentralRightsView.showNoPolicyTips();
                            }
                            //ExceptionHandler.handleException(_activity, e);
                        }
                    } else {
                        if (mCentralRightsView != null) {
                            mCentralRightsView.showNoPolicyTips();
                        }
                        //ExceptionHandler.handleException(_activity, e);
                    }
                }
            });
        }
    }

    private void initADHocRightsView(INxlFileFingerPrint fp) {
        ADHocRightsDisplayView rightsView = new ADHocRightsDisplayView(_activity);
        mRlRightsPlaceHolder.removeAllViews();
        mRlRightsPlaceHolder.addView(rightsView);

        rightsView.setRightsGravityStart();
        rightsView.setRightsTitleTextSize(14);
        rightsView.setRightsTitleTextColor(Color.parseColor("#828282"));
        rightsView.setRightsTitle(_activity.getString(R.string.permissions_applied_to_the_file));
        rightsView.setRightsTitleMargin(dp(10), dp(10), dp(10), dp(10));
        rightsView.setRightsTitlePadding(dp(10), dp(5), dp(10), dp(5));
        rightsView.showValidity(fp.formatString());
        rightsView.showWatermark(fp.getDisplayWatermark());
        rightsView.displayRights(fp);
    }

    private void initCentralRightsView(Map<String, Set<String>> tags) {
        mCentralRightsView = new CentralRightsView(_activity);
        mRlRightsPlaceHolder.removeAllViews();
        mRlRightsPlaceHolder.addView(mCentralRightsView);

        mCentralRightsView.setTitleTextNormal();
        mCentralRightsView.setTitleTextSize(14);
        mCentralRightsView.setTitleTextColor(Color.parseColor("#828282"));
        mCentralRightsView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mCentralRightsView.paddingData(tags);
        mCentralRightsView.setPermissionTitleTextNormal();
        mCentralRightsView.setPermissionTitleTextSize(14);
        mCentralRightsView.setPermissionTitleTextColor(Color.parseColor("#828282"));
    }

    private void loading(boolean show) {
        if (mCentralRightsView != null && mCentralRightsView.getLoadingLayout() != null) {
            mCentralRightsView.getLoadingLayout().setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private int dp(float value) {
        return DensityHelper.dip2px(_activity, value);
    }

    private List<String> getProjectDisplayRecipients() {
        List<String> ret = new ArrayList<>();
        if (mRecipients == null || mRecipients.isEmpty()) {
            return ret;
        }
        ret.addAll(mRecipients.keySet());
        return ret;
    }

    private Map<String, String> getFilteredShareWithRecipients() {
        Map<String, String> ret = new HashMap<>();
        Map<String, String> shareWith = mFile.getShareWith();
        if (shareWith == null || shareWith.isEmpty()) {
            return ret;
        }
        Set<String> keys = shareWith.keySet();
        for (String s : keys) {
            if (mRemovedRecipients.containsKey(s)) {
                continue;
            }
            String value = shareWith.get(s);
            if (value != null) {
                ret.put(s, value);
            }
        }
        return ret;
    }

    private void removeRecipients(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        if (mFile.isShared()) {
            if (mRecipients.containsKey(key)
                    && !mNewRecipients.containsKey(key)) {
                String id = mRecipients.get(key);
                if (id != null && !id.isEmpty()) {
                    mRemovedRecipients.put(key, id);
                }
            } else {
                mNewRecipients.remove(key);
            }
        }
        mRecipients.remove(key);
    }

    private List<Integer> getProjectRecipientsId() {
        List<Integer> ret = new ArrayList<>();
        if (mRecipients == null || mRecipients.isEmpty()) {
            return ret;
        }
        for (String id : mRecipients.values()) {
            ret.add(Integer.valueOf(id));
        }
        return ret;
    }

    private List<String> getNewRecipients() {
        return new ArrayList<>(mNewRecipients.values());
    }

    private List<String> getRemovedRecipients() {
        return new ArrayList<>(mRemovedRecipients.values());
    }

    private String getSharingRecipientsDisplayString() {
        StringBuilder builder = new StringBuilder();
        if (mRecipients == null || mRecipients.isEmpty()) {
            return builder.toString();
        }
        List<String> names = new ArrayList<>(mRecipients.keySet());
        for (int i = 0; i < names.size(); i++) {
            builder.append(names.get(i));
            if (i != names.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private void showRevokeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(_activity)
                .setTitle(R.string.app_name)
                .setMessage(R.string.revoke_all_rights_warning)
                .setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        revokeAllRights();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.common_cancel_initcap, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    private void revokeAllRights() {
        RevokingTask task = new RevokingTask(mService, mFile,
                mRevokingCallback = new RevokingCallback());
        task.run();
    }

    private void showLoadingDialog() {
        mLoadingDialog = LoadingDialog2.newInstance();
        mLoadingDialog.showModalDialog(_activity);
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissDialog();
            mLoadingDialog = null;
        }
    }

    private class GetFingerPrintCallback implements FileInfoTask.ITaskCallback<FileInfoTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(FileInfoTask.Result results) {
            INxlFileFingerPrint fp = results.fp;
            if (fp != null) {
                if (fp.hasRights()) {
                    initADHocRightsView(fp);
                } else {
                    initCentralRightsView(fp.getAll());
                    policyEvaluation(fp);
                }
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            e.printStackTrace();
            //ExceptionHandler.handleException(_activity, e);
        }

    }

    private class SharingCallback implements SharingToProjectTask.ITaskCallback<SharingToProjectTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            showLoadingDialog();
        }

        @Override
        public void onTaskExecuteSuccess(SharingToProjectTask.Result results) {
            dismissLoadingDialog();
            CommonUtils.popupProjectShareToProjectSuccessTip(mRootView,
                    getSharingRecipientsDisplayString(),
                    mFile.getName());
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            dismissLoadingDialog();
            ExceptionHandler.handleException(_activity, e);
        }

    }

    private class UpdatingCallback implements UpdatingTask.ITaskCallback<UpdatingTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            showLoadingDialog();
        }

        @Override
        public void onTaskExecuteSuccess(UpdatingTask.Result results) {
            dismissLoadingDialog();
            ToastUtil.showToast(_activity, _activity.getString(R.string.msg_hint_file_shared_successful));
            finishParent();
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            dismissLoadingDialog();
            ExceptionHandler.handleException(_activity, e);
        }
    }

    private class RevokingCallback implements RevokingTask.ITaskCallback<RevokingTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            showLoadingDialog();
        }

        @Override
        public void onTaskExecuteSuccess(RevokingTask.Result results) {
            dismissLoadingDialog();
            ToastUtil.showToast(_activity, _activity.getString(R.string.msg_hint_file_revoked));
            finishParent();
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            dismissLoadingDialog();
            ExceptionHandler.handleException(_activity, e);
        }

    }

}
