package com.skydrm.rmc.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skydrm.rmc.R;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseFileCtxMenu extends BottomSheetDialogFragment {
    protected Context mCtx;
    private OnItemClickListener mOnItemClickListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCtx = context;
    }

    public BaseFileCtxMenu() {
    }

    protected abstract OnItemClickListener getItemClickListener();

    protected abstract List<String> getData();

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_base_file_ctx_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView menuItems = view.findViewById(R.id.recycler_view);
        menuItems.setLayoutManager(new LinearLayoutManager(mCtx));
        BaseFileCtxMenuAdapter adapter = new BaseFileCtxMenuAdapter();
        menuItems.setAdapter(adapter);

        adapter.setData(getData());
        mOnItemClickListener = getItemClickListener();
    }

    class BaseFileCtxMenuAdapter extends RecyclerView.Adapter<BaseFileCtxMenuAdapter.ViewHolder> {
        private List<String> mData = new ArrayList<>();

        void setData(List<String> data) {
            mData.clear();
            if (data != null) {
                mData.addAll(data);
            }

            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_base_file_ctx_menu, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bandData(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mTvFileMenuItem;

            ViewHolder(View itemView) {
                super(itemView);
                mTvFileMenuItem = itemView.findViewById(R.id.tv_file_menu_item);

                if (mOnItemClickListener != null) {
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int pos = getLayoutPosition();
                            if (pos < 0 || pos >= mData.size()) {
                                return;
                            }
                            mOnItemClickListener.onItemClick(v, mData.get(pos), pos);
                        }
                    });
                }
            }

            public void bandData(String s) {
                if (s == null || s.isEmpty()) {
                    mTvFileMenuItem.setVisibility(View.GONE);
                } else {
                    if (getLayoutPosition() == 0) {
                        mTvFileMenuItem.setTextColor(mCtx.getResources().getColor(android.R.color.black));
                    }
                    if (s.equals(mCtx.getString(R.string.delete))) {
                        mTvFileMenuItem.setTextColor(mCtx.getResources().getColor(R.color.main_float_actionbtn_tint));
                    }
                    mTvFileMenuItem.setText(s);
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, String title, int pos);
    }
}
