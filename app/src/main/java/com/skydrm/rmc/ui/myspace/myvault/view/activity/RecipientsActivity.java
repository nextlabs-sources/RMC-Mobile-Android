package com.skydrm.rmc.ui.myspace.myvault.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.myspace.myvault.model.adapter.RecipientsAdapter;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.Recipient;
import com.skydrm.rmc.ui.myspace.myvault.view.interactor.IVaultFileShareContact;
import com.skydrm.rmc.ui.myspace.myvault.view.interactor.VaultFileSharePresenter;
import com.skydrm.rmc.ui.widget.CustomLinearLayoutManager;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hhu on 5/12/2017.
 */

public class RecipientsActivity extends BaseActivity implements IVaultFileShareContact.IView {
    @BindView(R.id.imageView_back)
    ImageButton mIbBack;
    @BindView(R.id.tv_first)
    TextView mTvToolbarTitle;
    @BindView(R.id.tv_second)
    TextView mTvToolbarSubTitle;
    @BindView(R.id.share_with_more)
    RelativeLayout mRlShareWithMore;
    @BindView(R.id.shared_count_desc)
    TextView mTvShareCount;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private RecipientsAdapter mAdapter;

    private List<String> mRecipients = new ArrayList<>();
    private List<String> mRemovedEmails = new ArrayList<>();

    private INxlFile mFileBase;
    private MyVaultMetaDataResult mMetaData;
    private IVaultFileShareContact.IPresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recepients);
        ButterKnife.bind(this);
        resolveIntent();
        mPresenter = new VaultFileSharePresenter(mFileBase, this);
        initViewAndEvents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.REQUEST_CODE_UPDATE_RECIPIENTS) {
                List<String> add = data.getStringArrayListExtra(Constant.RECIPIENT_ADD);
                List<String> remove = data.getStringArrayListExtra(Constant.RECIPIENT_REMOVE);
                if (add != null && add.size() != 0) {
                    for (String recipient : add) {
                        Recipient r = new Recipient();
                        r.setRecipientName(recipient);
                        r.setRecipientEmail(recipient);
                        r.setRecipientAvatar(null);
                        mRemovedEmails.remove(recipient);
                        mRecipients.add(recipient);
                    }
                }
                if (remove != null && remove.size() != 0) {
                    mRecipients.removeAll(remove);
                }
                mAdapter.setRecipients(mRecipients);
                updateShareCount();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onDestroy();
            mPresenter = null;
        }
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        List<String> recipients = intent.getStringArrayListExtra(Constant.RECIPIENT);
        if (recipients != null && recipients.size() != 0) {
            mRecipients.clear();
            mRecipients.addAll(recipients);
        }
        mFileBase = extras.getParcelable(Constant.FILE_INFO);
        mMetaData = (MyVaultMetaDataResult) extras.getSerializable(Constant.VAULT_META_DATA_ENTRY);
    }

    private void initViewAndEvents() {
        initMetaInfo();
        initListener();
    }

    private void initMetaInfo() {
        mTvToolbarTitle.setText(mFileBase.getName());
        mTvToolbarSubTitle.setText(getString(R.string.shared_with));
        mTvToolbarSubTitle.setTextColor(getResources().getColor(R.color.normal_text_color));
        mRecyclerView.setLayoutManager(new CustomLinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(false);
        mAdapter = new RecipientsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        loadRecipients();
    }

    private void loadRecipients() {
        updateShareCount();
        mAdapter.setRecipients(mRecipients);
    }

    private void initListener() {
        mIbBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mRlShareWithMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go2ShareFile();
            }
        });
        mAdapter.setOnRemoveRecipientListener(new RecipientsAdapter.OnRemoveRecipientListener() {
            @Override
            public void onRemoveRecipient(Recipient recipient, int position) {
                if (recipient != null && recipient.getRecipientEmail() != null) {
                    if (!mRemovedEmails.contains(recipient.getRecipientEmail())) {
                        mRemovedEmails.add(recipient.getRecipientEmail());
                        mPresenter.updateRecipients(null, mRemovedEmails, "");
                    }
                }
            }
        });
    }

    private void updateShareCount() {
        mTvShareCount.setText(String.format(Locale.getDefault(), "Shared with %d people", mRecipients.size()));
    }

    private void go2ShareFile() {
        Intent intent = new Intent();
        intent.setAction(Constant.ACTION_MANAGE);
        intent.setClass(this, VaultFileShareActivity.class);

        Bundle mdBundle = new Bundle();
        mdBundle.putSerializable(Constant.VAULT_META_DATA_ENTRY, mMetaData);
        mdBundle.putParcelable(Constant.FILE_INFO, (MyVaultFile) mFileBase);
        intent.putExtras(mdBundle);

        intent.putStringArrayListExtra(Constant.RECIPIENTS, (ArrayList<String>) mRecipients);
        startActivityForResult(intent, Constant.REQUEST_CODE_UPDATE_RECIPIENTS);
    }

    @Override
    public void updateRightsAndValidity(List<String> rights, long start, long end) {

    }

    @Override
    public void updateRightsAndValidity(INxlFileFingerPrint fp) {

    }

    @Override
    public void setShareIndicator(boolean active) {

    }

    @Override
    public void setLoadingRightsView(boolean active) {

    }

    @Override
    public void updateRecipients(List<String> added, List<String> removed) {
        if (added != null && added.size() != 0) {
            mRecipients.addAll(added);
        }
        if (removed != null && removed.size() != 0) {
            mRecipients.removeAll(removed);
        }
        mAdapter.setRecipients(mRecipients);
    }

    @Override
    public void showShareSuccessView() {

    }

    @Override
    public void showReShareSuccessView(List<String> newSharedEmails, List<String> alreadySharedEmails) {

    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(this, e);
    }
}
