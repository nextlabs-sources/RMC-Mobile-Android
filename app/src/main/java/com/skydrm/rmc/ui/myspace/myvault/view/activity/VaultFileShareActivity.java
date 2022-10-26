package com.skydrm.rmc.ui.myspace.myvault.view.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.datalayer.repo.sharedwithme.SharedWithMeFile;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.widget.animation.ShakeAnimator;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.myspace.myvault.model.adapter.RightsAdapter;
import com.skydrm.rmc.ui.myspace.myvault.view.interactor.IVaultFileShareContact;
import com.skydrm.rmc.ui.myspace.myvault.view.interactor.VaultFileSharePresenter;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.customcontrol.CommentWidget;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ExpiryChecker;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hhu on 5/12/2017.
 */

public class VaultFileShareActivity extends BaseActivity implements IVaultFileShareContact.IView {
    @BindView(R.id.back)
    ImageButton mIbBack;
    @BindView(R.id.file_name)
    TextView mTvName;
    @BindView(R.id.share)
    TextView mTvShare;

    @BindView(R.id.textInputLayout)
    TextInputLayout mTextInputLayout;
    @BindView(R.id.et_email_address)
    EditText mEtEmailAddress;
    @BindView(R.id.flowLayout_email_displayer)
    FlowLayout mFlEmail;

    //    @BindView(R.id.steward_tip)
//    TextView mTvStewardTip;
    @BindView(R.id.rights_view)
    GridView mRightsView;

    @BindView(R.id.comment_widget)
    CommentWidget commentWidget;
    @BindView(R.id.read_rights_loading_layout)
    LinearLayout mLlLoading;
    @BindView(R.id.validity_content)
    TextView mTvValidity;

    private DateFormat mSdf = new SimpleDateFormat("EEEE,MMMM d,yyyy", Locale.getDefault());

    private View mRootView;
    private String mAction;
    private INxlFile mBaseFile;
    private IFileInfo mFileInfo;

    private List<String> mRecipients = new ArrayList<>();
    private List<String> mRights = new ArrayList<>();

    private List<String> mEmailList = new ArrayList<>();
    private List<String> newAddEmails = new LinkedList<>();
    private List<String> mValidEmails = new LinkedList<>();
    private List<String> mDirtyEmails = new LinkedList<>();

    private RightsAdapter mRightsAdapter;
    // record whether is expired
    private boolean bIsExpired = false;


    private IVaultFileShareContact.IPresenter mPresenter;
    private MyVaultMetaDataResult mMetaData;

    private ShakeAnimator shakeAnimator;
    private LoadingDialog2 mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootView = View.inflate(this, R.layout.activity_vault_file_share, null);
        setContentView(mRootView);
        ButterKnife.bind(this);
        resolveIntent();
        initViewAndEvents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQUEST_CODE_SELECT_EMAILS && resultCode == RESULT_OK) {
            parseContactsParcelAndDisplay(data);
        }
    }

    private void parseContactsParcelAndDisplay(Intent data) {
        mFlEmail.wrapEmailFromContact(data);
    }


    @Override
    public void updateRightsAndValidity(List<String> rights, long start, long end) {
        mRights.clear();
        mRights.addAll(rights);
        showRights(rights, true);
        mTvValidity.setText(getValidationText(start, end));
    }

    @Override
    public void updateRightsAndValidity(INxlFileFingerPrint fp) {
        if (fp == null) {
            return;
        }
        showRights(mFileInfo.getRights(), SkyDRMApp.getInstance().isStewardOf(fp.getOwnerID()));
        bIsExpired = fp.isExpired();
        mTvValidity.setText(fp.formatString());
    }

    @Override
    public void setShareIndicator(boolean active) {
        if (active) {
            showLoadingDialog();
        } else {
            hideLoadingDialog();
        }
    }

    @Override
    public void setLoadingRightsView(boolean active) {
        mLlLoading.setVisibility(active ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateRecipients(List<String> added, List<String> removed) {
        Intent i = new Intent();
        if (added != null && added.size() != 0) {
            i.putStringArrayListExtra(Constant.RECIPIENT_ADD, (ArrayList<String>) added);
        }
        if (removed != null && removed.size() != 0) {
            i.putStringArrayListExtra(Constant.RECIPIENT_REMOVE, (ArrayList<String>) removed);
        }
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void showShareSuccessView() {
        ToastUtil.showToast(getApplicationContext(), "File has been shared successfully.");
        finish();
    }

    @Override
    public void showReShareSuccessView(List<String> newSharedEmails, List<String> alreadySharedEmails) {
        CommonUtils.popupReShareSucceedTip(VaultFileShareActivity.this,
                mRootView, mFileInfo.getName(),
                newSharedEmails,
                alreadySharedEmails);
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(this, e);
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        mAction = intent.getAction();
        if (mAction == null || mAction.isEmpty()) {
            return;
        }

        switch (mAction) {
            case Constant.ACTION_MANAGE:
                resolveManageBundle(intent);
                mPresenter = new VaultFileSharePresenter(mBaseFile, this);
                if (mMetaData == null) {
                    mPresenter.getMetadata();
                }
                break;
            case Constant.ACTION_SHARE:
                resolveShareBundle(intent);
                mPresenter = new VaultFileSharePresenter(mBaseFile, this);
                if (mMetaData == null) {
                    mPresenter.getMetadata();
                }
                break;
            case Constant.ACTION_RESHARE:
                mFileInfo = intent.getParcelableExtra(Constant.RESHARE_ENTRY);
                mPresenter = new VaultFileSharePresenter((SharedWithMeFile) mFileInfo, this);
                if (mFileInfo != null) {
                    mTvName.setText(mFileInfo.getName());
                    mPresenter.getFingerPrint();
                }
                break;
        }
    }

    private void resolveManageBundle(Intent intent) {
        if (intent == null) {
            return;
        }
        List<String> recipients = intent.getStringArrayListExtra(Constant.RECIPIENTS);
        if (recipients != null && recipients.size() != 0) {
            mRecipients.clear();
            mRecipients.addAll(recipients);

            mEmailList.clear();
            mEmailList.addAll(recipients);
        }

        Bundle md = intent.getExtras();
        if (md == null) {
            return;
        }
        mBaseFile = md.getParcelable(Constant.FILE_INFO);
        if (mBaseFile != null) {
            mTvName.setText(mBaseFile.getName());
        }

        mMetaData = (MyVaultMetaDataResult) md.getSerializable(Constant.VAULT_META_DATA_ENTRY);
        if (mMetaData == null) {
            return;
        }
        MyVaultMetaDataResult.ResultsBean results = mMetaData.getResults();
        if (results == null) {
            return;
        }
        MyVaultMetaDataResult.ResultsBean.DetailBean detail = results.getDetail();
        if (detail == null) {
            return;
        }
        List<String> rights = detail.getRights();
        if (rights != null && rights.size() != 0) {
            mRights.clear();
            mRights.addAll(rights);
        }

        mTvValidity.setText(getValidationText(detail.getValidity().getStartDate(),
                detail.getValidity().getEndDate()));
    }

    private void resolveShareBundle(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        mBaseFile = extras.getParcelable(Constant.VAULT_FILE_ENTRY);
        if (mBaseFile == null) {
            return;
        }
        if (mBaseFile instanceof MyVaultFile) {
            MyVaultFile f = (MyVaultFile) mBaseFile;
            mRights = f.getRights();
        }
        mTvName.setText(mBaseFile.getName());
    }

    private void initViewAndEvents() {
        mEtEmailAddress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Drawable drawable = mEtEmailAddress.getCompoundDrawables()[2];
                if (drawable == null) return false;
                if (motionEvent.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (motionEvent.getX() > mEtEmailAddress.getWidth() - mEtEmailAddress.getPaddingLeft() - drawable.getIntrinsicWidth()) {
                    mEtEmailAddress.setFocusableInTouchMode(false);
                    mEtEmailAddress.setFocusable(false);
                    lunchContactPageWithResult(Constant.REQUEST_CODE_SELECT_EMAILS);
                } else {
                    mEtEmailAddress.setFocusableInTouchMode(true);
                    mEtEmailAddress.setFocusable(true);
                }
                return VaultFileShareActivity.super.onTouchEvent(motionEvent);
            }
        });
        shakeAnimator = new ShakeAnimator();
        shakeAnimator.setTarget(mEtEmailAddress);
        initRightsView();
        initListener();
    }

    private void initRightsView() {
        mRightsAdapter = new RightsAdapter(this);
        mRightsView.setAdapter(mRightsAdapter);

        showRights(mRights, true);
    }

    private void initListener() {
        mIbBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mEtEmailAddress.getText().toString())) {
                    mFlEmail.wrapEmail(mEtEmailAddress.getText().append(" ").toString());
                }
                switch (mAction) {
                    case Constant.ACTION_MANAGE:
                        updateRecipients();
                        break;
                    case Constant.ACTION_SHARE:
                        shareProtectedNxl();
                        break;
                    case Constant.ACTION_RESHARE:
                        sharedWitheMeReShare();
                        break;
                }
            }
        });
        mEtEmailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mFlEmail.wrapEmail(s.toString());
            }
        });
        //handle enter key to wrap text user input (add by henry)
        mEtEmailAddress.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    mFlEmail.wrapEmail(mEtEmailAddress.getText().append(" ").toString());
                }
                return false;
            }
        });

        mFlEmail.setOnEmailChangeListener(new FlowLayout.OnEmailChangeListener() {

            @Override
            public void onEmailAdded(String email) {
                mEmailList.add(email);
                if (CommonUtils.checkEmailAddress(email)) {
                    //this list is the total valid emails
                    mValidEmails.add(email);
                } else {
                    mDirtyEmails.add(email);
                }
                if (mDirtyEmails.size() != 0) {
                    hintUserEmptyEmailInput(false, String.format(Locale.getDefault(), getString(R.string.email_is_not_valid), mDirtyEmails.get(0)));
                } else {
                    if (mRecipients != null && mRecipients.size() != 0) {
                        for (String e : mRecipients) {
                            if (mValidEmails.contains(e)) {
                                hintUserEmptyEmailInput(false, "Email:" + e + "has been shared.");
                            }
                        }
                    } else {
                        hintUserEmptyEmailInput(false, "");
                    }
                }
            }

            @Override
            public void onEmailRemoved(String email) {
                mEmailList.remove(email);
                if (CommonUtils.checkEmailAddress(email)) {
                    mValidEmails.remove(email);
                } else {
                    mDirtyEmails.remove(email);
                }
                if (mDirtyEmails.size() != 0) {
                    hintUserEmptyEmailInput(false, String.format(Locale.getDefault(), getString(R.string.email_is_not_valid), mDirtyEmails.get(0)));
                } else {
                    if (mValidEmails.size() == 0) {
                        hintUserEmptyEmailInput(false, getString(R.string.email_is_required));
                    } else {
                        hintUserEmptyEmailInput(false, "");
                    }
                }
            }

            @Override
            public void onEmailAlreadyExists(String email) {

            }
        });
        mFlEmail.setOnClearInputEmailListener(new FlowLayout.OnClearInputEmailListener() {
            @Override
            public void onClear() {
                mEtEmailAddress.setText("");
            }
        });
    }

    private void shareProtectedNxl() {
        if (mDirtyEmails.size() != 0) {
            hintUserEmptyEmailInput(true, getString(R.string.email_is_not_valid));
        } else {
            if (mValidEmails.size() == 0) {
                hintUserEmptyEmailInput(true, getString(R.string.email_is_required));
            } else {
                // judge share whether expired
                if (bIsExpired) { // or directly catch rest api exception then give prompt.
                    ToastUtil.showToast(VaultFileShareActivity.this, getResources().getString(R.string.share_rights_expired));
                    return;
                }
                hideSoftInput(mEtEmailAddress);
                //manageView.share(entry, rights, mValidEmails, commentWidget.getText().toString());
                mPresenter.share(mRights, mValidEmails, commentWidget.getText().toString());
            }
        }
    }

    private void updateRecipients() {
        if (mDirtyEmails.size() != 0) {
            hintUserEmptyEmailInput(true, getString(R.string.email_is_not_valid));
        } else {
            if (mValidEmails.size() == 0) {
                hintUserEmptyEmailInput(true, getString(R.string.email_is_required));
            } else {
                for (String email : mValidEmails) {
                    if (mRecipients != null) {
                        if (mRecipients.contains(email)) {
                            hintUserEmptyEmailInput(true, "Email:" + email + "has been shared.");
                            return;
                        } else {
                            newAddEmails.add(email);
                        }
                    }
                }
                if (newAddEmails.size() == 0) {
                    return;
                }
                hideSoftInput(mEtEmailAddress);
                hintUserEmptyEmailInput(false, "");
                mPresenter.updateRecipients(newAddEmails, null, commentWidget.getText().toString());
            }
        }
    }

    private void sharedWitheMeReShare() {
        if (mDirtyEmails.size() != 0) {
            hintUserEmptyEmailInput(true, getString(R.string.email_is_not_valid));
        } else {
            if (mValidEmails.size() == 0) {
                hintUserEmptyEmailInput(true, getString(R.string.email_is_required));
            } else {
                hideSoftInput(mEtEmailAddress);

                if (!(mFileInfo instanceof SharedWithMeFile)) {
                    finish();
                    return;
                }

                // judge share whether expired
                if (bIsExpired) { // or directly catch rest api exception then give prompt.
                    ToastUtil.showToast(VaultFileShareActivity.this,
                            getResources().getString(R.string.share_rights_expired));
                    return;
                }

                String comments = commentWidget.getText().toString();
                mPresenter.reShare(mValidEmails, comments);
            }
        }
    }

    private void hintUserEmptyEmailInput(boolean animate, String msg) {
        if (mEtEmailAddress == null) {
            return;
        }
        if (shakeAnimator == null) {
            return;
        }
        mTextInputLayout.setError(msg);
        if (!mEtEmailAddress.isFocused()) {
            mEtEmailAddress.requestFocus();
        }
        if (animate) {
            shakeAnimator.startAnimation();
        }
    }

    private String getValidationText(long startDate, long endDate) {
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
        bIsExpired = !new ExpiryChecker().isValidate(startDate, endDate);
        return content;
    }

    private void showRights(List<String> rights, boolean isOwner) {
        mRightsAdapter.showRights(rights);
        // owner has all rights tip info.
//        mTvStewardTip.setVisibility(isOwner ? View.VISIBLE : View.GONE);
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

    @Override
    public void finish() {
        super.finish();
        hideSoftInput(mEtEmailAddress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onDestroy();
            mPresenter = null;
        }
    }
}
