package com.skydrm.rmc.ui.project.feature.service.protect.view;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.project.feature.centralpolicy.RightsSpecifyPageAdapter;
import com.skydrm.rmc.ui.project.feature.service.core.MarkException;
import com.skydrm.rmc.ui.project.feature.service.protect.IViewBuilder;
import com.skydrm.rmc.ui.project.feature.service.share.IShare;
import com.skydrm.rmc.ui.project.service.ProjectOperateActivity;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.NoScrollViewPager;
import com.skydrm.rmc.ui.widget.customcontrol.rights.CentralTagDisplayView;
import com.skydrm.rmc.ui.widget.customcontrol.rights.RightsSpecifyView;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.rms.rest.user.User;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.rms.user.membership.IMemberShip;
import com.skydrm.sdk.rms.user.membership.ProjectMemberShip;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddFileFromProjectViewBuilder implements IViewBuilder, ViewPager.OnPageChangeListener,
        CentralTagDisplayView.IInvokePolicyEvaluationListener, IShare.IPolicyCallback {
    private NoScrollViewPager mViewPager;
    private CentralTagDisplayView mTagDisplayView;
    private RightsSpecifyView mRightsSpecifyView;
    private CentralTagDisplayView mTagDisplayAndBindView;

    private int mPos;
    private int mMaxSize;

    private Context mCtx;
    private IProject mProject;
    private INxlFileFingerPrint mFingerPrint;
    private IShare mShareFile;

    private View mRoot;
    private Button mAddBt;

    private String mNxlPath;
    private String mParentPathId;
    private AddFileCallback mAddFileCallback;

    private LoadingDialog2 mLoadingDialog;
    private Map<String, Set<String>> mInheritanceTags = new HashMap<>();

    public AddFileFromProjectViewBuilder(Context ctx, View root,
                                         IProject p, IShare f) {
        this.mCtx = ctx;
        this.mRoot = root;
        this.mProject = p;
        this.mShareFile = f;
    }

    @Override
    public boolean isPreviewNeeded() {
        return false;
    }

    @Override
    public View buildRoot(Context ctx) {
        mViewPager = new NoScrollViewPager(ctx);
        mViewPager.setAdapter(new RightsSpecifyPageAdapter(buildViewChain(ctx)));
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setPageEnabled(false);
        return mViewPager;
    }

    @Override
    public void showLoading(int type) {

    }

    @Override
    public void hideLoading(int type) {

    }

    @Override
    public void bindFingerPrint(INxlFileFingerPrint fp, File f, boolean nxl) {
        if (fp == null || f == null) {
            return;
        }
        this.mFingerPrint = fp;
        this.mNxlPath = f.getPath();
        selectFirstPage();
    }

    @Override
    public void configButton(Button button) {
        if (button == null) {
            return;
        }
        this.mAddBt = button;

        button.setText(button.getContext().getString(R.string.Next));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPos == mMaxSize - 1) {
                    addFileToProject();
                } else {
                    next();
                }
            }
        });
    }

    @Override
    public void configNavigator(Toolbar toolbar) {
        if (toolbar == null) {
            return;
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pre();
            }
        });
    }

    @Override
    public void updateExpiry(User.IExpiry expiry) {

    }

    @Override
    public void updateExtractStatus(boolean checked) {

    }

    @Override
    public void updateParentPath(String parentPathId) {
        this.mParentPathId = parentPathId;
    }

    @Override
    public boolean needInterceptBackPress() {
        return mPos >= 0 && mPos < mMaxSize;
    }

    @Override
    public void interceptBackPress() {
        pre();
    }

    public void next() {
        if (mPos < 0 || mPos > mMaxSize - 1) {
            return;
        }
        if (mPos == 1) {
            if (!mRightsSpecifyView.checkCentralMandatory()) {
                return;
            }
        }
        mViewPager.setCurrentItem(++mPos, true);
        if (mPos == 1) {
            mRightsSpecifyView.setSelectedTagsFromInheritanceIfNecessary(getInheritanceSelectedTags());
        }
        if (mAddBt != null) {
            if (mPos == mMaxSize - 1) {
                mAddBt.setText(mAddBt.getContext().getString(R.string.add_file));
            }
        }
    }

    private void pre() {
        if (mPos < 0 || mPos > mMaxSize - 1) {
            return;
        }
        if (mPos == 0) {
            popupFragment();
            return;
        }
        if (mAddBt != null) {
            mAddBt.setText(mAddBt.getContext().getString(R.string.Next));
        }
        mViewPager.setCurrentItem(--mPos, true);
    }

    private List<View> buildViewChain(Context ctx) {
        List<View> viewChain = new ArrayList<>();

        mTagDisplayView = new CentralTagDisplayView(ctx);
        mTagDisplayView.setDescText(ctx.getString(R.string.hint_add_file_to_project_t1));
        mTagDisplayView.setInvokePolicyEvaluationListener(this);
        viewChain.add(mTagDisplayView);

        mRightsSpecifyView = new RightsSpecifyView(ctx);
        //mRightsSpecifyView.changeIntoSelectTagModeOnly();
        mRightsSpecifyView.setDesc(String.format(ctx.getString(R.string.hint_add_file_to_project_from_project_select_rights),
                mProject.getName()), Gravity.START);
        viewChain.add(mRightsSpecifyView);

        mTagDisplayAndBindView = new CentralTagDisplayView(ctx);
        mTagDisplayAndBindView.setInvokePolicyEvaluationListener(this);
        mTagDisplayAndBindView.setDescText(String.format(ctx.getString(R.string.hint_add_file_to_project_from_project_display_selected_rights),
                mProject.getName()));
        viewChain.add(mTagDisplayAndBindView);

        mMaxSize = viewChain.size();
        return viewChain;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mPos = position;
        if (position == 0) {
            mTagDisplayView.setTags(getInheritanceTags());
            mTagDisplayView.setUserVisibleHint(true);
        }
        if (position == 1) {
            mRightsSpecifyView.changeIntoSelectTagModeOnly();
            mRightsSpecifyView.setTags(mProject.getClassificationRaw());
            mRightsSpecifyView.setAllowDefaultSelect(false);
            mRightsSpecifyView.setAllowInheritanceSelect(true);
            mRightsSpecifyView.setUserVisibleHint(true);
        }
        if (position == mMaxSize - 1) {
            mTagDisplayAndBindView.setTags(getFinalMixedTags());
            mTagDisplayAndBindView.showEmptyTag(true);
            mTagDisplayAndBindView.setUserVisibleHint(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onReleaseResource() {
        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(this);
            mViewPager = null;
        }
        if (mTagDisplayView != null) {
            CommonUtils.releaseResource(mTagDisplayView);
            mTagDisplayView = null;
        }
        if (mRightsSpecifyView != null) {
            CommonUtils.releaseResource(mRightsSpecifyView);
            mRightsSpecifyView = null;
        }
        if (mTagDisplayAndBindView != null) {
            CommonUtils.releaseResource(mTagDisplayAndBindView);
            mTagDisplayAndBindView = null;
        }
        if (mAddFileCallback != null) {
            mAddFileCallback = null;
        }
    }

    @Override
    public void beginInvoke(Map<String, Set<String>> tags) {
        String membershipId = "";

        if (mPos == 0) {
            if (mTagDisplayView != null) {
                mTagDisplayView.showLoading(true);
            }
            if (mFingerPrint != null) {
                membershipId = mFingerPrint.getOwnerID();
            }
        }
        if (mPos == mMaxSize - 1) {
            if (mTagDisplayAndBindView != null) {
                mTagDisplayAndBindView.showLoading(true);
            }
            membershipId = getTargetProjectMembershipId(mProject);
        }

        mShareFile.doPolicyEvaluation(membershipId, tags, this);
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
            if (mTagDisplayAndBindView != null) {
                mTagDisplayAndBindView.showLoading(false);
                mTagDisplayAndBindView.showRights(rights, obligations);
            }
        }
    }

    @Override
    public void onFailed(MarkException e) {
        if (mPos == 0) {
            if (mTagDisplayView != null) {
                mTagDisplayView.showLoading(false);
                mTagDisplayView.showNoPolicyTip();
            }
        }
        if (mPos == mMaxSize - 1) {
            if (mTagDisplayAndBindView != null) {
                mTagDisplayAndBindView.showLoading(false);
                mTagDisplayAndBindView.showNoPolicyTip();
            }
        }
    }

    private void selectFirstPage() {
        mPos = 0;
        mTagDisplayView.setTags(getInheritanceTags());
        mTagDisplayView.setUserVisibleHint(true);
    }

    private Map<String, Set<String>> getFinalMixedTags() {
        Map<String, Set<String>> ret = new HashMap<>();
        Map<String, Set<String>> selectedTags = getSelectedTags();
        if (selectedTags != null && selectedTags.size() != 0) {
            ret.putAll(selectedTags);
        }
        if (mInheritanceTags.size() != 0) {
            ret.putAll(mInheritanceTags);
        }
        return ret;
    }

    private Map<String, Set<String>> getInheritanceTags() {
        if (mFingerPrint == null) {
            return null;
        }
        return mFingerPrint.getAll();
    }

    private Map<String, Set<String>> getDisplayTags() {
        if (mRightsSpecifyView == null) {
            return null;
        }
        return mRightsSpecifyView.getDisplayTags();
    }

    private Map<String, Set<String>> getSelectedTags() {
        if (mRightsSpecifyView == null) {
            return null;
        }
        return mRightsSpecifyView.getCentralSelectedTags();
    }

    private Map<String, Set<String>> getInheritanceSelectedTags() {
        Map<String, Set<String>> ret = new HashMap<>();
        // Get target file's classification.
        Map<String, Set<String>> inheritanceTags = getInheritanceTags();
        if (inheritanceTags == null || inheritanceTags.size() == 0) {
            return ret;
        }
        // Get target project's classification.
        Map<String, Set<String>> displayTags = getDisplayTags();

        Set<String> inheritanceCategories = inheritanceTags.keySet();
        Set<String> allCategories = new HashSet<>();

        if (displayTags != null) {
            allCategories.addAll(displayTags.keySet());
        }

        Set<String> containsInAll = new HashSet<>();
        for (String category : inheritanceCategories) {
            if (allCategories.contains(category)) {
                containsInAll.add(category);
            } else {
                mInheritanceTags.put(category, inheritanceTags.get(category));
            }
        }

        if (containsInAll.size() == 0) {
            return ret;
        }

        for (String category : containsInAll) {
            ret.put(category, inheritanceTags.get(category));
        }

        return ret;
    }

    private void addFileToProject() {
        if (mProject == null) {
            return;
        }
        mAddFileCallback = new AddFileCallback();
        mProject.addFile(mNxlPath, getFinalMixedTags(), mParentPathId, mAddFileCallback);
    }

    class AddFileCallback implements IProject.IAddFileCallback {

        @Override
        public void onPreAdd() {
            showLoadingDialog();
        }

        @Override
        public void onSuccess() {
            dismissLoadingDialog();
            showSuccessfulView();
        }

        @Override
        public void onFailed(Exception e) {
            dismissLoadingDialog();
            ExceptionHandler.handleException(mCtx, e);
        }
    }

    private String getTargetProjectMembershipId(IProject p) {
        String retVal = "";
        if (p == null) {
            return retVal;
        }
        try {
            IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
            if (rmUser == null) {
                return retVal;
            }
            List<IMemberShip> memberships = rmUser.getMemberships();
            for (IMemberShip ms : memberships) {
                if (ms instanceof ProjectMemberShip) {
                    ProjectMemberShip pms = (ProjectMemberShip) ms;
                    if (pms.getProjectId() == p.getId()) {
                        retVal = pms.getId();
                        break;
                    }
                }
            }
            return retVal;
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    private void showLoadingDialog() {
        if (!(mCtx instanceof FragmentActivity)) {
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

    private void showSuccessfulView() {
        if (mCtx == null || mShareFile == null || mRoot == null) {
            return;
        }
        CommonUtils.popupProjectShareToProjectSuccessTip(mRoot, mProject.getName(), mShareFile.getName());
    }

    private void popupFragment() {
        if (mCtx == null) {
            return;
        }
        if (mCtx instanceof ProjectOperateActivity) {
            ProjectOperateActivity activity = (ProjectOperateActivity) mCtx;
            activity.popup();
        }
    }
}
