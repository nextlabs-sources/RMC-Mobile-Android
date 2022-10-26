package com.skydrm.rmc.ui.service.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.datalayer.repo.project.Member;
import com.skydrm.rmc.datalayer.repo.project.ProjectFile;
import com.skydrm.rmc.datalayer.repo.project.ProjectNode;
import com.skydrm.rmc.datalayer.repo.project.SharedWithProjectFile;
import com.skydrm.rmc.datalayer.repo.sharedwithme.SharedWithMeFile;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceFile;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceNode;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceRepo;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.engine.eventBusMsg.NxFileDeleteEvent;
import com.skydrm.rmc.engine.eventBusMsg.favorites.FavoriteStatusChangeFromMorePageEvent;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.ui.DividerItemDecoration;
import com.skydrm.rmc.ui.adapter.NXFileRecyclerViewAdapter;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.common.DeleteNxlFileTask;
import com.skydrm.rmc.ui.common.ItemClickListener;
import com.skydrm.rmc.ui.common.NxlAdapter;
import com.skydrm.rmc.ui.common.NxlFileItem;
import com.skydrm.rmc.ui.common.NxlItemHelper;
import com.skydrm.rmc.ui.common.RightMenuItemClickListener;
import com.skydrm.rmc.ui.service.favorite.model.FavoriteAdapter;
import com.skydrm.rmc.ui.service.favorite.model.IFavoriteFile;
import com.skydrm.rmc.ui.service.favorite.model.bean.FavoriteItem;
import com.skydrm.rmc.ui.myspace.MySpaceFileCtxMenu;
import com.skydrm.rmc.ui.myspace.MySpaceFileItemHelper;
import com.skydrm.rmc.ui.service.log.ActivityLogAdapter;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.IVaultFileLog;
import com.skydrm.rmc.ui.myspace.myvault.view.widget.MyVaultFileMenu;
import com.skydrm.rmc.ui.service.offline.IOfflineCallback;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.rmc.ui.project.common.FileContextMenu;
import com.skydrm.rmc.ui.project.common.MsgFileNotFound;
import com.skydrm.rmc.ui.project.feature.member.MemberItem;
import com.skydrm.rmc.ui.project.feature.member.ProjectMemberAdapter;
import com.skydrm.rmc.ui.project.feature.member.info.MemberInfoActivity;
import com.skydrm.rmc.ui.project.service.ProjectOperateActivity;
import com.skydrm.rmc.ui.project.service.adapter.ViewAllProjectAdapter;
import com.skydrm.rmc.ui.project.service.message.MsgNavigate2TargetFolder;
import com.skydrm.rmc.ui.project.service.message.MsgRevokeProjectMember;
import com.skydrm.rmc.ui.service.share.ShareActivity;
import com.skydrm.rmc.ui.myspace.sharewithme.OnItemClickListener;
import com.skydrm.rmc.ui.myspace.sharewithme.view.SharedWithMeFileMenu;
import com.skydrm.rmc.ui.widget.swipelayout.SwipeMenuRecyclerView;
import com.skydrm.rmc.ui.workspace.MsgNavigate2WorkSpaceFolder;
import com.skydrm.rmc.ui.workspace.WorkSpaceFileContextMenu;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends BaseActivity implements ISearchContact.IView {
    @BindView(R.id.root_view)
    LinearLayout mRootView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.search_view)
    EditText mEtSearchView;
    @BindView(R.id.delete_text)
    ImageView mIvDelete;
    @BindView(R.id.recyclerView)
    SwipeMenuRecyclerView mSwipeMenuRecyclerView;
    @BindView(R.id.home_files_category_layout2)
    RelativeLayout mCategoryLayout;
    @BindView(R.id.home_files_back2)
    TextView mTvBack;
    @BindView(R.id.home_files_current_category2)
    TextView mCategoryName;

    private Context mCtx;
    private String mAction;

    private ISearchContact.IPresenter mPresenter;
    private NxlAdapter mNxlAdapter;
    private FavoriteAdapter mFavAdapter;
    private NXFileRecyclerViewAdapter mRepoAdapter;
    private ProjectMemberAdapter mProjectMemberAdapter;
    private ActivityLogAdapter mLogAdapter;
    private ViewAllProjectAdapter mAllProjectAdapter;

    private MyVaultFileMenu mMyVaultFileMenu;
    private SharedWithMeFileMenu mSharedWithMeFileMenu;
    private FileContextMenu mProjectFileCtxMenu;
    private MySpaceFileCtxMenu mSpaceFileCtxMenu;
    private WorkSpaceFileContextMenu mWorkSpaceFileCtxMenu;

    private boolean showEmptyView;
    private IProject mProject;
    private DeleteNxlFileCallback mDeleteNxlFileCallback;
    private String mVaultType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCtx = this;
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        if (resolveIntent()) {
            initViewAndEvents();
        } else {
            finish();
        }
    }

    @Override
    protected View getLoadingTargetView() {
        return mSwipeMenuRecyclerView;
    }

    @Override
    public void finish() {
        super.finish();
        hideSoftInput(mEtSearchView);
    }

    @Override
    public void onInitialize(boolean active) {
        if (active) {
            showLoading("");
        } else {
            hideLoading();
        }
    }

    @Override
    public void showEmptyView(boolean active) {
        if (active) {
            showEmpty("");
        } else {
            hideEmpty();
        }
    }

    @Override
    public void showNoSearchResultView(boolean active) {
        if (active) {
            showEmpty(getString(R.string.empty_search));
        } else {
            hideEmpty();
        }
    }

    @Override
    public void updateNxlItem(List<NxlFileItem> data) {
        mNxlAdapter.setData(data);
    }

    @Override
    public void updateFavItem(List<FavoriteItem> data) {
        mFavAdapter.setData(data);
    }

    @Override
    public void updateRepoItem(List<NXFileItem> data) {
        mRepoAdapter.setData(data);
    }

    @Override
    public void updateMemberItem(List<MemberItem> data) {
        mProjectMemberAdapter.setData(data);
    }

    @Override
    public void updateLogItem(List<IVaultFileLog> data) {
        mLogAdapter.setTotalCount(data.size());
        mLogAdapter.setData(data);
    }

    @Override
    public void updateProjectItem(List<IProject> data) {
        mAllProjectAdapter.setAllData(data);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveProjectMemberRemovedMsg(MsgRevokeProjectMember msg) {
        if (mPresenter != null) {
            mPresenter.updateProjectMemberItem(msg.mTarget);
        }
    }

    /**
     * Subscriber for delete file
     *
     * @param event {@link NxFileDeleteEvent}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeleteFile(NxFileDeleteEvent event) {
        if (mPresenter != null) {
            mPresenter.deleteMyDriveFile(event.getDeletedItem());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateMyDriveFavStatus(FavoriteStatusChangeFromMorePageEvent event) {
        if (inFavMode()) {
            mFavAdapter.removeItem(event.mFile);
        }
        if (inRepoMode()) {
            if (event.favorite) {
                markAsFavorite(event.mFile, -1);
            } else {
                unMarkAsFavorite(event.mFile, -1);
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveFileNotFoundMsg(MsgFileNotFound msg) {
        finish();
    }

    private boolean resolveIntent() {
        Intent i = getIntent();
        if (i == null) {
            return false;
        }
        mAction = i.getAction();
        return mAction != null && !mAction.isEmpty();
    }

    public void initViewAndEvents() {
        setViewHelperController(mSwipeMenuRecyclerView);
        mToolbar.setNavigationIcon(R.drawable.icon_back_white);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initRecyclerViewByAction();
        mEtSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //hide show delete site(for clear text user input use)
                if (mEtSearchView.getText().toString().length() > 0) {
                    mIvDelete.setVisibility(View.VISIBLE);
                } else {
                    mIvDelete.setVisibility(View.GONE);
                }
                //do search action
                if (TextUtils.isEmpty(s.toString())) {
                    showEmptyView = true;
                    showNoSearchResultView(true);
                    return;
                }
                if (showEmptyView) {
                    hideEmpty();
                    showEmptyView = false;
                }
                mPresenter.searchByName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mIvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEtSearchView.setText("");
            }
        });
        mPresenter = new SearchPresenter(this);
        mPresenter.initialize(mAction);
    }

    private void initRecyclerViewByAction() {
        mSwipeMenuRecyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        mSwipeMenuRecyclerView.addItemDecoration(new DividerItemDecoration(mCtx, null,
                false, true));
        mSwipeMenuRecyclerView.configAnimator();
        if (inFavMode()) {
            initFavAdapter();
            mSwipeMenuRecyclerView.setAdapter(mFavAdapter);
        } else if (inRepoMode()) {
            initRepoAdapter();
            mSwipeMenuRecyclerView.setAdapter(mRepoAdapter);
        } else if (inProjectMemberMode()) {
            initProjectMemberAdapter();
            mSwipeMenuRecyclerView.setAdapter(mProjectMemberAdapter);
        } else if (inLogMode()) {
            initLogAdapter();
            mSwipeMenuRecyclerView.setAdapter(mLogAdapter);
        } else if (inProjectItemMode()) {
            initProjectAdapter();
            mSwipeMenuRecyclerView.setAdapter(mAllProjectAdapter);
        } else {
            initNxlAdapter();
            mSwipeMenuRecyclerView.setAdapter(mNxlAdapter);
        }
    }

    private void initNxlAdapter() {
        mNxlAdapter = new NxlAdapter(this);
        if (inProjectMode() || inProjectOfflineMode()) {
            boolean createdByMe = getIntent().getBooleanExtra("is_created_by_me", false);
            mNxlAdapter.setCreatedByMe(createdByMe);
            mNxlAdapter.setOnItemClickListener(new NxlAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(INxlFile f, int pos) {
                    if (f.isFolder()) {
                        EventBus.getDefault().post(new MsgNavigate2TargetFolder(f.getPathId(),
                                f.getPathDisplay()));
                        finish();
                    } else {
                        NxlItemHelper.viewFile(mCtx, 0, "", f);
                    }
                }
            });
        } else if (inWorkSpaceMode() || inWorkSpaceOfflineMode()) {
            mNxlAdapter.setOnItemClickListener(new NxlAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(INxlFile f, int pos) {
                    if (f.isFolder()) {
                        EventBus.getDefault().post(new MsgNavigate2WorkSpaceFolder(f.getPathId(),
                                f.getPathDisplay()));
                        finish();
                    } else {
                        NxlItemHelper.viewFile(SearchActivity.this, f);
                    }
                }
            });
        } else {
            mNxlAdapter.setOnItemClickListener(new ItemClickListener(this));
        }
        if (inSharedWithMeMode()) {
            mNxlAdapter.setDisableLeftSwipeMenu(true);
            mNxlAdapter.setDisableRightSwipeMenu(true);
        } else if (inProjectMode() || inProjectOfflineMode()) {
            mNxlAdapter.setDisableLeftSwipeMenu(true);
            int id = getIntent().getIntExtra("project_id", -1);
            String name = getIntent().getStringExtra("project_name");
            mNxlAdapter.setOnRightMenuItemClickListener(new RightMenuItemClickListener(this, id, name));
        } else {
            mNxlAdapter.setDisableLeftSwipeMenu(true);
            mNxlAdapter.setOnRightMenuItemClickListener(new RightMenuItemClickListener(this));
        }
        mNxlAdapter.setOnMenuToggleListener(new NxlAdapter.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(INxlFile f, int pos) {
                showComplexFileCtxMenu(f, pos);
            }
        });

        if (inSharedWithMeMode()) {
            initSharedWithMeFileMenu();
        } else if (inOfflineMode()) {
            initSharedWithMeFileMenu();
            initMyVaultFileCtxMenu();
        } else if (inProjectMode() || inProjectOfflineMode()) {
            initProjectFileCtxMenu();
        } else if (inWorkSpaceMode() || inWorkSpaceOfflineMode()) {
            initWorkSpaceFileCtxMenu();
        } else {
            initMyVaultFileCtxMenu();
        }
    }

    private void initProjectFileCtxMenu() {
        Intent intent = getIntent();
        mProject = intent.getParcelableExtra(Constant.PROJECT_DETAIL);
        int id = intent.getIntExtra("project_id", -1);
        String name = intent.getStringExtra("project_name");
        boolean createdByMe = intent.getBooleanExtra("is_created_by_me", false);
        mProjectFileCtxMenu = FileContextMenu.newInstance();
        mProjectFileCtxMenu.setProjectId(id);
        mProjectFileCtxMenu.setOfflineView(inProjectOfflineMode());
        mProjectFileCtxMenu.setProjectName(name);
        mProjectFileCtxMenu.setOwnerByMe(createdByMe);
        if (inProjectOfflineMode()) {
            mProjectFileCtxMenu.setViewActivityItemVisibility(View.GONE);
            mProjectFileCtxMenu.setDeleteVisibility(View.GONE);
        }
        mProjectFileCtxMenu.setOnMarkOfflineClickListener(new FileContextMenu.OnMarkOfflineClickListener() {
            @Override
            public void onClick(INxlFile f, boolean offline, int pos) {
                if (offline) {
                    unMarkAsOffline(f, pos);
                } else {
                    markAsOffline(f, pos);
                }
            }
        });
        mProjectFileCtxMenu.setOnShareItemClickListener(new FileContextMenu.OnShareItemClickListener() {
            @Override
            public void onAddToProjectItemClick(INxlFile f, int pos) {
                addToProject(f, pos);
            }

            @Override
            public void onShareToPersonItemClick(INxlFile f, int pos) {
                share(f, pos);
            }
        });
        mProjectFileCtxMenu.setOnModifyRightsClickListener(new FileContextMenu.OnModifyRightsClickListener() {
            @Override
            public void onModifyRights(INxlFile f, int pos) {
                modifyRights(f, pos);
            }
        });
        mProjectFileCtxMenu.setOnDeleteButtonClickListener(new NxlItemHelper.OnDeleteButtonClickListener() {
            @Override
            public void onClick(INxlFile f) {
                if (mDeleteNxlFileCallback == null) {
                    mDeleteNxlFileCallback = new DeleteNxlFileCallback(f);
                }
                DeleteNxlFileTask task = new DeleteNxlFileTask(f, mDeleteNxlFileCallback);
                task.run();
            }
        });
    }

    private void initWorkSpaceFileCtxMenu() {
        mWorkSpaceFileCtxMenu = WorkSpaceFileContextMenu.newInstance();
        mWorkSpaceFileCtxMenu.setOfflineView(inWorkSpaceOfflineMode());
        mWorkSpaceFileCtxMenu.setOnShareItemClickListener(new WorkSpaceFileContextMenu.OnShareItemClickListener() {
            @Override
            public void onAddToProject(INxlFile f, int pos) {
                ToastUtil.showToast(SearchActivity.this, "AddToProject");
            }
        });
        mWorkSpaceFileCtxMenu.setOnMarkOfflineClickListener(new WorkSpaceFileContextMenu.OnMarkOfflineClickListener() {
            @Override
            public void onClick(INxlFile f, boolean offline, int pos) {
                if (offline) {
                    unMarkAsOffline(f, pos);
                } else {
                    markAsOffline(f, pos);
                }
            }
        });
        mWorkSpaceFileCtxMenu.setOnModifyRightsClickListener(new WorkSpaceFileContextMenu.OnModifyRightsClickListener() {
            @Override
            public void onModifyRights(INxlFile f, int pos) {
                modifyWorkSpaceFileRights(f, pos);
            }
        });
        mWorkSpaceFileCtxMenu.setOnDeleteButtonClickListener(new WorkSpaceFileContextMenu.OnDeleteButtonClickListener() {
            @Override
            public void onClick(INxlFile f, int pos) {
                if (mDeleteNxlFileCallback == null) {
                    mDeleteNxlFileCallback = new DeleteNxlFileCallback(f);
                }
                DeleteNxlFileTask task = new DeleteNxlFileTask(f, mDeleteNxlFileCallback);
                task.run();
            }
        });
    }

    private void initProjectMemberAdapter() {
        mProjectMemberAdapter = new ProjectMemberAdapter(mCtx);
        mProjectMemberAdapter.setOnItemClickListener(new ProjectMemberAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(IMember member) {
                showMemberInfo(member, getIntent().getBooleanExtra("is_created_by_me", false));
            }
        });
    }

    private void initLogAdapter() {
        mLogAdapter = new ActivityLogAdapter(mCtx, "");
    }

    private void initProjectAdapter() {
        mAllProjectAdapter = new ViewAllProjectAdapter(mCtx);
    }

    private void initFavAdapter() {
        mFavAdapter = new FavoriteAdapter();
        mFavAdapter.setOnItemClickListener(new OnItemClickListener<IFavoriteFile>() {
            @Override
            public void onItemClick(IFavoriteFile entry, int position) {
                NxlItemHelper.viewFavoriteFile(mCtx, entry);
            }

            @Override
            public void onToggleItemMenu(IFavoriteFile entry, int position) {
                showVaultOrDriveCtxMenu(entry, position);
            }

            @Override
            public void onSwipeButton_01Click(IFavoriteFile entry, int position, View view) {
                NxlItemHelper.manageOrShareFavoriteFile(mCtx, entry, view);
            }

            @Override
            public void onSwipeButton_02Click(IFavoriteFile entry, int position, View view) {
                NxlItemHelper.protectOrViewLogFavoriteFile(mCtx, entry, view);
            }
        });
        initMyVaultFileCtxMenu();
        mSpaceFileCtxMenu = new MySpaceFileCtxMenu();
        mSpaceFileCtxMenu.setOnFavoriteMarkerChangeListener(new MySpaceFileCtxMenu.OnFavoriteMarkerChangeListener() {
            @Override
            public void onChanged(INxFile f, int position, boolean favorite) {
                if (inFavMode()) {
                    mFavAdapter.removeItem(f);
                }
            }
        });
    }

    private void initRepoAdapter() {
        mRepoAdapter = new NXFileRecyclerViewAdapter(mCtx);
        mSpaceFileCtxMenu = new MySpaceFileCtxMenu();
        mSpaceFileCtxMenu.setOnFavoriteMarkerChangeListener(new MySpaceFileCtxMenu.OnFavoriteMarkerChangeListener() {
            @Override
            public void onChanged(INxFile f, int pos, boolean favorite) {
                if (favorite) {
                    markAsFavorite(f, pos);
                } else {
                    unMarkAsFavorite(f, pos);
                }
            }
        });
        mRepoAdapter.setOnContentClickListener(new NXFileRecyclerViewAdapter.OnContentClickListener() {
            @Override
            public void onItemClick(INxFile f, View view, int position) {
                if (f.isFolder()) {
                    finish();
                    EventBus.getDefault().post(f);
                } else {
                    if (f instanceof NXDocument) {
                        MySpaceFileItemHelper.viewFile(mCtx, (NXDocument) f);
                    }
                }
            }

            @Override
            public void onDetailClick(INxFile f, View view, int position) {
                showMyDriveCtxMenu(f, position);
            }
        });
        mRepoAdapter.setOnRightMenuClickListener(new NXFileRecyclerViewAdapter.OnRightMenuClickListener() {
            @Override
            public void onShare(INxFile f, int position) {
                MySpaceFileItemHelper.share(mCtx, f);
            }

            @Override
            public void onProtect(INxFile f, View view, int position) {
                MySpaceFileItemHelper.protect(mCtx, f);
            }

            @Override
            public void onViewActivityLog(INxFile f, int position) {
                if (f instanceof NXDocument)
                    MySpaceFileItemHelper.viewActivityLog(mCtx, (NXDocument) f);
            }
        });
    }

    private void showVaultOrDriveCtxMenu(IFavoriteFile f, int pos) {
        if (f instanceof MyVaultFile) {
            showMyVaultFileCtxMenu((MyVaultFile) f, pos);
        } else {
            showMyDriveCtxMenu((NXDocument) f, pos);
        }
    }

    private void initMyVaultFileCtxMenu() {
        Intent intent = getIntent();
        if (intent != null) {
            mVaultType = intent.getStringExtra(Constant.ACTION_KEY_MY_VAULT);
        }
        mMyVaultFileMenu = MyVaultFileMenu.newInstance();
//        mMyVaultFileMenu.setDeleteBtVisibility(View.GONE);
        mMyVaultFileMenu.setOnItemClickListener(new MyVaultFileMenu.OnItemClickListener() {
            @Override
            public void onSetFavorite(INxlFile f, int pos, boolean favorite) {
                if (favorite) {
                    unMarkAsFavorite(f, pos);
                } else {
                    markAsFavorite(f, pos);
                }
            }

            @Override
            public void onSetOffline(INxlFile f, int pos, boolean offline) {
                if (offline) {
                    unMarkAsOffline(f, pos);
                } else {
                    markAsOffline(f, pos);
                }
            }

            @Override
            public void onDeleteFile(INxlFile f, final int pos) {
                NxlItemHelper.showDeleteDialog(SearchActivity.this, f,
                        new NxlItemHelper.OnDeleteButtonClickListener() {
                            @Override
                            public void onClick(INxlFile f) {
                                if (mDeleteNxlFileCallback == null) {
                                    mDeleteNxlFileCallback = new DeleteNxlFileCallback(f);
                                }
                                mDeleteNxlFileCallback.setVaultType(mVaultType);
                                DeleteNxlFileTask t = new DeleteNxlFileTask(f, mDeleteNxlFileCallback);
                                t.run();
                            }
                        });
            }
        });
    }

    private void initSharedWithMeFileMenu() {
        mSharedWithMeFileMenu = SharedWithMeFileMenu.newInstance();
    }

    private void showComplexFileCtxMenu(INxlFile f, int pos) {
        if (f instanceof MyVaultFile) {
            showMyVaultFileCtxMenu(f, pos);
        } else if (f instanceof SharedWithMeFile) {
            showSharedWithMeFileMenu(f, pos);
        } else if (f instanceof ProjectFile) {
            showProjectFileMenu(f, pos);
        } else if (f instanceof ProjectNode) {
            showProjectFileMenu(f, pos);
        } else if (f instanceof WorkSpaceFile) {
            showWorkSpaceFileCtxMenu(f, pos);
        } else if (f instanceof WorkSpaceNode) {
            showWorkSpaceFileCtxMenu(f, pos);
        } else if (f instanceof SharedWithProjectFile) {
            showProjectFileMenu(f, pos);
        }
    }

    private void showMyVaultFileCtxMenu(INxlFile f, int pos) {
        mMyVaultFileMenu.setFile(f);
        mMyVaultFileMenu.setPosition(pos);
        mMyVaultFileMenu.show(getSupportFragmentManager(), MyVaultFileMenu.class.getSimpleName());
    }

    private void showSharedWithMeFileMenu(INxlFile f, int position) {
        mSharedWithMeFileMenu.setSharedWithMeFile(f);
        mSharedWithMeFileMenu.setPosition(position);
        mSharedWithMeFileMenu.setOnItemClickListener(new SharedWithMeFileMenu.OnItemClickListener() {
            @Override
            public void onSetOffline(INxlFile f, int pos, boolean offline) {
                if (offline) {
                    unMarkAsOffline(f, pos);
                } else {
                    markAsOffline(f, pos);
                }
            }
        });
        mSharedWithMeFileMenu.show(getSupportFragmentManager(), SharedWithMeFileMenu.class.getSimpleName());
    }

    private void showProjectFileMenu(INxlFile f, int pos) {
        mProjectFileCtxMenu.setFile(f);
        mProjectFileCtxMenu.setPosition(pos);
        mProjectFileCtxMenu.show(getSupportFragmentManager(), FileContextMenu.class.getSimpleName());
    }

    private void showMyDriveCtxMenu(final INxFile f, final int pos) {
        mSpaceFileCtxMenu.setNxFile(f);
        mSpaceFileCtxMenu.setPosition(pos);
        mSpaceFileCtxMenu.show(getSupportFragmentManager(), MySpaceFileCtxMenu.class.getSimpleName());
    }

    private void showWorkSpaceFileCtxMenu(INxlFile f, int pos) {
        mWorkSpaceFileCtxMenu.setFile(f);
        mWorkSpaceFileCtxMenu.setPosition(pos);
        mWorkSpaceFileCtxMenu.show(getSupportFragmentManager(),
                WorkSpaceFileContextMenu.class.getSimpleName());
    }

    private void markAsFavorite(INxFile f, int pos) {
        if (inRepoMode()) {
            mRepoAdapter.setFavoriteStatus(f, true);
        }
    }

    private void unMarkAsFavorite(INxFile f, int pos) {
        if (inRepoMode()) {
            mRepoAdapter.setFavoriteStatus(f, false);
        }
    }

    private void markAsFavorite(INxlFile f, int pos) {
        f.markAsFavorite();
        mNxlAdapter.setFavoriteStatus(pos, true);
    }

    private void unMarkAsFavorite(INxlFile f, int pos) {
        f.unMarkAsFavorite();
        if (inFavMode()) {
            mFavAdapter.removeItem(pos);
            return;
        }
        mNxlAdapter.setFavoriteStatus(pos, false);
    }

    private void markAsOffline(INxlFile f, final int pos) {
        f.markAsOffline(new IOfflineCallback() {
            @Override
            public void onStarted() {
                if (inFavMode()) {
                    mFavAdapter.setOperationStatus(pos, INxlFile.PROCESS);
                    return;
                }
                mNxlAdapter.setOperationStatus(pos, INxlFile.PROCESS);
            }

            @Override
            public void onProgress() {

            }

            @Override
            public void onMarkDone() {
                if (inFavMode()) {
                    mFavAdapter.setOfflineStatus(pos, true);
                    return;
                }
                mNxlAdapter.setOfflineStatus(pos, true);
            }

            @Override
            public void onMarkFailed(OfflineException e) {
                ExceptionHandler.handleException(SearchActivity.this, e);
                if (inFavMode()) {
                    mFavAdapter.setOperationStatus(pos, INxlFile.MARK_ERROR);
                    return;
                }
                mNxlAdapter.setOperationStatus(pos, INxlFile.MARK_ERROR);
            }
        });
    }

    private void unMarkAsOffline(INxlFile f, int pos) {
        f.unMarkAsOffline();
        if (inFavMode()) {
            mFavAdapter.setOfflineStatus(pos, false);
            return;
        }
        if (inProjectOfflineMode()) {
            mNxlAdapter.removeItem(pos);
            if (mNxlAdapter.getItemCount() == 0) {
                showEmptyView(true);
            }
            return;
        }
        if (inWorkSpaceOfflineMode()) {
            mNxlAdapter.removeItem(pos);
            if (mNxlAdapter.getItemCount() == 0) {
                showEmptyView(true);
            }
            return;
        }

        mNxlAdapter.setOfflineStatus(pos, false);
    }

    private void showMemberInfo(IMember member, boolean ownerByMe) {
        Intent i = new Intent(mCtx, MemberInfoActivity.class);
        i.putExtra(Constant.PROJECT_OWNER_BY_ME, ownerByMe);
        i.putExtra(Constant.MEMBER_DETAIL, (Member) member);
        mCtx.startActivity(i);
    }

    @Deprecated
    private void addToProject(INxlFile f, int pos) {
        Intent i = new Intent(this, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_LUNCH_PROJECT_SELECT_FRAGMENT);
        i.putExtra(Constant.PROJECT_FILE_ENTRY, (NxlDoc) f);
        i.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mProject);
        startActivity(i);
    }

    @Deprecated
    private void shareToPerson(INxlFile f, int pos) {
        Intent i = new Intent(this, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_CHECK_THEN_SHARE_TO_PERSON);
        i.putExtra(Constant.PROJECT_FILE_ENTRY, (NxlDoc) f);
        i.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mProject);
        startActivity(i);
    }

    private void share(INxlFile f, int pos) {
        Intent i = new Intent(this, ShareActivity.class);
        i.putExtra(Constant.SHARING_SERVICE, (Parcelable) mProject);
        i.putExtra(Constant.SHARING_ENTRY, (Parcelable) f);
        startActivity(i);
    }

    private void modifyRights(INxlFile f, int pos) {
        Intent i = new Intent(this, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_CHECK_THEN_MODIFY_RIGHTS);
        i.putExtra(Constant.MODIFY_RIGHTS_ENTRY, (NxlDoc) f);
        i.putExtra(Constant.MODIFY_RIGHTS_SERVICE, (Parcelable) mProject);
        startActivity(i);
    }

    private void modifyWorkSpaceFileRights(INxlFile f, int pos) {
        Intent i = new Intent(this, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_CHECK_THEN_MODIFY_RIGHTS);
        i.putExtra(Constant.MODIFY_RIGHTS_ENTRY, (NxlDoc) f);
        i.putExtra(Constant.MODIFY_RIGHTS_SERVICE, (WorkSpaceRepo) RepoFactory.getRepo(RepoType.TYPE_WORKSPACE));
        startActivity(i);
    }

    private boolean inRepoMode() {
        return Constant.ACTION_SEARCH_REPO_SYSTEM.equals(mAction);
    }

    private boolean inMyVaultMode() {
        return Constant.ACTION_SEARCH_MYVAULT.equals(mAction);
    }

    private boolean inSharedByMeMode() {
        return Constant.ACTION_SEARCH_SHARED_BY_ME.equals(mAction);
    }

    private boolean inSharedWithMeMode() {
        return Constant.ACTION_SEARCH_SHARED_WITH_ME.equals(mAction);
    }

    private boolean inFavMode() {
        return Constant.ACTION_SEARCH_FAVORITE.equals(mAction);
    }

    private boolean inOfflineMode() {
        return Constant.ACTION_SEARCH_OFFLINE.equals(mAction);
    }

    private boolean inProjectMode() {
        return Constant.ACTION_SEARCH_PROJECT_FILES.equals(mAction);
    }

    private boolean inProjectOfflineMode() {
        return Constant.ACTION_SEARCH_PROJECT_OFFLINE_FILES.equals(mAction);
    }

    private boolean inProjectMemberMode() {
        return Constant.ACTION_SEARCH_PROJECT_MEMBERS.equals(mAction);
    }

    private boolean inLogMode() {
        return Constant.ACTION_SEARCH_ACTIVITY_LOG.equals(mAction);
    }

    private boolean inProjectItemMode() {
        return Constant.ACTION_SEARCH_ALL_PROJECTS.equals(mAction);
    }

    private boolean inWorkSpaceMode() {
        return Constant.ACTION_SEARCH_WORKSPACE_FILES.equals(mAction);
    }

    private boolean inWorkSpaceOfflineMode() {
        return Constant.ACTION_SEARCH_WORKSPACE_OFFLINE_FILES.equals(mAction);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        CommonUtils.releaseResource(mPresenter);
        CommonUtils.releaseResource(mNxlAdapter);
        CommonUtils.releaseResource(mFavAdapter);
        if (mDeleteNxlFileCallback != null) {
            mDeleteNxlFileCallback = null;
        }
    }

    private class DeleteNxlFileCallback implements DeleteNxlFileTask.ITaskCallback<DeleteNxlFileTask.Result, Exception> {
        private INxlFile mFile;
        private String mVaultType;

        DeleteNxlFileCallback(INxlFile f) {
            this.mFile = f;
        }

        void setVaultType(String vaultType) {
            this.mVaultType = vaultType;
        }

        @Override
        public void onTaskPreExecute() {

        }

        @Override
        public void onTaskExecuteSuccess(DeleteNxlFileTask.Result results) {
            if (mPresenter != null) {
                if (inMyVaultMode()) {
                    mPresenter.deleteMyVaultFile(mFile, mVaultType);
                }
                if (inProjectMode()) {
                    mPresenter.deleteProjectFile(mFile);
                }
                if (inWorkSpaceMode()) {
                    mPresenter.deleteProjectFile(mFile);
                }
            }
        }

        @Override
        public void onTaskExecuteFailed(Exception e) {
            ExceptionHandler.handleException(SearchActivity.this, e);
        }
    }
}
