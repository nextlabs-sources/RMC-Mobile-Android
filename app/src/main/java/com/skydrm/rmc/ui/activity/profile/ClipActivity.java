package com.skydrm.rmc.ui.activity.profile;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.engine.eventBusMsg.account.UpdateUserAvatarEvent;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.widget.avatar.ClipImageLayout;
import com.skydrm.rmc.utils.commonUtils.AvatarUtil;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Set;

import static com.skydrm.rmc.ExecutorPools.Select_Type.FIRED_BY_UI;

public class ClipActivity extends BaseActivity {
    public static final String IMAGE_FILE_NAME = "header.jpg";
    private static final String IMAGE_STORE_PATH = Environment.getExternalStorageDirectory()
            + File.separator
            + "DCIM"
            + File.separator
            + "Camera"
            + File.separator;
    private static DevLog log = new DevLog(ClipActivity.class.getSimpleName());
    private ClipImageLayout mClipImageLayout;
    private String pathUri;
    private String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clipimage);
        findViewById(R.id.back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        action = getIntent().getAction();
        pathUri = getIntent().getStringExtra("path_uri");
        log.v("onCreate:" + pathUri);
        //sanity check
        if (TextUtils.isEmpty(pathUri)) {
            return;
        }
        LoadPhotoAysncTask loadPhotoAysncTask = new LoadPhotoAysncTask(this);
        loadPhotoAysncTask.executeOnExecutor(ExecutorPools.SelectSmartly(FIRED_BY_UI), (Void) null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearTmpCopyFiles(PhotoPicker.mtempCopyedFiles);
    }

    public void clearTmpCopyFiles(Set<String> tempCopies) {
        if (tempCopies != null && tempCopies.size() > 0) {
            for (String tempPath : tempCopies) {
                File tempFile = new File(tempPath);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }
    }

    private class LoadPhotoAysncTask extends AsyncTask<Void, Void, Bitmap> {
        private ProgressDialog mLoadingDialog;
        private Context mContext;

        LoadPhotoAysncTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingDialog = ProgressDialog.show(mContext, mContext.getString(R.string.app_name),
                    mContext.getString(R.string.common_waiting_initcap_3dots));
            mLoadingDialog.show();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            String path = PhotoPicker.getPath(mContext, Uri.parse(pathUri));

            log.v("doInBackground: " + path);
            if (!TextUtils.isEmpty(action) && action.equals("take_photo")) {
                if (!TextUtils.isEmpty(pathUri) && !new File(pathUri).exists()) {
                    log.e("file does not found...");
                    return null;
                }
                return AvatarUtil.getInstance().convertToBitmap(pathUri);
            }
            if (!TextUtils.isEmpty(path) && !new File(path).exists()) {
                log.e("file does not found...");
                return null;
            }
            return AvatarUtil.getInstance().convertToBitmap(path);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            if (bitmap != null) {
                mClipImageLayout = (ClipImageLayout) findViewById(R.id.id_clipImageLayout);
                mClipImageLayout.setBitmap(bitmap);
                (findViewById(R.id.id_action_clip))
                        .setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                final Bitmap bitmap = mClipImageLayout.clip();
                                AvatarUtil.getInstance().sendAvatarToRMS(ClipActivity.this, bitmap, new AvatarUtil.IUpdateAvatar() {
                                    @Override
                                    public void updateSuccess() {
                                        log.v("Upload Avatar To RMS Success: ");
                                        AvatarUtil.getInstance().cacheAvatar(bitmap);
                                        EventBus.getDefault().post(new UpdateUserAvatarEvent());
                                        Intent intent = new Intent();
                                        intent.putExtra("clip_path", IMAGE_STORE_PATH + IMAGE_FILE_NAME);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }

                                    @Override
                                    public void updateFailed() {
                                        ToastUtil.showToast(ClipActivity.this, "Upload avatar failed...");
                                    }
                                });
                            }
                        });
            } else {
                ToastUtil.showToast(mContext, "File does not exist...");
                ((Activity) (mContext)).finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
