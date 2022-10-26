package com.skydrm.rmc.ui.service.contact;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.service.contact.impl.AddressBookAdapter;
import com.skydrm.rmc.ui.service.contact.impl.ContactLoader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.sure)
    Button mBtSure;
    @BindView(R.id.recycler_view)
    RecyclerView mContactView;

    private static final int LOADER_ID = 1;
    private LoaderManager mLoaderManager;
    private AddressBookAdapter mAddressBookAdapter;

    private SparseArray<List<String>> mSelectedEmails = new SparseArray<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        ButterKnife.bind(this);

        initView();
        checkPermissionThenLoadContacts();
    }

    private void initView() {
        mContactView.setLayoutManager(new LinearLayoutManager(this));
        mAddressBookAdapter = new AddressBookAdapter();
        mContactView.setAdapter(mAddressBookAdapter);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBtSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResultToRequester();
            }
        });
        mAddressBookAdapter.setOnItemCheckChangeListener(new AddressBookAdapter.OnItemCheckChangeListener() {
            @Override
            public void onCheckChanged(int pos, boolean checked, String email) {
                if (checked) {
                    if (!containsEmails(pos, email)) {
                        List<String> emails = mSelectedEmails.get(pos);
                        if (emails == null || emails.isEmpty()) {
                            List<String> newContainer = new ArrayList<>();
                            newContainer.add(email);
                            mSelectedEmails.put(pos, newContainer);
                        } else {
                            emails.add(email);
                        }
                    }
                } else {
                    if (containsEmails(pos, email)) {
                        List<String> emails = mSelectedEmails.get(pos);
                        emails.remove(email);
                    }
                }
            }
        });
    }

    private boolean containsEmails(int pos, String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        List<String> emails = mSelectedEmails.get(pos);
        if (emails == null || emails.isEmpty()) {
            return false;
        }
        return emails.contains(email);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ContactLoader.newInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAddressBookAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void checkPermissionThenLoadContacts() {
        mLoaderManager = getSupportLoaderManager();
        checkPermission(new BaseActivity.CheckPermissionListener() {
                            @Override
                            public void superPermission() {
                                loadContacts();
                            }

                            @Override
                            public void onPermissionDenied() {
                                List<String> permission = new ArrayList<>();
                                permission.add(Manifest.permission.READ_CONTACTS);
                                checkPermissionNeverAskAgain(null, permission);
                            }
                        }, R.string.permission_contacts_rationale,
                Manifest.permission.READ_CONTACTS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLoaderManager.destroyLoader(LOADER_ID);
    }

    private void loadContacts() {
        mLoaderManager.initLoader(LOADER_ID, null, this);
    }

    private void setResultToRequester() {
        Intent parcel = new Intent();
        parcel.putExtra(Constant.SELECT_EMAIL_RESULT, (Serializable) getParcelEmails());
        setResult(RESULT_OK, parcel);
        finish();
    }

    private Set<String> getParcelEmails() {
        Set<String> ret = new HashSet<>();
        if (mSelectedEmails.size() == 0) {
            return ret;
        }

        for (int i = 0; i < mSelectedEmails.size(); i++) {
            List<String> emails = mSelectedEmails.valueAt(i);
            if (emails != null && emails.size() != 0) {
                ret.addAll(emails);
            }
        }
        return ret;
    }
}
