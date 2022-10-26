package com.skydrm.rmc.ui.service.fileinfo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.project.ProjectFile;
import com.skydrm.rmc.datalayer.repo.project.SharedWithProjectFile;
import com.skydrm.rmc.datalayer.repo.sharedwithme.SharedWithMeFile;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.myspace.myvault.model.adapter.RightsAdapter;
import com.skydrm.rmc.ui.myspace.myvault.view.activity.VaultFileShareActivity;
import com.skydrm.rmc.ui.myspace.myvault.view.widget.ExpandableHeightGridView;
import com.skydrm.rmc.ui.project.feature.centralpolicy.CentralRightsView;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.commonUtils.IconHelper;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.RmsRestAPIException;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hhu on 7/31/2017.
 */

public class FileInfoActivity extends BaseActivity {
    @BindView(R.id.imageView_back)
    ImageButton mTvBack;
    @BindView(R.id.iv_file_icon)
    ImageView mIvFileIcon;
    @BindView(R.id.tv_first)
    TextView mTvFileName;
    @BindView(R.id.tv_second)
    TextView mTvFilePath;
    @BindView(R.id.file_size)
    TextView mTvFileSize;
    @BindView(R.id.file_date)
    TextView mTvFileDate;
    @BindView(R.id.ll_rights_content)
    LinearLayout mLlRightsContent;

    @BindView(R.id.read_rights_loading_layout)
    LinearLayout loadingLayout;

    @BindView(R.id.share_file_layout)
    RelativeLayout shareSite;

    private IFileInfo mFileInfo;

    private Context mCtx;
    private GetFingerPrintCallback mFPCallback;
    private CentralRightsView mCentralRightsView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = this;
        setContentView(R.layout.activity_file_info);
        ButterKnife.bind(this);
        if (parseIntent()) {
            initViewAndEvents();
        } else {
            finish();
        }
    }

    private boolean parseIntent() {
        Intent i = getIntent();
        if (i == null) {
            return false;
        }
        mFileInfo = i.getParcelableExtra(Constant.FILE_INFO_ENTRY);
        return mFileInfo != null;
    }

    private void initViewAndEvents() {
        mTvFileName.setText(mFileInfo.getName());
        mIvFileIcon.setVisibility(View.VISIBLE);
        mIvFileIcon.setImageResource(IconHelper.getNxlIconResourceIdByExtension(mFileInfo.getName()));

        mTvFilePath.setTextColor(getResources().getColor(R.color.main_green_light));
        String pathDisplay = getPathDisplay();
        if (pathDisplay == null || pathDisplay.isEmpty()) {
            mTvFilePath.setVisibility(View.GONE);
        } else {
            mTvFilePath.setText(pathDisplay);
        }

        mTvFileSize.setText(FileUtils.transparentFileSize(mFileInfo.getFileSize()));
        mTvFileDate.setText(TimeUtil.formatData(mFileInfo.getLastModifiedTime()));

        getFingerPrint();

        shareSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reShare();
            }
        });
        mTvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void reShare() {
        Intent i = new Intent(this, VaultFileShareActivity.class);
        i.setAction(Constant.ACTION_RESHARE);
        i.putExtra(Constant.RESHARE_ENTRY, (SharedWithMeFile) mFileInfo);
        startActivity(i);
    }

    private void getFingerPrint() {
        mFPCallback = new GetFingerPrintCallback();

        new FileInfoTask(mFileInfo, mFPCallback)
                .executeOnExecutor(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFPCallback != null) {
            mFPCallback = null;
        }
    }

    class GetFingerPrintCallback implements LoadTask.ITaskCallback<FileInfoTask.Result, Exception> {
        @Override
        public void onTaskPreExecute() {
            loadingLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onTaskExecuteSuccess(FileInfoTask.Result result) {
            loadingLayout.setVisibility(View.GONE);
            if (result == null) {
                return;
            }
            INxlFileFingerPrint fp = result.fp;
            if (fp == null) {
                return;
            }
            if (fp.hasRights()) {
                List<String> rights = getRights(fp);

                boolean owner = false;
                if (isViewSharedWitheMeFileInfo()) {
                    owner = SkyDRMApp.getInstance().isStewardOf(fp.getOwnerID());
                }

                initADHocRightsView(rights, owner, fp);

            } else if (fp.hasTags()) {
                Map<String, Set<String>> tags = fp.getAll();
                initCentralRightsView(tags);
                policyEvaluation(fp);
            } else if (!fp.hasTags() && !fp.hasRights()) {
                initCentralRightsView(new HashMap<String, Set<String>>());
                policyEvaluation(fp);
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            loadingLayout.setVisibility(View.GONE);
            ExceptionHandler.handleException(mCtx, e);
        }
    }

    private void initADHocRightsView(List<String> rights, boolean owner, INxlFileFingerPrint fingerPrint) {
        if (mCtx == null) {
            return;
        }
        View root = LayoutInflater.from(mCtx).inflate(R.layout.layout_rights_new, null);
//        TextView tvRightsTitle = root.findViewById(R.id.rights);
        TextView tvRightsTitle2 = root.findViewById(R.id.tv_rights_title2);
        if (isViewProjectFileInfo() || isViewSharedWithProjectFileInfo()) {
//            tvRightsTitle.setVisibility(View.GONE);
            tvRightsTitle2.setVisibility(View.VISIBLE);
        }
//        TextView tvStewardTip = root.findViewById(R.id.steward_tip);
        TextView tvValidityContent = root.findViewById(R.id.validity_content);
        ExpandableHeightGridView rightsView = root.findViewById(R.id.rights_view);

//        tvStewardTip.setVisibility(owner ? View.VISIBLE : View.GONE);
        tvValidityContent.setText(fingerPrint.formatString());

        RightsAdapter rightAdapter = new RightsAdapter(this);
        rightsView.setAdapter(rightAdapter);
        rightAdapter.showRights(rights);

        mLlRightsContent.removeAllViews();
        mLlRightsContent.addView(root);
    }

    private void initCentralRightsView(JSONObject tagsObj, List<String> rights) {
        CentralRightsView centralRightsView = new CentralRightsView(mCtx);
        centralRightsView.paddingData(tagsObj, rights);
        mLlRightsContent.addView(centralRightsView);
    }

    private void initCentralRightsView(Map<String, Set<String>> tags) {
        mCentralRightsView = new CentralRightsView(mCtx);
        mCentralRightsView.paddingData(tags);

        mLlRightsContent.removeAllViews();
        mLlRightsContent.addView(mCentralRightsView);
    }

    private void policyEvaluation(INxlFileFingerPrint fp) {
        loading(true);
        mFileInfo.doPolicyEvaluation(fp, new IFileInfo.IPolicyCallback() {
            @Override
            public void onSuccess(List<String> rights, String obligations) {
                loading(false);
                mCentralRightsView.paddingRights(rights, false);
                mCentralRightsView.invalidate();
            }

            @Override
            public void onFailed(Exception e) {
                loading(false);

                if (e instanceof RmsRestAPIException) {
                    RmsRestAPIException rmsRestAPIException = (RmsRestAPIException) e;
                    if (rmsRestAPIException.getDomain()
                            == RmsRestAPIException.ExceptionDomain.NOPOLICY_TO_EVALUATE) {
                        mCentralRightsView.showNoPolicyTips();
                    } else {
                        ExceptionHandler.handleException(mCtx, e);
                    }
                } else {
                    ExceptionHandler.handleException(mCtx, e);
                }

            }
        });
    }

    private void loading(boolean show) {
        if (mCentralRightsView != null && mCentralRightsView.getLoadingLayout() != null) {
            mCentralRightsView.getLoadingLayout().setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private List<String> getRights(INxlFileFingerPrint fingerPrint) {
        List<String> rights = new ArrayList<>();
        if (fingerPrint.hasView()) {
            rights.add(Constant.RIGHTS_VIEW);
        }
        if (fingerPrint.hasEdit()) {
            rights.add(Constant.RIGHTS_EDIT);
        }
        if (fingerPrint.hasPrint()) {
            rights.add(Constant.RIGHTS_PRINT);
        }
        if (fingerPrint.hasShare()) {
            rights.add(Constant.RIGHTS_SHARE);
        }
        if (fingerPrint.hasDecrypt()) {
            rights.add(Constant.RIGHTS_DECRYPT);
        }
        if (fingerPrint.hasDownload()) {
            rights.add(Constant.RIGHTS_DOWNLOAD);
        }
        if (fingerPrint.hasWatermark()) {
            rights.add(Constant.RIGHTS_WATERMARK);
        }
        return rights;
    }

    private String getPathDisplay() {
        if (mFileInfo == null) {
            return "";
        }
        if (mFileInfo instanceof ProjectFile || mFileInfo instanceof SharedWithProjectFile) {
            return String.format(Locale.getDefault(), "%s %s", mFileInfo.getServiceName(this),
                    mFileInfo.getPathDisplay());
        }
        return mFileInfo.getPathDisplay();
    }

    private boolean isViewProjectFileInfo() {
        if (mFileInfo == null) {
            return false;
        }
        return mFileInfo instanceof ProjectFile;
    }

    private boolean isViewSharedWithProjectFileInfo() {
        if (mFileInfo == null) {
            return false;
        }
        return mFileInfo instanceof SharedWithProjectFile;
    }

    private boolean isViewSharedWitheMeFileInfo() {
        if (mFileInfo == null) {
            return false;
        }
        return mFileInfo instanceof SharedWithMeFile;
    }
}
