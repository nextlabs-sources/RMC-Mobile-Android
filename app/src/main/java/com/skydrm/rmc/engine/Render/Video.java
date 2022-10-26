package com.skydrm.rmc.engine.Render;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.utils.MediaControllerEx;
import com.skydrm.rmc.utils.SharePreferUtils;
import com.skydrm.rmc.utils.VideoViewEx;

import java.io.File;

public class Video implements IFileRender {
    private static final DevLog log = new DevLog(Video.class.getSimpleName());

    private RelativeLayout mMainLayout;
    private File mFile;
    private VideoViewEx mVideoView;
    private MediaControllerEx mMediaController;
    private View mView;
    private ImageView mSwitchScreenButton;
    private int mScreenWidth;
    private int mScreenHeight;

    private Activity mActivity;
    private OrientationEventListener mOrientationListener;
    // whether is landscape or not
    private boolean mIsLand = false;
    // whether is click switch screen button
    private boolean mClickSwitch = false;
    // whether click and switch into landscape
    private boolean mClickLand = true;
    // whether click and switch into portrait
    private boolean mClickPort = true;

    public Video(Context context, RelativeLayout mainLayout, File workingFile) {
        mActivity = (Activity) context;
        mMainLayout = mainLayout;
        mFile = workingFile;
        init();
    }

    public void init() {

        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        mView = mActivity.getLayoutInflater().inflate(R.layout.activity_video, null);
        mVideoView = (VideoViewEx) mView.findViewById(R.id.video);
        mMediaController = new MediaControllerEx(mActivity);
        startListener();

        // add video sub view
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mMainLayout.getLayoutParams();
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mMainLayout.addView(mView, lp);

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                mSwitchScreenButton = mMediaController.getImageView();

                if (mScreenHeight > mScreenWidth) {
                    mSwitchScreenButton.setImageResource(R.drawable.spread_32);
                    mIsLand = false;
                    portraitDisplay();
                } else {
                    mSwitchScreenButton.setImageResource(R.drawable.shrink_32);
                    mIsLand = true;
                    landscapeDisplay();
                }

                mSwitchScreenButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClickSwitch = true;
                        switch (SkyDRMApp.getInstance().getResources().getConfiguration().orientation) {
                            case Configuration.ORIENTATION_LANDSCAPE:
                                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                                mSwitchScreenButton.setImageResource(R.drawable.spread_32);
                                break;
                            case Configuration.ORIENTATION_PORTRAIT://switch to landscape
                                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                                mSwitchScreenButton.setImageResource(R.drawable.shrink_32);
                                break;
                        }
                    }
                });
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(mActivity, "player completion", Toast.LENGTH_SHORT).show();
            }
        });

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                log.i("error for playing video !");
                return false;
            }
        });
    }

    @Override
    public void fileRender() {
        StartPlayVideo(mFile);
    }

    /**
     * start listen automatic screen rotate
     */
    private final void startListener() {
        mOrientationListener = new OrientationEventListener(mActivity) {
            @Override
            public void onOrientationChanged(int rotation) {
                // set portrait
                if (((rotation >= 0) && (rotation <= 30)) || (rotation >= 330)) {
                    if (mClickSwitch) {
                        if (mIsLand && !mClickLand) {
                            return;
                        } else {
                            mClickPort = true;
                            mClickSwitch = false;
                            mIsLand = false;
                        }
                    } else {
                        if (mIsLand) {
                            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            mIsLand = false;
                            mClickSwitch = false;
                        }
                    }
                } else if (((rotation >= 230) && (rotation <= 310))) {   // set landscape
                    if (mClickSwitch) {
                        if (!mIsLand && !mClickPort) {
                            return;
                        } else {
                            mClickLand = true;
                            mClickSwitch = false;
                            mIsLand = true;
                        }
                    } else {
                        if (!mIsLand) {
                            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            mIsLand = true;
                            mClickSwitch = false;
                        }
                    }
                }
            }
        };
        mOrientationListener.enable();
    }

    // full screen mode (landscape view)
    public void landscapeDisplay() {
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        FrameLayout.LayoutParams lp_mc = new FrameLayout.LayoutParams(mScreenWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp_mc.setMargins(0, 0, 0, 0);
        mMediaController.setLayoutParams(lp_mc);

        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mVideoView.setLayoutParams(layoutParams);
    }

    // window mode (portrait window view)
    public void portraitDisplay() {
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mScreenWidth, mScreenHeight);
        lp.setMargins(0, mScreenHeight / 4, 0, mScreenHeight / 4);
        mVideoView.setLayoutParams(lp);

        FrameLayout.LayoutParams lp_mc = new FrameLayout.LayoutParams(mScreenWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp_mc.setMargins(0, 0, 0, mScreenHeight / 4);
        mMediaController.setLayoutParams(lp_mc);
    }

    public View getVideoView() {
        return mView;
    }

    public ImageView getSwitchScreenButton() {
        return mSwitchScreenButton;
    }

    public void StartPlayVideo(String path) {
        if (!path.isEmpty()) {
            mVideoView.setVideoPath(path);
            mVideoView.setMediaController(mMediaController);
            mVideoView.requestFocus();
            mVideoView.start();
        }
    }

    public void StartPlayVideo(File document) {
        if (document != null) {
            mVideoView.setVideoURI(Uri.fromFile(document));
            mVideoView.setMediaController(mMediaController);
            mVideoView.requestFocus();
            mVideoView.start();
        }
    }

    public void pausePlayVideo() {
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
            int currentPosition = mVideoView.getCurrentPosition();
            SharePreferUtils.setParams(SkyDRMApp.getInstance(), "videostate", currentPosition);
        }
    }

    public void continuePlayVideo() {
        if (mVideoView != null && !mVideoView.isPlaying()) {
            mVideoView.start();
            int videostate = (int) SharePreferUtils.getParams(SkyDRMApp.getInstance(), "videostate", 0);
            if (videostate > 0) {
                mVideoView.seekTo(videostate);
            }
        }
    }

    public boolean isVideoPlaying() {
        return mVideoView.isPlaying();
    }

    public int getCurrentPosition() {
        return mVideoView.getCurrentPosition();
    }

    public void resetVideo() {
        if (mVideoView != null) {
            mVideoView.start();
            mVideoView.seekTo(0);
        }
    }
}
