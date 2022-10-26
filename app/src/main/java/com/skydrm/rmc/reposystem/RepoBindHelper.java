package com.skydrm.rmc.reposystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dropbox.core.v2.users.FullAccount;
import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.reposystem.remoterepo.dropbox.NxDropBox2;
import com.skydrm.rmc.reposystem.remoterepo.googledrive2.GoogleOAuth2;
import com.skydrm.rmc.reposystem.remoterepo.googledrive2.NxGoogleDrive3;
import com.skydrm.rmc.reposystem.remoterepo.onedrive2.NxOneDrive2;
import com.skydrm.rmc.reposystem.remoterepo.onedrive2.OAuth2Activity;
import com.skydrm.rmc.reposystem.remoterepo.sharepointonline.NXSharePointOnPremise;
import com.skydrm.rmc.reposystem.remoterepo.sharepointonline.NXSharePointOnline;
import com.skydrm.rmc.reposystem.remoterepo.sharepointonline.SharePointOnlineSdk;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.widget.animation.ShakeAnimator;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;

import java.util.List;

import static com.skydrm.rmc.ExecutorPools.Select_Type.NETWORK_TASK;


public class RepoBindHelper
        implements
        NxDropBox2.GetAccountAsyncTask.IGetAccountAsyncTask,
        NxOneDrive2.GetAccountAsyncTask.IGetAccountAsyncTask,
        NXSharePointOnline.GetAccountAsyncTask.IGetAccountAsyncTask,
        NxGoogleDrive3.GetAccountAsyncTask.IGetAccountAsyncTask,
        NXSharePointOnPremise.GetAccountAsyncTask.IGetAccountAsyncTask

//        appInstance.remoteRepo.sharepoint.AsyncTask.GetAccountAsyncTask.IGetAccountAsyncTask,
{
    static private DevLog log = new DevLog(RepoBindHelper.class.getSimpleName());

    private Activity mHomeActivity;
    private SkyDRMApp app;
    private OnGetServiceFinish onGetServiceFinish;


    public RepoBindHelper(Activity activity) {
        mHomeActivity = activity;
        app = SkyDRMApp.getInstance();
    }

    public void setOnGetServiceFinish(OnGetServiceFinish onItemClickListener) {
        this.onGetServiceFinish = onItemClickListener;
    }

    public void executeAccountAsyncTask(String name) {
        if (name.equals(mHomeActivity.getString(R.string.name_dropbox))) {
            NxDropBox2.GetAccountAsyncTask task = new NxDropBox2.GetAccountAsyncTask();
            task.setCallBack(NxDropBox2.getOAuth2Token(), this);
            task.executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
        }
        if (name.equals(mHomeActivity.getString(R.string.name_onedrive))) {
            NxOneDrive2.GetAccountAsyncTask task = new NxOneDrive2.GetAccountAsyncTask(OAuth2Activity.getLastAuthCode(), this);
            task.executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
        }

        if (name.equals(mHomeActivity.getString(R.string.name_sharepointonline))) {
            NXSharePointOnline.GetAccountAsyncTask task = new NXSharePointOnline.GetAccountAsyncTask();
            task.setCallBack(this);
            task.executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
        }
        if (name.equals(mHomeActivity.getString(R.string.name_googledrive))) {
            NxGoogleDrive3.GetAccountAsyncTask task = new NxGoogleDrive3.GetAccountAsyncTask(GoogleOAuth2.getLastAuthCode(), this);
            task.executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
        }

        if (name.equals(mHomeActivity.getString(R.string.name_sharepoint))) {
            NXSharePointOnPremise.GetAccountAsyncTask task = new NXSharePointOnPremise.GetAccountAsyncTask();
            task.setCallBack(this);
            task.executeOnExecutor(ExecutorPools.SelectSmartly(NETWORK_TASK));
        }
    }

    // CallBack Dorpbox
    @Override
    public void onFinishGet(FullAccount account) {
        //bugs account may be null
        if (account == null) {
            hintBoundServiceHadBind(mHomeActivity.getString(R.string.hint_msg_repo_bind_failed));
            return;
        }

        if (app.dbExistRepoOfCurrentUser(BoundService.ServiceType.DROPBOX, account.getEmail())) {
            hintBoundServiceHadBind(mHomeActivity.getString(R.string.normal_drive_bind_multiple));
            return;
        }

        final BoundService service = new BoundService(
                BoundService.ServiceType.DROPBOX,
                BoundService.DROPBOX,
                account.getEmail(),
                NxDropBox2.getOAuth2UID(),
                NxDropBox2.getOAuth2Token(),
                1);


        //
        // Osm , try to add RMS routines
        //
        final AddRepoDialog dialog = new AddRepoDialog(mHomeActivity, service);
        dialog.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (!dialog.isTaskFinished()) {
                    return;
                }
                // response to listener
                if (configByService(service)) {
                    onGetServiceFinish.onGetServiceFinish(service);
                } else {
                    Toast.makeText(mHomeActivity, "Failed when adding a service.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
    }

    // OneDrive
    @Override
    public void onFinishGet(NxOneDrive2.Account account) {
        if (account == null || account.userInfo == null || account.token == null) {
            return;
        }
        if (app.dbExistRepoOfCurrentUser(BoundService.ServiceType.ONEDRIVE, account.userInfo.email)) {
            hintBoundServiceHadBind(mHomeActivity.getString(R.string.normal_drive_bind_multiple));
            return;
        }
        // convert to bound service
        final BoundService service = new BoundService(
                BoundService.ServiceType.ONEDRIVE,
                BoundService.ONEDRIVE,
                account.userInfo.email,
                account.userInfo.userId,
                account.token.refreshToken,
                1);
        // that's very stupid code, but we have to satisfy RMS team
        service.accountName = account.userInfo.name;
        // add to rms routines
        final AddRepoDialog dialog = new AddRepoDialog(mHomeActivity, service);
        dialog.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (!dialog.isTaskFinished()) {
                    return;
                }
                // response to listener
                if (configByService(service)) {
                    onGetServiceFinish.onGetServiceFinish(service);
                } else {
                    Toast.makeText(mHomeActivity, "Failed when adding a service.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();

    }


    // CallBack OneDrive
//    @Override
//    public void onFinishGet(Account account) {
//        //there can cause NullPointerException add sanity check (by henry)
//        if (account == null) {
//            return;
//        }
//        if (app.dbExistRepoOfCurrentUser(BoundService.ServiceType.ONEDRIVE, account.getMail())) {
//            hintBoundServiceHadBind(mHomeActivity.getString(R.string.hint_msg_one_drive_bind_multiple));
//            return;
//        }
//
//        final BoundService service = new BoundService(
//                BoundService.ServiceType.ONEDRIVE, BoundService.ONEDRIVE, account.getMail(),
//                "" + account.getId(), NXOneDrive.getRefreshToken(), 1);
//
//        //
//        // Osm , try to add RMS routines
//        //
//        final AddRepoDialog dialog = new AddRepoDialog(mHomeActivity, service);
//        dialog.setListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//                if (!dialog.isTaskFinished()) {
//                    return;
//                }
//                // response to listener
//                if (configByService(service)) {
//                    onGetServiceFinish.onGetServiceFinish(service);
//                } else {
//                    Toast.makeText(mHomeActivity, "Failed when adding a service.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        dialog.show();
//
//    }

    // Callback sharepoint
    @Override
    public void onFinishGet(NXSharePointOnPremise.Account account) {
        List<BoundService> list = app.getUserLinkedRepos();
        if (list != null) {
            for (BoundService service : list) {
                if (service.type == BoundService.ServiceType.SHAREPOINT && service.account.equalsIgnoreCase(account.username) && service.accountID.equalsIgnoreCase(account.domain)
                        || service.type == BoundService.ServiceType.SHAREPOINT && service.accountID.equalsIgnoreCase(account.domain)) {
                    hintBoundServiceHadBind(mHomeActivity.getString(R.string.normal_drive_bind_multiple));
                    return;
                }
            }
        }

        final BoundService service = new BoundService(
                BoundService.ServiceType.SHAREPOINT, BoundService.SHAREPOINT, account.domain,
                account.domain, account.password, 1);
        service.rmsNickName = account.nickName;

        ExecutorPools.SelectSmartly(NETWORK_TASK).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final boolean result = UserReposSyncPolicy.addNewRepoToRMS(service, null);
                    mHomeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result) {
                                mHomeActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (configByService(service)) {
                                            onGetServiceFinish.onGetServiceFinish(service);
                                        } else {
                                            ToastUtil.showToast(mHomeActivity.getApplicationContext(), "Failed when adding a service.");
                                        }
                                    }
                                });
                            } else {
                                mHomeActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtil.showToast(mHomeActivity.getApplicationContext(), "Add service failed.");
                                    }
                                });
                            }
                        }
                    });
                } catch (final RmsRestAPIException e) {
                    mHomeActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast(mHomeActivity.getApplicationContext(), e.getMessage());
                        }
                    });
                }
            }
        });

    }

    // Callback sharepoint_online
    @Override
    public void onFinishGet(SharePointOnlineSdk.Account account) {

//        List<BoundService> list = app.getUserLinkedRepos();
//        if (list != null) {
//            for (BoundService service : list) {
//                if (service.type == BoundService.ServiceType.SHAREPOINT_ONLINE &&
//                        service.account.equalsIgnoreCase(account.getUsername()) && service.accountID.equalsIgnoreCase(account.getUrl())) {
//                    hintBoundServiceHadBind(mHomeActivity.getString(R.string.normal_drive_bind_multiple));
//                    return;
//                }
//            }
//        }
        //bugs account may be null
        if (account == null) {
            hintBoundServiceHadBind(mHomeActivity.getString(R.string.hint_msg_repo_bind_failed));
            return;
        }

        String accountPredict = account.getUsername() + account.getUrl();

        if (app.dbExistRepoOfCurrentUser(BoundService.ServiceType.SHAREPOINT_ONLINE, accountPredict)) {
            hintBoundServiceHadBind(mHomeActivity.getString(R.string.normal_drive_bind_multiple));
            return;
        }

        // add new repository to db and do update
        final BoundService service = new BoundService(BoundService.ServiceType.SHAREPOINT_ONLINE,
                BoundService.SHAREPOINT_ONLINE, account.getUrl(), account.getUsername(),
                account.getCookie(), 1);

        //
        // allen, try to add RMS routines
        //
        final AddRepoDialog dialog = new AddRepoDialog(mHomeActivity, service);
        dialog.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (!dialog.isTaskFinished()) {
                    return;
                }
                // response to listener
                if (configByService(service)) {
                    onGetServiceFinish.onGetServiceFinish(service);
                } else {
                    Toast.makeText(mHomeActivity, "Failed when adding a service.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();

    }

    // Callback googledrive
    @Override
    public void onFinishGet(NxGoogleDrive3.Account account) {
        //bugs account may be null
        if (account == null) {
            hintBoundServiceHadBind(mHomeActivity.getString(R.string.hint_msg_repo_bind_failed));
            return;
        }
        if (app.dbExistRepoOfCurrentUser(BoundService.ServiceType.GOOGLEDRIVE, account.getEmail())) {
            hintBoundServiceHadBind(mHomeActivity.getString(R.string.normal_drive_bind_multiple));
            return;
        }

        final BoundService service = new BoundService(
                BoundService.ServiceType.GOOGLEDRIVE,
                BoundService.GOOGLEDRIVE,
                account.getEmail(),
                account.getUserId(),
                account.getRefreshToken(),
                1);

        final AddRepoDialog dialog = new AddRepoDialog(mHomeActivity, service);
        dialog.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (!dialog.isTaskFinished()) {
                    return;
                }
                // response to listener
                if (configByService(service)) {
                    onGetServiceFinish.onGetServiceFinish(service);
                } else {
                    Toast.makeText(mHomeActivity, "Failed when adding a service.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();

    }

    private boolean configByService(BoundService service) {

        // add into db first ,then amend service.id
        boolean rt;
        app.dbInsertRepo(service);
        BoundService revisedService = app.dbFindRepo(service.type, service.account, service.accountID);

        if (revisedService != null) {
            try {
                // activateRepo by revised Boundservice
                app.getRepoSystem().activateRepo(revisedService);
                rt = true;
            } catch (Exception e) {
                rt = false;
                e.printStackTrace();
            }
        } else {
            rt = false;
            // todo: must be occuring a error when insert service item into DB
        }
        return rt;
    }

    private void hintBoundServiceHadBind(final String msg) {
        GenericError.showUI(mHomeActivity, msg,
                true,
                false,
                false,
                null);
    }

    public interface OnGetServiceFinish {
        void onGetServiceFinish(BoundService service);
    }

    private static class AddRepoDialog {

        private Button btnOk;
        private EditText editText;
        private View.OnClickListener mListener;
        private ProgressBar progressBar;
        private Context mContext;
        private BoundService mRepo;
        private AlertDialog dialog;

        private boolean taskFinished = false;
        private boolean sp_local_auth;

        public AddRepoDialog(Context context, BoundService repo) {
            mContext = context;
            mRepo = repo;
        }

        public boolean isTaskFinished() {
            return taskFinished;
        }

        public void setSpLocalAuth(boolean sp_local_auth) {
            this.sp_local_auth = sp_local_auth;
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
                    mRepo.rmsNickName = nickNameTrimmed;
                    long now = System.currentTimeMillis();
                    mRepo.rmsCreationTime = now;
                    mRepo.rmsUpdatedTime = now;
                    // by osmond: change the default vaule as false
//                    mRepo.rmsIsShared = true;
                    mRepo.rmsIsShared = false;
                    if (sp_local_auth) {
                        return true;
                    }
                    try {
                        return UserReposSyncPolicy.addNewRepoToRMS(mRepo, new RestAPI.Listener() {
                            @Override
                            public void progress(int current, int total) {
                                publishProgress(10);
                            }

                            @Override
                            public void currentState(String state) {
                                publishProgress(10);
                            }
                        });
                    } catch (RmsRestAPIException e) {//for 500 error param
                        mRmsRestAPIException = e;
                        e.printStackTrace();
                    } catch (Exception e) {
                        mRmsRestAPIException = new RmsRestAPIException(e.getMessage(), RmsRestAPIException.ExceptionDomain.Common);
                        e.printStackTrace();
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    progressBar.setVisibility(View.GONE);
                    if (aBoolean) {
                        taskFinished = true;
                        mListener.onClick(null);
                    } else {
                        if (mRmsRestAPIException != null) {
                            switch (mRmsRestAPIException.getDomain()) {
                                case AuthenticationFailed:
                                    SkyDRMApp.getInstance().getSession().sessionExpiredHandler(mContext);
                                    break;
                                case RepoAlreadyExist:
                                    ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.normal_drive_bind_multiple));
                                    break;
                                case RepoNameNotValid:
                                    ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.hint_msg_repoNameInvalid));
                                    break;
                                case NetWorkIOFailed:
                                    ToastUtil.showToast(mContext, mContext.getResources().getString(R.string.excep_network_unavailable));
                                    break;
                                default:
                                    ToastUtil.showToast(mContext, mRmsRestAPIException.getMessage());
                                    break;
                            }
                        }

                        onFalseAdd();
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

        private void onFalseAdd() {
            log.v("false to add new service");
            editText.setText("");
            editText.setHint("Illegal name, change another");
            ShakeAnimator animator = new ShakeAnimator();
            animator.setTarget(editText);
            animator.startAnimation();
        }

    }
}
