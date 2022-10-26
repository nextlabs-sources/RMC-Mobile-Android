package com.skydrm.rmc.ui.service.contact.impl;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.widget.avatar.AvatarPlaceholder;
import com.skydrm.rmc.ui.widget.avatar.AvatarView;
import com.skydrm.rmc.utils.sort.SortContext;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.ArrayList;
import java.util.List;

public class AddressBookAdapter extends RecyclerView.Adapter<AddressBookAdapter.ViewHolder> {
    private static final int TYPE_ITEM_NORMAL = 0x01;
    private static final int TYPE_ITEM_GROUP = 0x02;

    private List<ContactItem> mData = new ArrayList<>();
    private OnItemCheckChangeListener mOnItemCheckChangeListener;

    public void setOnItemCheckChangeListener(OnItemCheckChangeListener listener) {
        this.mOnItemCheckChangeListener = listener;
    }

    public void swapCursor(Cursor cursor) {
        if (cursor == null) {
            return;
        }
        setData(SortContext.sortContacts(Contact.valuesOf(cursor),
                SortType.NAME_ASCEND));
    }

    public void setData(List<ContactItem> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_ITEM_GROUP;
        } else {
            String currentTitle = mData.get(position).getTitle();
            boolean isDifferent = !mData.get(position - 1).getTitle().equals(currentTitle);
            return isDifferent ? TYPE_ITEM_GROUP : TYPE_ITEM_NORMAL;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ITEM_NORMAL:
                return new NormalItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_normal_address_book,
                        parent, false));
            case TYPE_ITEM_GROUP:
                return new GroupItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_address_book,
                        parent, false));
        }
        throw new IllegalArgumentException("Unrecognized view type " + viewType + " performed.");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bandData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bandData(ContactItem item);
    }

    class NormalItemViewHolder extends ViewHolder {
        private final AvatarView mAvatarView;
        private final TextView mTvName;
        private final EmailListView mEmailListView;

        NormalItemViewHolder(View itemView) {
            super(itemView);
            mAvatarView = itemView.findViewById(R.id.contact_avatar);
            mTvName = itemView.findViewById(R.id.name_view);
            mEmailListView = itemView.findViewById(R.id.email_ls_view);
            if (mOnItemCheckChangeListener != null) {
                mEmailListView.setOnItemCheckChangeListener(new EmailListView.OnItemCheckChangeListener() {
                    @Override
                    public void onItemChecked(int pos, boolean checked, String email) {
                        mOnItemCheckChangeListener.onCheckChanged(pos, checked, email);
                    }
                });
            }
        }

        @Override
        void bandData(ContactItem item) {
            Contact contact = item.getContact();
            if (contact == null) {
                return;
            }
            String name = contact.getName();
            mAvatarView.setImageDrawable(new AvatarPlaceholder(mAvatarView.getContext(), name,
                    30, "skydrm.com", " "));
            mTvName.setText(name);
            mEmailListView.setData(contact.getDetails(), getLayoutPosition());
        }
    }

    class GroupItemViewHolder extends NormalItemViewHolder {
        private final TextView tvTitle;

        GroupItemViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }

        @Override
        void bandData(ContactItem item) {
            super.bandData(item);
            tvTitle.setText(item.getTitle());
        }
    }

    public interface OnItemCheckChangeListener {
        void onCheckChanged(int pos, boolean checked, String email);
    }
}
