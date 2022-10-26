package com.skydrm.rmc.ui.project.feature.member.info;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.datalayer.repo.project.IMemberDetail;
import com.skydrm.rmc.datalayer.repo.project.IPendingMember;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.project.service.message.MsgRevokeProjectMember;
import com.skydrm.rmc.ui.widget.avatar.AvatarPlaceholder;
import com.skydrm.rmc.ui.widget.avatar.AvatarView;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MemberInfoActivity extends BaseActivity implements IMemberInfoContact.IView {
    @BindView(R.id.people_particulars_back)
    ImageButton mIbBack;
    @BindView(R.id.people_particulars_avatarView)
    AvatarView mAvatarView;
    @BindView(R.id.people_particulars_name)
    TextView mTvParticularName;
    @BindView(R.id.people_particulars_email)
    TextView mTvParticularEmail;

    @BindView(R.id.joined_container)
    RelativeLayout mRlJoinedContainer;
    @BindView(R.id.people_particulars_date)
    TextView mTvParticularDate;

    @BindView(R.id.pending_invited_by_name_container)
    LinearLayout mLlPendingInviteContainer;
    @BindView(R.id.pending_invited_date_container)
    LinearLayout mLlPendingInviteDateContainer;

    @BindView(R.id.remove_and_invited_by_name_container)
    RelativeLayout mRlRemoveContainer;
    @BindView(R.id.member_invited_by_name_container)
    LinearLayout mLlInvitedNameContainer;
    @BindView(R.id.pending_invited_by_name)
    TextView mTvPendingName;
    @BindView(R.id.pending_invited_date)
    TextView mTvPendingDate;

    @BindView(R.id.resend_and_revoke_container)
    RelativeLayout mRlResendAndRevokeContainer;
    @BindView(R.id.resend_invitation)
    Button mBtResendInvitation;
    @BindView(R.id.revoke_invitation)
    Button mBtRevokeInvitation;
    @BindView(R.id.people_remove_member)
    Button mBtRemove;

    @BindView(R.id.people_invited_by_name)
    TextView mTvInvitorName;

    private ProgressDialog mProgressDialog;

    private IMember mMember;
    private boolean ownerByMe;

    private IMemberInfoContact.IPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_info);
        ButterKnife.bind(this);
        resolveIntent();
        initViewAndEvents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onDestroy();
            mPresenter = null;
        }
    }

    @Override
    public void showInvitorName(IMemberDetail detail) {
        mTvInvitorName.setText(detail.getInviterDisplayName());
    }

    @Override
    public void showLoadingIndicator(boolean active) {
        if (active) {
            showProgressDialog();
        } else {
            dismissProgressDialog();
        }
    }

    @Override
    public void onActionSuccess(String msg, MemberActionTask.Type type, IMember target) {
        if (type == MemberActionTask.Type.REVOKE || type == MemberActionTask.Type.REMOVE) {
            EventBus.getDefault().post(new MsgRevokeProjectMember(target));
        }
        ToastUtil.showToast(getApplicationContext(), msg);
        finish();
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(this, e);
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        mMember = intent.getParcelableExtra(Constant.MEMBER_DETAIL);
        ownerByMe = intent.getBooleanExtra(Constant.PROJECT_OWNER_BY_ME, false);
    }

    private void initViewAndEvents() {
        mPresenter = new MemberInfoPresenter(mMember, this);
        initWidgetVisible();
        if (ownerByMe) {
            if (mMember.isPending()) {
                //pending
                IPendingMember pendingMember = (IPendingMember) mMember;

                mLlPendingInviteContainer.setVisibility(View.VISIBLE);
                mTvPendingName.setVisibility(View.VISIBLE);
                mTvParticularEmail.setVisibility(View.VISIBLE);
                mLlPendingInviteDateContainer.setVisibility(View.VISIBLE);
                mTvPendingDate.setVisibility(View.VISIBLE);
                mRlResendAndRevokeContainer.setVisibility(View.VISIBLE);
                mBtResendInvitation.setVisibility(View.VISIBLE);
                mBtRevokeInvitation.setVisibility(View.VISIBLE);

                mTvPendingName.setText(pendingMember.getInviterDisplayName());
                mTvParticularEmail.setText(pendingMember.getInviteeEmail());
                mTvPendingDate.setText(TimeUtil.formatData(pendingMember.getInviteTime()));

                mAvatarView.setImageDrawable(new AvatarPlaceholder(this, pendingMember.getInviteeEmail(),
                        30, "", " "));
            } else {
                //active
                if (mMember.isOwner()) {
                    mTvParticularName.setVisibility(View.VISIBLE);
                    mTvParticularEmail.setVisibility(View.VISIBLE);
                    mRlJoinedContainer.setVisibility(View.VISIBLE);
                    mTvParticularDate.setVisibility(View.VISIBLE);

                    mTvParticularName.setText(mMember.getDisplayName());
                    mTvParticularEmail.setText(mMember.getEmail());
                    mTvParticularDate.setText(TimeUtil.formatData(mMember.getCreationTime()));
                    mAvatarView.setImageDrawable(new AvatarPlaceholder(this, mMember.getDisplayName(),
                            30, "", " "));
                } else {
                    mTvParticularEmail.setVisibility(View.VISIBLE);
                    mRlJoinedContainer.setVisibility(View.VISIBLE);
                    mTvParticularDate.setVisibility(View.VISIBLE);
                    mRlRemoveContainer.setVisibility(View.VISIBLE);
                    mBtRemove.setVisibility(View.VISIBLE);
                    mLlInvitedNameContainer.setVisibility(View.VISIBLE);
                    mTvInvitorName.setVisibility(View.VISIBLE);

                    mTvParticularEmail.setText(mMember.getEmail());
                    mTvParticularDate.setText(TimeUtil.formatData(mMember.getCreationTime()));
                    mAvatarView.setImageDrawable(new AvatarPlaceholder(this, mMember.getDisplayName(),
                            30, "", " "));

                    mPresenter.getMemberDetail();
                }
            }
        } else {
            if (mMember.isPending()) {
                //pending
                //pending
                IPendingMember pendingMember = (IPendingMember) mMember;
                mTvParticularEmail.setVisibility(View.VISIBLE);
                mLlPendingInviteContainer.setVisibility(View.VISIBLE);
                mTvPendingName.setVisibility(View.VISIBLE);
                mLlPendingInviteDateContainer.setVisibility(View.VISIBLE);
                mTvPendingDate.setVisibility(View.VISIBLE);
                mTvParticularEmail.setText(pendingMember.getInviteeEmail());
                mTvPendingName.setText(pendingMember.getInviterDisplayName());
                mTvPendingDate.setText(TimeUtil.formatData(pendingMember.getInviteTime()));
                mAvatarView.setImageDrawable(new AvatarPlaceholder(this, pendingMember.getInviteeEmail(),
                        30, "", " "));
            } else {
                //active
                if (mMember.isOwner()) {
                    mTvParticularEmail.setVisibility(View.VISIBLE);
                    mRlJoinedContainer.setVisibility(View.VISIBLE);
                    mTvParticularDate.setVisibility(View.VISIBLE);

                    mTvParticularEmail.setText(mMember.getEmail());
                    mTvParticularDate.setText(TimeUtil.formatData(mMember.getCreationTime()));
                    mAvatarView.setImageDrawable(new AvatarPlaceholder(this, mMember.getDisplayName(),
                            30, "", " "));
                } else {
                    mTvParticularName.setVisibility(View.VISIBLE);
                    mTvParticularEmail.setVisibility(View.VISIBLE);
                    mRlRemoveContainer.setVisibility(View.VISIBLE);
                    mRlJoinedContainer.setVisibility(View.VISIBLE);
                    mTvParticularDate.setVisibility(View.VISIBLE);
                    mLlInvitedNameContainer.setVisibility(View.VISIBLE);
                    mTvInvitorName.setVisibility(View.VISIBLE);

                    mTvParticularName.setText(mMember.getDisplayName());
                    mTvParticularEmail.setText(mMember.getEmail());
                    mTvParticularDate.setText(TimeUtil.formatData(mMember.getCreationTime()));

                    mAvatarView.setImageDrawable(new AvatarPlaceholder(this, mMember.getDisplayName(),
                            30, "", " "));

                    mPresenter.getMemberDetail();
                }
            }
        }
        mIbBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBtResendInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendInvitation();
            }
        });
        mBtRevokeInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revokeInvitation();
            }
        });
        mBtRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeMember();
            }
        });
    }

    private void initWidgetVisible() {
        mTvParticularName.setVisibility(View.GONE);
        mTvParticularEmail.setVisibility(View.GONE);
        mTvParticularDate.setVisibility(View.GONE);
        mRlRemoveContainer.setVisibility(View.GONE);
        mLlPendingInviteContainer.setVisibility(View.GONE);
        mLlPendingInviteDateContainer.setVisibility(View.GONE);
        mTvPendingName.setVisibility(View.GONE);
        mTvPendingDate.setVisibility(View.GONE);
        mBtResendInvitation.setVisibility(View.GONE);
        mBtRevokeInvitation.setVisibility(View.GONE);
        mRlJoinedContainer.setVisibility(View.GONE);
        mRlResendAndRevokeContainer.setVisibility(View.GONE);
        mLlInvitedNameContainer.setVisibility(View.GONE);
        mBtRemove.setVisibility(View.GONE);

        mTvInvitorName.setVisibility(View.GONE);
    }

    private void resendInvitation() {
        mPresenter.resendInvitation();
    }

    private void revokeInvitation() {
        mPresenter.revokeInvitation();
    }

    private void removeMember() {
        mPresenter.removeMember();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(MemberInfoActivity.this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMessage(getString(R.string.common_waiting_initcap_3dots));
        }
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
