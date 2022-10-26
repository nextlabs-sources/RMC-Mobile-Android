package com.skydrm.rmc.ui.activity.server;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.domain.UserAccount;
import com.skydrm.rmc.ui.activity.server.ChangeURLPopupWindow;
import com.skydrm.rmc.ui.activity.server.MsgUpdateUserAccount;
import com.skydrm.rmc.ui.activity.server.cache.AccountCache;
import com.skydrm.rmc.ui.activity.server.cache.ICacheCallback;
import com.skydrm.rmc.ui.adapter.UserAccountAdapter;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CompanyAccountListActivity extends BaseActivity {
    private List<UserAccount> mUsrAccounts = new ArrayList<>();
    private UserAccountAdapter mAccountAdapter;
    private LinearLayout mRootView;
    private AccountCache mAccountCache = new AccountCache();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_user_account);
        initData();
        initView();
        initListener();
    }

    private void initData() {
        mAccountCache.readCache(new ICacheCallback<UserAccount>() {
            @Override
            public void onCacheLoad(List<UserAccount> caches) {
                mUsrAccounts.clear();
                mUsrAccounts.addAll(caches);
                mAccountAdapter.notifyAllDataSetChanged();
            }
        });
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mRootView = findViewById(R.id.ll_root);
        RecyclerView recyclerView = findViewById(R.id.recyclerView_user_account);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAccountAdapter = new UserAccountAdapter(mUsrAccounts);
        recyclerView.setAdapter(mAccountAdapter);
    }

    private void initListener() {
        mAccountAdapter.setOnHeaderClickListener(new UserAccountAdapter.OnHeaderClickListener() {
            @Override
            public void onHeaderClick() {
                addURL();
            }
        });

        mAccountAdapter.setOnItemClickListener(new UserAccountAdapter.OnItemClickListener() {
            @Override
            public void onEdit(String type, String accountEmail) {
                editURL(type, accountEmail);
            }

            @Override
            public void onItemClick(String type, String accountEmail) {
                updateSelect(type, accountEmail);
            }
        });
    }

    private void updateSelect(String type, String url) {
        for (UserAccount account : mUsrAccounts) {
            if (TextUtils.equals(account.getAccountType(), type)) {
                List<UserAccount.Item> accountItems = account.getAccountItems();
                for (UserAccount.Item item : accountItems) {
                    if (item.isSelected()) {
                        item.setSelected(false);
                    }
                    if (TextUtils.equals(item.getAccount(), url)) {
                        item.setSelected(true);
                    }
                }
            }
        }
        mAccountAdapter.notifyAllDataSetChanged();
    }

    private void addURL() {
        ChangeURLPopupWindow popupWindow = new ChangeURLPopupWindow(this, ChangeURLPopupWindow.Action.ADD);
        popupWindow.setOnURLAddListener(new ChangeURLPopupWindow.IAddURLListener() {
            @Override
            public void onURLAdd(String url, boolean select) {
                addIntoCache(url, select);
            }
        });
        popupWindow.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
    }

    private void addIntoCache(String url, boolean select) {
        if (mUsrAccounts == null) {
            return;
        }
        if (mUsrAccounts.size() == 0) {
            List<UserAccount.Item> items = new ArrayList<>();
            items.add(new UserAccount.Item(select, url));
            mUsrAccounts.add(new UserAccount(Constant.NAME_COMPANY_ACCOUNT, items));
        } else {
            for (UserAccount account : mUsrAccounts) {
                if (TextUtils.equals(account.getAccountType(), Constant.NAME_COMPANY_ACCOUNT)) {
                    if (account.contains(url)) {
                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.hint_the_url_existed));
                        break;
                    }
                    if (select) {
                        List<UserAccount.Item> accountItems = account.getAccountItems();
                        if (accountItems == null) {
                            List<UserAccount.Item> accounts = new ArrayList<>();
                            accounts.add(new UserAccount.Item(true, url));
                            mUsrAccounts.add(new UserAccount(Constant.NAME_COMPANY_ACCOUNT, accounts));
                        } else {
                            if (accountItems.size() != 0) {
                                for (UserAccount.Item item : accountItems) {
                                    if (item.isSelected()) {
                                        item.setSelected(false);
                                    }
                                }
                            }
                            accountItems.add(new UserAccount.Item(true, url));
                        }
                    } else {
                        List<UserAccount.Item> accountItems = account.getAccountItems();
                        if (accountItems != null) {
                            accountItems.add(new UserAccount.Item(false, url));
                        } else {
                            List<UserAccount.Item> accounts = new ArrayList<>();
                            accounts.add(new UserAccount.Item(false, url));
                            mUsrAccounts.add(new UserAccount(Constant.NAME_COMPANY_ACCOUNT, accounts));
                        }
                    }
                }
            }
        }

        mAccountAdapter.notifyAllDataSetChanged();
    }

    private void editURL(final String type, String accountEmail) {
        ChangeURLPopupWindow popupWindow = new ChangeURLPopupWindow(this, ChangeURLPopupWindow.Action.EDIT);
        popupWindow.setEditURL(accountEmail);
        popupWindow.setOnURLEditListener(new ChangeURLPopupWindow.IEditURLListener() {
            @Override
            public void onURLEdit(String oldOne, String newOne) {
                updateCache(type, oldOne, newOne);
            }

            @Override
            public void onURLDelete(String deleteOne) {
                clearFromCache(type, deleteOne);
            }
        });
        popupWindow.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
    }

    private void updateCache(String type, String oldOne, String newOne) {
        if (mUsrAccounts == null || mUsrAccounts.size() == 0) {
            return;
        }
        for (UserAccount account : mUsrAccounts) {
            if (TextUtils.equals(account.getAccountType(), type)) {
                List<UserAccount.Item> accountItems = account.getAccountItems();
                for (UserAccount.Item item : accountItems) {
                    if (TextUtils.equals(oldOne, item.getAccount())) {
                        item.setAccount(newOne);
                    }
                }
            }
        }
        mAccountAdapter.notifyAllDataSetChanged();
    }

    private void clearFromCache(String type, String deleteOne) {
        if (mUsrAccounts == null || mUsrAccounts.size() == 0) {
            return;
        }
        Iterator<UserAccount> oit = mUsrAccounts.iterator();
        while (oit.hasNext()) {
            UserAccount onext = oit.next();
            if (TextUtils.equals(onext.getAccountType(), type)) {
                List<UserAccount.Item> accountItems = onext.getAccountItems();
                Iterator<UserAccount.Item> iit = accountItems.iterator();
                while (iit.hasNext()) {
                    UserAccount.Item inext = iit.next();
                    if (TextUtils.equals(inext.getAccount(), deleteOne)) {
                        iit.remove();
                        break;
                    }
                }
                if (accountItems.size() == 0) {
                    oit.remove();
                    break;
                }
            }
        }
        if (mUsrAccounts.size() == 0) {
            finish();
        } else {
            mAccountAdapter.notifyAllDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(new MsgUpdateUserAccount(mUsrAccounts));
        mAccountCache.writeCache(mUsrAccounts);
    }
}
