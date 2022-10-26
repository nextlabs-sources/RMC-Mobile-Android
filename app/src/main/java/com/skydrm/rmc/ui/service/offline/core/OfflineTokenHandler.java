package com.skydrm.rmc.ui.service.offline.core;

import android.content.Context;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.project.SharedWithProjectFile;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.service.offline.architecture.IOffline;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineHandler;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineRequest;
import com.skydrm.rmc.ui.service.offline.architecture.OfflineStatus;
import com.skydrm.rmc.ui.service.offline.exception.OfflineException;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.INxlClient;
import com.skydrm.sdk.exception.NotNxlFileException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.exception.TokenAccessDenyException;
import com.skydrm.sdk.nxl.token.TokenException;

import java.io.FileNotFoundException;

public class OfflineTokenHandler extends OfflineHandler {
    private OfflineRequest mRequest;
    private ITokenHandlerCallback mCallback;

    OfflineTokenHandler(ITokenHandlerCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void handleRequest(OfflineRequest request) throws OfflineException {
        mRequest = paramCheck(request);

        ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI).execute(new Runnable() {
            @Override
            public void run() {
                INxlClient nxlClient = SkyDRMApp.getInstance().getSession().getRmsClient();
                try {
                    IOffline offline = mRequest.getOffline();
                    boolean active;
                    if (offline instanceof SharedWithProjectFile) {
                        SharedWithProjectFile swpf = (SharedWithProjectFile) offline;
                        int id = swpf.getId();
                        String membershipId = swpf.getMembershipId();
                        active = nxlClient.updateOfflineStatus(offline.getLocalPath(), 1, id, membershipId, true);
                    } else {
                        active = nxlClient.updateOfflineStatus(offline.getLocalPath(), true);
                    }
                    if (active) {
                        if (mCallback != null) {
                            mCallback.onTokenProcessed();
                        }
                        offline.updateOfflineStatus(true);
                        dispatchRequest();
                    } else {
                        if (mCallback != null) {
                            mCallback.onTokenError(new OfflineException(OfflineStatus.STATUS_TOKEN_PROCESS_FAILED, "Try active offline file failed."));
                        }
                    }
                } catch (FileNotFoundException
                        | InvalidRMClientException
                        | TokenException
                        | NotNxlFileException e) {
                    if (mCallback != null) {
                        mCallback.onTokenError(new OfflineException(OfflineStatus.STATUS_FAILED, e.getMessage(), e));
                    }
                } catch (RmsRestAPIException e) {
                    if (mCallback != null) {
                        mCallback.onTokenError(new OfflineException(OfflineStatus.STATUS_REST_API_EXCEPTION, e.getMessage(), e));
                    }
                } catch (TokenAccessDenyException e) {
                    if (mCallback != null) {
                        mCallback.onTokenError(new OfflineException(OfflineStatus.STATUS_TOKEN_ACCESS_DENY, e.getMessage(), e));
                    }
                }
            }
        });
    }

    private void dispatchRequest() {
        try {
            if (successor != null) {
                successor.handleRequest(mRequest);
            }
        } catch (OfflineException e) {
            mCallback.onTokenError(e);
        }
    }

    public interface ITokenHandlerCallback {
        void onTokenProcessed();

        void onTokenError(OfflineException e);
    }

    public static void reActiveToken(final Context ctx, final String nxlPath) {
        ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI).execute(new Runnable() {
            @Override
            public void run() {
                INxlClient client = SkyDRMApp.getInstance().getSession().getRmsClient();
                try {
                    client.updateOfflineStatus(nxlPath, true);
                } catch (final Exception e1) {
                    e1.printStackTrace();
                    CommonUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ctx != null) {
                                ToastUtil.showToast(ctx,
                                        ctx.getResources().getString(R.string.err_evaluate_denied));
                            }
                        }
                    });
                }
            }
        });
    }

    public static void reActiveToken(final Context ctx, final String nxlPath,
                                     final int sharedSpaceType, final int sharedSpaceId,
                                     final String sharedSpaceMembership) {
        ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI).execute(new Runnable() {
            @Override
            public void run() {
                INxlClient client = SkyDRMApp.getInstance().getSession().getRmsClient();
                try {
                    client.updateOfflineStatus(nxlPath,
                            sharedSpaceType, sharedSpaceId,
                            sharedSpaceMembership,
                            true);
                } catch (final Exception e1) {
                    e1.printStackTrace();
                    CommonUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (ctx != null) {
                                ToastUtil.showToast(ctx,
                                        ctx.getResources().getString(R.string.err_evaluate_denied));
                            }
                        }
                    });
                }
            }
        });
    }
}
