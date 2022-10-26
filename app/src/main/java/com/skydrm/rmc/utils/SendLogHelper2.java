package com.skydrm.rmc.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.types.SendLogRequestValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.skydrm.rmc.ExecutorPools.Select_Type.REGULAR_BACK_GROUND;

/**
 * Created by aning on 12/20/2016.
 */

public class SendLogHelper2 {

    static private final String TAG = "SendLogHelper2";
    static private final String LOG_RECORD = "/failedLog.log";
    private static final boolean DEBUG = SkyDRMApp.getInstance().isDebug();
    private SendLogRequestValue mLogRequestValue;
    private SendLogHelper2.LogFileHelper mLogFileHelper;
    private List<SendLogRequestValue> mLogRequestValueList;

    public SendLogHelper2(SendLogRequestValue logRequestValue) {
        mLogFileHelper = new SendLogHelper2.LogFileHelper();
        mLogRequestValue = logRequestValue;
        mLogRequestValueList = mLogFileHelper.unSerializeFromDisk();
        if (mLogRequestValueList == null) {
            mLogRequestValueList = new ArrayList<>();
        }
    }

    public SendLogHelper2() {
        mLogFileHelper = new SendLogHelper2.LogFileHelper();
    }

    public void reportToRMS() {
        SendLogHelper2.SendLogAsyncTask sendLogAsyncTask = new SendLogHelper2.SendLogAsyncTask();
        sendLogAsyncTask.executeOnExecutor(ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND));
    }

    public void reSubmitLogToRMS() {
        SendLogHelper2.ResubmitLogAsyncTask resubmitLogTask = new SendLogHelper2.ResubmitLogAsyncTask();
        resubmitLogTask.executeOnExecutor(ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND));
    }

    public void batchReportToRMS(List<SendLogRequestValue> requestValues) {
        if (requestValues == null || requestValues.size() == 0) {
            return;
        }
        BatchSubmitLogAsyncTask task = new BatchSubmitLogAsyncTask(mLogFileHelper, requestValues);
        task.executeOnExecutor(ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND));
    }

    class SendLogAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                if (session.getRmsRestAPI()
                        .getLogService(session.getRmUser())
                        .sendLogToRms(mLogRequestValue)) {
                    return true;
                }
            } catch (RmsRestAPIException e) {
                // can ignore this exception handler since the operation is in bg
                if (DEBUG) {
                    Log.d(TAG, "statusCode is: " + e.getRmsStatusCode());
                }
            } catch (SessionInvalidException | InvalidRMClientException e) {
            } catch (Exception e) {
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                mLogRequestValueList.add(mLogRequestValue);
                try {
                    mLogFileHelper.serializeToDisk(mLogRequestValueList);
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            } else {
                if (DEBUG) {
                    Log.e(TAG, "SendLogAsyncTask:  send log succeed! -- " + mLogRequestValue.getmOperationId());
                }
            }
        }
    }

    static class BatchSubmitLogAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private LogFileHelper mLogFileHelper;
        private List<SendLogRequestValue> mLogRequestValueList;

        BatchSubmitLogAsyncTask(LogFileHelper helper, List<SendLogRequestValue> logRequestValues) {
            this.mLogFileHelper = helper;
            this.mLogRequestValueList = logRequestValues;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (mLogRequestValueList != null && mLogRequestValueList.size() != 0) {
                Iterator<SendLogRequestValue> it = mLogRequestValueList.iterator();
                while (it.hasNext()) {
                    SendLogRequestValue requestValue = it.next();
                    try {
                        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                        if (session.getRmsRestAPI()
                                .getLogService(session.getRmUser())
                                .sendLogToRms(requestValue)) {
                            it.remove();
                        }
                    } catch (Exception e) {
                        if (DEBUG) {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                }
            }
            return null;
        }

//        @Override
//        protected void onPostExecute(Boolean unused) {
//            if (mLogRequestValueList != null && mLogRequestValueList.size() > 0) {
//                try {
//                    mLogFileHelper.serializeToDisk(mLogRequestValueList);
//                } catch (Exception e) {
//                    Log.e(TAG, e.getMessage());
//                }
//            }
//        }
    }

    class ResubmitLogAsyncTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            if (mLogRequestValueList != null) {
                for (SendLogRequestValue requestValue : mLogRequestValueList) {
                    try {
                        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
                        if (session.getRmsRestAPI()
                                .getLogService(session.getRmUser())
                                .sendLogToRms(requestValue)) {
                            mLogRequestValueList.remove(requestValue); // need more test!!!
                            if (DEBUG) {
                                Log.e(TAG, "ResubmitLogAsyncTask:  send log succeed! --" + mLogRequestValue.getmOperationId());
                            }
                        }
                    } catch (Exception e) {
                        if (DEBUG) {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                }

            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean unused) {
            if (mLogRequestValueList != null && mLogRequestValueList.size() > 0) {
                try {
                    mLogFileHelper.serializeToDisk(mLogRequestValueList);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }


    class LogFileHelper {

        public boolean serializeToDisk(List<SendLogRequestValue> requestValueList) {
            try {
                File file = getSendLogCachedFile();
                if (!file.exists()) {
                    file.createNewFile();
                }
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(requestValueList);
                oos.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public List<SendLogRequestValue> unSerializeFromDisk() {
            try {
                File file = getSendLogCachedFile();
                if (!file.exists()) {
                    return null;
                }
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                @SuppressWarnings("unchecked")
                List<SendLogRequestValue> sendLogRequestValues = (List<SendLogRequestValue>) ois.readObject();
                return sendLogRequestValues;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private File getSendLogCachedFile() throws Exception {
            return new File(SkyDRMApp.getInstance().getCommonDirs().userRootFile(), LOG_RECORD);
        }
    }
}
