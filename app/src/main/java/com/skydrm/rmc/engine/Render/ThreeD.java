package com.skydrm.rmc.engine.Render;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.ve.DVLCore;
import com.sap.ve.DVLTypes;
import com.sap.ve.SDVLProceduresInfo;
import com.skydrm.hoops.AndroidMobileSurfaceView;
import com.skydrm.hoops.AndroidUserMobileSurfaceView;
import com.skydrm.hoops.MobileApp;
import com.skydrm.hoops.ViewerUtils;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.activity.ViewActivity;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sap.IRetrieveThumbnail;
import com.skydrm.sap.SAPViewer;
import com.skydrm.sap.SapGalleryAdapter;

import java.io.File;
import java.lang.ref.WeakReference;

import static com.skydrm.rmc.ExecutorPools.Select_Type.REGULAR_BACK_GROUND;


/**
 * Created by aning on 11/22/2016.
 */

public class ThreeD implements IFileRender, AndroidMobileSurfaceView.Callback, IRetrieveThumbnail, IDestroyable {
    static private final int MOBILE_SURFACE_GUI_ID = 0;
    private Activity mActivity;
    private Context mContext;
    private RelativeLayout mMainLayout;
    private File mFile;
    private String mFilePath;
    private boolean bHSFViewFlag = false;
    private boolean mModeSimpleShadowEnabled;

    private AndroidUserMobileSurfaceView mSurfaceView;
    private View mCurrentToolbarView;
    private View mModesToolbarView;
    private View mUserCodeToolbarView;
    private View mHoopsModeSelect;
    private TextView mHoopsMode;
    private TextView mHoopsOperator;
    private TextView mHoopsCuttingPlane;

    private RecyclerView m_recyclerView;
    private SapGalleryAdapter m_dvlImageAdapter;

    private View.OnClickListener mOnClickListener;
    // normal view or preview
    private String mViewType;
    private View mSelectedToolbarView;
    private SAPViewer mSAPViewer;
    private LoadingDialog2 mLoadingDialog;
    private LoadFileTaskCallback mLoadFileTaskCallback;
    private int mobileSurfacePointer;
    private boolean isLoadingDialogShow;
    private boolean isReleaseResource;

    public ThreeD(Context context, RelativeLayout mainLayout, File workingFile, String viewType) {
        mContext = context;
        mActivity = (Activity) mContext;
        mMainLayout = mainLayout;
        mFile = workingFile;
        mViewType = viewType;
        init();
    }

    private void init() {
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hoopsModeSelect(v);
            }
        };

        mHoopsModeSelect = mActivity.getLayoutInflater().inflate(R.layout.hoops_mode_select, null);
        mHoopsOperator = mHoopsModeSelect.findViewById(R.id.hoops_operator);
        mHoopsOperator.setOnClickListener(mOnClickListener);
        mHoopsMode = mHoopsModeSelect.findViewById(R.id.hoops_mode);
        mHoopsMode.setOnClickListener(mOnClickListener);
        mHoopsCuttingPlane = mHoopsModeSelect.findViewById(R.id.hoops_cuttingPlane);
        mHoopsCuttingPlane.setOnClickListener(mOnClickListener);
        mHoopsOperator.setBackgroundColor(Color.parseColor("#0099FF"));
        // for preview page, don't have the control.
        if (mViewType.equals(Constant.VIEW_TYPE_NORMAL)) {
            m_recyclerView = ((ViewActivity) mActivity).getM_recyclerView();
            m_recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void fileRender() {
        String fileName = mFile.getName();
        String filePath = mFile.getPath();

        if (fileName.isEmpty()) {
            return;
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (ViewerUtils.canOpenFile(mFile)) {
            initialHSF(filePath);
        }
        if (extension.equals(RenderHelper.FILE_EXTENSION_VDS)) {
            initialVDS(filePath);
        }
    }

    @Override
    public void onSurfaceBind(boolean bindRet) {
        // Start load-file asynchronous task if MobileSurface::bind() was successful
        if (!bindRet) {
            Toast.makeText(mContext.getApplicationContext(), "C++ bind() failed to appInitialize", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isReleaseResource) {
            return;
        }
        mLoadFileTaskCallback = new LoadFileTaskCallback();
        new LoadFileAsyncTask(mSurfaceView, mFilePath, mLoadFileTaskCallback).run();

    }

    @Override
    public void onShowKeyboard() {

    }

    @Override
    public void eraseKeyboardTriggerField() {

    }

    @Override
    public void onShowPerformanceTestResult(float fps) {

    }

    public DVLCore getCore() {
        if (mSAPViewer != null) {
            return mSAPViewer.getDVLCore();
        }
        return null;
    }

    private void initialHSF(String FilePath) {
        MobileApp.setFontDirectory(ViewerUtils.FONT_DIRECTORY_PATH);
        MobileApp.setMaterialsDirectory(ViewerUtils.MATERIAL_DIRECTORY_PATH);
        mobileSurfacePointer = 0;
        mSurfaceView = new AndroidUserMobileSurfaceView(mContext, this, MOBILE_SURFACE_GUI_ID, mobileSurfacePointer);
        mFilePath = FilePath;
        bHSFViewFlag = true;
        addSubView();
    }

    public void removeSubView() {
        mMainLayout.removeView(mSurfaceView);
        mMainLayout.removeView(mHoopsModeSelect);
        mMainLayout.removeView(mCurrentToolbarView);
    }

    public void addSubView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        mCurrentToolbarView = mActivity.getLayoutInflater().inflate(R.layout.operators, null);
        mModesToolbarView = mActivity.getLayoutInflater().inflate(R.layout.modes, null);
        mUserCodeToolbarView = mActivity.getLayoutInflater().inflate(R.layout.user_code, null);
        mSelectedToolbarView = mCurrentToolbarView;
        mMainLayout.addView(mSurfaceView);
        // for preview, ignore these operate button.
        if (mViewType.equals(Constant.VIEW_TYPE_NORMAL)) {
            mMainLayout.addView(mHoopsModeSelect, params);
            mMainLayout.addView(mCurrentToolbarView);
        }
    }

    public void reloadSubView() {
        mMainLayout.removeAllViews();
        mMainLayout.addView(mSurfaceView);
        mMainLayout.addView(mSelectedToolbarView);
        mMainLayout.addView(mHoopsModeSelect);
    }

    public void reloadOperators() {
        mMainLayout.removeView(mCurrentToolbarView);
        mCurrentToolbarView = mActivity.getLayoutInflater().inflate(R.layout.operators, null);
        mMainLayout.addView(mCurrentToolbarView);
    }

    public void reloadModes() {
        mMainLayout.removeView(mCurrentToolbarView);
        mCurrentToolbarView = mActivity.getLayoutInflater().inflate(R.layout.modes, null);
        mMainLayout.addView(mCurrentToolbarView);
    }

    public void reloadUserCode() {
        mMainLayout.removeView(mCurrentToolbarView);
        mCurrentToolbarView = mActivity.getLayoutInflater().inflate(R.layout.user_code, null);
        mMainLayout.addView(mCurrentToolbarView);
    }

    public boolean getHSFViewFlag() {
        return bHSFViewFlag;
    }

    public AndroidUserMobileSurfaceView getmSurfaceView() {
        return mSurfaceView;
    }

    private void initialVDS(String filePath) {
        DVLCore.loadLibraries(mActivity.getResources().getDisplayMetrics(),
                mActivity);
        mSAPViewer = new SAPViewer(mContext);
        mMainLayout.addView(mSAPViewer);
        mSAPViewer.display(filePath, this);
    }

    @Override
    public void onDisplay(final SDVLProceduresInfo proceduresInfo) {
        if (m_recyclerView == null) {
            return;
        }
        CommonUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (m_recyclerView.getVisibility() == View.GONE) {
                    m_recyclerView.setVisibility(View.VISIBLE);
                }
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                m_recyclerView.setLayoutManager(linearLayoutManager);
                m_dvlImageAdapter = new SapGalleryAdapter();
                m_dvlImageAdapter.setData(mSAPViewer, proceduresInfo);

                m_dvlImageAdapter.setOnItemClickListener(new SapGalleryAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        mSAPViewer.activateStep(proceduresInfo.procedures.get(0).steps.get(position).id);
                    }
                });
                m_dvlImageAdapter.setSelected(0);
                mSAPViewer.activateStep(proceduresInfo.procedures.get(0).steps.get(0).id);
                m_recyclerView.setAdapter(m_dvlImageAdapter);
            }
        });
    }

    @Override
    public void onInitializeError(final DVLTypes.DVLRESULT dvlresult) {
        ExceptionHandler.handleDVLRESULT(mContext, dvlresult);
    }

    public void hoopsModeSelect(View view) {
        switch (view.getId()) {
            case R.id.hoops_operator:
                if (mModesToolbarView != null) {
                    mMainLayout.removeView(mModesToolbarView);
                }
                if (mUserCodeToolbarView != null) {
                    mMainLayout.removeView(mUserCodeToolbarView);
                }
                if (mCurrentToolbarView != null) {
                    mMainLayout.removeView(mCurrentToolbarView);
                }
                mCurrentToolbarView.setVisibility(View.VISIBLE);
                mSelectedToolbarView = mCurrentToolbarView;
                mMainLayout.addView(mCurrentToolbarView);
                mHoopsOperator.setBackgroundColor(Color.parseColor("#0099FF"));
                mHoopsMode.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                mHoopsCuttingPlane.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                break;
            case R.id.hoops_mode:
                if (mModesToolbarView != null) {
                    mMainLayout.removeView(mModesToolbarView);
                }
                if (mCurrentToolbarView != null) {
                    mMainLayout.removeView(mCurrentToolbarView);
                }
                if (mUserCodeToolbarView != null) {
                    mMainLayout.removeView(mUserCodeToolbarView);
                }
                mModesToolbarView.setVisibility(View.VISIBLE);
                mSelectedToolbarView = mModesToolbarView;
                mMainLayout.addView(mModesToolbarView);
                mHoopsOperator.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                mHoopsMode.setBackgroundColor(Color.parseColor("#0099FF"));
                mHoopsCuttingPlane.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                break;
            case R.id.hoops_cuttingPlane:
                if (mUserCodeToolbarView != null) {
                    mMainLayout.removeView(mUserCodeToolbarView);
                }
                if (mCurrentToolbarView != null) {
                    mMainLayout.removeView(mCurrentToolbarView);
                }
                if (mModesToolbarView != null) {
                    mMainLayout.removeView(mModesToolbarView);
                }
                mUserCodeToolbarView.setVisibility(View.VISIBLE);
                mSelectedToolbarView = mUserCodeToolbarView;
                mMainLayout.addView(mUserCodeToolbarView);
                mHoopsOperator.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                mHoopsMode.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                mHoopsCuttingPlane.setBackgroundColor(Color.parseColor("#0099FF"));
                break;
        }
    }

    public void toolbarButtonPressed(View view) {
        // Handle toolbar button press
        // These are connected from xml code via the
        // <ImageButton android:onClick="toolbarButtonPressed"> attribute
        // Calling the method on AndroidUserMobileSurfaceView calls down to the actions
        // in UserMobileSurface.h

        switch (view.getId()) {
            case R.id.orbitButton:
                mSurfaceView.setOperatorOrbit();
                break;
            case R.id.zoomAreaButton:
                mSurfaceView.setOperatorZoomArea();
                break;
            case R.id.selectButton:
                mSurfaceView.setOperatorSelectPoint();
                break;
            case R.id.selectAreaButton:
                mSurfaceView.setOperatorSelectArea();
                break;
            case R.id.flyButton:
                mSurfaceView.setOperatorFly();
                break;
            case R.id.simpleShadowButton:
                mModeSimpleShadowEnabled = !mModeSimpleShadowEnabled;
                mSurfaceView.onModeSimpleShadow(mModeSimpleShadowEnabled);
                break;
            case R.id.smoothButton:
                mSurfaceView.onModeSmooth();
                break;
            case R.id.hiddenLineButton:
                mSurfaceView.onModeHiddenLine();
                break;
            case R.id.frameRateButton:
                mSurfaceView.onModeFrameRate();
                break;
            case R.id.userCode1Button:
                mSurfaceView.onUserCode1();
                break;
            case R.id.userCode2Button:
                mSurfaceView.onUserCode2();
                break;
            case R.id.userCode3Button:
                mSurfaceView.onUserCode3();
                break;
////            case R.id.userCode4Button:
////                mSurfaceView.onUserCode4();
//                break;
        }
    }

    @Override
    public void onReleaseResource() {
        if (mLoadFileTaskCallback != null) {
            mLoadFileTaskCallback = null;
        }
        if (mSurfaceView != null) {
            mSurfaceView.removeCallbacks();
        }
        isReleaseResource = true;
        mobileSurfacePointer = 0;
    }

    private static class LoadFileAsyncTask extends LoadTask<Void, Boolean> implements Runnable {
        private WeakReference<AndroidUserMobileSurfaceView> mWSurfaceView;
        private String mFilePath;
        private ITaskCallback<Result, String> mCallback;

        LoadFileAsyncTask(AndroidUserMobileSurfaceView surfaceView, String filePath,
                          ITaskCallback<Result, String> callback) {
            this.mWSurfaceView = new WeakReference<>(surfaceView);
            this.mFilePath = filePath;
            this.mCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mCallback != null) {
                mCallback.onTaskPreExecute();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (mWSurfaceView == null) {
                return false;
            }
            AndroidUserMobileSurfaceView surfaceView = mWSurfaceView.get();
            if (surfaceView == null) {
                return false;
            }
            if (mFilePath == null || mFilePath.isEmpty()) {
                return false;
            }

            // Perform file load on separate thread
            return surfaceView.loadFile(mFilePath);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (mCallback != null) {
                    mCallback.onTaskExecuteSuccess(new Result());
                }
            } else {
                if (mCallback != null) {
                    mCallback.onTaskExecuteFailed("");
                }
            }
        }

        @Override
        public void run() {
            executeOnExecutor(ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND));
        }

        class Result implements IResult {

        }
    }

    public void showLoadingDialog(Context ctx) {
        if (ctx == null) {
            return;
        }
        if (((Activity) ctx).isFinishing()) {
            return;
        }
        if (((Activity) ctx).isDestroyed()) {
            return;
        }
        try {
            if (mLoadingDialog == null) {
                mLoadingDialog = LoadingDialog2.newInstance();
            }
            if (isLoadingDialogShow) {
                return;
            }
            mLoadingDialog.showModalDialog(ctx);
            isLoadingDialogShow = true;
        } catch (Exception e) {
            e.printStackTrace();
            isLoadingDialogShow = false;
        }
    }

    private void dismissLoadingDialog() {
        if (mContext == null) {
            return;
        }
        if (((Activity) mContext).isFinishing()) {
            return;
        }
        if (((Activity) mContext).isDestroyed()) {
            return;
        }
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissDialog();
            isLoadingDialogShow = false;
        }
    }

    class LoadFileTaskCallback implements LoadTask.ITaskCallback<LoadFileAsyncTask.Result, String> {

        @Override
        public void onTaskPreExecute() {
            showLoadingDialog(mContext);
        }

        @Override
        public void onTaskExecuteSuccess(LoadFileAsyncTask.Result results) {
            dismissLoadingDialog();
        }

        @Override
        public void onTaskExecuteFailed(String e) {
            dismissLoadingDialog();

            ToastUtil.showToast(mContext, mContext.getString(R.string.hint_msg_error_while_process));
        }
    }
}
