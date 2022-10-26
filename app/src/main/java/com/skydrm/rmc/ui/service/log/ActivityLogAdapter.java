package com.skydrm.rmc.ui.service.log;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.myspace.myvault.model.domain.IVaultFileLog;
import com.skydrm.rmc.utils.commonUtils.IconHelper;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhu on 4/6/2017.
 */

public class ActivityLogAdapter extends RecyclerView.Adapter<ActivityLogAdapter.ViewHolder> {
    private static final int TYPE_HEADER = 0x10;
    private static final int TYPE_ITEM = 0x11;
    private static final int TYPE_FOOTER = 0x12;

    private List<IVaultFileLog> mData = new ArrayList<>();
    private Context mCtx;
    private String mFileName;
    private int mTotalCount;

    private OnItemClickListener mOnItemClickListener;

    public ActivityLogAdapter(Context ctx, String name) {
        this.mCtx = ctx;
        this.mFileName = name;
    }

    public void setData(List<IVaultFileLog> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate
                        (R.layout.header_recycler_activity_log, parent, false));
            case TYPE_ITEM:
                return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate
                        (R.layout.item_recycler_activity_log, parent, false));
            case TYPE_FOOTER:
                return new FooterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_loading_more_recyler, parent, false));
        }
        throw new IllegalArgumentException("The viewType" + viewType +
                "does not correspond to the type :TYPE_HEADER ,TYPE_ITEM,TYPE_FOOTER");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        IVaultFileLog log = null;
        if (position != 0 && position != getItemCount() - 1) {
            log = mData.get(position - 1);
        }
        holder.bandData(log);
    }

    @Override
    public int getItemCount() {
        //2 represents recycler header & footer
        return mData.size() + 2;
    }

    public void setTotalCount(int count) {
        this.mTotalCount = count;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bandData(IVaultFileLog log);
    }

    private class HeaderViewHolder extends ViewHolder {
        private ImageView mIvThumbnail;
        private TextView mTvFileNameLog;

        HeaderViewHolder(View itemView) {
            super(itemView);
            mIvThumbnail = itemView.findViewById(R.id.iv_icon_log);
            mTvFileNameLog = itemView.findViewById(R.id.tv_file_name_log);
        }

        @Override
        void bandData(IVaultFileLog log) {
            if (TextUtils.isEmpty(mFileName)) {
                return;
            }
            mTvFileNameLog.setText(mFileName);
            String nxlFileType = mFileName.toLowerCase();
            mIvThumbnail.setImageResource(IconHelper.getNxlIconResourceIdByExtension(nxlFileType));
        }
    }

    private class FooterViewHolder extends ViewHolder {
        private final LinearLayout mLlFooterRooter;
        private final TextView mTvLoadingText;
        private final ProgressBar mPbLoadingProgress;

        FooterViewHolder(View itemView) {
            super(itemView);
            mLlFooterRooter = itemView.findViewById(R.id.ll_footer_rooter);
            mTvLoadingText = itemView.findViewById(R.id.tv_loading_text);
            mPbLoadingProgress = itemView.findViewById(R.id.tv_loading_progress);
        }

        @Override
        void bandData(IVaultFileLog log) {
            if (hasMore()) {
                mLlFooterRooter.setVisibility(View.VISIBLE);
                mTvLoadingText.setText("Loading more...");
                mPbLoadingProgress.setVisibility(View.VISIBLE);
            } else {
                mTvLoadingText.setText("No more items found.");
                mPbLoadingProgress.setVisibility(View.GONE);
                //holder.footerRooter.setVisibility(View.GONE);
            }
        }

        private boolean hasMore() {
            return mTotalCount != mData.size();
        }
    }

    private class ItemViewHolder extends ViewHolder {
        private ImageView mIvResultLog;
        private TextView mTvUsernameLog;
        private TextView mTvActionLog;
        private TextView mTvTimeLog;
        private TextView mTvResult;
        private TextView mTvAppAndDeviceId;

        ItemViewHolder(View itemView) {
            super(itemView);
            mIvResultLog = itemView.findViewById(R.id.iv_result_log);
            mTvUsernameLog = itemView.findViewById(R.id.tv_user_name_log);
            mTvActionLog = itemView.findViewById(R.id.tv_action_log);
            mTvTimeLog = itemView.findViewById(R.id.tv_time_log);
            mTvResult = itemView.findViewById(R.id.tv_result);
            mTvAppAndDeviceId = itemView.findViewById(R.id.tv_app_device);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(getAdapterPosition() - 1);
                    }
                }
            });
        }

        @Override
        void bandData(IVaultFileLog log) {
            if (log.getAccessResult().equalsIgnoreCase("Allow")) {
                mIvResultLog.setSelected(true);
            } else {
                mIvResultLog.setSelected(false);
            }
            mTvUsernameLog.setText(log.getEmail());
            StringBuilder operatinBuilder = new StringBuilder();
            String operation = "";
            String str = " the file";
            if (log.getOperation().equalsIgnoreCase("Revoke")) {
                operation = "Revoked";
                operatinBuilder.append(operation).append(str);
            } else if (log.getOperation().equalsIgnoreCase("Share")) {
                operation = "Shared";
                operatinBuilder.append(operation).append(str);
            } else if (log.getOperation().equalsIgnoreCase("View")) {
                operation = "Viewed";
                operatinBuilder.append(operation).append(str);
            } else if (log.getOperation().equalsIgnoreCase("Protect")) {
                operation = "Protected";
                operatinBuilder.append(operation).append(str);
            } else if (log.getOperation().equalsIgnoreCase("Reshare")) {
                operation = "Reshared";
                operatinBuilder.append(operation).append(str);
            } else if (log.getOperation().equalsIgnoreCase("Print")) {
                operation = "Printed";
                operatinBuilder.append(operation).append(str);
            } else if (log.getOperation().equalsIgnoreCase("Download")) {
                operation = "Downloaded";
                operatinBuilder.append(operation).append(str);
            } else if (log.getOperation().equalsIgnoreCase("Remove user")) {
                operation = "Removed user";
                str = " of the file";
                operatinBuilder.append(operation).append(str);
            } else {
                operation = log.getOperation();
                str = "";
                operatinBuilder.append(operation).append(str);
            }

            SpannableString logOperationStr = new SpannableString(operatinBuilder.toString());
            logOperationStr.setSpan(new ForegroundColorSpan(mCtx.getResources().getColor(R.color.main_green_light)),
                    0, operation.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTvActionLog.setText(logOperationStr);
            mTvTimeLog.setText(TimeUtil.formatData(log.getAccessTime()));
            if (log.getAccessResult().equalsIgnoreCase("Deny")) {
                mTvResult.setTextColor(mCtx.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                mTvResult.setTextColor(mCtx.getResources().getColor(R.color.main_green_light));
            }
            mTvAppAndDeviceId.setText(String.format("%s     %s", log.getDeviceType(), log.getDeviceId()));
            mTvResult.setText(log.getAccessResult());
        }
    }
}
