package com.skydrm.rmc.ui.myspace.myvault.view.interactor;

import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.base.NxlDoc;
import com.skydrm.rmc.datalayer.repo.myvault.MyVaultFile;
import com.skydrm.rmc.datalayer.repo.sharedwithme.SharedWithMeFile;
import com.skydrm.rmc.engine.eventBusMsg.RecipientsUpdateEvent;
import com.skydrm.rmc.ui.service.fileinfo.FileInfoTask;
import com.skydrm.rmc.ui.base.LoadTask;
import com.skydrm.rmc.ui.myspace.myvault.view.task.GetMyVaultMetadataTask;
import com.skydrm.rmc.ui.myspace.myvault.view.task.ShareTask;
import com.skydrm.rmc.ui.myspace.myvault.view.task.UpdateRecipientsTask;
import com.skydrm.rmc.ui.myspace.sharewithme.ReShareTask;
import com.skydrm.sdk.rms.rest.myVault.MyVaultMetaDataResult;
import com.skydrm.sdk.rms.rest.myVault.UpdateRecipientsResult;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class VaultFileSharePresenter implements IVaultFileShareContact.IPresenter {
    private INxlFile mFile;
    private IVaultFileShareContact.IView mView;

    public VaultFileSharePresenter(INxlFile f, IVaultFileShareContact.IView v) {
        this.mFile = f;
        this.mView = v;
    }

    @Override
    public void getMetadata() {
        new GetMyVaultMetadataTask((MyVaultFile) mFile, new LoadTask.ITaskCallback<GetMyVaultMetadataTask.Result, Exception>() {
            @Override
            public void onTaskPreExecute() {

            }

            @Override
            public void onTaskExecuteSuccess(GetMyVaultMetadataTask.Result results) {
                if (mView == null) {
                    return;
                }
                MyVaultMetaDataResult result = results.result;
                if (result == null) {
                    return;
                }
                MyVaultMetaDataResult.ResultsBean results1 = result.getResults();
                if (results1 == null) {
                    return;
                }
                MyVaultMetaDataResult.ResultsBean.DetailBean detail = results1.getDetail();
                if (detail == null) {
                    return;
                }
                List<String> rights = detail.getRights();
                MyVaultMetaDataResult.ResultsBean.DetailBean.ValidityBean validity = detail.getValidity();
                if (validity == null) {
                    return;
                }
                mView.updateRightsAndValidity(rights, validity.getStartDate(), validity.getEndDate());
            }

            @Override
            public void onTaskExecuteFailed(Exception e) {
                if (mView != null) {
                    mView.showErrorView(e);
                }
            }
        }).run();
    }

    @Override
    public void getFingerPrint() {
        new FileInfoTask((NxlDoc) mFile, new LoadTask.ITaskCallback<FileInfoTask.Result, Exception>() {
            @Override
            public void onTaskPreExecute() {
                if (mView != null) {
                    mView.setLoadingRightsView(true);
                }
            }

            @Override
            public void onTaskExecuteSuccess(FileInfoTask.Result results) {
                if (mView != null) {
                    mView.setLoadingRightsView(false);
                }
                if (mView != null) {
                    mView.updateRightsAndValidity(results.fp);
                }
            }

            @Override
            public void onTaskExecuteFailed(Exception e) {
                if (mView != null) {
                    mView.setLoadingRightsView(false);
                }
                if (mView != null) {
                    mView.showErrorView(e);
                }
            }
        }).run();
    }

    @Override
    public void share(List<String> rights, List<String> emails, String cmt) {
        ShareTask t = new ShareTask((MyVaultFile) mFile, rights, emails,
                new LoadTask.ITaskCallback<ShareTask.Result, Exception>() {
                    @Override
                    public void onTaskPreExecute() {
                        if (mView != null) {
                            mView.setShareIndicator(true);
                        }
                    }

                    @Override
                    public void onTaskExecuteSuccess(ShareTask.Result results) {
                        if (mView != null) {
                            mView.setShareIndicator(false);
                        }
                        if (mView != null) {
                            mView.showShareSuccessView();
                        }
                    }

                    @Override
                    public void onTaskExecuteFailed(Exception e) {
                        if (mView != null) {
                            mView.setShareIndicator(false);
                        }
                        if (mView != null) {
                            mView.showErrorView(e);
                        }
                    }
                });
        t.setComment(cmt);
        t.run();
    }

    @Override
    public void reShare(List<String> emails, String cmt) {
        new ReShareTask((SharedWithMeFile) mFile, emails, cmt, new LoadTask.ITaskCallback<LoadTask.IResult, Exception>() {
            @Override
            public void onTaskPreExecute() {

            }

            @Override
            public void onTaskExecuteSuccess(LoadTask.IResult result) {
                ReShareTask.Result r = (ReShareTask.Result) result;
                if (mView != null) {
                    if (r == null) {
                        mView.showReShareSuccessView(null, null);
                    } else {
                        mView.showReShareSuccessView(r.mNewSharedEmails, r.mAlreadySharedEmails);
                    }
                }
            }

            @Override
            public void onTaskExecuteFailed(final Exception e) {
                if (mView != null) {
                    mView.showErrorView(e);
                }
            }
        }).run();
    }

    @Override
    public void updateRecipients(List<String> added, List<String> removed, String cmt) {
        UpdateRecipientsTask t = new UpdateRecipientsTask((MyVaultFile) mFile, added, removed,
                new LoadTask.ITaskCallback<UpdateRecipientsTask.Result, Exception>() {
                    @Override
                    public void onTaskPreExecute() {
                        if (mView != null) {
                            mView.setShareIndicator(true);
                        }
                    }

                    @Override
                    public void onTaskExecuteSuccess(UpdateRecipientsTask.Result results) {
                        if (mView != null) {
                            mView.setShareIndicator(false);
                        }
                        UpdateRecipientsResult r = results.mResult;
                        if (r == null) {
                            return;
                        }
                        UpdateRecipientsResult.ResultsBean results1 = r.getResults();
                        if (results1 == null) {
                            return;
                        }
                        List<String> added = results1.getNewRecipients();
                        List<String> removed = results1.getRemovedRecipients();

                        if (null == added || added.size() == 0 &&
                                removed != null && removed.size() != 0) {
                            EventBus.getDefault().postSticky(new RecipientsUpdateEvent(added, removed, true));
                        }
                        if (null == removed || removed.size() == 0 && added != null && added.size() != 0) {
                            EventBus.getDefault().postSticky(new RecipientsUpdateEvent(added, removed, false));
                        }

                        if (mView != null) {
                            mView.updateRecipients(added, removed);
                        }
                    }

                    @Override
                    public void onTaskExecuteFailed(Exception e) {
                        if (mView != null) {
                            mView.setShareIndicator(false);
                        }
                        if (mView != null) {
                            mView.showErrorView(e);
                        }
                    }
                });
        t.setComments(cmt);
        t.run();
    }

    @Override
    public void onDestroy() {
        if (mView != null) {
            mView = null;
        }
    }
}
