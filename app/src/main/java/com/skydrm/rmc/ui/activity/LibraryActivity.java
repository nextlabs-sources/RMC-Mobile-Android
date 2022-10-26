package com.skydrm.rmc.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.domain.NXFileItem;
import com.skydrm.rmc.engine.enumData.CmdOperate;
import com.skydrm.rmc.engine.enumData.FileFrom;
import com.skydrm.rmc.engine.eventBusMsg.CommandOperateEvent;
import com.skydrm.rmc.presenter.ILibraryDataPresenter;
import com.skydrm.rmc.presenter.impl.LibraryDataPresenterImpl;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.adapter.LibraryFilesAdapter;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.widget.WrapperLinearLayoutManager;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.skydrm.rmc.ui.common.ContextMenu.ACTION_ADD;
import static com.skydrm.rmc.ui.common.ContextMenu.ACTION_ADD_PROJECT;
import static com.skydrm.rmc.ui.common.ContextMenu.ACTION_CREATE_NEW_FOLDER;
import static com.skydrm.rmc.ui.common.ContextMenu.ACTION_PROTECT;
import static com.skydrm.rmc.ui.common.ContextMenu.ACTION_SELECT_PATH;
import static com.skydrm.rmc.ui.common.ContextMenu.ACTION_SHARE;

/**
 * Created by hhu on 5/10/2017.
 */
@Deprecated
public class LibraryActivity extends BaseActivity {
    @BindView(R.id.back)
    ImageButton back;
    @BindView(R.id.display_path)
    TextView displayPath;
    @BindView(R.id.cancel)
    Button cancel;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.fl_container)
    FrameLayout flContainer;
    public static final String TAG = "LibraryActivity";
    public static final String KEY_CURRENT_PATH_ID = "CURRENT_PATH_ID";

    private BoundService boundService;
    private List<NXFileItem> mData = new ArrayList<>();
    private LibraryFilesAdapter filesAdapter;
    private ILibraryDataPresenter presenter;
    private int actionIntent;
    private NXFileItem selectItem;
    private View mEmptyFolderView;
    private IProject mProject;
    private String mCurrentPathId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        ButterKnife.bind(this);
        if (resolveIntent()) {
            initViewAndEvents();
        } else {
            finish();
        }
    }

    private void initViewAndEvents() {
        initView();
        initListener();
        initData();
    }

    private boolean resolveIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return false;
        }
        boundService = (BoundService) extras.getSerializable(Constant.BOUND_SERVICE);
        actionIntent = extras.getInt(Constant.CHOOSE_ACTION);

        if (actionIntent == ACTION_ADD_PROJECT) {
            mProject = extras.getParcelable(Constant.PROJECT_DETAIL);
            mCurrentPathId = extras.getString(KEY_CURRENT_PATH_ID);
        }

        return true;
    }

    private void initView() {
        initToolbar(boundService);
        mEmptyFolderView = LayoutInflater.from(this).inflate(R.layout.layout_empty_folder, null);
        flContainer.removeAllViews();
        flContainer.addView(recyclerView);
        flContainer.addView(mEmptyFolderView);
    }

    private void initToolbar(BoundService boundService) {
        if (boundService == null) {
            return;
        }
        displayPath.setText(boundService.getDisplayName());
        back.setVisibility(View.INVISIBLE);
        if (actionIntent == ACTION_SELECT_PATH) {
            cancel.setText(getString(R.string.select));
        } else if (actionIntent == ACTION_CREATE_NEW_FOLDER) {
            cancel.setText(getString(R.string.create));
        }
    }

    private void initListener() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancel.getText().toString().equalsIgnoreCase(getString(R.string.common_cancel_initcap))) {
                    finish();
                } else {
                    switch (actionIntent) {
                        case ACTION_SHARE:
                            if (selectItem != null) {
                                CommandOperateEvent eventMsg = new CommandOperateEvent(selectItem.getNXFile(), CmdOperate.COMMAND_SHARE_FROM_REPO);
                                EventBus.getDefault().postSticky(eventMsg);
                                startActivity(new Intent(LibraryActivity.this, CmdOperateFileActivity.class));
                                LibraryActivity.this.finish();
                            }
                            break;
                        case ACTION_PROTECT:
                            if (selectItem != null) {
                                CommandOperateEvent eventMsg = new CommandOperateEvent(selectItem.getNXFile(), CmdOperate.COMMAND_PROTECT_FROM_REPO);
                                EventBus.getDefault().postSticky(eventMsg);
                                startActivity(new Intent(LibraryActivity.this, CmdOperateFileActivity.class));
                                LibraryActivity.this.finish();

                            }
                            break;
                        case ACTION_ADD:
//                            if (selectItem != null) {
//                                CommandOperateMsg eventMsg = new CommandOperateMsg(selectItem.getNXFile(), CmdOperate.COMMAND_ADD);
//                                EventBus.getDefault().postSticky(eventMsg);
//                                startActivity(new Intent(LibraryActivity.this, CmdOperateFileActivity.class));
//                            }
                            break;

                        case ACTION_ADD_PROJECT:
                            if (selectItem != null) {

                                CommandOperateEvent.CommandProjectAddFrom3D event = new CommandOperateEvent.CommandProjectAddFrom3D(selectItem.getNXFile(),
                                        mProject, mCurrentPathId,
                                        FileFrom.FILE_FROM_PROJECT_PAGE, CmdOperate.COMMAND_PROJECT_ADD_FROM_3D);
                                EventBus.getDefault().postSticky(event);
                                startActivity(new Intent(LibraryActivity.this, CmdOperateFileActivity2.class));
                                LibraryActivity.this.finish();
                            }
                            break;

                        case ACTION_SELECT_PATH:
                            Intent intent = getIntent();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Constant.BOUND_SERVICE, boundService);
                            bundle.putSerializable(Constant.DEST_FOLDER, (Serializable) presenter.getDestFolder());
                            intent.putExtras(bundle);
                            setResult(RESULT_OK, intent);
                            finish();
                            break;
                        case ACTION_CREATE_NEW_FOLDER:
                            Intent newFolderIntent = new Intent();
                            newFolderIntent.setClass(LibraryActivity.this, CreateFolderActivity.class);
                            Bundle arguments = new Bundle();
                            arguments.putSerializable(Constant.BOUND_SERVICE, boundService);
                            arguments.putSerializable(Constant.DEST_FOLDER, (Serializable) presenter.getDestFolder());
                            newFolderIntent.putExtras(arguments);
                            startActivity(newFolderIntent);
                            finish();
                            break;
                    }
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backspace();
            }
        });
    }

    private void initData() {
        presenter = new LibraryDataPresenterImpl();
        recyclerView.setLayoutManager(new WrapperLinearLayoutManager(LibraryActivity.this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(false);
        filesAdapter = new LibraryFilesAdapter(LibraryActivity.this, mData);
        recyclerView.setAdapter(filesAdapter);
        filesAdapter.setOnItemClickListener(new LibraryFilesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(NXFileItem entity, int position) {
                try {
                    if (entity != null) {
                        if (position < 0) {
                            log.e("error" + position);
                            return;
                        }
                        if (entity.getNXFile().isFolder()) {
                            onEnterFolder(entity.getNXFile());
                        } else {
                            onSelectItem(entity);
                        }
                    }
                } catch (Exception e) {
                    log.e(e);
                }
            }
        });
        loadRootFiles(actionIntent);
    }

    public static final String filter = ".nxl";

    private void loadRootFiles(int actionIntent) {
        List<NXFileItem> nxFileItems;

        if (actionIntent == ACTION_ADD_PROJECT) {
            nxFileItems = presenter.loadFilterRoots(boundService, filter);
        } else {
            nxFileItems = presenter.loadRoots(boundService);
        }

        if (nxFileItems != null && nxFileItems.size() != 0) {
            mData.clear();

            mData.addAll(nxFileItems);

            filesAdapter.setActionIntent(actionIntent);

            notifyDataChanged();
        }
    }

    public void notifyDataChanged() {
        if (mData.size() == 0) {
            showCurrentView(mEmptyFolderView);
        } else {
            showCurrentView(recyclerView);
            filesAdapter.notifyDataSetChanged();
        }
    }

    public void showCurrentView(View view) {
        recyclerView.setVisibility(View.GONE);
        mEmptyFolderView.setVisibility(View.GONE);

        view.setVisibility(View.VISIBLE);
    }

    private void onSelectItem(NXFileItem entity) {
        if (entity.isSelected()) {
            switch (actionIntent) {
                case ACTION_SHARE:
                    cancel.setText(getString(R.string.share));
                    selectItem = entity;
                    break;
                case ACTION_PROTECT:
                    cancel.setText(getString(R.string.protect));
                    selectItem = entity;
                    break;
                case ACTION_ADD:
                    cancel.setText(getString(R.string.upload));
                    selectItem = entity;
                    break;
                case ACTION_ADD_PROJECT:
                    cancel.setText(getString(R.string.upload));
                    selectItem = entity;
                    break;
            }
        } else {
            cancel.setText(getString(R.string.common_cancel_initcap));
            selectItem = null;
        }
    }

    public void onEnterFolder(INxFile nxFile) {
        if (filesAdapter == null || mData == null || presenter == null || nxFile == null) {
            log.e("Error happened in Library Activity");
            return;
        }
        displayPath.setText(nxFile.getDisplayPath());
        if (actionIntent == ACTION_SELECT_PATH) {
            cancel.setText(getString(R.string.select));
        } else if (actionIntent == ACTION_CREATE_NEW_FOLDER) {
            cancel.setText(getString(R.string.create));
        } else {
            cancel.setText(getString(R.string.common_cancel_initcap));
        }
        back.setVisibility(View.VISIBLE);
        mData.clear();
        if (actionIntent == ACTION_ADD_PROJECT) {
            mData.addAll(presenter.getFilterChildren(nxFile, filter));
        } else {
            mData.addAll(presenter.getChildren(nxFile));
        }
        notifyDataChanged();
    }

    private void backspace() {
        if (filesAdapter == null || mData == null || presenter == null) {
            return;
        }
        mData.clear();
        if (actionIntent == ACTION_ADD_PROJECT) {
            mData.addAll(presenter.getFilterParentFile(filter));
        } else {
            mData.addAll(presenter.getParentFile());
        }
        notifyDataChanged();
        if (presenter.isRoot()) {
            if (actionIntent == ACTION_SELECT_PATH) {
                cancel.setText(getString(R.string.select));
            } else if (actionIntent == ACTION_CREATE_NEW_FOLDER) {
                cancel.setText(getString(R.string.create));
            } else {
                cancel.setText(getString(R.string.common_cancel_initcap));
            }
            back.setVisibility(View.INVISIBLE);
            displayPath.setText(boundService.getDisplayName());
        } else {
            displayPath.setText(presenter.getDisplayPath());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (!presenter.isRoot()) {
                backspace();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
