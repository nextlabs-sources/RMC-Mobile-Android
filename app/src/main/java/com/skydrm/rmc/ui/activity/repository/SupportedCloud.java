package com.skydrm.rmc.ui.activity.repository;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.account.UserLinkedRepoChangedEvent;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.remoterepo.sharepointonline.SharePointAuthActivity;
import com.skydrm.rmc.reposystem.remoterepo.sharepointonline.SharePointAuthManager;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.activity.splash.SplashActivity;
import com.skydrm.rmc.ui.widget.animation.ShakeAnimator;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.sdk.exception.RmsRestAPIException;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

//import com.skydrm.rmc.reposystem.remoterepo.sharepoint.SharePointSdk;

/**
 * Experimental Activity
 * Be used to show all supported remote repositories
 */
public class SupportedCloud extends BaseActivity {
    static final String TAG = "SupportedCloud";
    static final DevLog log = new DevLog(SupportedCloud.class.getSimpleName());
    public static final int REQ_BIND_SP_OnLine = 0x105;
    //    static private boolean WaitDropBoxOAuth2 = false;
//    static private boolean WaitOneDriveOAuth2 = false;
//    static private boolean WaitGoogleDriveOAuth2 = false;
//    private boolean waitSharePointOnlineOAuth2;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private AddRepoRoutine routine = null;
    private boolean waitRetrievingSPSiteURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supported_cloud);
        ButterKnife.bind(this);
        initToolbar(toolbar, getString(R.string.connect));
        //init list controls
        ListObj listService = new ListObj(this);
        listService.AttachListUIById(R.id.list_supported_cloud);

        // item OneDrive
        listService.add(new ListObj.Item(R.drawable.bottom_sheet_onedrive, getString(R.string.name_onedrive)));
        // item DropBox
        listService.add(new ListObj.Item(R.drawable.bottom_sheet_dropbox, getString(R.string.name_dropbox)));
        //item GoogleDrive
        listService.add(new ListObj.Item(R.drawable.bottom_sheet_googledrive, getString(R.string.name_googledrive)));

        listService.add(new ListObj.Item(R.drawable.bottom_sheet_box, getString(R.string.name_box)));
        //
        // by osmond, sharepoint online will be available in next release
        //
        // item SharePointOnline
        listService.add(new ListObj.Item(R.drawable.home_account_sharepoint_online, getString(R.string.name_sharepointonline)));
        // item SharePoint
        listService.add(new ListObj.Item(R.drawable.home_account_sharepoint, getString(R.string.name_sharepoint)));

        // set callback
        listService.LinkItemsToUI(this, R.layout.support_service_item2);

        listService.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListObj.Item item = (ListObj.Item) parent.getAdapter().getItem(position);
                if (item == null) { ///when user click list header that connect_cloud_service, don't deal with.
                    return;
                }
                if (item.getName().equalsIgnoreCase(getString(R.string.name_dropbox))) {
//                    onClickDropBox();
                    onClickDropBox2();
                } else if (item.getName().equalsIgnoreCase(getString(R.string.name_onedrive))) {
//                    onClickOneDrive();
                    onClickOneDrive2();
                } else if (item.getName().equalsIgnoreCase(getString(R.string.name_sharepointonline))) {
                    //onClickSharePointOnline();
                    onClickSharePointOnline2();
                } else if (item.getName().equalsIgnoreCase(getString(R.string.name_googledrive))) {
//                    onClickGoogleDrive();
                    onClickGoogleDrive3();
                } else if (item.getName().equalsIgnoreCase(getString(R.string.name_box))) {
                    onClickBox();
                } else if (item.getName().equalsIgnoreCase(getString(R.string.name_sharepoint))) {
                    onClickSharePoint();
                }
            }
        });

    }

    private void onClickBox() {
        this.routine = new AddRepoRoutine(this, "BOX", BoundService.ServiceType.BOX, BoundService.BOX);
        routine.shouUI();
    }

    void onClickDropBox2() {
        this.routine = new AddRepoRoutine(this, "DROPBOX", BoundService.ServiceType.DROPBOX, BoundService.DROPBOX);
        routine.shouUI();
    }

    void onClickOneDrive2() {
        this.routine = new AddRepoRoutine(this, "ONE_DRIVE", BoundService.ServiceType.ONEDRIVE, BoundService.ONEDRIVE);
        routine.shouUI();
    }

    void onClickGoogleDrive3() {
        this.routine = new AddRepoRoutine(this, "GOOGLE_DRIVE", BoundService.ServiceType.GOOGLEDRIVE, BoundService.GOOGLEDRIVE);
        routine.shouUI();
    }

    void onClickSharePointOnline2() {
        this.routine = new AddRepoRoutine(this, "SHAREPOINT_ONLINE", BoundService.ServiceType.SHAREPOINT_ONLINE, BoundService.SHAREPOINT_ONLINE);
        this.routine.setNeedInputSiteURL(true);
        waitRetrievingSPSiteURL = true;
        routine.shouUI();
    }

    void onClickSharePoint() {
        this.routine = new AddRepoRoutine(this, "SHAREPOINT_ONPREMISE", BoundService.ServiceType.SHAREPOINT, BoundService.SHAREPOINT);
        this.routine.setAuthSPOnPremise(true);
        this.routine.shouUI();
    }

//    void onClickDropBox() {
//        WaitDropBoxOAuth2 = true;
////        NXDropBox.startOAuth2Authentication(SupportedCloud.this);
//        NxDropBox2.startOAuth2Authentication(SupportedCloud.this);
//    }

//    void onClickSharePointOnline() {
//        waitSharePointOnlineOAuth2 = true;
//        SharePointOnlineSdk.startAuth(SupportedCloud.this);
//    }


//    void onClickOneDrive() {
//        //fix bug when double click onedrive app crash
//        WaitOneDriveOAuth2 = true;
//        OAuth2Activity.startOAuth2Authentication(SupportedCloud.this);
//    }


//    void onClickGoogleDrive() {
//        WaitGoogleDriveOAuth2 = true;
//        GoogleOAuth2.startOAuth2Authentication(SupportedCloud.this);
//    }


    @Override
    protected void onResume() {
        super.onResume();
        log.d("onResume");
        if (SharePointAuthManager.isAuthorized()) {
            SharePointAuthManager.resetAuthStatus();
            setResult(RESULT_OK, getIntent().putExtra(getString(R.string.picked_cloud_name),
                    getString(R.string.name_sharepoint)));
            finish();
        }

//        if (waitSharePointOnlineOAuth2) {
//            waitSharePointOnlineOAuth2 = false;
//            if (SharePointOnlineSdk.getAuthStatus()) {
//                SharePointOnlineSdk.resetAuthStatus();
//                setResult(RESULT_OK, getIntent().putExtra(getString(R.string.picked_cloud_name), getString(R.string.name_sharepointonline)));
//                finish();
//            }
//        }
//
//        if (WaitDropBoxOAuth2) {
//            WaitDropBoxOAuth2 = false;
//            /*v2*/
//            if (NxDropBox2.getOAuth2Token() != null) {
//                setResult(RESULT_OK, getIntent().putExtra(getString(R.string.picked_cloud_name),
//                        getString(R.string.name_dropbox)));
//                Log.i(TAG, "successful Authentication of dropBox OAuth2");
//                // add the OAuto token to com.skydrm.rmc.database
//                finish();
//            } else {
//                log.e("Authentication failed of dropBox OAuth2");
//            }
//        }
        //
        // receive the RMS result here
        //
        log.d("waitRetrievingSPSiteURL:" + waitRetrievingSPSiteURL);
        if (waitRetrievingSPSiteURL) {
            waitRetrievingSPSiteURL = false;
        } else {
            log.d("routine != null:" + (routine != null));
            if (routine != null) {
                final String rmsURL = SplashActivity.nextlabs_oauth_result;
                if (rmsURL != null) {
                    log.i("get the rmsURL callback:" + rmsURL);
                    routine.onParseRMSUrl(rmsURL);
                } else {
                    // error occurs, prompt user and dismiss routine ui
                    routine.dismissUI();
                }
                // abandon former oauth result
                SplashActivity.nextlabs_oauth_result = null;
                // set routine to null
                routine = null;
            }
        }

//        try {
//        }catch (Exception e){
//            log.e(e);
//        }
    }

    // check if called this
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        log.d("onActivityResult");
        if (requestCode == REQ_BIND_SP_OnLine && data != null) {
            log.w(data.toString());
            if (resultCode == Activity.RESULT_OK) {
                String spOnlineSiteURL = data.getStringExtra(Constant.SP_BIND_SITE_URL);
                this.routine.setSiteURL(spOnlineSiteURL);
                this.routine.retrieveRmsURL();
            }
        }
    }

    static class ListObj {
        private Activity mRoot;
        private ListView mListUi;
        private List<Item> mListItems = new ArrayList<>();
        private ItemAdapter mAdapter;


        public ListObj(Activity mRoot) {
            this.mRoot = mRoot;
        }

        public void LinkItemsToUI(Context context, int resource) {
            mAdapter = new ItemAdapter(context, resource, mListItems);
            mListUi.setAdapter(mAdapter);
        }

        public void AttachListUIById(int ListViewResId) {
            mListUi = (ListView) mRoot.findViewById(ListViewResId);
        }

        public void addHeaderView(View v) {
            mListUi.addHeaderView(v);
        }

        public boolean add(Item object) {
            return mListItems.add(object);
        }

        public void add(int location, Item object) {
            mListItems.add(location, object);
        }
        //    public void setAdapter(ListAdapter adapter) {
//        mListUi.setAdapter(adapter);
//    }

        public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
            mListUi.setOnItemClickListener(listener);
        }


        public void notifyDataSetChanged() {
            mAdapter.notifyDataSetChanged();
        }


        public static class Item {
            private int imgId;
            private String name;
            private BoundService service = null;


            public Item(int imgId, String name) {
                this.imgId = imgId;
                this.name = name;
            }

            public Item(int imgId, String name, BoundService service) {
                this.imgId = imgId;
                this.name = name;
                this.service = service;
            }

            public BoundService getService() {
                return service;
            }

            public int getImgId() {
                return imgId;
            }

            public void setImgId(int imgId) {
                this.imgId = imgId;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        public static class ItemAdapter extends ArrayAdapter<Item> {
            private int resItemLayoutId;

            public ItemAdapter(Context context, int resource, List<Item> objects) {
                super(context, resource, objects);
                resItemLayoutId = resource;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                Item item = getItem(position);

                View view = LayoutInflater.from(getContext()).inflate(resItemLayoutId, null);
                ImageView imageView = (ImageView) view.findViewById(R.id.service_image);
                TextView textView = (TextView) view.findViewById(R.id.service_name);
                View mDivideLine = (View) view.findViewById(R.id.divide_line);
                mDivideLine.setVisibility(position == getCount() - 1 ? View.GONE : View.VISIBLE);
                imageView.setImageResource(item.getImgId());
                textView.setText(item.getName());
                return view;
            }
        }
    }

    private class AddRepoRoutine {
        public static final String regEx = "[A-Za-z1-9 _-]{1,40}";
        public Pattern pattern;
        private Button btnOk;
        private EditText editText;
        private ProgressBar progressBar;
        private AlertDialog dialog;
        private String repoType;
        private BoundService.ServiceType serviceType;
        private String nameAlias;
        private String nickNameTrimmed;
        private String siteURL;
        private boolean needInputSiteURL;
        private boolean authSP;

        public AddRepoRoutine(Context context, String repoType, BoundService.ServiceType serviceType, String nameAlias) {
            this.repoType = repoType;
            this.serviceType = serviceType;
            this.nameAlias = nameAlias;

            // prompt user enter a nick name of the repo
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
            progressBar = (ProgressBar) layout.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);

            dialog = new AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle(R.string.app_name)
                    .setView(layout)
                    .create();
        }

        public void setSiteURL(String siteURL) {
            this.siteURL = siteURL;
        }

        public void setNeedInputSiteURL(boolean needInputSiteURL) {
            this.needInputSiteURL = needInputSiteURL;
        }

        public void setAuthSPOnPremise(boolean authSP) {
            this.authSP = authSP;
        }

        public void shouUI() {
            if (dialog != null) {
                dialog.show();
            }
        }

        public void dismissUI() {
            SupportedCloud.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

        }

        private void onUIProgressingTo(int i) {
            // range [0,100]
            if (i > 100) {
                i = 100;
            }
            if (i < 0) {
                i = 0;
            }

            if (progressBar != null) {
                final int finalI = i;
                SupportedCloud.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(finalI);
                    }
                });

            }
        }

        // callback
        public void onBtnOk() {
            // tell progressbar to move forward
            // get text from EditText as nick name
            // call RMS to add the new repo
            // amend this repo's service_id;

            final String nickname = editText.getText().toString();

            if (nickname.isEmpty() || nickname.trim().isEmpty()) {
                editText.setText("");
                editText.setHint("The Display Name can only contain alphanumeric characters, spaces, hyphens, and underscores");
                ShakeAnimator animator = new ShakeAnimator();
                animator.setTarget(editText);
                animator.startAnimation();
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(0);
                return;
            }

            pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(nickname);

            if (!matcher.matches()) {
                editText.setText("");
                editText.setHint("The Display Name can only contain alphanumeric characters, spaces, hyphens, and underscores");
                ShakeAnimator animator = new ShakeAnimator();
                animator.setTarget(editText);
                animator.startAnimation();
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(0);
                return;
            }

            // as QA required, nickname must be trimmed
            nickNameTrimmed = nickname.trim();

            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(10);


            //
            if (needInputSiteURL) {
                dismissUI();
                retrieveSiteURLLocal();
            } else if (authSP) {
                dismissUI();
                lunchSPAuthPage(nickNameTrimmed);
            } else {
                retrieveRmsURL();
            }
        }

        void retrieveSiteURLLocal() {
            Intent bindSPIntent = new Intent(SupportedCloud.this, SharePointAuthActivity.class);
            bindSPIntent.putExtra(Constant.SP_BIND_SITE_URL, true);
            startActivityForResult(bindSPIntent, REQ_BIND_SP_OnLine);
        }

        void lunchSPAuthPage(String nickNameTrimmed) {
            Intent bindSPIntent = new Intent(SupportedCloud.this, SharePointAuthActivity.class);
            bindSPIntent.putExtra(Constant.REPO_SP_NAME, nickNameTrimmed);
            startActivity(bindSPIntent);
        }

        void retrieveRmsURL() {
            ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI).execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String rmsURL = SkyDRMApp.getInstance()
                                .getSession()
                                .getRmsRestAPI()
                                .getRepositoryService(SkyDRMApp.getInstance().getSession().getRmUser())
                                .getAuthorizationURL(repoType, nickNameTrimmed, siteURL);
                        log.w(rmsURL);
                        onDependSplashActivityToRedirectOAuth(rmsURL);
                        onUIProgressingTo(30);
                    } catch (RmsRestAPIException e) {
                        e.printStackTrace();
                    } catch (SessionInvalidException e) {
                        e.printStackTrace();
                    } catch (InvalidRMClientException e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        public void onParseRMSUrl(String rmsURL) {
            Uri uri = Uri.parse(rmsURL);
            String statuscode = uri.getQueryParameter("statusCode");
            if (!TextUtils.equals(statuscode, "200")) {
                // rms error returns
                // dismiss the UI and prompt user the error
                dismissUI();

                String msg = uri.getQueryParameter("message");
                if (msg == null) {
                    msg = getString(R.string.hint_msg_repo_bind_failed);
                }
                if (TextUtils.equals(statuscode, "304")) {
                    msg = getString(R.string.hint_msg_repo_bind_exists);
                }
                GenericError.showUI(SupportedCloud.this,
                        msg,
                        true,
                        false,
                        false,
                        null);
                return;
            }
            final String repoId = uri.getQueryParameter("repoId");
            String name = uri.getQueryParameter("name");
            String type = uri.getQueryParameter("type");
            final String accountName = uri.getQueryParameter("accountName");
            final String accountId = uri.getQueryParameter("accountId");

            // progressing
            onUIProgressingTo(60);

            // as RMS defined ,we have to find the access token by this new add repo
            ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI).execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        // get token first
                        String accessToken = SkyDRMApp.getInstance().getSession().getRmsRestAPI()
                                .getRepositoryService(SkyDRMApp.getInstance().getSession().getRmUser())
                                .getAccessTokenByRepoID(repoId);
                        onUIProgressingTo(70);

                        // add this repo into local repo system
                        BoundService service = new BoundService(
                                serviceType,
                                nameAlias,
                                accountName,
                                accountId,
                                accessToken,
                                1,
                                repoId,
                                nickNameTrimmed,
                                false,
                                accessToken,
                                "null",
                                System.currentTimeMillis()
                        );

                        // insert it into local db
                        SkyDRMApp.getInstance().dbInsertRepo(service);
                        //
                        SkyDRMApp.getInstance().getRepoSystem().activateRepo(service);
                        onUIProgressingTo(100);
                        // close UI
                        dismissUI();
                        // send event onto EventBus
                        EventBus.getDefault().postSticky(new UserLinkedRepoChangedEvent());
                        // close Supported activity
                        SupportedCloud.this.finish();

                    } catch (Exception e) {
                        log.e(e);
                        // error occurs
                        GenericError.showUI(SupportedCloud.this,
                                getString(R.string.hint_msg_repo_bind_failed),
                                true,
                                false,
                                false,
                                null);
                    }
                }
            });
        }

        void onDependSplashActivityToRedirectOAuth(final String rmsURL) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SupportedCloud.this, SplashActivity.class);
                    intent.putExtra("nextlabs_oauth", rmsURL);
                    startActivity(intent);
                }
            });
        }
    }
}

