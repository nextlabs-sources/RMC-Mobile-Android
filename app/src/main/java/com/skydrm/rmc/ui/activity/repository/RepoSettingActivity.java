package com.skydrm.rmc.ui.activity.repository;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.dropbox.core.v2.users.FullAccount;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.account.UserLinkedRepoChangedEvent;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.RepoBindHelper;
import com.skydrm.rmc.reposystem.remoterepo.dropbox.NxDropBox2;
import com.skydrm.rmc.reposystem.remoterepo.googledrive2.GoogleOAuth2;
import com.skydrm.rmc.reposystem.remoterepo.googledrive2.NxGoogleDrive2;
import com.skydrm.rmc.reposystem.remoterepo.onedrive2.NxOneDrive2;
import com.skydrm.rmc.reposystem.remoterepo.onedrive2.OAuth2Activity;
import com.skydrm.rmc.reposystem.remoterepo.sharepointonline.SharePointAuthManager;
import com.skydrm.rmc.reposystem.remoterepo.sharepointonline.SharePointOnlineSdk;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.exception.RmsRestAPIException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;
import static com.skydrm.sdk.exception.RmsRestAPIException.ExceptionDomain.RepoNotExist;

public class RepoSettingActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.fl_subview_container)
    FrameLayout mFlSubviewContainer;
    private List<BoundService> mRepos;
    private SkyDRMApp app = SkyDRMApp.getInstance();
    private List<BoundService> mSupportedRepos = new ArrayList<>();
    private List<BoundService> mNextLabsRepos = new ArrayList<>();
    private RepoBindHelper mRepoBindHelper;
    private BoundService spoiledRepo;
    private boolean WaitDropBoxOAuth2 = false;
    private boolean WaitSharepointOnlineOAuth2 = false;
    private boolean waitSharePointReAuth;
    private RepositoryAdapter repositoryAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo_setting);
        //
        // 3rd party force
        //
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        initToolbar();
        initView();
        initData();
        initEvent();
        // by Osmond.
        // as QA required feature: NO 3rd party repos attached, go to Add Repository directly
        if (mSupportedRepos.isEmpty()) {
            startActivityForResult(new Intent(RepoSettingActivity.this, SupportedCloud.class),
                    getResources().getInteger(R.integer.req_pick_a_cloud_service));
        }
        // new feature, select a spoiled repo at home to re-auth
        reAuthentication();
    }

    private void reAuthentication() {
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action) && action.equals(Constant.RE_AUTHENTICATION)) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    final BoundService invalidService = (BoundService) extras.getSerializable(Constant.BOUND_SERVICE);
                    if (invalidService != null) {
                        //
                        // to avoid bug, wrapper onClickSpoiledRepo in event, to avoid the life-cycle onCreate,onResume
                        //
                        SkyDRMApp.getInstance().getGlobalUIHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                onClickSpoiledRepo(invalidService);
                            }
                        });
                    }
                }
            }
        }
    }

    private void initToolbar() {
        initToolbar(toolbar, getString(R.string.repositories));
    }

    private void initView() {
        mRepoBindHelper = new RepoBindHelper(RepoSettingActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(RepoSettingActivity.this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(false);
        repositoryAdapter = new RepositoryAdapter(RepoSettingActivity.this, mSupportedRepos, mNextLabsRepos);
        recyclerView.setAdapter(repositoryAdapter);
        mFlSubviewContainer = (FrameLayout) findViewById(R.id.fl_subview_container);
    }

    private void initData() {
        //fill data
        mRepos = app.getUserLinkedRepos();
        mSupportedRepos.clear();
        mNextLabsRepos.clear();
        if (mRepos != null && mRepos.size() != 0) {
            for (BoundService boundService : mRepos) {
                if (boundService.type.equals(BoundService.ServiceType.MYDRIVE)) {
                    if (!mNextLabsRepos.contains(boundService)) {
                        mNextLabsRepos.add(boundService);
                    }
                } else {
                    if (!mSupportedRepos.contains(boundService)) {
                        mSupportedRepos.add(boundService);
                    }
                }
            }
        }
        repositoryAdapter.notifyAllDataSetChanged();
    }

    private void initEvent() {
        repositoryAdapter.setOnItemClickListener(new RepositoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, BoundService boundService) {
                if (position == 0 && boundService == null) {
                    startActivityForResult(new Intent(RepoSettingActivity.this, SupportedCloud.class),
                            getResources().getInteger(R.integer.req_pick_a_cloud_service));
                } else {
                    if (boundService != null) {
                        if (boundService.isValidRepo()) {
                            launchTo(RepositoryDetailActivity.class, Constant.BOUND_SERVICE, boundService);
                        } else {
                            onClickSpoiledRepo(boundService);
                        }
                    }
                }
            }
        });

        repositoryAdapter.setOnDelItemListener(new RepositoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, final BoundService service) {
                if (service != null && !service.isValidRepo()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RepoSettingActivity.this)
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.hint_msg_ask_delete_repo_in_rms)
                            .setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    // del local files and records in cache_db
                                    try {
                                        SkyDRMApp.getInstance().getRepoSystem().detach(service);
                                    } catch (Exception e) {
                                        ToastUtil.showToast(RepoSettingActivity.this.getApplicationContext(), "Exception: remove this service's local files");
                                        log.e(e);
                                    }
                                    // del current service from database
                                    SkyDRMApp.getInstance().dbDelRepo(service);
                                    // tell RMS to del this service
                                    notifyRMSDelBy(service);
                                    // send event onto EventBus
                                    EventBus.getDefault().postSticky(new UserLinkedRepoChangedEvent());
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
            }
        });

        mRepoBindHelper.setOnGetServiceFinish(new RepoBindHelper.OnGetServiceFinish() {
            @Override
            public void onGetServiceFinish(BoundService boundService) {
                initData();
                // tell others with Event bus
                EventBus.getDefault().post(new UserLinkedRepoChangedEvent());
            }
        });
    }

    private void notifyRMSDelBy(final BoundService service) {
        // sanity check
        if (service == null) {
            log.e("service is null / notifyRMSDelBy");
            return;
        }
        if (service.rmsRepoId == null || service.rmsRepoId.length() == 0) {
            log.e("service.rmsRepoId is null or empty / notifyRMSDelBy");
            return;
        }
        // forbid user to delete mydrive
        if (service.type == BoundService.ServiceType.MYDRIVE) {
            log.e("forbid user to delete myDrive / notifyRMSDelBy");
            return;
        }

        ExecutorPools.SelectSmartly(FIRED_BY_UI).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                    session.getRmsRestAPI().getRepositoryService(session.getRmUser()).repositoryRemove(service.rmsRepoId);
                } catch (RmsRestAPIException e) {
                    log.e(e);
                    GenericError.handleCommonException(RepoSettingActivity.this, false, e);
                } catch (SessionInvalidException | InvalidRMClientException e) {
                    log.e(e);
                    GenericError.handleCommonException(RepoSettingActivity.this, false,
                            new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed));
                } catch (Exception e) {
                    log.e(e);
                }
            }
        });
    }

    /* in RepositoryDetailsActivity3,  a repo nick name may be changed,
     *  so here is to be notified to update itself
     *  */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncUserRepoWithRMSHandler(UserLinkedRepoChangedEvent event) {
        log.v("onSyncUserRepoWithRMSHandler");
        initData();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (WaitDropBoxOAuth2) {
            WaitDropBoxOAuth2 = false;
            onAuthReAuth_Dropbox();
        }
//        if (WaitSharepointOnlineOAuth2) {
//            WaitSharepointOnlineOAuth2 = false;
//            onAuthReAuth_SharepointOnline();
//        }
        if (waitSharePointReAuth) {
            onAuthReAuth_SharePoint();
            waitSharePointReAuth = false;
        }
    }

    private void onAuthReAuth_Dropbox() {
        final String dropboxAccessToken = NxDropBox2.getOAuth2Token();
        if (dropboxAccessToken != null) {
            Log.i("SettingAty", "successful Authentication of dropBox OAuth2");
            // update the spoiled repo
            // try to update this repo
            ExecutorPools.SelectSmartly(FIRED_BY_UI).execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        FullAccount account = NxDropBox2.getAccount(dropboxAccessToken);
                        if (account != null) {
                            String id = NxDropBox2.getOAuth2UID();
                            if (TextUtils.equals(spoiledRepo.accountID, id)) {
                                //update spoiledRepo's EncryptToken
                                spoiledRepo.account = account.getEmail();
                                spoiledRepo.rmsToken = dropboxAccessToken;
                                spoiledRepo.accountToken = dropboxAccessToken;
                                // must update database
                                app.dbUpdateRepoToken(spoiledRepo);

                                // tell ui
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mRepos.clear();
                                        mRepos.addAll(app.getUserLinkedRepos());
                                        repositoryAdapter.notifyDataSetChanged();
                                    }
                                });
                                // tell repo-system to activate this repo
                                SkyDRMApp.getInstance().getRepoSystem().updateRepo(spoiledRepo);
                                SkyDRMApp.getInstance().getRepoSystem().activateRepo(spoiledRepo);

                                try {
                                    // tell rms to update
                                    SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                                    session.getRmsRestAPI().getRepositoryService(session.getRmUser()).repositoryUpdate(
                                            spoiledRepo.rmsRepoId,
                                            spoiledRepo.rmsNickName,
                                            spoiledRepo.accountToken);
                                } catch (RmsRestAPIException e) {
                                    log.e(e);
                                    if (e.getDomain() == RepoNotExist) {
                                        onErrorHandler_UpdateWithRMSWhenRepoNotExist(spoiledRepo);
                                    }
                                }
                                // tell to EventUBs
                                EventBus.getDefault().post(new UserLinkedRepoChangedEvent());
                            } else { // mismatch User Id
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        GenericError.showUI(RepoSettingActivity.this,
                                                getString(R.string.error_repo_mismatch_account),
                                                true,
                                                false,
                                                false,
                                                null);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e("SettingAty", "Authentication failed of dropBox OAuth2");
            Toast.makeText(RepoSettingActivity.this, getString(R.string.hint_msg_oauth2_failed), Toast.LENGTH_SHORT).show();
        }
    }

//    private void onAuthReAuth_SharepointOnline() {
//        if (SharePointOnlineSdk.getAuthStatus()) {
//            Log.i("SettingAty", "successful Authentication of dropBox OAuth2");
//            // update the spoiled repo
//            // try to update this repo
//            ExecutorPools.SelectSmartly(FIRED_BY_UI).execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
////                            DropboxAPI.Account account = NXDropBox.getAccount();
//                        SharePointOnlineSdk.Account account = SharePointOnlineSdk.getAuthAccount();
//
//                        if (account != null) {
//                            String id = account.getAccountId();
//                            if (TextUtils.equals(spoiledRepo.accountID, id)) {
//                                //update spoiledRepo's EncryptToken
//                                spoiledRepo.account = account.getAccountName();
//                                spoiledRepo.rmsToken = account.getToken();
//                                spoiledRepo.accountToken = account.getToken();
//                                // must update database
//                                app.dbUpdateRepoToken(spoiledRepo);
//
//                                // tell ui
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        mRepos.clear();
//                                        mRepos.addAll(app.getUserLinkedRepos());
//                                        repositoryAdapter.notifyDataSetChanged();
//                                    }
//                                });
//                                // tell repo-system to activate this repo
//                                SkyDRMApp.getInstance().getRepoSystem().updateRepo(spoiledRepo);
//                                SkyDRMApp.getInstance().getRepoSystem().activateRepo(spoiledRepo);
//                                // tell rms to update
//                                try {
//                                    SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
//                                    session.getRmsRestAPI().getRepositoryService(session.getRmUser()).repositoryUpdate(
//                                            spoiledRepo.rmsRepoId,
//                                            spoiledRepo.rmsNickName,
//                                            spoiledRepo.accountToken);
//                                } catch (RmsRestAPIException e) {
//                                    log.e(e);
//                                    if (e.getDomain() == RepoNotExist) {
//                                        onErrorHandler_UpdateWithRMSWhenRepoNotExist(spoiledRepo);
//                                    }
//                                }
//                                // tell to EventUBs
//                                EventBus.getDefault().post(new UserLinkedRepoChangedEvent());
//                            } else { // mismatch User Id
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        GenericError.showUI(RepoSettingActivity.this,
//                                                getString(R.string.error_repo_mismatch_account),
//                                                true,
//                                                false,
//                                                false,
//                                                null);
//                                    }
//                                });
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        } else {
//            Log.e("SettingAty", "Authentication failed of SharepointOnline OAuth2");
//            Toast.makeText(RepoSettingActivity.this, getString(R.string.hint_msg_oauth2_failed), Toast.LENGTH_SHORT).show();
//        }
//    }

    private void onAuthReAuth_SharePoint() {
        if (SharePointAuthManager.isAuthorized()) {
            SharePointAuthManager.resetAuthStatus();
            ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI).execute(new Runnable() {
                @Override
                public void run() {
                    SharePointAuthManager.Account account = SharePointAuthManager.getAccount();
                    if (account != null) {
                        if (TextUtils.equals(spoiledRepo.accountID, account.domain)) {
                            //update spoiledRepo's EncryptToken
                            spoiledRepo.accountID = account.domain;
                            spoiledRepo.accountName = account.domain;
                            spoiledRepo.accountToken = account.password;
                            // must update database
                            app.dbUpdateRepoToken(spoiledRepo);
                            // tell ui
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRepos.clear();
                                    mRepos.addAll(app.getUserLinkedRepos());
                                    repositoryAdapter.notifyDataSetChanged();

                                }
                            });
                            // tell repo-system to activate this repo
                            try {
                                SkyDRMApp.getInstance().getRepoSystem().updateRepo(spoiledRepo);
                                SkyDRMApp.getInstance().getRepoSystem().activateRepo(spoiledRepo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // tell to EventUBs
                            EventBus.getDefault().post(new UserLinkedRepoChangedEvent());
                        }
                    }
                }
            });
        }
    }

    private void onAuthReAuth_OneDrive(final String authCode) {
        if (authCode == null) {
            Toast.makeText(RepoSettingActivity.this, getString(R.string.hint_msg_oauth2_failed), Toast.LENGTH_SHORT).show();
            return;
        }
        // try to update this repo
        ExecutorPools.SelectSmartly(FIRED_BY_UI).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    NxOneDrive2.Token token = NxOneDrive2.TokenExchangeHandler.handler(authCode).call();
                    NxOneDrive2.UserInfo account = NxOneDrive2.UserInfoHandler.handler(token.accessToken).call();
                    if (account != null) {
                        String id = account.userId;
                        String trimmed_id = id.substring(1);
                        if (TextUtils.equals(spoiledRepo.accountID, id) || TextUtils.equals(spoiledRepo.accountID, trimmed_id)) {
                            //update spoiledRepo's EncryptToken
                            spoiledRepo.account = account.email;
                            spoiledRepo.rmsToken = token.refreshToken;
                            spoiledRepo.accountToken = token.refreshToken;
                            // must update database
                            app.dbUpdateRepoToken(spoiledRepo);
                            // notify UI change
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRepos.clear();
                                    mRepos.addAll(app.getUserLinkedRepos());
                                    repositoryAdapter.notifyDataSetChanged();
                                }
                            });
                            // tell repo-system to activate this repo
                            SkyDRMApp.getInstance().getRepoSystem().updateRepo(spoiledRepo);
                            SkyDRMApp.getInstance().getRepoSystem().activateRepo(spoiledRepo);
                            // tell rms to update
                            try {
                                SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                                session.getRmsRestAPI()
                                        .getRepositoryService(session.getRmUser())
                                        .repositoryUpdate(
                                                spoiledRepo.rmsRepoId,
                                                spoiledRepo.rmsNickName,
                                                spoiledRepo.accountToken);
                            } catch (RmsRestAPIException e) {
                                log.e(e);
                                if (e.getDomain() == RepoNotExist) {
                                    onErrorHandler_UpdateWithRMSWhenRepoNotExist(spoiledRepo);
                                }
                            }
                            // tell to EventUBs
                            EventBus.getDefault().post(new UserLinkedRepoChangedEvent());
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    GenericError.showUI(RepoSettingActivity.this,
                                            getResources().getString(R.string.error_repo_mismatch_account),
                                            true,
                                            false,
                                            false,
                                            null);
                                }
                            });
                        }
                    } else {
                        Log.e("SettingAty", "Authentication failed of Onedrive OAuth2");
                        Toast.makeText(RepoSettingActivity.this, getString(R.string.hint_msg_oauth2_failed), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    log.e(e);
                }
            }
        });
    }

    private void onAutReAuth_GoogleDrive(final String authCode) {
        if (authCode == null) {
            Toast.makeText(RepoSettingActivity.this, getString(R.string.hint_msg_oauth2_failed), Toast.LENGTH_SHORT).show();
            return;
        }
        // update the spoiled repo
        // try to update this repo
        ExecutorPools.SelectSmartly(FIRED_BY_UI).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    NxGoogleDrive2.Token t = NxGoogleDrive2.TokenExchangeHandler.handler(authCode).call();
                    NxGoogleDrive2.About account = NxGoogleDrive2.AboutHandler.handler(t.accessToken, t.type).call();
                    // for google drive, make sure emailAddress matched
                    if (!TextUtils.equals(spoiledRepo.accountID, t.userID)) { // mismatch User Id
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                GenericError.showUI(RepoSettingActivity.this,
                                        getString(R.string.error_repo_mismatch_account),
                                        true,
                                        false,
                                        false,
                                        null);
                            }
                        });
                        return;
                    }
                    //update spoiledRepo's EncryptToken
                    spoiledRepo.account = account.emailAddress;
                    spoiledRepo.rmsToken = t.refreshToken;
                    spoiledRepo.accountToken = t.refreshToken;
                    // must update database
                    app.dbUpdateRepoToken(spoiledRepo);

                    // tell ui
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRepos.clear();
                            mRepos.addAll(app.getUserLinkedRepos());
                            repositoryAdapter.notifyDataSetChanged();
                        }
                    });
                    // tell repo-system to activate this repo
                    SkyDRMApp.getInstance().getRepoSystem().updateRepo(spoiledRepo);
                    SkyDRMApp.getInstance().getRepoSystem().activateRepo(spoiledRepo);

                    try {
                        // tell rms to update
                        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                        session.getRmsRestAPI().getRepositoryService(session.getRmUser()).repositoryUpdate(
                                spoiledRepo.rmsRepoId,
                                spoiledRepo.rmsNickName,
                                spoiledRepo.accountToken);
                    } catch (RmsRestAPIException e) {
                        log.e(e);
                        if (e.getDomain() == RepoNotExist) {
                            onErrorHandler_UpdateWithRMSWhenRepoNotExist(spoiledRepo);
                        }
                    }
                    // tell to EventUBs
                    EventBus.getDefault().post(new UserLinkedRepoChangedEvent());
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RepoSettingActivity.this, getString(R.string.hint_msg_oauth2_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    private void onErrorHandler_UpdateWithRMSWhenRepoNotExist(BoundService s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GenericError.showUI(RepoSettingActivity.this,
                        getString(R.string.hint_msg_repoNotExistWhenReauth),
                        true,
                        false,
                        false,
                        null);
            }
        });
        // delete this non-existed repo at local
        try {
            // del local files and records in fs
            SkyDRMApp.getInstance().getRepoSystem().detach(s);
            // del current service from database
            SkyDRMApp.getInstance().dbDelRepo(s);
        } catch (Exception e) {
            log.e(e);
        }
    }

    // repo can't be used, need to re-auth
    private void onClickSpoiledRepo(BoundService repo) {
        spoiledRepo = repo;
        // try to re-auth
        switch (repo.type) {
            case DROPBOX:
                WaitDropBoxOAuth2 = true;
                // receive result at Activity.onResume
                NxDropBox2.startOAuth2Authentication(RepoSettingActivity.this);
                break;
            case SHAREPOINT_ONLINE:
//                WaitSharepointOnlineOAuth2 = true;
//                // receive result at Activity.onResume
//                SharePointOnlineSdk.startAuth(this);
                break;
            case ONEDRIVE:
                OAuth2Activity.startOAuth2Authentication(this);
                break;
            case GOOGLEDRIVE:
                GoogleOAuth2.startOAuth2Authentication(this);
                break;
            case SHAREPOINT:
                waitSharePointReAuth = true;
                SharePointAuthManager.reAuth(this, repo);
                break;
            default:
                break;
        }
    }

    /* for user attach repo at SupportedCloud will return result here*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int TASK = getResources().getInteger(R.integer.req_pick_a_cloud_service);
        if (requestCode == TASK) {
            if (resultCode == Activity.RESULT_OK) {
                String name = data.getStringExtra(getString(R.string.picked_cloud_name));
                mRepoBindHelper.executeAccountAsyncTask(name);
                return;
            }
        }
        // try to get google re-auth and onedrive re-auth
        // parse result here
        if (data == null) {
            return;
        }
        boolean isOneDrive = TextUtils.equals(data.getStringExtra("Repo"), "OneDrive");
        boolean isGoogleDrive = TextUtils.equals(data.getStringExtra("Repo"), "GoogleDrive");
        String AuthCode = data.getStringExtra("AuthorizationCode");
        if (AuthCode == null) {
            return;
        }
        if (isGoogleDrive) {
            onAutReAuth_GoogleDrive(AuthCode);
            return;
        }
        if (isOneDrive) {
            onAuthReAuth_OneDrive(AuthCode);
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
