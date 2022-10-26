package com.skydrm.rmc.ui.activity.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.account.UpdateUserAvatarEvent;
import com.skydrm.rmc.engine.eventBusMsg.account.UpdateUserNameEvent;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.filemark.FavoriteMarkImpl;
import com.skydrm.rmc.reposystem.UserReposSyncPolicy;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.interacor.UserProfileHandler;
import com.skydrm.rmc.ui.service.offline.OfflineManager;
import com.skydrm.rmc.ui.widget.animation.ShakeAnimator;
import com.skydrm.rmc.ui.widget.avatar.AvatarView;
import com.skydrm.rmc.utils.FileHelper;
import com.skydrm.rmc.utils.SharePreferUtils;
import com.skydrm.rmc.utils.commonUtils.AvatarUtil;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.types.favorite.AllRepoFavFileRequestParas;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.utils.ParseJsonUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;

/**
 * 5/22/2017 use EventBus at this activity level to update userName and userEmail
 */
public class ProfileActivity extends BaseActivity {
    protected static DevLog log = new DevLog(ProfileActivity.class.getSimpleName());
    @BindView(R.id.root_layout)
    RelativeLayout mRlRootView;
    @BindView(R.id.user_avatar_profile)
    AvatarView mUserAvatar;
    @BindView(R.id.user_name_profile)
    TextView mTvUserName;
    @BindView(R.id.user_email_profile)
    TextView mTvUserEmail;
    @BindView(R.id.tv_sync_site)
    TextView mTvSyncSite;
    @BindView(R.id.tv_sesstion_time)
    TextView mTvSessionTime;
    @BindView(R.id.tv_last_sync_time)
    TextView mTvLastSyncTime;
    @BindView(R.id.tv_server_url)
    TextView mTvServerUrl;
    @BindView(R.id.rl_change_Password)
    RelativeLayout mRlChangePasswordContainer;

    private File mAvatarFile;
    private DataImpl presenter;

    // a sub View
    private ProfileSubviewLoader subviewLoader;

    private CalculateCacheCallback mCalculateCacheCallback;
    private CleanCacheCallback mCleanCacheCallback;

    private AlertDialog mCleanDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        initViewAndEvents();
        EventBus.getDefault().register(this);
    }

    private void initViewAndEvents() {
        (findViewById(R.id.ib_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        (findViewById(R.id.tv_logout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDUI_Logout();
            }
        });
        (findViewById(R.id.ll_user_detail)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDUI_ShowUserDetail();
            }
        });

        try {
            if (SkyDRMApp.getInstance().getSession().getRmUser().getIdpType() != 0) {
                mRlChangePasswordContainer.setVisibility(View.GONE);
            }
        } catch (InvalidRMClientException e) {
            e.printStackTrace();
        }
        mRlChangePasswordContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDUI_ChangePassword();
            }
        });

        mTvSyncSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDUI_Sync();
            }
        });
        findViewById(R.id.set_server_site).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeServerUI();
            }
        });
        // clean cache section
        (findViewById(R.id.tv_clean_site)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDUI_CleanCache();
            }
        });
        // license section
        (findViewById(R.id.rl_license_site)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDUI_License();
            }
        });
        //getting start section
        findViewById(R.id.rl_getting_start_site).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDUI_gettingStarted();
            }
        });
        // help section
        (findViewById(R.id.rl_help_site)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDUI_Help();
            }
        });
        // contact me secion
        (findViewById(R.id.rl_contact_site)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDUI_Contact();
            }
        });
        //about section
        (findViewById(R.id.rl_about_site)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCMDUI_AboutSkyDRM();
            }
        });
        findViewById(R.id.rl_preferences_site).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchTo(PreferencesActivity.class);
            }
        });
        //user avatar file callback
        AvatarUtil.getInstance().setISavePhoto(new AvatarUtil.ISavePhoto() {
            @Override
            public void savePhoto(File photoFile) {
                mAvatarFile = photoFile;
            }
        });


        // fillData
        updateUILastSyncTime(null);
        presenter = new DataImpl();
        presenter.setUserAvatar(mUserAvatar);
        presenter.setDisplayName(mTvUserName);
        presenter.setDisplayEmail(mTvUserEmail);
        presenter.setSession(mTvSessionTime);
        //get config url
        mTvServerUrl.setText(getServerUrl());
    }

    private void showChangeServerUI() {
//        ChangeServerUrlPopupWindow popupWindow = new ChangeServerUrlPopupWindow(ProfileActivity.this);
//        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        //launchTo(CompanyAccountListActivity.class);
    }

    private String getServerUrl() {
        return (String) SharePreferUtils.getParams(getApplicationContext(), Constant.INPUT_SERVER_URL, "");
    }

    /* change user avatar may choose a pic from local or take a picture by camera
     *  both of them will start a new activity, so here it for to receive that started activity result*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constant.PICK_PHOTO_FOR_AVATAR:
                    if (data != null && data.getData() != null) {
                        try {
                            Intent clipIntent = new Intent(ProfileActivity.this, ClipActivity.class);
                            clipIntent.putExtra("path_uri", data.getData().toString());
                            startActivityForResult(clipIntent, Constant.RESIZE_REQUEST_CODE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case Constant.TAKE_PHOTO_FOR_AVATAR:
                    if (mAvatarFile == null) {
                        log.e("onActivityResult: avatar==null");
                        return;
                    }
                    try {
                        MediaStore.Images.Media.insertImage(getContentResolver(),
                                mAvatarFile.getAbsolutePath(), mAvatarFile.getName(), null);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    //this is used to notify system image gallery to update
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mAvatarFile)));
                    // after get the camera taked phote, need to resize/compact the file
                    Intent takePhotoIntent = new Intent(ProfileActivity.this, ClipActivity.class);
                    takePhotoIntent.setAction("take_photo");
                    takePhotoIntent.putExtra("path_uri", mAvatarFile.getAbsolutePath());
                    startActivityForResult(takePhotoIntent, Constant.RESIZE_REQUEST_CODE);
                    break;
                case Constant.RESIZE_REQUEST_CODE:  // ClipActivity returned
                    String clip_path = data.getStringExtra("clip_path");
                    log.v("onActivityResult: " + clip_path);
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (subviewLoader != null) {
                return subviewLoader.handleBackKey() || super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AvatarUtil.getInstance().setISavePhoto(null);
        // unregister EventBus
        EventBus.getDefault().unregister(this);
        if (mCalculateCacheCallback != null) {
            mCalculateCacheCallback = null;
        }
        if (mCleanCacheCallback != null) {
            mCleanCacheCallback = null;
        }
        if (mCleanDialog != null) {
            mCleanDialog.dismiss();
            mCleanDialog = null;
        }
    }

    private void updateUILastSyncTime(@Nullable final String timeString) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (timeString == null || timeString.isEmpty()) {
                        mTvLastSyncTime.setText(SkyDRMApp.getInstance().getSession().getSyncTimeFriendly());
                    } else {
                        mTvLastSyncTime.setText(timeString);
                    }
                    // give an animation
                    ShakeAnimator animation = new ShakeAnimator();
                    animation.setTarget(mTvLastSyncTime);
                    animation.startAnimation();
                } catch (Exception e) {
                    log.e(e);
                }
            }
        });
    }

    private void onCMDUI_ShowUserDetail() {
//        profileViewHelper.showUserDetailView((FrameLayout) findViewById(R.id.subview_container));
        subviewLoader = new ProfileSubviewLoader(this,
                ProfileSubviewLoader.SubviewType.USER_DETAIL_VIEW,
                (FrameLayout) findViewById(R.id.subview_container));
        subviewLoader.dispatchSubview();
    }

    private void onCMDUI_Logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(OfflineManager.getInstance().isTaskRunning() ? R.string.hint_msg_ask_log_out_when_marking_offline :
                        R.string.hint_msg_ask_log_out)
                .setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (OfflineManager.getInstance().isTaskRunning()) {
                            OfflineManager.getInstance().cancelAll();
                        }
                        SkyDRMApp.getInstance().logout(ProfileActivity.this);
                    }
                })
                .setNegativeButton(R.string.common_cancel_initcap, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    private void onCMDUI_ChangePassword() {
        launchTo(PasswordActivity.class);
    }

    @SuppressLint("SetTextI18n")
    private void onCMDUI_Sync() {
        new SessionSyncFeature().Syncing();
    }

    private void onCMDUI_CleanCache() {
        mCalculateCacheCallback = new CalculateCacheCallback();
        CacheCalculateTask task = new CacheCalculateTask(mCalculateCacheCallback);
        task.run();
    }

    private void onCMDUI_License() {
//        ToastUtil.showToast(getApplicationContext(), "License");
    }

    private void onCMDUI_gettingStarted() {
        subviewLoader = new ProfileSubviewLoader(this,
                ProfileSubviewLoader.SubviewType.GETTING_START_VIEW,
                (FrameLayout) findViewById(R.id.subview_container));
        subviewLoader.dispatchSubview();
    }

    private void onCMDUI_Help() {
        subviewLoader = new ProfileSubviewLoader(this,
                ProfileSubviewLoader.SubviewType.HELP_VIEW,
                (FrameLayout) findViewById(R.id.subview_container));
        subviewLoader.dispatchSubview();
    }

    private void onCMDUI_Contact() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:support@skydrm.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "from skyDRM android app");
        intent.putExtra(Intent.EXTRA_TEXT, "hi,skydrm.com");
        startActivity(intent);
    }

    private void onCMDUI_AboutSkyDRM() {
        launchTo(AboutSkyDRMActivity.class);
    }


    /* need a point to detect that user has change account name by calling RMS api*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateNameEventHandler(UpdateUserNameEvent event) {
        presenter.setDisplayName(mTvUserName);
        // give subView a chance to update itself
        if (subviewLoader != null) {
            subviewLoader.onNotify_NameChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateAvatarEventHandler(UpdateUserAvatarEvent event) {
        presenter.setUserAvatar(mUserAvatar);
        // give subView a chance to update itself
        if (subviewLoader != null) {
            subviewLoader.onNotify_AvatarChanged();
        }
    }

    // update sync textview;
    class SessionSyncFeature {
        int count = 0;

        void setText(final String msg) {
            ProfileActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvSyncSite.setText(msg);
                }
            });
        }

        Activity getActivity() {
            return ProfileActivity.this;
        }

        void Syncing() {
            setText("Syncing");
            //add by henry (sync user profile info)
            UserProfileHandler.getDefault().retrieveUserProfileInfo(getActivity(), null);
            // sync repo from RMS
            UserReposSyncPolicy.syncing(ExecutorPools.SelectSmartly(FIRED_BY_UI), new UserReposSyncPolicy.UIListener() {
                @Override
                public void progressing(@NonNull String msg) {
                    switch ((++count) % 3) {
                        case 0:
                            setText("Syncing.");
                            break;
                        case 1:
                            setText("Syncing..");
                            break;
                        case 2:
                            setText("Syncing...");
                            break;
                        default:
                            setText("Syncing");
                    }
                }

                @Override
                public void result(boolean status) {
                    if (status) {
                        // update sync time when OK
                        SkyDRMApp.getInstance().getSession().setSyncTime(System.currentTimeMillis());
                        updateUILastSyncTime(SkyDRMApp.getInstance().getSession().getSyncTimeFriendly());
                        setText(getString(R.string.sync_now));
                    } else {
                        setText(getString(R.string.failed_try_again));
                    }
//                    // add sync fav&off marks
                    ExecutorPools.SelectSmartly(FIRED_BY_UI).execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                log.d("update file marks fav&off in Profile Sync");
                                // post
                                FavoriteMarkImpl.getInstance().syncMarkedFileToRms();
                                // get
                                List<ParseJsonUtils.AllRepoFavoListBean> repoList = FavoriteMarkImpl.getInstance().getAllRepoFavFileList(new AllRepoFavFileRequestParas());
                                // update
                                SkyDRMApp.getInstance().onUpdateFileMark(repoList);
                                //add sync user preference feature.
                                try {
                                    SkyDRMApp.getInstance().getSession().syncUserPreference();
                                } catch (SessionInvalidException e) {
                                    e.printStackTrace();
                                }
                                // update UI
                                setText(getString(R.string.sync_now));
                            } catch (RmsRestAPIException e) {
                                setText(getString(R.string.failed_try_again));
                                GenericError.handleCommonException(getActivity(), false, e);
                            }
                        }
                    });
                }
            });
        }
    }


    public class DataImpl {
        public void setDisplayName(TextView nameComponent) {
            try {
                if (null == nameComponent) {
                    log.e("error:namePresenter is null.");
                    return;
                }
                IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
                if (null == rmUser) {
                    log.e("error:rmUser is null.");
                    return;
                }
                nameComponent.setText(rmUser.getName());
            } catch (InvalidRMClientException e) {
                log.e(e);
            }
        }

        public void setDisplayEmail(TextView emailComponent) {
            try {
                if (null == emailComponent) {
                    log.e("error:emailComponent is null.");
                    return;
                }
                IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
                if (null == rmUser) {
                    log.e("error:rmUser is null");
                    return;
                }
                emailComponent.setText(rmUser.getEmail());
            } catch (InvalidRMClientException e) {
                log.e(e);
            }
        }

        public void setUserAvatar(AvatarView avatarComponent) {
            AvatarUtil.getInstance().setUserAvatar(ProfileActivity.this, avatarComponent);
        }

        public void setSession(TextView sessionComponent) {
            if (null == sessionComponent) {
                log.e("error:sessionComponent is null");
                return;
            }
            sessionComponent.setText(SkyDRMApp.getInstance().getSession().getExpiredTimeFriendly());
        }
    }

    class CalculateCacheCallback implements CacheCalculateTask.ITaskCallback<CacheCalculateTask.Result, String> {

        @Override
        public void onTaskPreExecute() {
            mCleanDialog = new AlertDialog.Builder(ProfileActivity.this).create();
            mCleanDialog.setTitle(R.string.app_name);
            View view = View.inflate(ProfileActivity.this, R.layout.layout_calculating_size, null);
            mCleanDialog.setView(view);
            mCleanDialog.setCancelable(false);
            mCleanDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            mCleanDialog.show();
        }

        @Override
        public void onTaskExecuteSuccess(CacheCalculateTask.Result results) {
            if (mCleanDialog != null) {
                mCleanDialog.dismiss();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
            builder.setMessage(ProfileActivity.this.getString(R.string.clean_cache_content) + " " + FileHelper.formatSize(results.size));
            builder.setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    mCleanCacheCallback = new CleanCacheCallback();
                    CacheCleanTask task = new CacheCleanTask(mCleanCacheCallback);
                    task.run();
                }
            });
            builder.setNegativeButton(R.string.common_cancel_initcap, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            mCleanDialog = builder.create();
            builder.setCancelable(false);
            builder.show();
        }

        @Override
        public void onTaskExecuteFailed(String e) {
            ToastUtil.showToast(ProfileActivity.this, e);
            if (mCleanDialog != null) {
                mCleanDialog.dismiss();
            }
        }
    }

    class CleanCacheCallback implements CacheCleanTask.ITaskCallback<CacheCleanTask.Result, String> {

        @Override
        public void onTaskPreExecute() {
            if (mCleanDialog != null) {
                mCleanDialog.dismiss();
            }
        }

        @Override
        public void onTaskExecuteSuccess(CacheCleanTask.Result results) {
            if (mCleanDialog != null) {
                mCleanDialog.dismiss();
            }
        }

        @Override
        public void onTaskExecuteFailed(String e) {
            ToastUtil.showToast(ProfileActivity.this, e);
            if (mCleanDialog != null) {
                mCleanDialog.dismiss();
            }
        }
    }
}
