package com.skydrm.rmc.ui.service.modifyrights.view;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.Project;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceRepo;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.share.IShare;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsFile;
import com.skydrm.rmc.ui.service.modifyrights.IModifyRightsService;
import com.skydrm.rmc.ui.service.modifyrights.task.ReClassifyTask;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.NoScrollViewPager;
import com.skydrm.rmc.ui.widget.customcontrol.rights.CentralTagDisplayView;
import com.skydrm.rmc.ui.widget.customcontrol.rights.RightsSpecifyView;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.StringUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CentralFileViewBuilder implements IViewBuilder, ViewPager.OnPageChangeListener,
        CentralTagDisplayView.IInvokePolicyEvaluationListener, IShare.IPolicyCallback {
    private RightsSpecifyView mRightsSpecifyView;
    private CentralTagDisplayView mTagDisplayView;
    private CentralTagDisplayView mNewSelectTagDisplayView;

    private Context mCtx;
    private View mRootView;
    private IModifyRightsService mService;
    private IModifyRightsFile mFile;
    private NoScrollViewPager mViewPager;

    private INxlFileFingerPrint mFingerPrint;
    private int mPos = -1;
    private int mMaxSize;

    private Button mOperateBt;
    private boolean isRightsSpecifyViewFirstVisible = true;

    private ReClassifyCallback mCallback;
    private LoadingDialog2 mLoadingDialog;

    CentralFileViewBuilder(Context ctx, View rootView,
                           IModifyRightsService service,
                           IModifyRightsFile file) {
        this.mCtx = ctx;
        this.mRootView = rootView;
        this.mService = service;
        this.mFile = file;
    }

    @Override
    public void configureToolbar(Toolbar toolbar) {
        if (toolbar == null) {
            return;
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPos == 0) {
                    finishParent();
                } else {
                    pre();
                }
            }
        });
    }

    @Override
    public View buildRoot(Context ctx) {
        mViewPager = new NoScrollViewPager(ctx);
        mViewPager.setPageEnabled(false);
        mViewPager.setAdapter(new CentralFileViewPageAdapter(buildViewChain(ctx)));
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(mMaxSize);
        return mViewPager;
    }

    @Override
    public void bindFingerPrint(INxlFileFingerPrint fp) {
        if (fp == null) {
            return;
        }
        this.mFingerPrint = fp;

        mPos = 0;
        mTagDisplayView.setTags(fp.getAll());
        mTagDisplayView.setUserVisibleHint(true);
    }

    @Override
    public void configureOperateButton(Button button) {
        if (button == null) {
            return;
        }
        mOperateBt = button;
        button.setText(button.getContext().getString(R.string.Next));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPos == mMaxSize - 1) {
                    checkThenFireToReClassify();
                } else {
                    next();
                }
            }
        });
    }

    @Override
    public void showLoading() {
        showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        dismissLoadingDialog();
    }

    @Override
    public boolean needInterceptBackPress() {
        return mPos > 0 && mPos < mMaxSize;
    }

    @Override
    public void interceptBackPress() {
        pre();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mPos = position;
        if (mPos == 1) {
            if (mRightsSpecifyView != null) {
                mRightsSpecifyView.setTags(mService.getClassificationRaw());
                mRightsSpecifyView.setUserVisibleHint(true);
                mRightsSpecifyView.setSelectedTagsFromInheritanceIfNecessary(getDefaultTags());
                isRightsSpecifyViewFirstVisible = false;
            }
        }
        if (position == mMaxSize - 1) {
            mNewSelectTagDisplayView.setTags(getSelectedTags());
            mNewSelectTagDisplayView.showEmptyTag(true);
            mNewSelectTagDisplayView.setUserVisibleHint(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onReleaseResource() {
        if (mRightsSpecifyView != null) {
            CommonUtils.releaseResource(mRightsSpecifyView);
            mRightsSpecifyView = null;
        }
        if (mTagDisplayView != null) {
            CommonUtils.releaseResource(mTagDisplayView);
            mTagDisplayView = null;
        }
        if (mNewSelectTagDisplayView != null) {
            CommonUtils.releaseResource(mNewSelectTagDisplayView);
            mNewSelectTagDisplayView = null;
        }
        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(this);
            mViewPager = null;
        }
        if (mCallback != null) {
            mCallback = null;
        }
    }

    @Override
    public void beginInvoke(Map<String, Set<String>> tags) {
        if (mPos == 0) {
            if (mTagDisplayView != null) {
                mTagDisplayView.showLoading(true);
            }
        }
        if (mPos == mMaxSize - 1) {
            if (mNewSelectTagDisplayView != null) {
                mNewSelectTagDisplayView.showLoading(true);
            }
        }
        if (mFingerPrint != null) {
            String membershipId = mFingerPrint.getOwnerID();
            mFile.doPolicyEvaluation(membershipId, tags, this);
        }
    }

    @Override
    public void onSuccess(List<String> rights, String obligations) {
        if (mPos == 0) {
            if (mTagDisplayView != null) {
                mTagDisplayView.showLoading(false);
                mTagDisplayView.showRights(rights, obligations);
            }
        }
        if (mPos == mMaxSize - 1) {
            if (mNewSelectTagDisplayView != null) {
                mNewSelectTagDisplayView.showLoading(false);
                mNewSelectTagDisplayView.showRights(rights, obligations);
            }
        }
    }

    @Override
    public void onFailed(MarkException e) {
        if (mCtx != null) {
            ExceptionHandler.handleException(mCtx, e);
        }
    }

    private List<View> buildViewChain(Context ctx) {
        List<View> views = new ArrayList<>();

        mTagDisplayView = new CentralTagDisplayView(ctx);
        mTagDisplayView.setDescText(ctx.getString(R.string.hint_classification_title));
        mTagDisplayView.setDescTextSize(16);
        mTagDisplayView.setDescTextColor(ctx.getResources().getColor(android.R.color.black));
        mTagDisplayView.setInvokePolicyEvaluationListener(this);
        views.add(mTagDisplayView);

        mRightsSpecifyView = new RightsSpecifyView(ctx);
        mRightsSpecifyView.changeIntoSelectTagModeOnly();

        String textForSelectClassify = "";
        if (mService instanceof Project) {
            textForSelectClassify = String.format(ctx.getString(R.string.hint_select_new_classification_from_project),
                    mService.getServiceName(mCtx));
        }
        if (mService instanceof WorkSpaceRepo) {
            textForSelectClassify = String.format(ctx.getString(R.string.hint_select_new_classification_from_workspace),
                    mService.getServiceName(mCtx));
        }

        mRightsSpecifyView.setDesc(StringUtils.getBoldStyle(textForSelectClassify,
                textForSelectClassify.lastIndexOf(mService.getServiceName(mCtx)),
                textForSelectClassify.length()),
                Gravity.START);
        views.add(mRightsSpecifyView);

        mNewSelectTagDisplayView = new CentralTagDisplayView(ctx);

        String textForNewTitle = "";
        if (mService instanceof Project) {
            textForNewTitle = String.format(ctx.getString(R.string.hint_new_classification_title),
                    mService.getServiceName(mCtx));
        }
        if (mService instanceof WorkSpaceRepo) {
            textForNewTitle = String.format(ctx.getString(R.string.hint_new_classification_title_workspace),
                    mService.getServiceName(mCtx));
        }

        mNewSelectTagDisplayView.setDescText(StringUtils.getBoldStyle(textForNewTitle,
                textForNewTitle.lastIndexOf(mService.getServiceName(mCtx)),
                textForNewTitle.length()));
        mNewSelectTagDisplayView.setDescTextSize(16);
        mNewSelectTagDisplayView.setDescTextColor(ctx.getResources().getColor(android.R.color.black));
        mNewSelectTagDisplayView.setInvokePolicyEvaluationListener(this);
        views.add(mNewSelectTagDisplayView);

        mMaxSize = views.size();
        return views;
    }

    private void pre() {
        if (mPos < 0 || mPos > mMaxSize - 1) {
            return;
        }
        if (mOperateBt != null) {
            mOperateBt.setText(mOperateBt.getContext().getString(R.string.Next));
        }
        mViewPager.setCurrentItem(--mPos, true);
    }

    private void next() {
        if (mPos < 0 || mPos > mMaxSize - 1) {
            return;
        }
        if (mPos == 1) {
            if (!mRightsSpecifyView.checkCentralMandatory()) {
                return;
            }
        }
        mViewPager.setCurrentItem(++mPos, true);
        if (mOperateBt != null) {
            if (mPos == mMaxSize - 1) {
                mOperateBt.setText(mOperateBt.getContext().getString(R.string.save));
            }
        }
    }

    private Map<String, Set<String>> getSelectedTags() {
        return mRightsSpecifyView.getCentralSelectedTags();
    }

    private Map<String, Set<String>> getDefaultTags() {
        if (mRightsSpecifyView == null) {
            return mFingerPrint.getAll();
        }
        if (isRightsSpecifyViewFirstVisible) {
            return mFingerPrint.getAll();
        }
        return getSelectedTags();
    }

    private void finishParent() {
        if (mCtx == null) {
            return;
        }
        Activity activity = (Activity) mCtx;
        activity.finish();
    }

    private void checkThenFireToReClassify() {
        if (checkDifferences()) {
            //String duid = mFile.getDuid();
            //String pathDisplay = mFile.getPathDisplay();
            String fileName = mFile.getName();
            String parentPathId = mFile.getParent();
            String rawTags = "{}";
            try {
                rawTags = getRawTags(getSelectedTags());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mCallback = new ReClassifyCallback();
            ReClassifyTask task = new ReClassifyTask(mService,
                    mFile, fileName,
                    parentPathId, rawTags,
                    mCallback);
            task.run();
        }
    }

    private boolean checkDifferences() {
        if (mFingerPrint == null) {
            return false;
        }
        Map<String, Set<String>> older = mFingerPrint.getAll();
        Map<String, Set<String>> newer = getSelectedTags();
        if (older == null && newer == null) {
            return false;
        }
        if (older == null && newer.size() != 0) {
            return true;
        }
        if (newer.equals(older)) {
            ToastUtil.showToast(mCtx, "Please re-select new classifications to save it.");
            return false;
        }
        return true;
    }

    private String getRawTags(Map<String, Set<String>> tags) throws JSONException {
        String ret = "{}";
        if (tags == null || tags.size() == 0) {
            return ret;
        }
        JSONObject kvObj = new JSONObject();
        Set<String> keys = tags.keySet();
        for (String k : keys) {
            Set<String> values = tags.get(k);
            if (values == null || values.size() == 0) {
                continue;
            }
            JSONArray valueArr = new JSONArray();
            for (String v : values) {
                valueArr.put(v);
            }
            kvObj.put(k, valueArr);
        }
        return kvObj.toString();
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

    private class ReClassifyCallback implements ReClassifyTask.ITaskCallback<ReClassifyTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            showLoading();
        }

        @Override
        public void onTaskExecuteSuccess(ReClassifyTask.Result results) {
            hideLoading();
            CommonUtils.popupReClassifySuccessTip(mCtx, mRootView, mFile.getName());
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            hideLoading();
            ExceptionHandler.handleException(mCtx, e);
        }
    }
}
