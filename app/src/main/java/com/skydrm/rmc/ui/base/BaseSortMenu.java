package com.skydrm.rmc.ui.base;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.common.SortMenuItem;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseSortMenu extends PopupWindow {
    protected Context mCtx;
    protected SortType mSortType = SortType.TIME_DESCEND;
    protected final SortMenuAdapter mAdapter;
    private OnSortItemClickListener mOnSortItemClickListener;

    protected abstract List<String> getData();

    public void setOnSortItemClickListener(OnSortItemClickListener listener) {
        this.mOnSortItemClickListener = listener;
    }

    public SortType getSortType() {
        return mSortType;
    }

    public void setSortType(SortType sortType) {
        mSortType = sortType;
        mAdapter.updateBySortType(sortType);
    }

    public BaseSortMenu(@NonNull Context ctx) {
        this.mCtx = ctx;
        final View root = LayoutInflater.from(ctx).inflate(R.layout.layout_sort_menu, null, false);
        setContentView(root);
        GridView gridView = root.findViewById(R.id.grid_view);
        mAdapter = new SortMenuAdapter();
        gridView.setAdapter(mAdapter);

        mAdapter.setData(adaptToItem(getData()));

        setOutsideTouchable(true);
        setFocusable(true);
        setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        ColorDrawable drawable = new ColorDrawable(0xb0000000);
        setBackgroundDrawable(drawable);
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = root.findViewById(R.id.ll_bottom).getBottom();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (y > height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        initListener();
    }

    private void initListener() {
        mAdapter.setOnItemClickListener(new SortMenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SortMenuItem item, int pos) {
                if (mSortType == item.getSortType()) {
                    return;
                }
                mSortType = item.getSortType();
                mAdapter.updateBySortType(mSortType);
                if (mOnSortItemClickListener != null) {
                    mOnSortItemClickListener.onSortItemClick(mSortType);
                }
            }
        });
    }

    private List<SortMenuItem> adaptToItem(List<String> data) {
        List<SortMenuItem> ret = new ArrayList<>();
        if (data == null || data.isEmpty()) {
            return ret;
        }
        // mend sort type according to the ui display order.
        mSortType = SortType.valueOf(mCtx, data.get(0));
        for (String val : data) {
            if (val == null || val.isEmpty()) {
                continue;
            }
            ret.add(SortMenuItem.newByItemValue(mCtx, val, mSortType));
        }
        return ret;
    }

    static class SortMenuAdapter extends BaseAdapter {
        private List<SortMenuItem> mData = new ArrayList<>();
        private OnItemClickListener mOnItemClickListener;

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.mOnItemClickListener = listener;
        }

        public void setData(List<SortMenuItem> data) {
            mData.clear();
            if (data != null) {
                mData.addAll(data);
            }
            notifyDataSetChanged();
        }

        void updateBySortType(SortType sortType) {
            if (mData == null || mData.isEmpty()) {
                return;
            }
            for (SortMenuItem item : mData) {
                if (item == null) {
                    continue;
                }
                item.setChecked(item.getSortType() == sortType);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sort_menu,
                        parent, false);
                vh = new ViewHolder(convertView, position);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            vh.bindData(mData.get(position));
            return convertView;
        }

        public class ViewHolder {
            CheckedTextView mCtvItem;

            ViewHolder(@NonNull View itemView, final int pos) {
                mCtvItem = itemView.findViewById(R.id.ctv_item);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (pos == -1) {
                            return;
                        }
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onItemClick(mData.get(pos), pos);
                        }
                    }
                });
            }

            void bindData(SortMenuItem item) {
                if (item == null) {
                    return;
                }
                String title = item.getTitle();
                if (title == null || title.isEmpty()) {
                    mCtvItem.setVisibility(View.GONE);
                    return;
                }
                mCtvItem.setText(title);
                mCtvItem.setChecked(item.isChecked());
            }
        }

        public interface OnItemClickListener {
            void onItemClick(SortMenuItem item, int pos);
        }

    }

    public interface OnSortItemClickListener {
        void onSortItemClick(SortType sortType);
    }
}
