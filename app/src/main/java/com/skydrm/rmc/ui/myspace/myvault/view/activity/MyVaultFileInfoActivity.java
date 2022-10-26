package com.skydrm.rmc.ui.myspace.myvault.view.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.myvault.IMyVaultFile;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.RecipientsUpdateEvent;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.myspace.myvault.model.adapter.RightsAdapter;
import com.skydrm.rmc.ui.myspace.myvault.view.interactor.IVaultFileInfoContact;
import com.skydrm.rmc.ui.myspace.myvault.view.interactor.VaultFileInfoPresenter;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.ProjectInflateIconHelper;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hhu on 5/11/2017.
 */

public class MyVaultFileInfoActivity extends BaseActivity implements IVaultFileInfoContact.IView {
    @BindView(R.id.imageView_back)
    ImageButton mIbBack;
    @BindView(R.id.tv_first)
    TextView mTvToolbarTitle;
    @BindView(R.id.tv_second)
    TextView mTvToolbarSubTitle;
    @BindView(R.id.shared_on)
    TextView mTvSharedOn;
    @BindView(R.id.file_link_container)
    LinearLayout mLlFileLinkContainer;
    @BindView(R.id.file_access_link)
    TextView mTvFileAccessLink;
    @BindView(R.id.copy_link)
    TextView mTvCopyLink;

//    @BindView(R.id.steward_tip)
//    TextView mTvStewardTip;
    @BindView(R.id.rights_view)
    GridView mRightsView;

    @BindView(R.id.tb_shared_with_desc)
    TextView mTvSharedWithDesc;
    @BindView(R.id.flowLayout)
    FlowLayout mFlEmail;
    @BindView(R.id.manage)
    Button mBtManage;
    @BindView(R.id.revoke_rights)
    Button mBtRevokeRights;
    @BindView(R.id.rl_file_status_container)
    RelativeLayout mRlVaultFileStatusContainer;
    @BindView(R.id.tv_revoke_desc)
    TextView mTvRevokeDesc;
    @BindView(R.id.validity_content)
    TextView mTvValidityContent;

    private DateFormat mSdf = new SimpleDateFormat("EEEE,MMMM d,yyyy", Locale.getDefault());
    private IVaultFileInfoContact.IPresenter mPresenter;
    private IFileInfo mFileInfo;

    private MyVaultMetaDataResult mMetaData;
    private String mFileLink;

    private List<String> mRecipients = new ArrayList<>();
    private RightsAdapter mRightsAdapter;

    private LoadingDialog2 mLoadingDialog;
    private ProjectInflateIconHelper mIconHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault_file_info);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        resolveIntent();
        initViewAndEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mPresenter != null) {
            mPresenter.onDestroy();
            mPresenter = null;
        }
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (active) {
            showLoadingDialog();
        } else {
            hideLoadingDialog();
        }
    }

    @Override
    public void updateMetadata(MyVaultMetaDataResult result) {
        if (result == null) {
            ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.read_rights_failed));
            return;
        }
        mMetaData = result;
        final MyVaultMetaDataResult.ResultsBean.DetailBean detail = result.getResults().getDetail();
        if (detail == null) {
            return;
        }
        if (TextUtils.isEmpty(detail.getFileLink())) {
            mLlFileLinkContainer.setVisibility(View.GONE);
        } else {
            mTvFileAccessLink.setText(detail.getFileLink());
        }
        mFileLink = detail.getFileLink();

        //Display rights.
        checkRights(detail.getRights(), true);
        //Display validity text.
        mTvValidityContent.setText(getValidityText(detail.getValidity().getStartDate(),
                detail.getValidity().getEndDate()));
        //Display shared with list.
        fillSharedWith(detail.getRecipients());
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(this, e);
    }

    @Override
    public void onRevokeRights() {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateRecipients(RecipientsUpdateEvent event) {
        if (event.isRemoved()) {
            List<String> removed = event.getRemovedEmails();
            if (removed != null && removed.size() != 0) {
                mRecipients.removeAll(removed);
            }
        } else {
            List<String> added = event.getNewAddedEmails();
            if (added != null && added.size() != 0) {
                mRecipients.addAll(added);
            }
        }
        fillSharedWith(new ArrayList<>(mRecipients));
    }

    private void resolveIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        mFileInfo = extras.getParcelable(Constant.VAULT_FILE_ENTRY);
    }

    private void initViewAndEvents() {
        initFileInfo();
        initListener();
        mIconHelper = new ProjectInflateIconHelper(this);
    }

    private void initFileInfo() {
        if (mFileInfo == null) {
            return;
        }
        mRightsAdapter = new RightsAdapter(this);
        mRightsView.setAdapter(mRightsAdapter);

        mTvToolbarTitle.setText(mFileInfo.getName());
        mTvToolbarSubTitle.setText(getString(R.string.my_vault));
        mTvToolbarSubTitle.setTextColor(getResources().getColor(R.color.normal_text_color));
        mTvSharedOn.setText(TimeUtil.formatData(mFileInfo.getLastModifiedTime()));
        IMyVaultFile vaultFile = (IMyVaultFile) mFileInfo;
        if (vaultFile.isRevoked()) {
            mLlFileLinkContainer.setVisibility(View.GONE);
            mBtManage.setVisibility(View.GONE);
            mBtRevokeRights.setVisibility(View.GONE);
            mRlVaultFileStatusContainer.setVisibility(View.GONE);
            mTvRevokeDesc.setText(getString(R.string.this_file_has_been_revoked));
        }
        if (vaultFile.isDeleted()) {
            mLlFileLinkContainer.setVisibility(View.GONE);
            mBtManage.setVisibility(View.GONE);
            mBtRevokeRights.setVisibility(View.GONE);
            mRlVaultFileStatusContainer.setVisibility(View.GONE);
            mTvRevokeDesc.setText(getString(R.string.this_file_has_been_deleted));
        }
        mPresenter = new VaultFileInfoPresenter((MyVaultFile) mFileInfo, this);
        mPresenter.getMetadata();
    }

    private void initListener() {
        mIbBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBtManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go2RecipientsActivity();
            }
        });
        mBtRevokeRights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRevokeDialog();
            }
        });
        mTvCopyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cpyLink();
            }
        });
    }

    private void showRevokeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.revoke_all_rights_warning)
                .setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mPresenter.revokeRights();
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

    private void cpyLink() {
        ToastUtil.showToast(getApplicationContext(), getString(R.string.hint_msg_cpy_link_success));
        mPresenter.copyLink(this, mFileLink);
    }

    private void checkRights(List<String> rights, boolean isOwner) {
        mRightsAdapter.showRights(rights);
//        mTvStewardTip.setVisibility(isOwner ? View.VISIBLE : View.GONE);
    }

    private String getValidityText(long startDate, long endDate) {
        String content = "";
        if (startDate == 0) {
            if (endDate == 0) {
                content = getString(R.string.never_expire);
            } else {
                content = "Until " + mSdf.format(new Date(endDate));
            }
        } else {
            content = mSdf.format(new Date(startDate)) + " - " + mSdf.format(new Date(endDate));
        }
        return content;
    }

    private void fillSharedWith(List<String> recipients) {
        mRecipients.clear();
        //mFlEmail.clearEmailList();
        if (recipients != null && recipients.size() != 0) {
            mRecipients.addAll(recipients);
        } else {
            mTvSharedWithDesc.setVisibility(View.GONE);
        }
        mIconHelper.inflateInitialByEmail(mFlEmail, recipients);
        //mFlEmail.wrapEmail(mRecipients);
    }

    private void go2RecipientsActivity() {
        Intent intent = new Intent();
        intent.setClass(this, RecipientsActivity.class);
        Bundle arguments = new Bundle();
        arguments.putParcelable(Constant.FILE_INFO, (MyVaultFile) mFileInfo);
        arguments.putSerializable(Constant.VAULT_META_DATA_ENTRY, mMetaData);
        arguments.putStringArrayList(Constant.RECIPIENT, (ArrayList<String>) mRecipients);
        intent.putExtras(arguments);
        startActivity(intent);
    }

    private void showLoadingDialog() {
        mLoadingDialog = LoadingDialog2.newInstance();
        mLoadingDialog.showModalDialog(this);
    }

    private void hideLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissDialog();
            mLoadingDialog = null;
        }
    }
}
