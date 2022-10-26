package com.skydrm.rmc.engine.Render;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.utils.SharePreferUtils;

import java.io.File;
import java.io.IOException;

public class Audio implements IFileRender, IStopAudioPreview, Chronometer.OnChronometerTickListener {
    private static final DevLog log = new DevLog(Audio.class.getSimpleName());
    private Context mContent;
    private RelativeLayout mMainLayout;
    private File mFile;

    private static final int UPDATE_FREQUENCY = 500;
    private static final int STEP_VALUE = 4000;
    private final Handler handler = new Handler();
    private TextView selectedFile = null;
    private SeekBar seekbar = null;
    private MediaPlayer player = null;
    private final Runnable updatePositionRunnable = new Runnable() {
        public void run() {
            updatePosition();
        }
    };
    private ImageView playButton = null;
    private boolean isStarted = true;
    private boolean isMovingSeekBar = false;
    private String mViewType;

    private View.OnClickListener onButtonClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play: {
                    if (player.isPlaying()) {
                        // pause
                        handler.removeCallbacks(updatePositionRunnable);
                        player.pause();
                        playButton.setImageResource(R.drawable.play_32);
                    } else {
                        // start play.
                        if (isStarted) {
                            player.start();
                            playButton.setImageResource(R.drawable.pause_32);
                            updatePosition();
                        } else {
                            startPlay(mFile.getPath());
                        }
                    }
                    break;
                }
                case R.id.next: {
                    int seekto = player.getCurrentPosition() + STEP_VALUE;
                    if (seekto > player.getDuration())
                        seekto = player.getDuration();
                    player.pause();
                    player.seekTo(seekto);
                    player.start();
                    break;
                }
                case R.id.prev: {
                    int seekto = player.getCurrentPosition() - STEP_VALUE;
                    if (seekto < 0)
                        seekto = 0;
                    player.pause();
                    player.seekTo(seekto);
                    player.start();
                    break;
                }
            }
        }
    };
    private MediaPlayer.OnCompletionListener onCompletion = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            stopPlay();
        }
    };
    private MediaPlayer.OnErrorListener onError = new MediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            log.i("error for playing audio!");
            return false;
        }
    };
    private SeekBar.OnSeekBarChangeListener seekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isMovingSeekBar = false;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isMovingSeekBar = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (isMovingSeekBar) {
                player.seekTo(progress);
                log.i("onProgressChanged in OnSeekBarChangeListener");
            }
        }
    };

    public Audio(Context context, RelativeLayout contentLayout, File file, String viewType) {
        mContent = context;
        mMainLayout = contentLayout;
        mFile = file;
        mViewType = viewType;
        init();
    }

    private void init() {
        View audioView = null;
        if (mViewType.equals(Constant.VIEW_TYPE_NORMAL)) {
            audioView = ((Activity) mContent).getLayoutInflater().inflate(R.layout.activity_audio, null);
            selectedFile = (TextView) audioView.findViewById(R.id.selectedfile);
        } else { // preview file
            audioView = ((Activity) mContent).getLayoutInflater().inflate(R.layout.activity_preview_audio, null);
        }

        seekbar = (SeekBar) audioView.findViewById(R.id.seekbar);
        playButton = (ImageView) audioView.findViewById(R.id.play);
        ImageView prevButton = (ImageView) audioView.findViewById(R.id.prev);
        ImageView nextButton = (ImageView) audioView.findViewById(R.id.next);
        player = new MediaPlayer();
        player.setOnCompletionListener(onCompletion);
        player.setOnErrorListener(onError);
        seekbar.setOnSeekBarChangeListener(seekBarChanged);
        playButton.setOnClickListener(onButtonClick);
        nextButton.setOnClickListener(onButtonClick);
        prevButton.setOnClickListener(onButtonClick);

        // add audio sub view
        RelativeLayout.LayoutParams lp =  mViewType.equals(Constant.VIEW_TYPE_NORMAL) ?
                (RelativeLayout.LayoutParams) mMainLayout.getLayoutParams() :   // normal view
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); // preview

        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mMainLayout.addView(audioView, lp);
    }

    @Override
    public void fileRender() {
        startPlay(mFile.getPath());
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {

    }

    @Override
    public void onStopAudioPreview() {
        pause();
    }

    public void startPlay(String file) {
        // for normal view, set file name.
        if (mViewType.equals(Constant.VIEW_TYPE_NORMAL)) {
            selectedFile.setText(file.substring(file.lastIndexOf("/") + 1));
        }
        seekbar.setProgress(0);
        player.stop();
        player.reset();
        try {
            player.setDataSource(file);
            player.prepare();
            player.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        seekbar.setMax(player.getDuration());
        playButton.setImageResource(R.drawable.pause_32);
        updatePosition();
        isStarted = true;
    }

    public void stopPlay() {
        player.stop();
        player.reset();
        playButton.setImageResource(R.drawable.play_32);
        handler.removeCallbacks(updatePositionRunnable);
        seekbar.setProgress(0);
        isStarted = false;
    }

    public void continuePlay() {
        player.start();
        player.pause();
        int audioposition = (int) SharePreferUtils.getParams(SkyDRMApp.getInstance(), "audioposition", 0);
        if (audioposition > 0) {
            seekbar.setProgress(audioposition);
            player.seekTo(audioposition);
        }
        playButton.setImageResource(R.drawable.pause_32);
        player.start();
    }


    public MediaPlayer getAudioPlayer() {
        return player;
    }

    public void pause() {
        if (player != null && player.isPlaying()) {
            player.pause();
            SharePreferUtils.setParams(SkyDRMApp.getInstance(), "audioposition", player.getCurrentPosition());
            playButton.setImageResource(R.drawable.play_32);
        }
    }

    private void updatePosition() {
        handler.removeCallbacks(updatePositionRunnable);

        seekbar.setProgress(player.getCurrentPosition());

        handler.postDelayed(updatePositionRunnable, UPDATE_FREQUENCY);
    }

}
