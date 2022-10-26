package com.skydrm.rmc.ui.widget.customcontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.skydrm.rmc.SkyDRMApp;

/**
 * Created by aning on 11/15/2016.
 * --- used to listen the status of soft keyboard, visible or hidden
 */

public class SoftKeyBoardStatusView extends LinearLayout {

    static private final boolean DEBUG = SkyDRMApp.getInstance().isDebug();
    // we think the soft key board display when the layout height has changed is greater CHANGE_SIZE
    private final static int CHANGE_SIZE = 100;

    private ISoftKeyBoardListener mSoftKeyBoardListener;
    private Context mContext;

    public SoftKeyBoardStatusView(Context context) {
        super(context);
        mContext = context;
    }

    public SoftKeyBoardStatusView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (oldh == 0 || oldw == 0) {
            return;
        }

        if (mSoftKeyBoardListener != null) {
            mSoftKeyBoardListener.onKeyBoardStatus(w, h, oldw, oldh);
            // soft key board is visible
            if (h - oldh < -CHANGE_SIZE) {
                mSoftKeyBoardListener.onKeyBoardVisible(Math.abs(h - oldh));
            }
            // soft key board is invisible
            if (h - oldh > CHANGE_SIZE) {
                mSoftKeyBoardListener.onKeyBoardInvisible(Math.abs(h - oldh));
            }
        }
    }

    public void setSoftKeyBoardListener(ISoftKeyBoardListener softKeyBoardListener) {
        mSoftKeyBoardListener = softKeyBoardListener;
    }

    public interface ISoftKeyBoardListener {
        void onKeyBoardStatus(int w, int h, int oldw, int oldh);

        /**
         * @param move : is the height of soft key board.
         */
        void onKeyBoardVisible(int move);

        void onKeyBoardInvisible(int move);
    }
}
