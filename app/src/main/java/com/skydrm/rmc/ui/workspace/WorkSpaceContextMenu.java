package com.skydrm.rmc.ui.workspace;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.RepoFactory;
import com.skydrm.rmc.datalayer.repo.base.IBaseRepo;
import com.skydrm.rmc.datalayer.repo.base.RepoType;
import com.skydrm.rmc.datalayer.repo.base.Utils;
import com.skydrm.rmc.datalayer.repo.workspace.WorkSpaceRepo;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.activity.CreateFolderActivity;
import com.skydrm.rmc.ui.activity.home.PhotographMsg;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.base.BaseContextMenu;
import com.skydrm.rmc.ui.service.protect.ProtectActivity;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorkSpaceContextMenu extends BaseContextMenu {
    private String mCurrentPathId;

    public static WorkSpaceContextMenu newInstance() {
        return new WorkSpaceContextMenu();
    }

    public void setCurrentPathId(String pathId) {
        this.mCurrentPathId = pathId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_workspace_ctx_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout llCreateFolder = view.findViewById(R.id.ll_create_folder);
        LinearLayout llAddFile = view.findViewById(R.id.ll_add_file);
        LinearLayout llScanDoc = view.findViewById(R.id.ll_scan_doc);

        boolean isTenantAdmin = isTenantAdmin();

        llCreateFolder.setVisibility(isTenantAdmin ? View.VISIBLE : View.GONE);
        if (isVisible(llCreateFolder)) {
            llCreateFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lunchCreateFolderPage();
                }
            });
        }

        llAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunchAddFilePage();
            }
        });

        llScanDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanDoc();
            }
        });
    }

    private void lunchCreateFolderPage() {
        dismiss();

        Intent i = new Intent(mCtx, CreateFolderActivity.class);
        i.setAction(Constant.ACTION_NEW_FOLDER_FROM_WORKSPACE);

        if (TextUtils.isEmpty(mCurrentPathId)) {
            i.putExtra(Constant.CREATE_FOLDER_PARENT_PATH_ID, "/");
        } else {
            i.putExtra(Constant.CREATE_FOLDER_PARENT_PATH_ID, mCurrentPathId);
        }

        i.putExtra(Constant.CREATE_FOLDER_SERVICE, getService());
        mCtx.startActivity(i);

    }

    private void lunchAddFilePage() {
        dismiss();

        Intent i = new Intent(mCtx, ProtectActivity.class);
        i.putExtra(Constant.PROTECT_SERVICE, getService());
        i.putExtra(Constant.NAME_ROOT_PATH_ID, "/");
        i.putExtra(Constant.NAME_CURRENT_PATH_ID, mCurrentPathId);
        mCtx.startActivity(i);
    }

    private void scanDoc() {
        dismiss();

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

                                                 EventBus.getDefault().post(new PhotographMsg(photo, mCurrentPathId, getService()));
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

    private WorkSpaceRepo getService() {
        IBaseRepo repo = RepoFactory.getRepo(RepoType.TYPE_WORKSPACE);
        if (repo == null) {
            return null;
        }
        return (WorkSpaceRepo) repo;
    }

    private File getTmpMountPoint() {
        try {
            return Utils.getTmpMountPoint();
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
        return null;
    }
}
