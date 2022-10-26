package com.skydrm.rmc.ui.fragment.share;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.engine.eventBusMsg.ShareCompleteNotifyEvent;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.fragment.IViewBuilder;
import com.skydrm.rmc.ui.fragment.task.ShareTask;
import com.skydrm.rmc.ui.project.feature.centralpolicy.PolicyEvaluation;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.customcontrol.rights.ADHocRightsDisplayView;
import com.skydrm.rmc.ui.widget.customcontrol.rights.CentralTagDisplayView;
import com.skydrm.rmc.ui.widget.customcontrol.share.ShareView;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.INxlRights;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.rms.rest.user.User;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.rms.user.membership.IMemberShip;
import com.skydrm.sdk.rms.user.membership.ProjectMemberShip;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NxlViewBuilder implements IViewBuilder, CentralTagDisplayView.IInvokePolicyEvaluationListener {
    private ADHocRightsDisplayView mADHocRightsDisplayView;
    private ShareView mShareView;

    private File mWorkingFile;
    private ShareCallback mShareCallback;

    private Context mCtx;
    private View mRoot;

    private INxlFileFingerPrint mFp;
    private LoadingDialog2 mLoadingDialog;

    private LinearLayout mLlSubRoot;
    private CentralTagDisplayView mCentralTagDisplayView;

    private Button mBt;

    public NxlViewBuilder(Context ctx, View root) {
        this.mCtx = ctx;
        this.mRoot = root;
    }

    @Override
    public View buildRoot(Context ctx) {
        if (ctx == null) {
            return null;
        }
        mLlSubRoot = new LinearLayout(ctx);
        mLlSubRoot.setOrientation(LinearLayout.VERTICAL);

        mADHocRightsDisplayView = new ADHocRightsDisplayView(ctx);
        LinearLayout.LayoutParams rightsViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mLlSubRoot.addView(mADHocRightsDisplayView, rightsViewParams);

        mShareView = new ShareView(ctx);
        LinearLayout.LayoutParams shareViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mLlSubRoot.addView(mShareView, shareViewParams);

        return mLlSubRoot;
    }

    @Override
    public void showLoading(int type) {
        if (mADHocRightsDisplayView != null) {
            mADHocRightsDisplayView.showLoadingRightsLayout();
        }
    }

    @Override
    public void hideLoading(int type) {
        if (mADHocRightsDisplayView != null) {
            mADHocRightsDisplayView.hideLoadingRightsLayout();
        }
    }

    @Override
    public void bindFingerPrint(INxlFileFingerPrint fp, File workingFile, boolean nxl) {
        mWorkingFile = workingFile;
        mFp = fp;
        if (nxl) {
            if (fp == null) {
                return;
            }
            if (fp.hasTags()) {
                showCentralTagView(fp);
            } else if (fp.hasRights()) {
                showAdhocRightsView(fp);
                if (isFromProject(fp.getOwnerID())) {
                    mLlSubRoot.removeView(mShareView);
                    mBt.setVisibility(View.GONE);
                }
            } else {
                showCentralTagView(fp);
            }
        }
    }

    private boolean isFromProject(String ownerId) {
        try {
            if (ownerId == null || ownerId.isEmpty()) {
                return false;
            }
            IRmUser user = SkyDRMApp.getInstance().getSession().getRmUser();
            if (user == null) {
                return false;
            }
            List<IMemberShip> memberships = user.getMemberships();
            if (memberships == null || memberships.size() == 0) {
                return false;
            }
            for (IMemberShip ms : memberships) {
                if (ms == null) {
                    continue;
                }
                if (ownerId.equals(ms.getId())) {
                    return ms instanceof ProjectMemberShip;
                }
            }
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAdhocRightsView(INxlFileFingerPrint fp) {
        mADHocRightsDisplayView.displayRights(fp);
        mADHocRightsDisplayView.showWatermark(fp.getDisplayWatermark());
        mADHocRightsDisplayView.showValidity(fp.formatString());
    }

    private void showCentralTagView(INxlFileFingerPrint fp) {
        mLlSubRoot.removeAllViews();
        mCentralTagDisplayView = new CentralTagDisplayView(mCtx);
        mCentralTagDisplayView.setTags(fp.getAll());
        mCentralTagDisplayView.setInvokePolicyEvaluationListener(this);
        mCentralTagDisplayView.onUserFirstVisible();
        mLlSubRoot.addView(mCentralTagDisplayView);

        mBt.setVisibility(View.GONE);
    }

    @Override
    public void configureShareOrProtectButton(Button button) {
        if (button == null) {
            return;
        }
        this.mBt = button;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareNxlFile();
            }
        });
    }

    @Override
    public void updateExpiry(User.IExpiry expiry) {

    }

    @Override
    public void wrapContacts(Intent data) {
        if (mShareView != null) {
            mShareView.wrapContactParcel(data);
        }
    }

    @Override
    public void onReleaseResource() {
        if (mShareCallback != null) {
            mShareCallback = null;
        }
        if (mCentralTagDisplayView != null) {
            CommonUtils.releaseResource(mCentralTagDisplayView);
            mCentralTagDisplayView = null;
        }
    }

    private void shareNxlFile() {
        if (!mShareView.tryShare()) {
            return;
        }

        if (mFp == null) {
            ToastUtil.showToast(mCtx, "Failed to parse the nxl file.");
            return;
        }

        Expiry expiry = mFp.getExpiry();

        if (expiry.isExpired() && !expiry.isFuture()) {
            ToastUtil.showToast(mCtx, mCtx.getString(R.string.share_rights_expired));
            return;
        }

        List<String> validEmailList = mShareView.getValidEmailList();
        String comments = mShareView.getComments();

        mShareCallback = new ShareCallback();
        ShareTask task = new ShareTask(mWorkingFile, validEmailList, comments, mShareCallback);
        task.run();
    }

    @Override
    public void beginInvoke(Map<String, Set<String>> tags) {
        if (mCentralTagDisplayView != null) {
            mCentralTagDisplayView.showLoading(true);
        }
        policyEvaluation(tags);
    }

    class ShareCallback implements ShareTask.ITaskCallback<ShareTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            showLoadingDialog();
        }

        @Override
        public void onTaskExecuteSuccess(ShareTask.Result results) {
            dismissLoadingDialog();

            EventBus.getDefault().post(new ShareCompleteNotifyEvent());

            CommonUtils.popupShareSucceedTip(mCtx, mWorkingFile.getName(), mRoot,
                    mShareView.getValidEmailList(), false);
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            dismissLoadingDialog();
            ExceptionHandler.handleException(mCtx, e);
        }
    }

    private void policyEvaluation(Map<String, Set<String>> tags) {
        int userId = -1;
        try {
            userId = SkyDRMApp.getInstance().getSession().getRmUser().getUserId();
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }

        if (userId == -1 || mFp == null) {
            if (mCentralTagDisplayView != null) {
                mCentralTagDisplayView.showNoPolicyTip();
            }
            return;
        }

        int evalType = 0;//defined by rms api.
        int rights = INxlRights.VIEW + INxlRights.EDIT + INxlRights.PRINT
                + INxlRights.SHARE + INxlRights.DOWNLOAD
                + INxlRights.WATERMARK + INxlRights.DECRYPT;

        PolicyEvaluation.evaluate(PolicyEvaluation.buildEvalBean(mFp.getOwnerID(), userId,
                evalType, rights, mWorkingFile.getName(), mFp.getDUID(), tags),
                new PolicyEvaluation.IEvaluationCallback() {
                    @Override
                    public void onEvaluated(String result) {
                        if (mCentralTagDisplayView != null) {
                            mCentralTagDisplayView.showLoading(false);
                        }
                        if (TextUtils.isEmpty(result)) {
                            if (mCentralTagDisplayView != null) {
                                mCentralTagDisplayView.showNoPolicyTip();
                            }
                            return;
                        }
                        try {
                            JSONObject responseObj = new JSONObject(result);
                            if (responseObj.has("results")) {
                                JSONObject resultsObj = responseObj.optJSONObject("results");
                                if (resultsObj != null) {
                                    int rights = resultsObj.optInt("rights");
                                    JSONArray obligations = resultsObj.optJSONArray("obligations");
                                    List<String> rightsArray = NxlDoc.integer2Rights(rights);
                                    if (obligations != null && obligations.length() != 0) {
                                        if (rightsArray != null) {
                                            rightsArray.add("WATERMARK");
                                        }
                                    }

                                    if (mCentralTagDisplayView != null) {
                                        mCentralTagDisplayView.showRights(rightsArray, "");
                                    }
                                } else {
                                    if (mCentralTagDisplayView != null) {
                                        mCentralTagDisplayView.showNoPolicyTip();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (mCentralTagDisplayView != null) {
                                mCentralTagDisplayView.showNoPolicyTip();
                            }
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        e.printStackTrace();
                        if (mCentralTagDisplayView != null) {
                            mCentralTagDisplayView.showLoading(false);
                            mCentralTagDisplayView.showNoPolicyTip();
                        }
                    }
                });
    }

    private void showLoadingDialog() {
        if (mCtx == null) {
            return;
        }
        mLoadingDialog = LoadingDialog2.newInstance();
        mLoadingDialog.showModalDialog(mCtx);
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissDialog();
            mLoadingDialog = null;
        }
    }
}
