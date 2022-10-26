package com.skydrm.rmc.ui.fragment.projects;

import android.content.Intent;
import android.graphics.Rect;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IInvitePending;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.project.common.SortMenu;
import com.skydrm.rmc.ui.project.service.IProjectContact;
import com.skydrm.rmc.ui.project.service.ProjectPresenter;
import com.skydrm.rmc.ui.project.service.adapter.ViewAllProjectAdapter;
import com.skydrm.rmc.ui.service.search.SearchActivity;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class ProjectsFragment extends BaseFragment implements IProjectContact.IView {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.pending)
    TextView mTvPendingCount;
    @BindView(R.id.create_by_me)
    TextView mTvCreateByMeCount;
    @BindView(R.id.invited_by_other)
    TextView mTvInvitedByOtherCount;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.list_project)
    RecyclerView mRecyclerView;

    @BindView(R.id.pending_comment)
    TextView mTvPendingComment;
    @BindView(R.id.create_by_me_comment)
    TextView mTvCreateByMeComment;
    @BindView(R.id.invited_by_other_comment)
    TextView mTvInvitedByOtherComment;

    @BindView(R.id.pending_container)
    LinearLayout mLlPendingQuickAccessSite;
    @BindView(R.id.owner_by_me_container)
    LinearLayout mLlOwnerByMeQuickAccessSite;
    @BindView(R.id.owner_by_other_container)
    LinearLayout mLlOwnerByOtherQuickAccessSite;

    private ViewAllProjectAdapter mProjectAdapter;
    private LinearLayoutManager mLayoutManager;

    private int mPendingCount;
    private int mOwnerByMeCount;
    private int mOwnerByOtherCount;

    private IProjectContact.IPresenter mPresenter;
    private SortType mSortType = SortType.NAME_ASCEND;

    public static ProjectsFragment newInstance() {
        return new ProjectsFragment();
    }

    @Override
    protected void onUserFirstVisible() {
        mPresenter.getProjects(0);
        mPresenter.getPendingInvitation();
    }

    @Override
    protected void onUserVisible() {
        mPresenter.getProjects(0);
        mPresenter.getPendingInvitation();
    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected void initViewAndEvents() {
        mPresenter = new ProjectPresenter(this);
        mPresenter.updateSortType(mSortType);

        mToolbar.setNavigationIcon(R.drawable.icon_back_white);
        mToolbar.inflateMenu(R.menu.menu_project_all_projects);

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_sort:
                        showSortMenu();
                        break;
                    case R.id.action_search:
                        lunchToSearchPage();
                        break;
                }
                return false;
            }
        });
        initRecyclerView();
        initListener();
    }

    @Override
    protected View getLoadingTargetView() {
        return mSwipeRefreshLayout;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_view_all_proejcts;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    @Override
    public void onInitialize(boolean active) {
        mSwipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showLoadingIndicator(boolean active) {
        mSwipeRefreshLayout.setRefreshing(active);
    }

    @Override
    public void showCreatedByMeProjects(List<IProject> createdByMe) {
        if (createdByMe != null) {
            mOwnerByMeCount = createdByMe.size();
            mTvCreateByMeCount.setText(String.valueOf(mOwnerByMeCount));
            if (mOwnerByMeCount < 2) {
                mTvCreateByMeComment.setText(R.string.project);
            } else {
                mTvCreateByMeComment.setText(R.string.Projects);
            }
        }
        mProjectAdapter.setOwnerByMeData(createdByMe);
    }

    @Override
    public void showInvitedByOtherProjects(List<IProject> invitedByOther) {
        int pendingCount = 0;
        int ownerByOtherCount = 0;
        List<IProject> other = new ArrayList<>();
        List<IProject> pending = new ArrayList<>();
        if (invitedByOther != null) {
            for (IProject p : invitedByOther) {
                if (p.isPendingInvite()) {
                    pendingCount++;
                    pending.add(p);
                } else {
                    ownerByOtherCount++;
                    other.add(p);
                }
            }
            mPendingCount = pendingCount;
            mOwnerByOtherCount = ownerByOtherCount;
            mTvPendingCount.setText(String.valueOf(mPendingCount));
            mTvInvitedByOtherCount.setText(String.valueOf(mOwnerByOtherCount));

            if (mPendingCount < 2) {
                mTvPendingComment.setText(R.string.project);
            } else {
                mTvPendingComment.setText(R.string.Projects);
            }

            if (mOwnerByOtherCount < 2) {
                mTvInvitedByOtherComment.setText(R.string.project);
            } else {
                mTvInvitedByOtherComment.setText(R.string.Projects);
            }
            mProjectAdapter.setOwnerByOtherAndPendingData(pending, other);
        }
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(_activity, e);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onDestroy();
            mPresenter = null;
        }
    }

    private void initListener() {
        initToolbarNavi(mToolbar, false);
        mLlPendingQuickAccessSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLayoutManager.scrollToPositionWithOffset(0, 0);
            }
        });
        mLlOwnerByMeQuickAccessSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOwnerByMeCount != 0) {
                    mLayoutManager.scrollToPositionWithOffset(mPendingCount, 0);
                }
            }
        });
        mLlOwnerByOtherQuickAccessSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOwnerByOtherCount != 0) {
                    mLayoutManager.scrollToPositionWithOffset(mPendingCount + mOwnerByMeCount + 2, 0);
                }
            }
        });
    }

    private void initRecyclerView() {
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.main_green_light),
                getResources().getColor(R.color.color_blue));
        mLayoutManager = new LinearLayoutManager(_activity);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mProjectAdapter = new ViewAllProjectAdapter(_activity);
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(30));
        mRecyclerView.setAdapter(mProjectAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.refresh();
            }
        });
        mProjectAdapter.setOnInvitationItemClickListener(new ViewAllProjectAdapter.OnInvitationItemClickListener() {
            @Override
            public void onAccept(IInvitePending pending, View loadingBar, int pos) {
                mPresenter.acceptInvitation(pending, loadingBar);
            }

            @Override
            public void onDeny(IInvitePending pending, View loadingBar, int pos) {
                showDenyDialog(pending, loadingBar);
            }
        });
    }

    private void showDenyDialog(final IInvitePending pending, final View loadingBar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
        final View ignoreView = LayoutInflater.from(_activity).inflate(R.layout.layout_ignore_invite_dialog, null);
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

    private void showSortMenu() {
        SortMenu menu = new SortMenu(_activity,
                new SortMenu.OnSortByItemSelectListener() {
                    @Override
                    public void onItemSelected(SortType type) {
                        mSortType = type;
                        mPresenter.sort(type);
                    }
                });
        menu.setSortType(mSortType);
        menu.setModifyDateText();
        menu.hideSizeButton();
        menu.showAtLocation(mToolbar, Gravity.TOP, 0, 0);
    }

    private void lunchToSearchPage() {
        Intent i = new Intent(_activity, SearchActivity.class);
        i.setAction(Constant.ACTION_SEARCH_ALL_PROJECTS);
        startActivity(i);
    }

    class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        int mSpace;

        /**
         * Retrieve any offsets for the given item. Each field of <code>outRect</code> specifies
         * the number of pixels that the item view should be inset by, similar to padding or margin.
         * The default implementation sets the bounds of outRect to 0 and returns.
         * <p>
         * <p>
         * If this ItemDecoration does not affect the positioning of item views, it should set
         * all four fields of <code>outRect</code> (left, top, right, bottom) to zero
         * before returning.
         * <p>
         * <p>
         * If you need to access Adapter for additional data, you can call
         * {@link RecyclerView#getChildAdapterPosition(View)} to get the adapter position of the
         * View.
         *
         * @param outRect Rect to receive the output.
         * @param view    The child view to decorate
         * @param parent  RecyclerView this ItemDecoration is decorating
         * @param state   The current state of RecyclerView.
         */

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.left = mSpace;
            outRect.right = mSpace;
            outRect.bottom = mSpace;
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = mSpace;
            }
        }

        public SpaceItemDecoration(int space) {
            this.mSpace = space;
        }
    }
}
