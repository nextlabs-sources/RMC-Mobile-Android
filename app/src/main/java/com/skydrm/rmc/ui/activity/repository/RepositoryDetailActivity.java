package com.skydrm.rmc.ui.activity.repository;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.account.UserLinkedRepoChangedEvent;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.errorHandler.IErrorResult;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.ILocalRepo;
import com.skydrm.rmc.reposystem.RepoInfo;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.widget.animation.ShakeAnimator;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.utils.FileHelper;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.exception.RmsRestAPIException;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;
import static com.skydrm.rmc.ExecutorPools.Select_Type.NETWORK_TASK;

/**
 * Created by hhu on 5/10/2017.
 */

public class RepositoryDetailActivity extends BaseActivity {
    //memory cache
    private static final SparseArray<RepoInfo> mRepoInfoCaches = new SparseArray<>();
    private static DevLog log = new DevLog(RepositoryDetailActivity.class.getSimpleName());
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.repo_info_container)
    LinearLayout repoInfoContainer;
    @BindView(R.id.service_image)
    ImageView serviceImage;
    @BindView(R.id.tv_user_name)
    TextView userName;
    @BindView(R.id.tv_email)
    TextView userEmail;
    @BindView(R.id.service_display_name)
    TextView serviceDisplayName;
    @BindView(R.id.edit_name)
    TextView editName;
    @BindView(R.id.service_type)
    TextView serviceType;
    @BindView(R.id.repo_space_desc)
    TextView serviceSpaceDescription;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.offline_size)
    TextView offlineSize;
    @BindView(R.id.cache_size)
    TextView cacheSize;
    @BindView(R.id.total_size)
    TextView totalSize;
    @BindView(R.id.clean_cache)
    Button cleanCache;
    @BindView(R.id.delete_attached_service)
    Button deleteService;
    @BindView(R.id.label_tv_email)
    TextView labelTvEmail;

    private BoundService boundService;
    private ProgressDialog mProgressDialog;
    private RepoInfo mRepoInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repository_detail);
        ButterKnife.bind(this);
        receiveIntent();
        initData();
        initEvent();
    }

    private void receiveIntent() {
        Bundle extras = getIntent().getExtras();
        boundService = (BoundService) extras.getSerializable(Constant.BOUND_SERVICE);
        if (boundService != null) {
            initToolbar(toolbar, boundService.getDisplayName());
        }
    }

    private void initData() {
        if (boundService == null) {
            log.e("Error:boundService == null");
            return;
        }

        // for Edit the repo nick name,
        //   each one only has rmdID can support it also, MyDrive, dose not support it
        if (boundService.type == BoundService.ServiceType.MYDRIVE) {
            editName.setVisibility(View.INVISIBLE);
        } else if (boundService.rmsRepoId == null || boundService.rmsRepoId.isEmpty()) {
            editName.setVisibility(View.INVISIBLE);
        } else {
            editName.setVisibility(View.VISIBLE);
        }

        if (boundService.type == BoundService.ServiceType.ONEDRIVE) {
            labelTvEmail.setVisibility(View.INVISIBLE);
            userEmail.setVisibility(View.INVISIBLE);
        } else {
            labelTvEmail.setVisibility(View.VISIBLE);
            userEmail.setVisibility(View.VISIBLE);
        }

        // in accordance with Requirement, Hide  Name,Emial,RepoPic for MyDrive
        if (boundService.type == BoundService.ServiceType.MYDRIVE) {
            repoInfoContainer.setVisibility(View.GONE);
        }

        setupBoundServiceImage(serviceImage, boundService);
        setupUserInfoAboutService(boundService);
    }

    private void initEvent() {
        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final UpdateRepoNameDialog dialog = new UpdateRepoNameDialog(RepositoryDetailActivity.this, boundService);
                dialog.setListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (!dialog.isTaskFinished()) {
                            return;
                        }
                        // BoundService has been update the rmsNickName need to tell other UI to change it self
                        // update current page's UI
                        initToolbar(toolbar, boundService.getDisplayName());
                        serviceDisplayName.setText(boundService.getDisplayName());
                        // and also update it into db
                        try {
                            SkyDRMApp.getInstance().dbUpdateRepoNickName(boundService);
                        } catch (Exception e) {
                            log.e(e);
                        }
                        // update in into repo system
                        try {
                            SkyDRMApp.getInstance().getRepoSystem().updateRepo(boundService);
                        } catch (Exception e) {
                            log.e(e);
                        }
                        // tell other EventBus subscribers
                        EventBus.getDefault().post(new UserLinkedRepoChangedEvent());
                    }
                });
                dialog.show();
            }
        });
        cleanCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearCacheDialog(RepositoryDetailActivity.this, boundService);
            }
        });
        deleteService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteRepoDialog(RepositoryDetailActivity.this, boundService);
            }
        });
    }

    private void setupUserInfoAboutService(BoundService boundService) {
        serviceDisplayName.setText(boundService.getDisplayName());
        mRepoInfo = mRepoInfoCaches.get(boundService.getDisplayName().hashCode());
        if (mRepoInfo == null) {
            mProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.common_waiting_initcap_3dots));
            mRepoInfo = new RepoInfo();
            retrieveRepoInfoFromRemote(boundService, false);
        } else {
            log.d("found in cache.");
            paddingData();
            retrieveRepoInfoFromRemote(boundService, true);
        }
        serviceType.setText(boundService.alias);
    }

    private void paddingData() {
        userName.setText(mRepoInfo.displayName);
        userEmail.setText(mRepoInfo.email);
        //In this case : mRepoInfo.remoteUsedSpace (1.04 GB) bigger than mRepoInfo.remoteTotalSpace(1 GB)
        serviceSpaceDescription.setText(mRepoInfo.remoteUsedSpace <= mRepoInfo.remoteTotalSpace ?
                FileHelper.formatSize(mRepoInfo.remoteUsedSpace) + " used / "
                        + FileHelper.formatSize(mRepoInfo.remoteTotalSpace) + " total" :
                FileHelper.formatSize(mRepoInfo.remoteTotalSpace) + " used / "
                        + FileHelper.formatSize(mRepoInfo.remoteTotalSpace) + " total");
        offlineSize.setText(FileHelper.formatSize(mRepoInfo.localOfflineSize));
        cacheSize.setText(FileHelper.formatSize(mRepoInfo.localCachedSize));
        totalSize.setText(FileHelper.formatSize(mRepoInfo.localTotalSize));
        double useRate = mRepoInfo.remoteUsedSpace * 1.0 / mRepoInfo.remoteTotalSpace;
        progressBar.setProgress((int) (useRate < 1.0 ? useRate * 100 : 100));
    }

    /**
     * This method is used to retrieve repository info from remote
     *
     * @param boundService the current service you need to acquire
     * @param cached       whether cached or not
     */
    private void retrieveRepoInfoFromRemote(final BoundService boundService, final boolean cached) {
        try {
            SkyDRMApp.getInstance().getRepoSystem().getRepoInformation(boundService, new ILocalRepo.IRepoInfoCallback() {
                @Override
                public void result(boolean status, RepoInfo info, String errorMsg) {
                    try {
                        if (!cached && mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        if (status) {
                            //find repoInfo item changed then notifyDataChange
                            if (mRepoInfoCaches.get(boundService.getDisplayName().hashCode()) != info) {
                                mRepoInfoCaches.put(boundService.getDisplayName().hashCode(), info);
                                mRepoInfo = info;
                                paddingData();
                            } else {
                                log.i("result: already cached the item found in remote...");
                            }
                        } else {
                            // TODO: 4/14/2017 currently we hint user with the error msg,but we will try another way to retry in future.
                            GenericError.showUI(RepositoryDetailActivity.this,
                                    getResources().getString(R.string.hint_msg_get_repo_info_failed),
                                    true,
                                    false,
                                    false,
                                    new IErrorResult() {
                                        @Override
                                        public void cancelHandler() {

                                        }

                                        @Override
                                        public void okHandler() {
                                            //                                        finish();
                                        }
                                    });
                        }
                    } catch (Exception e) {
                        //dismiss ui when exception happened
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            GenericError.showUI(RepositoryDetailActivity.this,
                    getResources().getString(R.string.hint_msg_get_repo_info_failed),
                    true,
                    false,
                    false,
                    new IErrorResult() {
                        @Override
                        public void cancelHandler() {

                        }

                        @Override
                        public void okHandler() {
                            //                                        finish();
                        }
                    });
        }
    }

    /**
     * This method is used to cleae the repository cache in local
     */
    private void showClearCacheDialog(final Context context, final BoundService boundService) {
        if (boundService == null) {
            log.e("Error:boundService==null");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        builder.setMessage(context.getResources().getString(R.string.hint_msg_ask_clean_cache));
        builder.setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SkyDRMApp.getInstance().getRepoSystem().clearRepoCache(boundService, new SkyDRMApp.ClearCacheListener() {
                    @Override
                    public void finished() {
                        // dismiss the loading dialog
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                            mProgressDialog = null;
                        }
                        //update data displayed in ui
                        mRepoInfo.localCachedSize = 0;
                        paddingData();
                        //update in cache in local
                        mRepoInfoCaches.put(boundService.getDisplayName().hashCode(), mRepoInfo);
                    }
                });
                mProgressDialog = ProgressDialog.show(context, "", context.getResources().getString(R.string.common_waiting_initcap_3dots));
            }
        });
        builder.setNegativeButton(R.string.common_cancel_initcap, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    /**
     * This method is used to delete repository user attached
     */
    private void showDeleteRepoDialog(final Context context, final BoundService boundService) {
        if (boundService == null) {
            log.e("Error:boundService == null");
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.app_name)
                .setMessage(R.string.hint_msg_ask_delete_repo_in_rms)
                .setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //clear local cache when repo is deleted
                        mRepoInfoCaches.remove(boundService.getDisplayName().hashCode());

                        // del local files and records in cache_db
                        try {
                            SkyDRMApp.getInstance().getRepoSystem().detach(boundService);
                        } catch (Exception e) {
                            ToastUtil.showToast(context.getApplicationContext(), "Exception: remove this service's local files");
                            log.e(e);
                        }
                        // del current service from database
                        SkyDRMApp.getInstance().dbDelRepo(boundService);
                        // tell RMS to del this service
                        notifyRMSDelBy(boundService);
                        // send event onto EventBus
                        EventBus.getDefault().postSticky(new UserLinkedRepoChangedEvent());
                        RepositoryDetailActivity.this.finish();
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
                    GenericError.handleCommonException(RepositoryDetailActivity.this, false, e);
                } catch (SessionInvalidException | InvalidRMClientException e) {
                    log.e(e);
                    GenericError.handleCommonException(RepositoryDetailActivity.this, false,
                            new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.AuthenticationFailed));
                } catch (Exception e) {
                    log.e(e);
                }
            }
        });
    }

    private void setupBoundServiceImage(ImageView serviceImage, BoundService boundService) {
        if (serviceImage == null) {
            log.e("Error:serviceImage == null");
            return;
        }
        if (boundService.type.equals(BoundService.ServiceType.DROPBOX)) {
            serviceImage.setImageResource(R.drawable.bottom_sheet_dropbox);
            serviceImage.setColorFilter(Color.BLACK);
        } else if (boundService.type.equals(BoundService.ServiceType.ONEDRIVE)) {
            serviceImage.setImageResource(R.drawable.bottom_sheet_onedrive);
            serviceImage.setColorFilter(Color.BLACK);
        } else if (boundService.type.equals(BoundService.ServiceType.SHAREPOINT)) {
            serviceImage.setImageResource(R.drawable.bottom_sheet_sharepoint);
            serviceImage.setColorFilter(Color.BLACK);
        } else if (boundService.type.equals(BoundService.ServiceType.SHAREPOINT_ONLINE)) {
            serviceImage.setImageResource(R.drawable.bottom_sheet_sharepoint_online);
            serviceImage.setColorFilter(Color.BLACK);
        } else if (boundService.type.equals(BoundService.ServiceType.GOOGLEDRIVE)) {
            serviceImage.setImageResource(R.drawable.bottom_sheet_googledrive);
            serviceImage.setColorFilter(Color.BLACK);
            // we support del google repo now
//            deleteService.setVisibility(View.GONE);
        } else if (boundService.type.equals(BoundService.ServiceType.BOX)) {
            serviceImage.setImageResource(R.drawable.bottom_sheet_box);
            serviceImage.setColorFilter(Color.BLACK);
        } else if (boundService.type.equals(BoundService.ServiceType.MYDRIVE)) {
            serviceImage.setImageResource(R.drawable.bottom_sheet_my_drive);
            deleteService.setVisibility(View.GONE);
        }
    }


    private static class UpdateRepoNameDialog {

        private Button btnOk;
        private EditText editText;
        private View.OnClickListener mListener;
        private ProgressBar progressBar;
        private Context mContext;
        private BoundService mRepo;
        private AlertDialog dialog;

        private boolean taskFinished = false;

        public UpdateRepoNameDialog(Context context, BoundService repo) {
            mContext = context;
            mRepo = repo;
        }

        public boolean isTaskFinished() {
            return taskFinished;
        }

        public void setListener(View.OnClickListener listener) {
            this.mListener = listener;
        }

        public void show() {

            LayoutInflater inflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.dialog_addrepo_layout, null);
            editText = (EditText) layout.findViewById(R.id.editText);
            btnOk = (Button) layout.findViewById(R.id.btn_ok);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBtnOk();
                }
            });
            // cancel btn
            layout.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    taskFinished = false;
                    mListener.onClick(null);
                }
            });
            progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);

            dialog = new AlertDialog.Builder(mContext)
                    .setCancelable(false)
                    .setTitle(R.string.app_name)
                    .setView(layout)
                    .show();
        }

        public void dismiss() {
            if (dialog != null) {
                hideSoftKeyboard();
                dialog.dismiss();
            }
        }

        private void hideSoftKeyboard() {
            if (editText == null) {
                return;
            }
            editText.clearFocus();
            InputMethodManager imm = (InputMethodManager)
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }

        public void onBtnOk() {
            // tell progressbar to move forward
            // get text from EditText as nick name
            // call RMS to add the new repo
            // amend this repo's service_id;

            final String nickname = editText.getText().toString();

            if (nickname.isEmpty()) {
                editText.setText("");
                editText.setHint("Must input at least 1 character");
                ShakeAnimator animator = new ShakeAnimator();
                animator.setTarget(editText);
                animator.startAnimation();
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(0);
                return;
            }
            // as QA required, nickname must be trimmed
            final String nickNameTrimmed = nickname.trim();

            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(10);

            class AT extends AsyncTask<Void, Integer, Boolean> {
                String newRepoID;
                private RmsRestAPIException mRmsRestAPIException;

                @Override
                protected Boolean doInBackground(Void... params) {
                    publishProgress(10);    // fake progressbar
                    try {
                        publishProgress(10);    //// fake progressbar
                        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                        return session
                                .getRmsRestAPI()
                                .getRepositoryService(session.getRmUser())
                                .repositoryUpdateNickName(mRepo.rmsRepoId, nickNameTrimmed);
                    } catch (RmsRestAPIException e) {//for 500 error param
                        mRmsRestAPIException = e;
                        log.e(e);
                    } catch (Exception e) {
                        mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
                        log.e(e);
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    progressBar.setVisibility(View.GONE);
                    if (aBoolean) {
                        taskFinished = true;
                        //
                        // only update ok, will update the repo
                        //
                        mRepo.rmsNickName = nickNameTrimmed;
                        mListener.onClick(null);
                    } else {
                        if (mRmsRestAPIException != null) {
                            switch (mRmsRestAPIException.getDomain()) {
                                case AuthenticationFailed:
                                    SkyDRMApp.getInstance().getSession().sessionExpiredHandler(mContext);
                                    break;
                                case RepoNameNotValid:
                                    ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.hint_msg_repoNameInvalid));
                                    break;
                                case NetWorkIOFailed:
                                    ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.excep_network_unavailable));
                                    break;
                                case NameTooLong:
                                    ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.hint_msg_repoNameTooLong));
                                    break;
                                case NamingViolation:
                                    ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.hint_msg_repoNameViolation));
                                    break;
                                case RepoNameCollided:
                                    ToastUtil.showToast(mContext.getApplicationContext(), mRmsRestAPIException.getMessage());
                                    break;
                                default:
                                    ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.hint_msg_repoNameEdit_error_general));
                                    break;
                            }
                        }
                        onFailedUpdate();
                    }
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    progressBar.incrementProgressBy(5);
                }
            }
            progressBar.incrementProgressBy(10);

            new AT().executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
        }

        private void onFailedUpdate() {
            log.v("false to update repo name");
            editText.setText("");
            editText.setHint("Illegal name, change another");
            ShakeAnimator animator = new ShakeAnimator();
            animator.setTarget(editText);
            animator.startAnimation();
        }

    }

}
