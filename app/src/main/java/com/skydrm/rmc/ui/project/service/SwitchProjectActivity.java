package com.skydrm.rmc.ui.project.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.project.IInvitePending;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.activity.home.HomeActivity;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.common.SpacesItemDecoration;
import com.skydrm.rmc.ui.project.ProjectActivity;
import com.skydrm.rmc.ui.project.service.adapter.ProjectCreatedByMeAdapter;
import com.skydrm.rmc.ui.project.service.adapter.ProjectInvitedByOtherAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SwitchProjectActivity extends BaseActivity implements IProjectContact.IView {
    @BindView(R.id.projects_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.to_MySpace)
    ImageView mIv2MySpace;
    @BindView(R.id.go_to_MySpace)
    RelativeLayout mRlGoToMySpace;
    @BindView(R.id.tv_switch_to)
    TextView mTvSwitchTo;
    @BindView(R.id.create_new_project)
    LinearLayout mLlNewProjectSite;
    @BindView(R.id.project_owner_by_me)
    RecyclerView mRvOwnerByMe;
    @BindView(R.id.project_owner_by_other)
    RecyclerView mRvInvitedByOther;

    @BindView(R.id.bt_update_owner_by_me)
    TextView mTvUpdateOwnerByMe;
    @BindView(R.id.bt_update_owner_by_other)
    TextView mTvUpdateOwnerByOther;

    @BindView(R.id.tv_created_by_me_num)
    TextView mTvCreatedByMeCount;
    @BindView(R.id.tv_invited_by_other_num)
    TextView mTvInvitedByOtherCount;

    @BindView(R.id.project_activate_container)
    LinearLayout mLlProjectActivateContainer;
    @BindView(R.id.project_owner_by_me_container)
    LinearLayout mLlProjectCreatedByMeContainer;
    @BindView(R.id.project_owner_by_other_container)
    LinearLayout mLlProjectInvitedByOtherContainer;

    @BindView(R.id.bt_active_project)
    Button mBtActiveProject;

    private int flagFrom;
    int projectSpacingInPixels = 40;


    private ProjectCreatedByMeAdapter mCreatedByMeAdapter;
    private ProjectInvitedByOtherAdapter mInviteByOtherAdapter;

    private IProjectContact.IPresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_switch_to_other_projects);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        flagFrom = intent.getIntExtra(Constant.KEY, Constant.FLAG_FROM_PROJECT);
        initViewAndEvents();
        mPresenter = new ProjectPresenter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.getProjects(0);
        mPresenter.getPendingInvitation();
    }

    @Override
    protected View getLoadingTargetView() {
        return null;
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
    public void onInitialize(boolean active) {

    }

    @Override
    public void showLoadingIndicator(boolean active) {

    }

    @Override
    public void showCreatedByMeProjects(List<IProject> createdByMe) {
        if (createdByMe != null && createdByMe.size() != 0) {
            if (mLlNewProjectSite.getVisibility() == View.GONE && !SkyDRMApp.getInstance().isOnPremise()) {
                mLlNewProjectSite.setVisibility(View.VISIBLE);
            }
            if (mLlProjectCreatedByMeContainer.getVisibility() == View.GONE) {
                mLlProjectCreatedByMeContainer.setVisibility(View.VISIBLE);
            }
//            if (mLlProjectActivateContainer.getVisibility() == View.VISIBLE) {
//                mLlProjectActivateContainer.setVisibility(View.GONE);
//            }
            if (mTvUpdateOwnerByMe.getVisibility() == View.VISIBLE) {
                mTvUpdateOwnerByMe.setVisibility(View.INVISIBLE);
            }
            mTvCreatedByMeCount.setText(String.valueOf(createdByMe.size()));
            mCreatedByMeAdapter.setData(createdByMe);
        } else {
            if (mLlNewProjectSite.getVisibility() == View.VISIBLE) {
                mLlNewProjectSite.setVisibility(View.GONE);
            }
            if (mLlProjectCreatedByMeContainer.getVisibility() != View.GONE) {
                mLlProjectCreatedByMeContainer.setVisibility(View.GONE);
            }
//            if (mLlProjectActivateContainer.getVisibility() != View.VISIBLE) {
//                mLlProjectActivateContainer.setVisibility(View.VISIBLE);
//            }
        }
    }

    @Override
    public void showInvitedByOtherProjects(List<IProject> invitedByOther) {
        if (invitedByOther != null && invitedByOther.size() != 0) {
            if (mLlProjectInvitedByOtherContainer.getVisibility() == View.GONE) {
                mLlProjectInvitedByOtherContainer.setVisibility(View.VISIBLE);
            }
            if (mTvUpdateOwnerByOther.getVisibility() == View.VISIBLE) {
                mTvUpdateOwnerByOther.setVisibility(View.INVISIBLE);
            }
            mTvInvitedByOtherCount.setText(String.valueOf(invitedByOther.size()));
            mInviteByOtherAdapter.setData(invitedByOther);
        } else {
            if (mLlProjectInvitedByOtherContainer.getVisibility() != View.GONE) {
                mLlProjectInvitedByOtherContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(this, e);
    }

    private void initViewAndEvents() {
        initToolbar();
        initRecyclerView();
        initListener();
    }

    private void initToolbar() {
        switch (flagFrom) {
            case Constant.FLAG_FROM_SPACE:
                mToolbar.setTitle(getString(R.string.mySpace));
                mToolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
                mRlGoToMySpace.setVisibility(View.GONE);
                mTvSwitchTo.setVisibility(View.VISIBLE);
                break;
            case Constant.FLAG_FROM_PROJECT:
                String projectName = getIntent().getStringExtra("project_name");
                mToolbar.setTitle(projectName);
                mToolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
                mRlGoToMySpace.setVisibility(View.VISIBLE);
                mTvSwitchTo.setVisibility(View.VISIBLE);
                break;
            case Constant.FLAG_FROM_WORKSPACE:
                mToolbar.setTitle(getString(R.string.name_workspace));
                mToolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
                mRlGoToMySpace.setVisibility(View.GONE);
                mTvSwitchTo.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void initRecyclerView() {
        initCreatedByMe();
        initInviteByOther();
    }

    private void initCreatedByMe() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        mRvOwnerByMe.setLayoutManager(layoutManager);
        mRvOwnerByMe.addItemDecoration(new SpacesItemDecoration(projectSpacingInPixels));

        mCreatedByMeAdapter = new ProjectCreatedByMeAdapter(this);
        mCreatedByMeAdapter.setOnItemClickListener(new ProjectCreatedByMeAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(IProject p) {
                handlerProjectCardClick(p);
            }
        });
        mRvOwnerByMe.setAdapter(mCreatedByMeAdapter);
        mTvUpdateOwnerByMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mPresenter.getProjects(1);
                if (mTvUpdateOwnerByMe.getVisibility() == View.VISIBLE) {
                    mTvUpdateOwnerByMe.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initInviteByOther() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        mRvInvitedByOther.setLayoutManager(layoutManager);
        mRvInvitedByOther.addItemDecoration(new SpacesItemDecoration(projectSpacingInPixels));
        mInviteByOtherAdapter = new ProjectInvitedByOtherAdapter(this);

        mRvInvitedByOther.setAdapter(mInviteByOtherAdapter);

        mTvUpdateOwnerByOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.getProjects(1);
                if (mTvUpdateOwnerByOther.getVisibility() == View.VISIBLE) {
                    mTvUpdateOwnerByOther.setVisibility(View.INVISIBLE);
                }
            }
        });
        mInviteByOtherAdapter.setOnInvitationItemClickListener(new ProjectInvitedByOtherAdapter.
                OnInvitationItemClickListener() {
            @Override
            public void onAccept(IInvitePending pending, View loadingBar, int pos) {
                mPresenter.acceptInvitation(pending, loadingBar);
            }

            @Override
            public void onDeny(IInvitePending pending, View loadingBar, int pos) {
                showDenyDialog(pending, loadingBar);
            }
        });
        mInviteByOtherAdapter.setOnItemClickListener(new ProjectInvitedByOtherAdapter.
                OnItemClickListener() {
            @Override
            public void onItemClick(IProject p, int position) {
                handlerProjectCardClick(p);
            }
        });
    }

    private void showDenyDialog(final IInvitePending pending, final View loadingBar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View ignoreView = LayoutInflater.from(this).inflate(R.layout.layout_ignore_invite_dialog, null);
        final EditText et_ignore = ignoreView.findViewById(R.id.ev_ignoreReason);
        builder.setCancelable(false);
        builder.setView(ignoreView);
        builder.setPositiveButton(this.getResources().getString(R.string.Decline), null);
        builder.setNegativeButton(this.getResources().getString(R.string.common_cancel_initcap), null);
        builder.setTitle(R.string.app_name);
        builder.setMessage(this.getResources().getString(R.string.hint_msg_ask_ignore_reason));
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reason = et_ignore.getText().toString();
                dialog.dismiss();
                mPresenter.denyInvitation(pending, reason, loadingBar);
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void initListener() {
        mIv2MySpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mRlGoToMySpace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunchToHomeMySpacePage();
            }
        });

        mLlProjectActivateContainer.setVisibility(View.GONE);
        if (SkyDRMApp.getInstance().isOnPremise()) {
            mLlNewProjectSite.setVisibility(View.GONE);
        } else {
            mLlNewProjectSite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lunchNewProjectActivity();
                }
            });
            mBtActiveProject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lunch2ProjectSplashActivity();
                }
            });
        }
    }

    private void lunchToHomeMySpacePage() {
        Intent i = new Intent(this, HomeActivity.class);
        i.setAction(Constant.ACTION_LUNCH_HOME_FROM_SWITCH_PROJECT_PAGE);
        startActivity(i);
    }

    private void handlerProjectCardClick(IProject p) {
        Intent intentProjectActivity = new Intent(this, ProjectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constant.PROJECT_DETAIL, (Parcelable) p);
        bundle.putInt(Constant.PROJECT_INDEX, 0);
        intentProjectActivity.putExtras(bundle);
        startActivity(intentProjectActivity);
    }

    private void lunchNewProjectActivity() {
        Intent newProjectIntent = new Intent(this, NewProjectActivity.class);
        startActivity(newProjectIntent);
    }

    private void lunch2ProjectSplashActivity() {
        Intent intent = new Intent(this, NewProjectSplashActivity.class);
        startActivity(intent);
    }
}
