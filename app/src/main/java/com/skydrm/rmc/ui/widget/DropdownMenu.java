package com.skydrm.rmc.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.domain.UserAccount;

import java.util.ArrayList;
import java.util.List;

public class DropdownMenu extends LinearLayout {
    private TextView mTvSelected;
    private PopupWindow mBody;
    private List<Item> mItems = new ArrayList<>();
    private DropdownListAdapter mListAdapter;
    private int mPreviousSelectPosition = -1;
    private String mSelectUrl;

    public DropdownMenu(Context context) {
        this(context, null);
    }

    public DropdownMenu(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DropdownMenu(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        initDropdownHeader();
    }

    private void initDropdownHeader() {
        View header = LayoutInflater.from(getContext()).inflate(R.layout.layout_dropdown_header,
                this);
        mTvSelected = (TextView) header.findViewById(R.id.tv_selected);
        ImageView ivDown = (ImageView) header.findViewById(R.id.iv_down);
        ivDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDropdownBody();
            }
        });
        //paddingTestData();
    }

    private void displayDropdownBody() {
        if (mBody != null) {
            mBody.showAsDropDown(this);
        } else {
            View bodyContent = LayoutInflater.from(getContext()).inflate(R.layout.layout_dropdown_body,
                    null, false);
            ListView lvList = (ListView) bodyContent.findViewById(R.id.lv_list);
            mListAdapter = new DropdownListAdapter(mItems);
            lvList.setAdapter(mListAdapter);
            mBody = new PopupWindow(bodyContent, getWidth(), LayoutParams.WRAP_CONTENT);
            mBody.setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));
            mBody.setOutsideTouchable(true);
            mBody.showAsDropDown(this);
            lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectItem(position);
                    mBody.dismiss();
                }
            });
        }
    }

    private void selectItem(int position) {
        if (mItems != null && mItems.size() != 0) {
            if (mPreviousSelectPosition != -1) {
                mItems.get(mPreviousSelectPosition).checked = false;
            }
            Item item = mItems.get(position);
            item.checked = true;
            mSelectUrl = item.url;
            mTvSelected.setText(item.url);
            mPreviousSelectPosition = position;
        }
        //mListAdapter.notifyDataSetChanged();
    }

    public void setAccounts(List<UserAccount> accounts) {
        if (accounts != null && accounts.size() != 0) {
            for (UserAccount account : accounts) {
                if (account.getAccountType().equalsIgnoreCase(Constant.NAME_COMPANY_ACCOUNT)) {
                    translateItem(account.getAccountItems());
                }
            }
        }
    }

    private void translateItem(List<UserAccount.Item> accountItems) {
        if (accountItems == null) {
            return;
        }
        mItems.clear();
        if (accountItems.size() != 0) {
            for (int i = 0; i < accountItems.size(); i++) {
                UserAccount.Item item = accountItems.get(i);
                mItems.add(new Item(item.isSelected(), item.getAccount()));
                if (item.isSelected()) {
                    mPreviousSelectPosition = i;
                    mSelectUrl = item.getAccount();
                    mTvSelected.setText(item.getAccount());
                }
            }
            if (TextUtils.isEmpty(mTvSelected.getText().toString())) {
                mTvSelected.setText(accountItems.get(0).getAccount());
            }
        }
    }

    public String getSelectItem() {
        return mSelectUrl;
    }

    class DropdownListAdapter extends BaseAdapter {
        public List<Item> mItems;

        DropdownListAdapter(List<Item> items) {
            this.mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                View itemRoot = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_dropdown_item,
                        parent, false);
                holder = new ViewHolder();
                holder.llItem = (LinearLayout) itemRoot.findViewById(R.id.ll_item);
                holder.ivChecked = (ImageView) itemRoot.findViewById(R.id.iv_checked);
                holder.tvUrl = (TextView) itemRoot.findViewById(R.id.tv_url);
                convertView = itemRoot;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Item item = mItems.get(position);
            convertView.setSelected(item.checked);
            holder.llItem.setBackgroundResource(item.checked ? R.drawable.rect_dropdown_menu_item_bg_selected : R.drawable.rect_dropdown_menu_item_bg_normal);
            holder.ivChecked.setVisibility(item.checked ? VISIBLE : INVISIBLE);
            holder.tvUrl.setText(item.url);
            return convertView;
        }

        class ViewHolder {
            LinearLayout llItem;
            ImageView ivChecked;
            TextView tvUrl;
        }
    }

    public class Item {
        boolean checked;
        String url;

        public Item(boolean checked, String url) {
            this.checked = checked;
            this.url = url;
        }
    }
}
