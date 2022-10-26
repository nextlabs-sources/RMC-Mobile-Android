package com.skydrm.rmc.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.UserAccount;
import com.skydrm.rmc.ui.base.BaseAdapter;

import org.w3c.dom.Text;

import java.util.List;

public class UserAccountAdapter extends BaseAdapter {
    private List<UserAccount> mUsrAccounts;

    public UserAccountAdapter(List<UserAccount> accounts) {
        this.mUsrAccounts = accounts;
    }

    @Override
    public int getNumberOfSections() {
        return mUsrAccounts.size();
    }

    @Override
    public int getNumberOfItemsInSection(int sectionIndex) {
        return mUsrAccounts.get(sectionIndex).getAccountItems().size();
    }

    @Override
    public RecyclerHeaderViewHolder onCreateRecyclerHeaderViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usr_account_header,
                parent, false));
    }

    @Override
    public RecyclerFooterViewHolder onCreateRecyclerFooterViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerFootViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usr_account_footer,
                parent, false));
    }

    @Override
    public SectionHeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        return new SecHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usr_sec_header,
                parent, false));
    }

    @Override
    public SectionItemViewHolder onCreateSectionItemViewHolder(ViewGroup parent, int viewType) {
        return new SecItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usr_sec_item,
                parent, false));
    }

    class RecyclerHeaderViewHolder extends BaseAdapter.RecyclerHeaderViewHolder {

        RecyclerHeaderViewHolder(View itemView) {
            super(itemView);
            itemView.findViewById(R.id.tv_add_usr_account).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHeaderClickListener != null) {
                        mHeaderClickListener.onHeaderClick();
                    }
                }
            });
        }
    }

    class RecyclerFootViewHolder extends BaseAdapter.RecyclerFooterViewHolder {

        RecyclerFootViewHolder(View itemView) {
            super(itemView);
        }
    }

    class SecHeaderViewHolder extends BaseAdapter.SectionHeaderViewHolder {

        private final TextView mTvSecHeader;

        SecHeaderViewHolder(View itemView) {
            super(itemView);
            mTvSecHeader = (TextView) itemView.findViewById(R.id.tv_sec_header);
        }
    }

    class SecItemViewHolder extends BaseAdapter.SectionItemViewHolder {
        private View mItemView;
        private final ImageView mIvChecked;
        private final TextView mTvUsrAccount;
        private final TextView mtvEdit;

        SecItemViewHolder(View itemView) {
            super(itemView);
            this.mItemView = itemView;
            mIvChecked = (ImageView) itemView.findViewById(R.id.iv_checked);
            mTvUsrAccount = (TextView) itemView.findViewById(R.id.tv_usr_account);
            mtvEdit = (TextView) itemView.findViewById(R.id.tv_edit);
        }

        public void setOnItemClickListener() {

        }
    }

    @Override
    public void onBindSectionItemViewHolder(SectionItemViewHolder sectionItemViewHolder, int position,
                                            final int sectionIndex, int positionInSection) {
        SecItemViewHolder itemViewHolder = (SecItemViewHolder) sectionItemViewHolder;
        final UserAccount.Item item = mUsrAccounts.get(sectionIndex).getAccountItems().get(positionInSection);
        itemViewHolder.mIvChecked.setVisibility(item.isSelected() ? View.VISIBLE : View.INVISIBLE);
        itemViewHolder.mTvUsrAccount.setText(item.getAccount());

        itemViewHolder.mtvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onEdit(mUsrAccounts.get(sectionIndex).getAccountType(), item.getAccount());
                }
            }
        });
        itemViewHolder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(mUsrAccounts.get(sectionIndex).getAccountType(), item.getAccount());
                }
            }
        });
    }

    @Override
    public void onBindSectionHeaderViewHolder(SectionHeaderViewHolder sectionHeaderViewHolder, int position, int itemViewType) {
        super.onBindSectionHeaderViewHolder(sectionHeaderViewHolder, position, itemViewType);
        SecHeaderViewHolder viewHolder = (SecHeaderViewHolder) sectionHeaderViewHolder;
        viewHolder.mTvSecHeader.setText(mUsrAccounts.get(position).getAccountType());
    }

    public interface OnHeaderClickListener {
        void onHeaderClick();
    }

    public interface OnItemClickListener {
        void onEdit(String type, String account);

        void onItemClick(String type, String account);
    }

    private OnHeaderClickListener mHeaderClickListener;
    private OnItemClickListener mOnItemClickListener;

    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        this.mHeaderClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
