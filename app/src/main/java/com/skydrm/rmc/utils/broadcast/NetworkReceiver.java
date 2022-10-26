package com.skydrm.rmc.utils.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by hhu on 3/13/2017.
 */

public class NetworkReceiver extends BroadcastReceiver {
    public static final String TAG = "NetworkReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            if (activeNetwork != null) {
                // connected to the internet
                if (activeNetwork.isConnected()) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifi
                        Log.e(TAG, "Current wifi is available");
                        mIListenNetworkStatus.onNetworkConnected(activeNetwork.getExtraInfo());
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        // connected to the mobile provider's data plan
                        Log.e(TAG, "Connected to the mobile provider's data plan");
                        mIListenNetworkStatus.onNetworkConnected(activeNetwork.getExtraInfo());
                    }
                } else {
                    Log.e(TAG, "Currently there is no network connection, please make sure that you have already opened the network ");
                    mIListenNetworkStatus.onNetworkDisconnected();
                }
                Log.e(TAG, "getSubtypeName()-----" + activeNetwork.getTypeName()
                        + "\ngetDetailedState()--" + activeNetwork.getExtraInfo()
                        + "\ngetState()----------" + activeNetwork.getState()
                        + "\ngetType()-----------" + activeNetwork.getType());
            } else {
                // not connected to the internet
                Log.e(TAG, "Currently there is no network connection, please make sure that you have already opened the network ");
                mIListenNetworkStatus.onNetworkDisconnected();
            }
        }
    }

    private IListenNetworkStatus mIListenNetworkStatus = getDefaultListener();

    private IListenNetworkStatus getDefaultListener() {
        return new IListenNetworkStatus() {
            @Override
            public void onNetworkConnected(String extraInfo) {

            }

            @Override
            public void onNetworkDisconnected() {

            }
        };
    }

    public void setNetworkStatusListener(IListenNetworkStatus iListenNetworkStatus) {
        this.mIListenNetworkStatus = iListenNetworkStatus;
    }

    public interface IListenNetworkStatus {
        void onNetworkConnected(String extraInfo);

        void onNetworkDisconnected();
    }
}
