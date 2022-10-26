package com.skydrm.rmc.ui.widget;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.activity.CmdOperateFileActivity2;
import com.skydrm.rmc.ui.activity.LibraryActivity;
import com.skydrm.rmc.ui.adapter.LibrarySelectAdapter;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.common.ContextMenu;

import java.util.ArrayList;
import java.util.List;


public class RepoDisplayView extends LinearLayout {
    private Context mCtx;
    private List<BoundService> mValidServices = new ArrayList<>();

    private IProject mProject;

    private String mRootPathId = "/";
    private String mCurrentPathId;

    private int mAction;
    private OnBackButtonClickListener mListener;
    private OnDismissListener mDismissListener;
    private ImageView mActionImage;
    private TextView mActionDesc;
    private View mLibrarySite;
    private TextView mActionHint;

    public RepoDisplayView(Context context) {
        this(context, null);
    }

    public RepoDisplayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RepoDisplayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mCtx = context;
        initContext();
    }

    public void setOnBackButtonClickListener(OnBackButtonClickListener listener) {
        this.mListener = listener;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.mDismissListener = listener;
    }

    public void setAction(@ContextMenu.ChooseAction int action) {
        this.mAction = action;
    }

    public void setProject(IProject p) {
        this.mProject = p;
    }

    public void setRootPathId(String rootPathId) {
        this.mRootPathId = rootPathId;
    }

    public void setCurrentPathId(String currentPathId) {
        this.mCurrentPathId = currentPathId;
    }

    public void displayActionImageAndDesc(int action) {
        if (action == ContextMenu.ACTION_SHARE) {
            mActionImage.setImageResource(R.drawable.icon_share3);
            mActionDesc.setText(mCtx.getString(R.string.share_from_library));
        } else if (action == ContextMenu.ACTION_PROTECT) {
            mActionImage.setImageResource(R.drawable.icon_protect3);
            mActionDesc.setText(mCtx.getString(R.string.protect_from_library));
        } else if (action == ContextMenu.ACTION_ADD) {
            mLibrarySite.setVisibility(GONE);
            mActionImage.setImageResource(R.drawable.icon_add_file3);
            mActionDesc.setText(mCtx.getString(R.string.upload_from_library));
        } else if (action == ContextMenu.ACTION_CREATE_NEW_FOLDER) {
            mActionHint.setText(mCtx.getString(R.string.choose_connected_repo_create_folder));
            mActionImage.setImageResource(R.drawable.icon_add_folder3);
            mActionDesc.setText(mCtx.getString(R.string.add_foler_to_library));
        }
    }

    private void initContext() {
        setOrientation(VERTICAL);
        View root = LayoutInflater.from(getContext()).inflate(R.layout.layout_repo_display, this);
        View backToMenu = root.findViewById(R.id.back_to_menu);
        backToMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick();
                }
            }
        });
        mLibrarySite = root.findViewById(R.id.ll_image_container);

        mActionHint = root.findViewById(R.id.action_hint);
        mActionImage = root.findViewById(R.id.action_image);
        mActionDesc = root.findViewById(R.id.action_desc);
        mActionImage.setImageResource(R.drawable.icon_share3);
        mActionDesc.setText(R.string.share_from_library);

        ListView listview = root.findViewById(R.id.list_view);
        mValidServices.clear();
        mValidServices.addAll(getFilteredBoundServiceByAccountType());
        LibrarySelectAdapter adapter = new LibrarySelectAdapter(getContext(),
                R.layout.item_library_select, mValidServices);
        listview.setAdapter(adapter);

        mLibrarySite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionThenLunch();
                dismiss();
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lunch2LibraryActivity(position, mAction);
                dismiss();
            }
        });
    }

    private List<BoundService> getFilteredBoundServiceByAccountType() {
        List<BoundService> retVal = new ArrayList<>();
        SkyDRMApp app = SkyDRMApp.getInstance();
        List<BoundService> userLinkedRepos = app.getRepoSystem().getStockedNotSpoiledServiceInRepoSystem();
        if (userLinkedRepos == null || userLinkedRepos.size() == 0) {
            return retVal;
        }
        for (BoundService service : userLinkedRepos) {
            if (service == null) {
                continue;
            }
            if (!service.isValidRepo()) {
                continue;
            }
            if (service.type == BoundService.ServiceType.MYDRIVE) {
                retVal.add(service);
            } else {
                if (!app.isOnPremise()) {
                    retVal.add(service);
                }
            }
        }
        return retVal;
    }

    private void checkPermissionThenLunch() {
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
                                             lunchByAction(mAction);
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

    private void lunchByAction(int action) {
        if (action == ContextMenu.ACTION_SHARE) {
            Intent intent = new Intent(mCtx, CmdOperateFileActivity2.class);
            intent.setAction(Constant.ACTION_SHARE_FROM_LIBRARY);
            mCtx.startActivity(intent);
        }
        if (action == ContextMenu.ACTION_PROTECT) {
            Intent intent = new Intent(mCtx, CmdOperateFileActivity2.class);
            intent.setAction(Constant.ACTION_PROTECT_FROM_LIBRARY);
            mCtx.startActivity(intent);
        }
    }

    private void lunch2LibraryActivity(int position, int action) {
        if (action == ContextMenu.ACTION_SHARE) {
            Intent intent = new Intent(mCtx, CmdOperateFileActivity2.class);
            intent.setAction(Constant.ACTION_SHARE_FROM_REPO);
            intent.putExtra(Constant.BOUND_SERVICE, mValidServices.get(position));
            mCtx.startActivity(intent);
        }
        if (action == ContextMenu.ACTION_PROTECT) {
            Intent intent = new Intent(mCtx, CmdOperateFileActivity2.class);
            intent.setAction(Constant.ACTION_PROTECT_FROM_REPO);
            intent.putExtra(Constant.BOUND_SERVICE, mValidServices.get(position));
            mCtx.startActivity(intent);
        }
    }

    private void dismiss() {
        if (mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    public interface OnBackButtonClickListener {
        void onClick();
    }

    public interface OnDismissListener {
        void onDismiss();
    }
}
