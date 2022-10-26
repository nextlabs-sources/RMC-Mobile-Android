package com.skydrm.rmc.ui.service.protect;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.base.INxlFile;
import com.skydrm.rmc.datalayer.repo.library.LibraryFile;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.reposystem.types.INxFile;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.base.NxlBaseFragment;
import com.skydrm.rmc.ui.repository.feature.LibraryFileFragment;
import com.skydrm.rmc.ui.repository.feature.RepoFileFragment;
import com.skydrm.rmc.ui.repository.feature.RepoSelectFragment;
import com.skydrm.rmc.ui.service.protect.file.LocalProtectFile;
import com.skydrm.rmc.ui.service.protect.file.MySpaceProtectFile;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

import java.io.File;


public class ProtectActivity extends BaseActivity {
    private static final int REQUEST_CODE_SELECT_FILE = 0x100;
    private static final String TAG = ProtectActivity.class.getSimpleName();

    private IProtectService mService;
    private String mCurrentPathId;
    private String mAction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protect);

        loadRepoSelectFragAsRoot(getIntent());
        syncTenantPreferences();
    }

    private void loadRepoSelectFragAsRoot(Intent intent) {
        mAction = intent.getAction();
        final IProtectService service = intent.getParcelableExtra(Constant.PROTECT_SERVICE);
        final String rootPathId = intent.getStringExtra(Constant.NAME_ROOT_PATH_ID);
        final String currentPathId = intent.getStringExtra(Constant.NAME_CURRENT_PATH_ID);

        if (isProtectFromLibrary()) {
            openMediaStore(service, rootPathId, currentPathId);
            return;
        }

        RepoSelectFragment frag = RepoSelectFragment.newInstance();
        frag.setOnRepoItemClickListener(new RepoSelectFragment.OnRepoItemClickListener() {
            @Override
            public void onLibraryItemClick() {
                openMediaStore(service, rootPathId, currentPathId);
            }

            @Override
            public void onRepoItemClick(BoundService s) {
                replaceLoadRepoFileFragment(service, s, currentPathId);
            }
        });

        loadRootFragment(R.id.fl_container, frag);
    }

    private boolean isProtectFromLibrary() {
        return Constant.ACTION_PROTECT_FROM_LIBRARY.equals(mAction);
    }
//    private void replaceLoadLibraryFileFrag(final IProtectService service,
//                                            String rootPathId,
//                                            final String currentPathId) {
//        LibraryFileFragment frag = LibraryFileFragment.newInstance();
//        frag.setOnItemClickListener(new LibraryFileFragment.OnItemClickListener() {
//            @Override
//            public void onItemClick(INxlFile f, int pos) {
//                replaceLoadProtectFileFragment(service, f, currentPathId);
//            }
//        });
//        frag.setOnPopupFragmentListener(new LibraryFileFragment.OnPopupFragmentListener() {
//            @Override
//            public void onPopup() {
//                popup();
//            }
//        });
//        frag.setUserVisibleHint(true);
//        replaceLoadRootFragment(R.id.fl_container, frag, true);
//    }

    public void openMediaStore(final IProtectService service,
                               String rootPathId,
                               final String currentPathId) {
        this.mService = service;
        this.mCurrentPathId = currentPathId;
        Intent chooser = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getRootDirectory().getPath());
        chooser.addCategory(Intent.CATEGORY_OPENABLE);
        chooser.setDataAndType(uri, "*/*");
        try {
            startActivityForResult(chooser, REQUEST_CODE_SELECT_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            ToastUtil.showToast(this, "Please install a File Manager.");
        }
    }

    private void replaceLoadRepoFileFragment(final IProtectService service,
                                             BoundService s,
                                             final String pathId) {
        RepoFileFragment frag = RepoFileFragment.newInstance();
        frag.setOnRepoFileItemClickListener(new RepoFileFragment.OnRepoFileItemClickListener() {
            @Override
            public void onItemClick(INxFile f) {
                replaceLoadProtectFileFragment(service, f, pathId);
            }
        });
        frag.setOnPopupFragmentListener(new RepoFileFragment.OnPopupFragmentListener() {
            @Override
            public void onPopup() {
                popup();
            }
        });
        Bundle args = new Bundle();
        args.putSerializable(Constant.BOUND_SERVICE, s);
        frag.setArguments(args);

        frag.setUserVisibleHint(true);
        replaceLoadRootFragment(R.id.fl_container, frag, true);
    }

    private void replaceLoadProtectFileFragment(IProtectService service, INxlFile f, String pathId) {
        replaceLoadProtectFileFragment(service, (IProtectFile) f, pathId);
    }

    private void replaceLoadProtectFileFragment(IProtectService service, INxFile f, String pathId) {
        replaceLoadProtectFileFragment(service, makeFile(f), pathId);
    }

    private void replaceLoadProtectFileFragment(IProtectService service, IProtectFile f, String pathId) {
        ProtectFragment frag = ProtectFragment.newInstance();

        Bundle args = new Bundle();
        args.putParcelable(Constant.PROTECT_SERVICE, (Parcelable) service);
        args.putParcelable(Constant.PROTECT_FILE_ENTRY, (Parcelable) f);
        args.putString(Constant.NAME_CURRENT_PATH_ID, pathId);
        frag.setArguments(args);

        if (isProtectFromLibrary()) {
            loadRootFragment(R.id.fl_container, frag);
        } else {
            replaceLoadRootFragment(R.id.fl_container, frag, true);
        }
    }

    private IProtectFile makeFile(INxFile f) {
        return new MySpaceProtectFile(f);
    }

    private IProtectFile makeFile(INxlFile f) {
        return new LocalProtectFile(new File(f.getPathId()));
    }

    public void popup() {
        popupFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_FILE) {
                // The result data contains a URI for the document or directory that
                // the user selected.
                Uri uri;
                if (data != null) {
                    uri = data.getData();

                    if (mService != null && uri != null) {
                        replaceLoadProtectFileFragment(mService, createNxlFile(uri), mCurrentPathId);
                    }
                }
            }
        }
    }

    private INxlFile createNxlFile(Uri uri) {
        if (uri == null) {
            return null;
        }
        try (Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null, null)) {
            // moveToFirst() returns false if the cursor has 0 rows. Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {
                // Note it's called "Display Name". This is
                // provider-specific, and might not necessarily be the file name.
                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null. But because an
                // int can't be null, the behavior is implementation-specific,
                // and unpredictable. So as
                // a rule, check if it's null before assigning to an int. This will
                // happen often: The storage API allows for remote files, whose
                // size might not be locally known.
                String size;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "-1";
                }

                long lstModifiedTime;
                int lstModifiedIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
                if (!cursor.isNull(lstModifiedIdx)) {
                    lstModifiedTime = cursor.getLong(lstModifiedIdx);
                } else {
                    lstModifiedTime = -1;
                }
                String path = uri.getPath();
                LibraryFile result = new LibraryFile(displayName, "", Long.parseLong(size),
                        "", path, path,
                        path, false, false,
                        -1, -1, -1,
                        lstModifiedTime, lstModifiedTime);
                result.setUri(uri);
                return result;
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        ProtectFragment protectFrag = findChildFragment(ProtectFragment.class.getSimpleName());
        if (protectFrag == null) {
            NxlBaseFragment nxlBaseFra = findChildFragment(LibraryFileFragment.class.getSimpleName());
            if (nxlBaseFra == null) {
                RepoFileFragment repoFileFrag = findChildFragment(RepoFileFragment.class.getSimpleName());
                if (repoFileFrag == null) {
                    super.onBackPressed();
                } else {
                    if (repoFileFrag.needIntercept()) {
                        repoFileFrag.intercept();
                    } else {
                        super.onBackPressed();
                    }
                }
            } else {
                if (nxlBaseFra.needInterceptBackPress()) {
                    nxlBaseFra.interceptBackPress();
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            if (protectFrag.needInterceptBackPress()) {
                protectFrag.interceptBackPress();
            } else {
                super.onBackPressed();
            }
        }
    }
}
