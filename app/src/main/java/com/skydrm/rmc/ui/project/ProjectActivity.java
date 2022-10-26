package com.skydrm.rmc.ui.project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.CommandOperateEvent;
import com.skydrm.rmc.exceptions.ErrorDialog;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.activity.CmdOperateFileActivity2;
import com.skydrm.rmc.ui.activity.home.HomeActivity;
import com.skydrm.rmc.ui.activity.home.PhotographMsg;
import com.skydrm.rmc.ui.activity.profile.ProfileActivity;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.base.SupportFragment;
import com.skydrm.rmc.ui.myspace.myvault.ICommand;
import com.skydrm.rmc.ui.myspace.myvault.data.Error;
import com.skydrm.rmc.ui.project.feature.configuration.ProjectConfigurationFragment;
import com.skydrm.rmc.ui.project.feature.files.view.ProjectFileControlFragment;
import com.skydrm.rmc.ui.project.feature.member.ProjectMemberFragment;
import com.skydrm.rmc.ui.project.feature.summary.ProjectSummaryFragment;
import com.skydrm.rmc.ui.project.service.contact.IProjectContact;
import com.skydrm.rmc.ui.project.service.data.IProjectCommand;
import com.skydrm.rmc.ui.project.service.data.ProjectCommandExecutor;
import com.skydrm.rmc.ui.project.service.data.Result;
import com.skydrm.rmc.ui.project.service.message.MsgNotFound;
import com.skydrm.rmc.ui.widget.avatar.AvatarView;
import com.skydrm.rmc.utils.commonUtils.AvatarUtil;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.rms.user.membership.IMemberShip;
import com.skydrm.sdk.rms.user.membership.ProjectMemberShip;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.skydrm.rmc.R.id.nav_people_configuration;
import static com.skydrm.rmc.domain.Constant.REQUEST_CODE_SELECT_EMAILS;


public class ProjectActivity extends BaseActivity implements
        NavigationView.OnNavigationItemSelectedListener, IProjectContact.IView {
    public static final int SUMMARY = 0;
    public static final int FILES = 1;
    public static final int PEOPLE = 2;
    public static final int CONFIGURATION = 3;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    private File mPhotoFile;
    public SupportFragment[] projectFragments = new SupportFragment[4];
    private Bundle mArguments;
    private int mIndex = 0;
    private int mPId;
    private CommandOperateEvent.CommandScanMsg eventMsg;

    private IProject mPDetail;
    private boolean isShowExitDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        if (resolveIntent()) {
            loadFragment(savedInstanceState);
            initViewAndEvents();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.PROJECT_ALBUM_REQUEST_CODE) {
                try {
                    if (mPhotoFile == null) {
                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.camera_file_null));
                        return;
                    }
                    MediaStore.Images.Media.insertImage(getContentResolver(),
                            mPhotoFile.getAbsolutePath(), mPhotoFile.getName(), null);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mPhotoFile)));

                    boolean summaryHidden = projectFragments[SUMMARY].isHidden();
                    boolean filesHidden = projectFragments[FILES].isHidden();
                    String currentPathId = "/";
                    if (!summaryHidden) {
                        currentPathId = "/";
                    } else if (!filesHidden) {
                        ProjectFileControlFragment controlFrag = findChildFragment(ProjectFileControlFragment.class);
                        if (controlFrag != null) {
                            NxlBaseFragment current = controlFrag.getCurrentFragment();
                            if (current != null) {
                                currentPathId = current.getCurrentPathId();
                            }
                        }
                    }
                    Intent i = new Intent(this, CmdOperateFileActivity2.class);
                    i.setAction(Constant.ACTION_PROJECT_PROTECT_FROM_SCAN_DOC);
                    i.putExtra(Constant.PROJECT_DETAIL, (Parcelable) mPDetail);
                    i.putExtra(Constant.PROJECT_PARENT_PATH_ID, currentPathId);
                    i.putExtra(Constant.LIBRARY_FILE_ENTRY, mPhotoFile.getPath());
                    startActivity(i);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == REQUEST_CODE_SELECT_EMAILS) {
                if (mIndex == PEOPLE) {
                    updateContactParcel(data);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        isShowExitDialog = false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceivePhotographMsg(PhotographMsg msg) {
        this.mPhotoFile = msg.mPhoto;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveInvalidProjectMsg(MsgNotFound msg) {
        if (isShowExitDialog) {
            return;
        }
        isShowExitDialog = true;
        ErrorDialog.showUI(this, getString(R.string.invalid_project), true, false,
                true, null);
    }

    @Override
    public void onBackPressed() {
        ProjectFileControlFragment controlFrag = findChildFragment(ProjectFileControlFragment.class);
        if (controlFrag == null) {
            super.onBackPressed();
        } else {
            NxlBaseFragment curFrag = controlFrag.getCurrentFragment();
            if (curFrag == null) {
                super.onBackPressed();
            } else {
                if (curFrag.needInterceptBackPress()) {
                    curFrag.interceptBackPress();
                } else {
                    super.onBackPressed();
                }
            }
        }
    }

    @Override
    public void onDrawerOpen() {
        super.onDrawerOpen();
        if (!mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
        if (!menuItem.isChecked()) {
            menuItem.setChecked(true);
        }
        mDrawer.closeDrawer(GravityCompat.START);
        mDrawer.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        lunchToHome();
                        break;
                    case R.id.nav_summary_project:
                        if (mIndex != 0) {
                            mIndex = 0;
                            setUserVisibility(0);
                            showHideFragment(projectFragments[SUMMARY], null);
                        }
                        break;
                    case R.id.nav_files_project:
                        if (mIndex != 1) {
                            mIndex = 1;
                            setUserVisibility(1);
                            showHideFragment(projectFragments[FILES], null);
                        }
                        break;
                    case R.id.nav_people_project:
                        if (mIndex != 2) {
                            mIndex = 2;
                            setUserVisibility(2);
                            showHideFragment(projectFragments[PEOPLE], null);
                        }
                        break;
                    case nav_people_configuration:
                        if (mIndex != 3) {
                            mIndex = 3;
                            showHideFragment(projectFragments[CONFIGURATION], null);
                        }
                        break;
                }
            }
        }, 200);
        return true;
    }

    private void lunchToHome() {
        Intent i = new Intent(this, HomeActivity.class);
        i.setAction(Constant.ACTION_LUNCH_HOME_FORM_PROJECT_HOME);
        startActivity(i);
    }

    private void setUserVisibility(int index) {
        for (int i = 0; i < projectFragments.length; i++) {
            SupportFragment frag = projectFragments[i];
            if (frag == null) {
                continue;
            }
            frag.setUserVisibleHint(i == index);
        }
    }

    private void updateContactParcel(Intent data) {
        ProjectMemberFragment memberFrag = findChildFragment(ProjectMemberFragment.class);
        if (memberFrag != null) {
            memberFrag.updateContactParcel(data);
        }
    }

    private boolean resolveIntent() {
        mArguments = getIntent().getExtras();
        if (mArguments == null) {
            return false;
        }
        mPDetail = mArguments.getParcelable(Constant.PROJECT_DETAIL);
        mIndex = mArguments.getInt(Constant.PROJECT_INDEX);
        if (mPDetail == null) {
            return false;
        }
        mPDetail.hitPoint();
        mPId = mPDetail.getId();
        return true;
    }

    private void initViewAndEvents() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.getMenu().getItem(mIndex + 1).setChecked(true);
        if (!mPDetail.isOwnedByMe()) {
            mNavigationView.getMenu().findItem(R.id.nav_people_configuration).setVisible(false);
        }
        mNavigationView.setNavigationItemSelectedListener(this);

        //init Navigation Head view
        View headerView = mNavigationView.inflateHeaderView(R.layout.nav_header_main);
        AvatarView userAvatar = headerView.findViewById(R.id.user_avatar_drawer);
        ImageView launchProfilePage = headerView.findViewById(R.id.iv_setting_drawer);
        launchProfilePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.closeDrawer(GravityCompat.START);
                launchTo(ProfileActivity.class);
            }
        });
        TextView userName = headerView.findViewById(R.id.tv_user_name_drawer);
        TextView userEmail = headerView.findViewById(R.id.tv_email_drawer);
        onInitializeUserData(userAvatar, userName, userEmail);
        retrieveMembershipDataIfNotExist(mPId);
    }

    private void loadFragment(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {//when the activity first created load all fragment need to show.
            //for project summary fragment.
            ProjectSummaryFragment summaryFragment = ProjectSummaryFragment.newInstance();
            summaryFragment.setArguments(mArguments);
            summaryFragment.setUserVisibleHint(true);
            projectFragments[SUMMARY] = summaryFragment;

            ProjectFileControlFragment fileControlFragment = ProjectFileControlFragment.newInstance();
            fileControlFragment.setArguments(mArguments);
            projectFragments[FILES] = fileControlFragment;
            //mProjectFilesFragment = fileControlFragment.getProjectFilesFragment();

            //for project members fragment.
            ProjectMemberFragment memberFragment = ProjectMemberFragment.newInstance();
            memberFragment.setArguments(mArguments);
            memberFragment.setUserVisibleHint(false);
            projectFragments[PEOPLE] = memberFragment;

            if (mPDetail.isOwnedByMe()) {
                ProjectConfigurationFragment configurationFragment = ProjectConfigurationFragment.newInstance();
                configurationFragment.setArguments(mArguments);
                projectFragments[CONFIGURATION] = configurationFragment;

                loadMultipleRootFragment(R.id.fl_container, mIndex, null, projectFragments[SUMMARY],
                        projectFragments[FILES], projectFragments[PEOPLE], projectFragments[CONFIGURATION]);
            } else {
                loadMultipleRootFragment(R.id.fl_container, mIndex, null, projectFragments[SUMMARY],
                        projectFragments[FILES], projectFragments[PEOPLE]);
            }

        } else {//when app crash(or something else) memory restart,load fragment from savedInstanceState
            projectFragments[SUMMARY] = findChildFragment(ProjectSummaryFragment.class);
            projectFragments[FILES] = findChildFragment(ProjectFileControlFragment.class);
            projectFragments[PEOPLE] = findChildFragment(ProjectMemberFragment.class);
            if (mPDetail.isOwnedByMe()) {
                projectFragments[CONFIGURATION] = findChildFragment(ProjectConfigurationFragment.class);
            }
        }
    }

    private void retrieveMembershipDataIfNotExist(int projectId) {
        try {
            SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
            List<IMemberShip> memberships = session.getRmUser().getMemberships();
            String membershipId = "";
            if (memberships != null && memberships.size() != 0) {
                for (IMemberShip membership : memberships) {
                    if (membership instanceof ProjectMemberShip) {
                        ProjectMemberShip pms = (ProjectMemberShip) membership;
                        if (pms.getProjectId() == projectId) {
                            membershipId = membership.getId();
                            break;
                        }
                    }
                }
            }
            if (TextUtils.isEmpty(membershipId)) {
                //get membershipId form rms.
                IProjectCommand command = new ProjectCommandExecutor();
                command.getProjectMemberShipId(projectId,
                        new ICommand.ICommandExecuteCallback<Result.GetMembershipResult, Error>() {
                            @Override
                            public void onInvoked(Result.GetMembershipResult result) {

                            }

                            @Override
                            public void onFailed(Error error) {
                                log.e("getProjectMemberShipId error:\n" + error.msg);
                            }
                        });
            }
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
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
}
