package com.skydrm.rmc.ui.project.feature.service.share.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.project.feature.service.protect.IViewBuilder;
import com.skydrm.rmc.ui.project.feature.service.share.core.task.ShareToPersonTask;
import com.skydrm.rmc.ui.widget.LoadingDialog;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.customcontrol.rights.ADHocRightsDisplayView;
import com.skydrm.rmc.ui.widget.customcontrol.share.ShareView;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.IconHelper;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.user.User;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ShareToPersonViewBuilder implements IViewBuilder {
    private Context mCtx;
    private View mRoot;
    private IProject mProject;
    private INxlFile mFile;
    private File mWorkingFile;
    private ADHocRightsDisplayView mADHocRightsDisplayView;
    private ShareView mShareView;

    private INxlFileFingerPrint mFingerPrint;

    private ShareToPersonCallback mShareToPersonCallback;
    private LoadingDialog2 mLoadingDialog;

    public ShareToPersonViewBuilder(Context ctx, View root, IProject p, INxlFile f) {
        this.mCtx = ctx;
        this.mRoot = root;
        this.mProject = p;
        this.mFile = f;
    }

    @Override
    public boolean isPreviewNeeded() {
        return false;
    }

    @Override
    public View buildRoot(Context ctx) {
        View root = LayoutInflater.from(ctx).inflate(R.layout.layout_project_share_to_person,
                null, false);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        root.setLayoutParams(params);

        if (mFile == null) {
            return root;
        }

        NxlDoc doc = (NxlDoc) mFile;
        TextView tvSizeValue = root.findViewById(R.id.tv_size_value);
        TextView tvLastModifiedTimeValue = root.findViewById(R.id.tv_last_modified_time_value);
        tvSizeValue.setText(FileUtils.transparentFileSize(doc.getFileSize()));
        tvLastModifiedTimeValue.setText(TimeUtil.formatData(doc.getLastModifiedTime()));

        mADHocRightsDisplayView = root.findViewById(R.id.adhoc_rights_display_view);
        mShareView = root.findViewById(R.id.share_view);
        return root;
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
    public void bindFingerPrint(INxlFileFingerPrint fp, File f, boolean nxl) {
        this.mWorkingFile = f;
        this.mFingerPrint = fp;

        if (fp != null) {
            mADHocRightsDisplayView.displayRights(fp);
            mADHocRightsDisplayView.showWatermark(fp.getDisplayWatermark());
            mADHocRightsDisplayView.showValidity(fp.formatString());
        }
    }

    @Override
    public void configButton(Button button) {
        if (button == null) {
            return;
        }
        button.setText(button.getContext().getString(R.string.share_a_protect_file));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkThenShare();
            }
        });
    }

    @Override
    public void configNavigator(Toolbar toolbar) {
        toolbar.findViewById(R.id.tv_cancel).setVisibility(View.GONE);
        toolbar.setTitle(mFile.getName());
        toolbar.setSubtitle(mProject.getName() + "" + mFile.getPathId());
        toolbar.setSubtitleTextColor(mCtx.getResources().getColor(R.color.Black));
        toolbar.setLogo(IconHelper.getNxlIconResourceIdByExtension(mFile.getName()));
        configureToolbar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishParent();
            }
        });
    }

    private void configureToolbar(Toolbar toolbar) {
        if (toolbar == null) {
            return;
        }
        toolbar.setContentInsetsRelative(0, 0);
        toolbar.setContentInsetStartWithNavigation(0);
        try {
            Class<? extends Toolbar> tbClass = toolbar.getClass();
            if (tbClass == null) {
                return;
            }
            // set up title text view.
            Field fTitleTextView = tbClass.getDeclaredField("mTitleTextView");
            if (fTitleTextView != null) {
                fTitleTextView.setAccessible(true);
                Object titleTvObj = fTitleTextView.get(toolbar);
                if (titleTvObj instanceof TextView) {
                    TextView tvTitle = (TextView) titleTvObj;
                    tvTitle.setTextSize(18);
                    tvTitle.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                }
            }
            Field fSubTitleTextView = tbClass.getDeclaredField("mSubtitleTextView");
            if (fSubTitleTextView != null) {
                fSubTitleTextView.setAccessible(true);
                Object subTitleTvObj = fSubTitleTextView.get(toolbar);
                if (subTitleTvObj instanceof TextView) {
                    TextView tvSubTitle = (TextView) subTitleTvObj;
                    tvSubTitle.setTextSize(12);
                    tvSubTitle.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateExpiry(User.IExpiry expiry) {

    }

    @Override
    public void updateExtractStatus(boolean checked) {

    }

    @Override
    public void updateParentPath(String parentPathId) {

    }

    @Override
    public boolean needInterceptBackPress() {
        return false;
    }

    @Override
    public void interceptBackPress() {

    }

    @Override
    public void onReleaseResource() {
        if (mShareToPersonCallback != null) {
            mShareToPersonCallback = null;
        }
    }

    public void wrapContactParcel(Intent data) {
        mShareView.wrapContactParcel(data);
    }

    private void checkThenShare() {
        if (mShareView == null) {
            finishParent();
            return;
        }
        if (mShareView.tryShare()) {
            List<String> validEmailList = getEmails();
            String comments = mShareView.getComments();

            if (mWorkingFile == null) {
                return;
            }
            if (!mWorkingFile.exists() || !mWorkingFile.isFile()) {
                return;
            }
            if (mFingerPrint == null) {
                return;
            }
            String nxlPath = mWorkingFile.getPath();
            Rights rights = getRights(mFingerPrint);
            Obligations obligations = getObligations(mFingerPrint);

            Expiry expiry = getExpiry(mFingerPrint);
            if (expiry.isExpired()) {
                ToastUtil.showToast(mCtx, mCtx.getString(R.string.invalid_digital_rights_period));
                return;
            }
            mShareToPersonCallback = new ShareToPersonCallback();
            String filePathId = mProject.getName() + mFile.getPathId();
            ShareToPersonTask task = new ShareToPersonTask(nxlPath, filePathId, filePathId,
                    rights, obligations, expiry,
                    validEmailList, comments,
                    mShareToPersonCallback);
            task.run();
        }
    }

    private Rights getRights(INxlFileFingerPrint fp) {
        if (fp == null) {
            return null;
        }
        Rights r = new Rights();
        r.setPermissions(fp.toInteger());
        r.IntegerToRights();
        return r;
    }

    private Obligations getObligations(INxlFileFingerPrint fp) {
        if (fp == null) {
            return null;
        }
        Obligations obligations = new Obligations();
        if (!fp.hasWatermark()) {
            return obligations;
        }
        Map<String, String> values = new HashMap<>();

        Iterator<Map.Entry<String, String>> it = fp.getIterator();
        while (it != null && it.hasNext()) {
            Map.Entry<String, String> next = it.next();
            values.put(next.getKey(), next.getValue());
        }
        obligations.setObligation(values);
        return obligations;
    }

    private Expiry getExpiry(INxlFileFingerPrint fp) {
        if (fp == null) {
            return null;
        }
        return fp.getExpiry();
    }

    private List<String> getEmails() {
        if (mShareView == null) {
            return Collections.emptyList();
        }
        return mShareView.getValidEmailList();
    }

    private void finishParent() {
        if (mCtx instanceof Activity) {
            Activity activity = (Activity) mCtx;
            activity.finish();
        }
    }

    class ShareToPersonCallback implements ShareToPersonTask.ITaskCallback<ShareToPersonTask.Result, Exception> {

        @Override
        public void onTaskPreExecute() {
            showLoadingDialog();
        }

        @Override
        public void onTaskExecuteSuccess(ShareToPersonTask.Result result) {
            dismissLoadingDialog();
            CommonUtils.popupShareSucceedTip(mCtx, mWorkingFile.getName(), mRoot, getEmails(), true);
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            dismissLoadingDialog();
            ExceptionHandler.handleException(mCtx, e);
        }
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
