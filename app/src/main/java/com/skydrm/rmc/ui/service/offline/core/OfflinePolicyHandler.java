package com.skydrm.rmc.ui.service.offline.core;

import android.support.annotation.NonNull;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.service.fileinfo.IFileInfo;
import com.skydrm.rmc.ui.service.offline.architecture.IOffline;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineHandler;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineRequest;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineStatus;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.sdk.INxlFileFingerPrint;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;

import java.io.IOException;
import java.util.List;

public class OfflinePolicyHandler extends OfflineHandler {
    private OfflineRequest mRequest;
    private IPolicyCallback mCallback;

    OfflinePolicyHandler(IPolicyCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void handleRequest(OfflineRequest request) throws OfflineException {
        this.mRequest = request;
        IOffline offline = paramCheck(request.getOffline());
        processPolicy(offline);
    }

    private void processPolicy(final IOffline offline) throws OfflineException {
        handleExactly(offline, new InternalCallback() {
            @Override
            public void onADHocPolicy() {
                dispatchRequest(mRequest);
            }

            @Override
            public void onCentralPolicy(String obligations, int rights) {
                offline.setRights(rights, obligations);
                //make successor process request.
                dispatchRequest(mRequest);
            }

            @Override
            public void onFailed(OfflineException e) {
                //handle exception
                handleException(e);
            }
        });
    }

    private void handleExactly(IOffline offline, InternalCallback callback) {
        ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.REGULAR_BACK_GROUND).execute(new InternalTask(offline, callback));
    }

    /**
     * This method is used to process adhoc policy situation.
     * if adhoc policy then make the successor process the request.
     *
     * @param request concurrent request need to be handled.
     */
    private void dispatchRequest(OfflineRequest request) {
        try {
            mCallback.onCheckPolicy(0);
            successor.handleRequest(request);
        } catch (OfflineException e) {
            handleException(e);
        }
    }

    private void handleException(OfflineException e) {
        mCallback.onCheckError(e);
    }

    public interface IPolicyCallback {
        void onCheckPolicy(int type);

        void onCacheRights();

        void onCheckError(OfflineException e);
    }

    private static class InternalTask implements Runnable {
        private IOffline mOffline;
        private InternalCallback mCallback;

        InternalTask(IOffline offline, @NonNull InternalCallback callback) {
            this.mOffline = offline;
            this.mCallback = callback;
        }

        @Override
        public void run() {
            try {
                INxlFileFingerPrint fp = mOffline.getFingerPrint();
                if (fp == null) {
                    handleException(new Exception("Failed to get finger print."));
                    return;
                }
                if (fp.hasRights() && fp.hasTags()) {
                    //central
                    processCentralPolicy(mOffline, fp, mCallback);
                } else if (fp.hasRights()) {
                    //adhoc
                    mCallback.onADHocPolicy();
                } else if (fp.hasTags()) {
                    //central
                    processCentralPolicy(mOffline, fp, mCallback);
                } else if (!fp.hasRights() && !fp.hasTags()) {
                    //central
                    processCentralPolicy(mOffline, fp, mCallback);
                }
            } catch (RmsRestAPIException
                    | SessionInvalidException
                    | InvalidRMClientException
                    | TokenAccessDenyException e) {
                handleException(e);
            } catch (IOException e) {
                if (e instanceof NotNxlFileException) {
                    if (mCallback != null) {
                        mCallback.onFailed(new OfflineException(OfflineStatus.STATUS_INVALID_NXL_FILE, e.getMessage(), e));
                    }
                } else {
                    handleException(e);
                }
            }
        }

        private void handleException(Exception e) {
            if (e == null) {
                return;
            }
            e.printStackTrace();
            if (mCallback != null) {
                if (e instanceof RmsRestAPIException) {
                    RmsRestAPIException rmsRestAPIException = (RmsRestAPIException) e;
                    if (rmsRestAPIException.getDomain() == RmsRestAPIException.ExceptionDomain.FileNotFound) {
                        mCallback.onFailed(new OfflineException(OfflineStatus.STATUS_FILE_NOT_FOUND, e.getMessage(), e));
                        return;
                    }
                    if (rmsRestAPIException.getDomain() == RmsRestAPIException.ExceptionDomain.AccessDenied) {
                        mCallback.onFailed(new OfflineException(OfflineStatus.STATUS_UNAUTHORIZED, e.getMessage(), e));
                        return;
                    }
                }
                mCallback.onFailed(new OfflineException(OfflineStatus.STATUS_FAILED, e.getMessage(), e));
            }
        }

        private void processCentralPolicy(IOffline offline, INxlFileFingerPrint fp, final InternalCallback callback) {
            offline.doPolicyEvaluation(fp, new IFileInfo.IPolicyCallback() {
                @Override
                public void onSuccess(List<String> rights, String obligations) {

                    if (rights != null && rights.contains(Constant.RIGHTS_VIEW)) {
                        callback.onCentralPolicy(obligations, NxlDoc.rights2Integer(rights));
                    } else {
                        callback.onFailed(new OfflineException(OfflineStatus.STATUS_FAILED,
                                "You are not authorized to perform this action."));
                    }
                }

                @Override
                public void onFailed(Exception e) {
                    if (e instanceof RmsRestAPIException) {
                        RmsRestAPIException rmsRestAPIException = (RmsRestAPIException) e;
                        if (rmsRestAPIException.getDomain() == RmsRestAPIException.ExceptionDomain.FileNotFound) {
                            callback.onFailed(new OfflineException(OfflineStatus.STATUS_FILE_NOT_FOUND, e.getMessage(), e));
                        } else {
                            callback.onFailed(new OfflineException(OfflineStatus.STATUS_FAILED, e.getMessage(), e));
                        }
                        return;
                    }
                    callback.onFailed(new OfflineException(OfflineStatus.STATUS_FAILED, e.getMessage(), e));
                }
            });
        }
    }

    interface InternalCallback {
        void onADHocPolicy();

        void onCentralPolicy(String obligations, int rights);

        void onFailed(OfflineException e);
    }
}
