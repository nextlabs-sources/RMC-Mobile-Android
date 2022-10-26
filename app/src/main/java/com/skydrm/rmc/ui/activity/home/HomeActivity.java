package com.skydrm.rmc.ui.activity.home;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.heartbeat.HeartbeatPolicyGenerator;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatListener;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatPolicy;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.project.ProjectRepo;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.RepositorySelectMyDrive;
import com.skydrm.rmc.engine.eventBusMsg.RepositoryUpdateEvent;
import com.skydrm.rmc.engine.eventBusMsg.account.UpdateUserAvatarEvent;
import com.skydrm.rmc.engine.eventBusMsg.account.UpdateUserNameEvent;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.activity.CmdOperateFileActivity2;
import com.skydrm.rmc.ui.activity.profile.ProfileActivity;
import com.skydrm.rmc.ui.activity.repository.RepoSettingActivity;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.base.NavigationType;
import com.skydrm.rmc.ui.base.SupportFragment;
import com.skydrm.rmc.ui.fragment.home.HomeFragment;
import com.skydrm.rmc.ui.fragment.projects.ProjectsFragment;
import com.skydrm.rmc.ui.myspace.FilesControlFragment;
import com.skydrm.rmc.ui.myspace.MyDriveFragment;
import com.skydrm.rmc.ui.myspace.base.MySpaceFileBaseFragment;
import com.skydrm.rmc.ui.myspace.myvault.view.fragment.MyVaultControlFragment;
import com.skydrm.rmc.ui.project.service.SwitchProjectActivity;
import com.skydrm.rmc.ui.service.protect.IProtectService;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.avatar.AvatarView;
import com.skydrm.rmc.ui.workspace.WorkSpaceControlFragment;
import com.skydrm.rmc.utils.checkVersion.VersionCheckUtils;
import com.skydrm.rmc.utils.commonUtils.AvatarUtil;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.rms.user.IRmUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends BaseActivity {
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView mNavView;
    private AvatarView mAvatarView;
    private TextView mTvUserName;

    private static final String ACTION_TO_PROFILE_PAGE = "action_to_profile_page";
    private static final String ACTION_SELECT_DRAWER_ITEM = "action_select_drawer_item";
    private static final int MIN__ITEM_IN_DRAWER = 0;
    private static final int MAX__ITEM_IN_DRAWER = 4;

    private SparseArray<SupportFragment> mFragments = new SparseArray<>();
    private int IDX_DRAWER_ITEM_HOME = 0;
    private int IDX_DRAWER_ITEM_ALL_FILES = 1;
    private int IDX_DRAWER_ITEM_MY_VAULT = 2;
    private int IDX_DRAWER_ITEM_PROJECTS = 3;
    private int IDX_DRAWER_ITEM_WORKSPACE = 4;

    private String[] mFragmentTags = new String[]{
            "TAG_FRAGMENT_HOME",
            "TAG_FRAGMENT_ALL_FILES",
            "TAG_FRAGMENT_MY_VAULT",
            "TAG_FRAGMENT_PROJECTS",
            "TAG_FRAGMENT_WORKSPACE"};

    private final Handler mPostHandler = new Handler();
    private Runnable mPendingRunnable;
    private String mAction;
    private int mIdx = 0;
    private long mIntervalBackPressedMills = 0;
    private File mPhotoFile;
    private String mWorkSpaceCurrentPathId;
    private IProtectService mService;

    private HeartbeatListener mHeartbeatListener;
    private LoadingDialog2 mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            mFragments.put(IDX_DRAWER_ITEM_HOME, HomeFragment.newInstance(IDX_DRAWER_ITEM_HOME));
            mFragments.put(IDX_DRAWER_ITEM_ALL_FILES, FilesControlFragment.newInstance());
            mFragments.put(IDX_DRAWER_ITEM_MY_VAULT, MyVaultControlFragment.newInstance());
            mFragments.put(IDX_DRAWER_ITEM_PROJECTS, ProjectsFragment.newInstance());
            mFragments.put(IDX_DRAWER_ITEM_WORKSPACE, WorkSpaceControlFragment.newInstance());

            loadMultipleRootFragment(
                    R.id.fl_container,
                    IDX_DRAWER_ITEM_HOME,
                    mFragmentTags,
                    mFragments.get(IDX_DRAWER_ITEM_HOME),
                    mFragments.get(IDX_DRAWER_ITEM_ALL_FILES),
                    mFragments.get(IDX_DRAWER_ITEM_MY_VAULT),
                    mFragments.get(IDX_DRAWER_ITEM_PROJECTS),
                    mFragments.get(IDX_DRAWER_ITEM_WORKSPACE));

            mFragments.get(IDX_DRAWER_ITEM_HOME).setUserVisibleHint(true);
        } else {
            mFragments.put(IDX_DRAWER_ITEM_HOME, findChildFragment(mFragmentTags[0]));
            mFragments.put(IDX_DRAWER_ITEM_ALL_FILES, findChildFragment(mFragmentTags[1]));
            mFragments.put(IDX_DRAWER_ITEM_MY_VAULT, findChildFragment(mFragmentTags[2]));
            mFragments.put(IDX_DRAWER_ITEM_PROJECTS, findChildFragment(mFragmentTags[3]));
            mFragments.put(IDX_DRAWER_ITEM_WORKSPACE, findChildFragment(mFragmentTags[4]));
        }

        initViewAndEvents();
        // check version
        checkVersion();
        // register this class into EventBus
        EventBus.getDefault().register(this);

        registerHeartBeatListener();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (isLunchFromProjectHome(intent)) {
            selectHomeFragIfNecessary();
        }
        if (isLunchFromSwitchProjectGoToMySpace(intent)) {
            selectMySpaceFragIfNecessary();
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (mIdx == IDX_DRAWER_ITEM_ALL_FILES) {
                handleMySpaceFragBackPressEvent();
            } else if (mIdx == IDX_DRAWER_ITEM_WORKSPACE) {
                handleWorkSpaceBackPressEvent();
            } else {
                exit();
            }
        }
    }

    /* when user scan and use camera to take a photo, handler it here */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.ALBUM_REQUEST_CODE) {
                try {
                    if (mPhotoFile == null) {
                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.camera_file_null));
                        return;
                    }
                    // save photo to local album
                    MediaStore.Images.Media.insertImage(getContentResolver(),
                            mPhotoFile.getAbsolutePath(), mPhotoFile.getName(), null);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mPhotoFile)));

                    Intent i = new Intent(this, CmdOperateFileActivity2.class);

                    if (mIdx == IDX_DRAWER_ITEM_WORKSPACE) {
                        i.setAction(Constant.ACTION_WORKSPACE_PROTECT_FROM_SCAN_DOC);
                        i.putExtra(Constant.PROTECT_SERVICE, (Parcelable) mService);
                        i.putExtra(Constant.NAME_CURRENT_PATH_ID, mWorkSpaceCurrentPathId);
                    } else {
                        i.setAction(Constant.ACTION_MYSPACE_PROTECT_FROM_SCAN_DOC);
                    }

                    i.putExtra(Constant.LIBRARY_FILE_ENTRY, mPhotoFile.getPath());
                    startActivity(i);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mHeartbeatListener != null) {
            mHeartbeatListener = null;
        }
    }

    @Override
    public void onDrawerOpen() {
        super.onDrawerOpen();
        if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void toProjects() {
        super.toProjects();
        Intent intent = new Intent(this, SwitchProjectActivity.class);
        intent.putExtra(Constant.KEY, Constant.FLAG_FROM_SPACE);
        startActivity(intent);
    }

    @Override
    public void onNavigationStart(NavigationType type) {
        super.onNavigationStart(type);
        if (type == NavigationType.TYPE_TO_MYDRIVE) {
            nav2MyDrive();
        } else if (type == NavigationType.TYPE_TO_MYVAULT) {
            nav2MyVault();
        } else if (type == NavigationType.TYPE_VIEW_ALL_PROJECTS) {
            nav2ProjectPage();
        } else if (type == NavigationType.TYPE_TO_WORKSPACE) {
            nav2WorkSpacePage();
        }
    }

    @Override
    public void onNavigationToRepo(BoundService service) {
        super.onNavigationToRepo(service);
        nav2AllFiles(service);
    }

    /* when user changed it's name will notify  */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateNameEventHandler(UpdateUserNameEvent event) {
        try {
            mTvUserName.setText(SkyDRMApp.getInstance().getSession().getRmUser().getName());
        } catch (Exception e) {
            log.e(e);
        }
    }

    /* when user changed it's avatar will notify */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateAvatarEventHandler(UpdateUserAvatarEvent event) {
        try {
            AvatarUtil.getInstance().setUserAvatar(this, mAvatarView);
        } catch (Exception e) {
            log.e(e);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceivePhotographMsg(PhotographMsg msg) {
        this.mPhotoFile = msg.mPhoto;
        this.mWorkSpaceCurrentPathId = msg.mPathId;
        this.mService = msg.mService;
    }

    private void initViewAndEvents() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavView = findViewById(R.id.nav_view);

        if (isSupportWorkSpace()) {
            mNavView.inflateMenu(R.menu.home_main_drawer2);
        } else {
            mNavView.inflateMenu(R.menu.home_main_drawer_saas);
        }

        mNavView.getMenu().getItem(0).setChecked(true);
        View headerView = mNavView.inflateHeaderView(R.layout.nav_header_main);
        mAvatarView = headerView.findViewById(R.id.user_avatar_drawer);
        mTvUserName = headerView.findViewById(R.id.tv_user_name_drawer);
        TextView tvEmail = headerView.findViewById(R.id.tv_email_drawer);
        onInitializeUserData(mAvatarView, mTvUserName, tvEmail);

        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                mDrawerLayout.closeDrawer(GravityCompat.START);
                mAction = ACTION_SELECT_DRAWER_ITEM;
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    if (mIdx != 0) {
                        selectDrawerItem(0);
                        mIdx = 0;
                    }
                } else if (id == R.id.nav_all_files) {
                    if (mIdx != 1) {
                        selectDrawerItem(1);
                        mIdx = 1;
                    }
                } else if (id == R.id.nav_myvault) {
                    if (mIdx != 2) {
                        selectDrawerItem(2);
                        mIdx = 2;
                    }
                } else if (id == R.id.nav_projects) {
                    if (mIdx != 3) {
                        selectDrawerItem(3);
                        mIdx = 3;
                    }
                } else if (id == R.id.nav_workspace) {
                    if (mIdx != 4) {
                        selectDrawerItem(4);
                        mIdx = 4;
                    }
                }
                return true;
            }
        });
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (TextUtils.isEmpty(mAction)) {
                    return;
                }
                switch (mAction) {
                    case ACTION_TO_PROFILE_PAGE:
                        launchTo(ProfileActivity.class);
                        mAction = "";
                        break;
                    case ACTION_SELECT_DRAWER_ITEM:
                        if (mPendingRunnable != null) {
                            mPostHandler.post(mPendingRunnable);
                            mPendingRunnable = null;
                        }
                        mAction = "";
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        // direct to Profile page
        headerView.findViewById(R.id.iv_setting_drawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                mAction = ACTION_TO_PROFILE_PAGE;
            }
        });
    }

    private void setUserVisibleHint(int index) {
        for (int i = 0; i < mFragments.size(); i++) {
            SupportFragment frag = mFragments.get(i);
            if (frag == null) {
                continue;
            }
            frag.setUserVisibleHint(i == index);
        }
    }

    private void selectDrawerItem(final int position) {
        switch (position) {
            case 0:
                mPendingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        nav2HomePage();
                    }
                };
                break;
            case 1:
                mPendingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        nav2AllFiles();
                    }
                };
                break;
            case 2:
                mPendingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        nav2MyVault();
                    }
                };
                break;
            case 3:
                mPendingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // to projects.
                        nav2ProjectPage();
                    }
                };
                break;
            case 4:
                mPendingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // to workspace.
                        nav2WorkSpacePage();
                    }
                };
                break;
        }
    }

    private void nav2HomePage() {
        setUserVisibleHint(0);
        showHideFragment(mFragments.get(IDX_DRAWER_ITEM_HOME), null);
    }

    private void nav2MyDrive() {
        try {
            SkyDRMApp.getInstance().getRepoSystem().selectOnlyMyDrive();

            showHideFragment(mFragments.get(IDX_DRAWER_ITEM_ALL_FILES), null);
            changeDrawerItemSelectedStatus(IDX_DRAWER_ITEM_ALL_FILES);
            EventBus.getDefault().post(new RepositorySelectMyDrive());
            setUserVisibleHint(IDX_DRAWER_ITEM_ALL_FILES);
        } catch (Exception e) {
            log.e(e);
        }
    }

    public void nav2MyVault() {
        showHideFragment(mFragments.get(IDX_DRAWER_ITEM_MY_VAULT), null);
        changeDrawerItemSelectedStatus(IDX_DRAWER_ITEM_MY_VAULT);
        setUserVisibleHint(IDX_DRAWER_ITEM_MY_VAULT);
    }

    public void nav2AllFiles() {
        try {
            showHideFragment(mFragments.get(IDX_DRAWER_ITEM_ALL_FILES), null);
            changeDrawerItemSelectedStatus(IDX_DRAWER_ITEM_ALL_FILES);
            if (SkyDRMApp.getInstance().isOnPremise()) {
                SkyDRMApp.getInstance().getRepoSystem().selectOnlyMyDrive();
            } else {
                SkyDRMApp.getInstance().getRepoSystem().selectAllRepo();
            }
            EventBus.getDefault().post(new RepositoryUpdateEvent());
            setUserVisibleHint(IDX_DRAWER_ITEM_ALL_FILES);
        } catch (Exception e) {
            log.e(e);
        }
    }

    private void nav2AllFiles(BoundService s) {
        try {
            if (s != null && !s.isValidRepo()) {
                handleInValidBoundService(s);
                return;
            }
            showHideFragment(mFragments.get(IDX_DRAWER_ITEM_ALL_FILES), null);
            changeDrawerItemSelectedStatus(IDX_DRAWER_ITEM_ALL_FILES);
            SkyDRMApp.getInstance().getRepoSystem().selectOnlyOneRepo(s);
            EventBus.getDefault().post(new RepositoryUpdateEvent());
        } catch (Exception e) {
            log.e(e);
        }
    }

    private void handleInValidBoundService(BoundService s) {
        Intent intent = new Intent(this, RepoSettingActivity.class);
        intent.setAction(Constant.RE_AUTHENTICATION);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constant.BOUND_SERVICE, s);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void nav2ProjectPage() {
        setUserVisibleHint(IDX_DRAWER_ITEM_PROJECTS);
        showHideFragment(mFragments.get(IDX_DRAWER_ITEM_PROJECTS), null);
        changeDrawerItemSelectedStatus(IDX_DRAWER_ITEM_PROJECTS);
    }

    public void nav2WorkSpacePage() {
        setUserVisibleHint(IDX_DRAWER_ITEM_WORKSPACE);
        showHideFragment(mFragments.get(IDX_DRAWER_ITEM_WORKSPACE), null);
        changeDrawerItemSelectedStatus(IDX_DRAWER_ITEM_WORKSPACE);
    }

    private void changeDrawerItemSelectedStatus(int idx) {
        try {
            if (idx < MIN__ITEM_IN_DRAWER || idx > MAX__ITEM_IN_DRAWER) {
                throw new Exception("out of bounds in drawer items:" + idx + ";\n bug!!!!!");
            }
            mIdx = idx;
            mNavView.getMenu().getItem(mIdx).setChecked(true);
            return;
        } catch (Exception e) {
            log.e(e);
        }
        // for exception ,set as default
        mIdx = IDX_DRAWER_ITEM_HOME;
    }

    private void onInitializeUserData(AvatarView userAvatar, TextView userName, TextView userEmail) {
        try {
            AvatarUtil.getInstance().setUserAvatar(this, userAvatar);
            IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
            if (null != rmUser) {
                userName.setText(rmUser.getName());
                userEmail.setText(rmUser.getEmail());
            }
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
    }

    private void handleMySpaceFragBackPressEvent() {
        FilesControlFragment controlFrag = findChildFragment(mFragmentTags[1]);
        if (controlFrag == null) {
            exit();
        } else {
            MySpaceFileBaseFragment frag = controlFrag.getAllFragment();
            handleSpaceBaseFragBackPressEvent(frag);
        }
    }

    private void handleMyDriveFragBackPressEvent() {
        MyDriveFragment frag = findChildFragment(mFragmentTags[2]);
        handleSpaceBaseFragBackPressEvent(frag);
    }

    private void handleSpaceBaseFragBackPressEvent(MySpaceFileBaseFragment frag) {
        if (frag == null) {
            exit();
        } else {
            if (frag.needInterceptBackPress()) {
                frag.handleBackPress();
            } else {
                exit();
            }
        }
    }

    private void handleWorkSpaceBackPressEvent() {
        WorkSpaceControlFragment frag = findChildFragment(mFragmentTags[IDX_DRAWER_ITEM_WORKSPACE]);
        if (frag == null) {
            exit();
        } else {
            if (frag.needInterceptBackPress()) {
                frag.handleBackPress();
            } else {
                exit();
            }
        }
    }

    private boolean isLunchFromProjectHome(Intent i) {
        if (i == null) {
            return false;
        }
        String action = i.getAction();
        if (action == null || action.isEmpty()) {
            return false;
        }
        return Constant.ACTION_LUNCH_HOME_FORM_PROJECT_HOME.equals(action);
    }

    private boolean isLunchFromSwitchProjectGoToMySpace(Intent i) {
        if (i == null) {
            return false;
        }
        String action = i.getAction();
        if (action == null || action.isEmpty()) {
            return false;
        }
        return Constant.ACTION_LUNCH_HOME_FROM_SWITCH_PROJECT_PAGE.equals(action);
    }

    private void selectHomeFragIfNecessary() {
        if (mIdx != IDX_DRAWER_ITEM_HOME) {
            showHideFragment(mFragments.get(IDX_DRAWER_ITEM_HOME), null);
            changeDrawerItemSelectedStatus(IDX_DRAWER_ITEM_HOME);
        }
    }

    private void selectMySpaceFragIfNecessary() {
        if (mIdx != IDX_DRAWER_ITEM_MY_VAULT) {
            nav2MyVault();
        }
    }

    private void exit() {
        if (!getSupportFragmentManager().popBackStackImmediate()) {
            // for no fragment can be pop-up
            if ((System.currentTimeMillis() - mIntervalBackPressedMills) > 2000) {
                Toast.makeText(getApplicationContext(), getString(R.string.initcap_exit_if_press_again), Toast.LENGTH_SHORT).show();
                mIntervalBackPressedMills = System.currentTimeMillis();
            } else {
                //supportFinishAfterTransition();
                moveTaskToBack(true);
            }
        }
    }

    /**
     * Check app version if has updated
     */
    private void checkVersion() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            final String currentVersion = info.versionName;
            VersionCheckUtils.GetLatestVersion(getPackageName(), new VersionCheckUtils.IGetVersionComplete() {
                @Override
                public void onGetVersionFinish(String latestVersion) {
                    if (!TextUtils.isEmpty(latestVersion) && VersionCheckUtils.isNeedUpdate(currentVersion, latestVersion)) {
                        VersionCheckUtils.updateDialog(HomeActivity.this, currentVersion, latestVersion);
                    }
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void registerHeartBeatListener() {
        if (inUserLoginMode()) {
            ProjectRepo repo = (ProjectRepo) RepoFactory.getRepo(RepoType.TYPE_PROJECT);
            mHeartbeatListener = new HeartbeatListener();
            repo.registerHeartBeatListener(mHeartbeatListener);
        }
    }

    private void unregisterListener() {
        ProjectRepo repo = (ProjectRepo) RepoFactory.getRepo(RepoType.TYPE_PROJECT);
        repo.registerHeartBeatListener(null);
        if (mHeartbeatListener != null) {
            mHeartbeatListener = null;
        }
    }

    private boolean inUserLoginMode() {
        return HeartbeatPolicyGenerator.getOne(HeartbeatPolicyGenerator.TYPE_COMMON).getType() ==
                IHeartBeatPolicy.TYPE_NEW_USER_LOGIN;
    }

    class HeartbeatListener implements IHeartBeatListener {

        @Override
        public void onTaskBegin() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLoadingDialog();
                }
            });
        }

        @Override
        public void onTaskProgress(final int progress) {

        }

        @Override
        public void onTaskFinish() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoadingDialog();
                    if (mIdx == 0) {
                        setUserVisibleHint(0);
                    }
                    unregisterListener();
                }
            });
        }

        @Override
        public void onTaskFailed(Exception e) {

        }
    }

    private void showLoadingDialog() {
        mLoadingDialog = LoadingDialog2.newInstance();
        mLoadingDialog.showModalDialog(this);
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissDialog();
            mLoadingDialog = null;
        }
    }
}
