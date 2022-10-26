package com.skydrm.rmc.ui.widget.popupwindow;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.base.Utils;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.activity.CmdOperateFileActivity2;
import com.skydrm.rmc.ui.activity.CreateFolderActivity;
import com.skydrm.rmc.ui.activity.home.PhotographMsg;
import com.skydrm.rmc.ui.activity.repository.RepoSettingActivity;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.common.ContextMenu;
import com.skydrm.rmc.ui.project.service.NewProjectActivity;
import com.skydrm.rmc.ui.widget.RepoDisplayView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhu on 5/9/2017.
 */

public class HomeContextMenu2 extends BottomSheetDialogFragment {
    private Context mCtx;
    private ScrollView mSvMainContainer;
    private FrameLayout mFlSubContainer;

    private RepoDisplayView mRepoDisplayView;

    private int subContextContainerVisibility;
    private int createProjectSiteVisibility;

    public static HomeContextMenu2 newInstance() {
        return new HomeContextMenu2();
    }

    public void setSubContextVisibility(int visible) {
        subContextContainerVisibility = visible;
    }

    public void setCreateProjectSiteVisibility(int visible) {
        createProjectSiteVisibility = visible;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mCtx = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_myspace_context_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSvMainContainer = view.findViewById(R.id.ll_top);
        mFlSubContainer = view.findViewById(R.id.subview_container);

        View subContextContainer = view.findViewById(R.id.subContextContainer);
        View shareSite = view.findViewById(R.id.share_site);
        View protectSite = view.findViewById(R.id.protect_site);
        View connectSite = view.findViewById(R.id.connect_site);
        View createFolderSite = view.findViewById(R.id.create_new_folder_site);
        View addFileSite = view.findViewById(R.id.add_file_site);
        View scanDocSite = view.findViewById(R.id.scan_doc_site);
        View createProjectSite = view.findViewById(R.id.create_new_project);

        subContextContainer.setVisibility(subContextContainerVisibility);

        if (SkyDRMApp.getInstance().isOnPremise()) {
            createProjectSite.setVisibility(View.GONE);
            connectSite.setVisibility(View.GONE);
        } else {
            createProjectSite.setVisibility(createProjectSiteVisibility);
        }

        initRepoDisplayView();
        shareSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRepoDisplayViewVisibility(View.GONE, View.VISIBLE);
                mRepoDisplayView.setAction(ContextMenu.ACTION_SHARE);
                mRepoDisplayView.displayActionImageAndDesc(ContextMenu.ACTION_SHARE);
            }
        });
        protectSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRepoDisplayViewVisibility(View.GONE, View.VISIBLE);
                mRepoDisplayView.setAction(ContextMenu.ACTION_PROTECT);
                mRepoDisplayView.displayActionImageAndDesc(ContextMenu.ACTION_PROTECT);
            }
        });

        if (connectSite.getVisibility() != View.GONE) {
            connectSite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goConnectRepo();
                    dismiss();
                }
            });
        }

        createFolderSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRepoDisplayView.displayActionImageAndDesc(ContextMenu.ACTION_CREATE_NEW_FOLDER);
                createFolder();
                dismiss();
            }
        });
        addFileSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goAddFile();
                dismiss();
            }
        });
        scanDocSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanDoc();
                dismiss();
            }
        });
        if (createProjectSite.getVisibility() != View.GONE) {
            createProjectSite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goCreateProject();
                    dismiss();
                }
            });
        }
    }

    private void goConnectRepo() {
        Intent intent = new Intent();
        intent.setClass(mCtx, RepoSettingActivity.class);
        mCtx.startActivity(intent);
    }

    private void createFolder() {
        try {
            Intent newFolderIntent = new Intent();
            newFolderIntent.setAction(Constant.ACTION_NEW_FOLDER_FROM_HOME);
            newFolderIntent.setClass(mCtx, CreateFolderActivity.class);
            Bundle arguments = new Bundle();
            if (SkyDRMApp.getInstance().getRepoSystem().isInSyntheticRoot()) {
                List<BoundService> stockedNotSpoiledServiceInRepoSystem = SkyDRMApp
                        .getInstance()
                        .getRepoSystem()
                        .getStockedNotSpoiledServiceInRepoSystem();
                INxFile workingFolder = SkyDRMApp.getInstance().getRepoSystem().findWorkingFolder();
                for (BoundService boundService : stockedNotSpoiledServiceInRepoSystem) {
                    if (boundService.type.equals(BoundService.ServiceType.MYDRIVE)) {
                        arguments.putSerializable(Constant.BOUND_SERVICE, boundService);
                        break;
                    }
                }
                arguments.putSerializable(Constant.DEST_FOLDER, (Serializable) workingFolder);
                newFolderIntent.putExtras(arguments);
            } else {
                BoundService boundService = SkyDRMApp.getInstance().getRepoSystem().findWorkingFolder().getService();
                INxFile workingFolder = SkyDRMApp.getInstance().getRepoSystem().findWorkingFolder();
                arguments.putSerializable(Constant.BOUND_SERVICE, boundService);
                arguments.putSerializable(Constant.DEST_FOLDER, (Serializable) workingFolder);
                newFolderIntent.putExtras(arguments);
            }
            mCtx.startActivity(newFolderIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goAddFile() {
        if (mCtx == null) {
            dismiss();
            return;
        }
        if (!(mCtx instanceof BaseActivity)) {
            dismiss();
            return;
        }
        final BaseActivity baseActivity = (BaseActivity) mCtx;
        baseActivity.checkPermission(new BaseActivity.CheckPermissionListener() {
                                         @Override
                                         public void superPermission() {
                                             Intent intent = new Intent();
                                             intent.setClass(mCtx, CmdOperateFileActivity2.class);
                                             intent.setAction(Constant.ACTION_MYSPACE_ADD_FILE);
                                             mCtx.startActivity(intent);
                                         }

                                         @Override
                                         public void onPermissionDenied() {
                                             List<String> permission = new ArrayList<>();
                                             permission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                                             permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                             baseActivity.checkPermissionNeverAskAgain(null, permission);
                                         }
                                     }, R.string.permission_storage_rationale,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void scanDoc() {
        final Context launcher = mCtx;
        if (mCtx instanceof BaseActivity) {
            final BaseActivity baseActivity = (BaseActivity) launcher;
            baseActivity.checkPermission(new BaseActivity.CheckPermissionListener() {
                                             @Override
                                             public void superPermission() {
                                                 Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                 String name = System.currentTimeMillis() + ".jpg";
                                                 File photo = new File(getTmpMountPoint(), name);

                                                 cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(launcher,
                                                         mCtx.getPackageName() + ".fileprovider", photo));
                                                 cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                                                 ((Activity) launcher).startActivityForResult(cameraIntent, Constant.ALBUM_REQUEST_CODE);

                                                 EventBus.getDefault().post(new PhotographMsg(photo));
                                             }

                                             @Override
                                             public void onPermissionDenied() {
                                                 List<String> permission = new ArrayList<>();
                                                 permission.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                                                 permission.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                                 baseActivity.checkPermissionNeverAskAgain(null, permission);
                                             }
                                         }, R.string.permission_storage_rationale,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private File getTmpMountPoint() {
        try {
            return Utils.getTmpMountPoint();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void goCreateProject() {
        Intent intent = new Intent(mCtx, NewProjectActivity.class);
        mCtx.startActivity(intent);
    }

    private void initRepoDisplayView() {
        mRepoDisplayView = new RepoDisplayView(mCtx);
        mFlSubContainer.addView(mRepoDisplayView);
        mRepoDisplayView.setOnDismissListener(new RepoDisplayView.OnDismissListener() {
            @Override
            public void onDismiss() {
                dismiss();
            }
        });
        mRepoDisplayView.setOnBackButtonClickListener(new RepoDisplayView.OnBackButtonClickListener() {
            @Override
            public void onClick() {
                setRepoDisplayViewVisibility(View.VISIBLE, View.GONE);
            }
        });
        mRepoDisplayView.setOnDismissListener(new RepoDisplayView.OnDismissListener() {
            @Override
            public void onDismiss() {
                dismiss();
            }
        });
    }

    private void setRepoDisplayViewVisibility(int visible, int gone) {
        mSvMainContainer.setVisibility(visible);
        mFlSubContainer.setVisibility(gone);
    }
}
