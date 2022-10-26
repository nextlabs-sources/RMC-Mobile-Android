package com.skydrm.rmc.ui.service.log;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.DownloadManager;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.errorHandler.IErrorResult;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.reposystem.exception.FileDownloadException;
import com.skydrm.rmc.reposystem.localrepo.helper.Helper;
import com.skydrm.rmc.reposystem.remoterepo.ICancelable;
import com.skydrm.rmc.reposystem.types.NXDocument;
import com.skydrm.rmc.ui.service.search.SearchActivity;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.IVaultFileLog;
import com.skydrm.rmc.ui.widget.NxlSwipeRefreshLayout;
import com.skydrm.rmc.ui.widget.WrapperLinearLayoutManager;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.rmc.utils.sort.SortType;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RmsRestAPIException;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hhu on 1/22/2017.
 */

public class LogActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener, ILoadCallback<List<IVaultFileLog>, LogException> {
    private static final int STATE_LOADING = -1;
    private static final int STATE_ERROR = -2;
    private static final int STATE_EMPTY = -3;
    private static final int STATE_EMPTY_SEARCH = -4;
    private static final int STATE_SUCCESS = -5;
    private static final int STATE_DOWNLOADING = -8;
    private static DevLog log = new DevLog(LogActivity.class.getSimpleName());

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_layout)
    RelativeLayout mainLayout;

    private String filename;
    private String duid;
    private RecyclerView mLogRecyclerView;
    private NxlSwipeRefreshLayout mSwipeRefreshLayout;

    private List<IVaultFileLog> mActivityLogs = new ArrayList<>();
    private ActivityLogAdapter mLogAdapter;
    private SubviewLoader mSubviewLoader;
    //subview container
    private FrameLayout mFlLogDetailPage;

    private NXDocument selectedDoc;
    private ProgressBar mProgressBar;
    private TextView mProgressValue;

    private ICancelable mDownLoadCancelHandler;
    private boolean mIsActivityVisibility;
    private SortType mSortType = SortType.TIME_DESCEND;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        ButterKnife.bind(this);
        mSubviewLoader = new SubviewLoader(LogActivity.this, findViewById(R.id.loading_page));
        initView();
        receiveIntent();
        initEvent();
    }

    private void receiveIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (TextUtils.equals(action, Constant.VIEW_ACTIVITY_LOG_FROM_MAIN)) {
            selectedDoc = (NXDocument) intent.getSerializableExtra(Constant.SELECTED_ITEM);
            tryGetFile(selectedDoc);
        } else {
            filename = getIntent().getStringExtra("file_name");
            if (!TextUtils.isEmpty(filename)) {
                toolbar.setTitle(filename);
            }
            duid = getIntent().getStringExtra("duid");
            initData(duid);
        }
    }

    private void tryGetFile(final NXDocument selectedDoc) {
        if (selectedDoc == null || mProgressBar == null || mProgressValue == null) {
            log.e("error in tryGetFile");
            return;
        }
        filename = selectedDoc.getName();
        if (!TextUtils.isEmpty(filename)) {
            toolbar.setTitle(filename);
        }
        File doc = DownloadManager.getInstance().tryGetFile(LogActivity.this,
                selectedDoc,
                mProgressBar,
                mProgressValue,
                true,
                new DownloadManager.IDownloadCallBack() {
                    @Override
                    public void onDownloadFinished(boolean taskStatus, String localPath, @Nullable FileDownloadException e) {
                        if (taskStatus) {
                            downloadFileFinished(localPath);
                            DownloadManager.getInstance().removeDownloader(selectedDoc);
                        } else {
                            // download failed, will delete part tmp file.
                            Helper.deleteFile(new File(localPath));
                            // exception handler
                            if (e != null) {
                                switch (e.getErrorCode()) {
                                    case AuthenticationFailed:
                                        SkyDRMApp.getInstance().getSession().sessionExpiredHandler(LogActivity.this);
                                        break;
                                    case NetWorkIOFailed:
                                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.excep_network_unavailable));
                                        break;
                                    default:
                                        if (mIsActivityVisibility) {
                                            GenericError.showUI(LogActivity.this, getString(R.string.down_load_failed), true, true, false, new IErrorResult() {
                                                @Override
                                                public void cancelHandler() {
                                                    LogActivity.this.finish();
                                                }

                                                @Override
                                                public void okHandler() {  // retry to download file.
                                                    tryGetFile(selectedDoc);
                                                }
                                            });
                                        }
                                        break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onDownloadProgress(long value) {
                        mProgressBar.setProgress((int) value);
                    }
                }
        );
        // get cancel handler
        if (DownloadManager.getInstance().tryGetDownloader(selectedDoc) != null) {
            mDownLoadCancelHandler = DownloadManager.getInstance().tryGetDownloader(selectedDoc).getDownLoadCancelHandler();
        }
        // this file is loading
        if (doc == null && DownloadManager.getInstance().tryGetDownloader(selectedDoc) != null &&
                DownloadManager.getInstance().tryGetDownloader(selectedDoc).isbIsDownloading()) {
            // here also need to continue to display the download progress.
            showProgressView();
            return;
        }
        if (doc != null) {
            downloadFileFinished(doc.getAbsolutePath());
        } else {
            showProgressView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActivityVisibility = true;
    }

    private void downloadFileFinished(String filePath) {
        try {
            INxlFileFingerPrint nxlFileRights = SkyDRMApp.getInstance()
                    .getSession()
                    .getRmsClient()
                    .extractFingerPrint(filePath);
            if (SkyDRMApp.getInstance().isStewardOf(nxlFileRights.getOwnerID())) {
                duid = nxlFileRights.getDUID();
                initData(duid);
            } else {
                mSubviewLoader.dispathSubview(STATE_SUCCESS);
                //notify user not steward of the nxl file
                GenericError.showUI(LogActivity.this, getString(R.string.hint_msg_show_log_not_steward_failed), true, false, true, null);
            }
        } catch (FileNotFoundException | RmsRestAPIException e) {
            e.printStackTrace();
        } catch (NotNxlFileException e) {
            ToastUtil.showToast(getApplicationContext(), R.string.hint_msg_nxl_invalid_type);
            e.printStackTrace();
        } catch (Exception e) {
            log.e(e);
        }
    }

    private void showProgressView() {
        mSubviewLoader.dispathSubview(STATE_DOWNLOADING);
    }

    private void initView() {
        //initToolbar
        toolbar.setNavigationIcon(getDrawable(R.drawable.icon_back_3));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
//        toolbar.setSubtitleTextColor();
        toolbar.setSubtitle(getString(R.string.activity_log));
        toolbar.inflateMenu(R.menu.myspace);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(this);
        //this is used to disable the setting menu item long click listener
        findViewById(R.id.action_settings).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        //this is used to disable the search menu item item long click listener.
        findViewById(R.id.action_search).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        //initLogContent
        mFlLogDetailPage = findViewById(R.id.fl_subview_container);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mLogRecyclerView = findViewById(R.id.recyclerView);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.main_green_light),
                getResources().getColor(R.color.color_blue));
        mLogRecyclerView.setLayoutManager(new WrapperLinearLayoutManager(LogActivity.this));
        mLogRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mLogRecyclerView.setHasFixedSize(false);

        mLogAdapter = new ActivityLogAdapter(LogActivity.this, filename);
        mLogRecyclerView.setAdapter(mLogAdapter);

        initRecyclerView();
    }

    private void initRecyclerView() {
        mLogRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    RecyclerView.LayoutManager layoutManager = mLogRecyclerView.getLayoutManager();
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                    int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                    if (layoutManager.getChildCount() > 0
                            && lastVisibleItemPosition >= layoutManager.getItemCount() - 1
                            && layoutManager.getItemCount() > layoutManager.getChildCount()) {
                        listLog(duid);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void initData(String duid) {
        listLog(duid);
    }

    private void initEvent() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listLog(duid);
            }
        });
    }

    public void listLog(final String duid) {
        //LogLoadManager.getInstance().getSortedLog(duid, mSortType, this);
        listLogByPage(duid);
    }

    public void listLogByPage(final String duid) {
        int start = mActivityLogs == null ? 0 : mActivityLogs.size();
        LogLoadManager.getInstance().getSortedPageLog(duid, mSortType, start, 20,
                new ILoadCallback<List<IVaultFileLog>, LogException>() {
                    @Override
                    public void onLoading() {
                        mSubviewLoader.dispathSubview(STATE_LOADING);
                    }

                    @Override
                    public void onResult(List<IVaultFileLog> result, int total) {
                        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                        log.d("listLogByPage:total number is:" + total);
                        if (total != -1) {
                            mLogAdapter.setTotalCount(total);
                        }
                        if (result.isEmpty()) {
                            mLogAdapter.setTotalCount(mActivityLogs.size());
                        }
                        mActivityLogs.addAll(result);
                        if (mActivityLogs.size() == 0) {
                            mSubviewLoader.dispathSubview(STATE_EMPTY);
                            return;
                        }
                        mSubviewLoader.dispathSubview(STATE_SUCCESS);
                        mLogAdapter.setData(mActivityLogs);
                    }

                    @Override
                    public void onError(final LogException error) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handleLogException(error);
                            }
                        });
                    }
                });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                LogSortMenu sortMenu = new LogSortMenu(LogActivity.this, mSortType, new LogSortMenu.OnSortItemClickListener() {
                    @Override
                    public void onSortItemClick(SortType sortType) {
                        mSortType = sortType;
                        sortLog(sortType);
                    }
                });
                sortMenu.showAtLocation(mainLayout, Gravity.TOP, 0, 0);
                break;
            case R.id.action_search:
                Intent searchIntent = new Intent(LogActivity.this, SearchActivity.class);
                searchIntent.setAction(Constant.ACTION_SEARCH_ACTIVITY_LOG);
                startActivity(searchIntent);
                break;
        }
        return true;
    }

    private void sortLog(SortType sortType) {
        LogLoadManager.getInstance().sort(mActivityLogs, -1, sortType, this);
    }

    @Override
    public void onLoading() {
        mSubviewLoader.dispathSubview(STATE_LOADING);
    }

    @Override
    public void onResult(List<IVaultFileLog> result, int total) {
        notifyData(result);
    }

    @Override
    public void onError(final LogException e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleLogException(e);
            }
        });
    }

    private void handleLogException(LogException e) {
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        if (e.getStatusCode() == LogException.EXCEPTION_RMS_REST_API) {
            RmsRestAPIException restAPIException = (RmsRestAPIException) e.getCause();
            RmsRestAPIException.ExceptionDomain domain = restAPIException.getDomain();
            if (domain == RmsRestAPIException.ExceptionDomain.NetWorkIOFailed) {
                mSubviewLoader.dispathSubview(STATE_ERROR);
            } else if (domain == RmsRestAPIException.ExceptionDomain.FileNotFound) {
                mSubviewLoader.dispathSubview(STATE_EMPTY);
                ExceptionHandler.handleException(this, restAPIException);
            }
        } else if (e.getStatusCode() == LogException.EXCEPTION_SESSION_INVALID) {
            SkyDRMApp.getInstance().logout(this);
        } else if (e.getStatusCode() == LogException.EXCEPTION_RMC_CLIENT_INVALID) {
            finish();
        } else {
            notifyData(Collections.<IVaultFileLog>emptyList());
        }
        ToastUtil.showToast(getApplicationContext(), e.getMessage());
    }

    private void notifyData(List<IVaultFileLog> logs) {
        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mActivityLogs.clear();
        mActivityLogs.addAll(logs);
        notifyDataSetChanged();
    }

    private void notifyDataSetChanged() {
        if (mActivityLogs.size() != 0) {
            mLogAdapter.setData(mActivityLogs);
            mSubviewLoader.dispathSubview(STATE_SUCCESS);
        } else {
            mSubviewLoader.dispathSubview(STATE_EMPTY);
        }
    }

    private class SubviewLoader {
        private WeakReference<Context> mContext;
        private View loadingView;
        private View emptyFolderView;
        private View emptySearchView;
        private View networkErrorView;
        private View successView;
        private View mDownloadView;

        SubviewLoader(Context context, View container) {
            mContext = new WeakReference<>(context);
            fillSubview((FrameLayout) container);
        }

        private void fillSubview(FrameLayout subContainer) {
            //add 4 subview first
            loadingView = View.inflate(mContext.get(), R.layout.layout_loading, null);
            //empty folder subview
            emptyFolderView = View.inflate(mContext.get(), R.layout.layout_empty_folder, null);
            TextView emptyDesc = emptyFolderView.findViewById(R.id.nxfile_empty_descript);
            emptyDesc.setText(getResources().getString(R.string.empt_msg_log));
            //empty search subview
            emptySearchView = View.inflate(mContext.get(), R.layout.layout_empty_search, null);
            //no net connect subview
            networkErrorView = View.inflate(mContext.get(), R.layout.page_error, null);
            mDownloadView = View.inflate(mContext.get(), R.layout.view_download_of_viewactivity2, null);
            mProgressBar = mDownloadView.findViewById(R.id.progress);
            mProgressValue = mDownloadView.findViewById(R.id.textView_progress);
            //the success subview
            successView = View.inflate(mContext.get(), R.layout.layout_common_normal_recyclerview, null);
            subContainer.removeAllViews();
            subContainer.addView(loadingView);
            subContainer.addView(emptyFolderView);
            subContainer.addView(networkErrorView);
            subContainer.addView(successView);
            subContainer.addView(emptySearchView);
            subContainer.addView(mDownloadView);
            dispathSubview(STATE_LOADING);
        }

        private void dispathSubview(int subType) {
            loadingView.setVisibility(View.GONE);
            networkErrorView.setVisibility(View.GONE);
            successView.setVisibility(View.GONE);
            emptyFolderView.setVisibility(View.GONE);
            emptySearchView.setVisibility(View.GONE);
            mDownloadView.setVisibility(View.GONE);
            switch (subType) {
                case STATE_LOADING:
                    loadingView.setVisibility(View.VISIBLE);
                    break;
                case STATE_ERROR:
                    networkErrorView.setVisibility(View.VISIBLE);
                    break;
                case STATE_SUCCESS:
                    successView.setVisibility(View.VISIBLE);
                    break;
                case STATE_EMPTY:
                    emptyFolderView.setVisibility(View.VISIBLE);
                    break;
                case STATE_EMPTY_SEARCH:
                    emptySearchView.setVisibility(View.VISIBLE);
                    break;
                case STATE_DOWNLOADING:
                    mDownloadView.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
