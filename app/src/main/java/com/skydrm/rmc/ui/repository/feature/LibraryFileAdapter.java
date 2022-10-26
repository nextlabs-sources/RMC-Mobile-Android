package com.skydrm.rmc.ui.repository.feature;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.ILocalFile;
import com.skydrm.rmc.domain.LocalFileItem;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.rmc.utils.commonUtils.ImageLoader;
import com.skydrm.rmc.utils.commonUtils.TimeUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class LibraryFileAdapter extends RecyclerView.Adapter<LibraryFileAdapter.ViewHolder> {
    private static final int ITEM_NORMAL = 0x01;
    private static final int ITEM_GROUP = 0x02;

    private List<LocalFileItem> mData = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public void setData(List<LocalFileItem> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }

        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_GROUP;
        } else {
            LocalFileItem entity = mData.get(position);
            String currentTitle = entity.getTitle();
            int prevIndex = position - 1;
            boolean isDifferent = !mData.get(prevIndex).getTitle().equals(currentTitle);
            return isDifferent ? ITEM_GROUP : ITEM_NORMAL;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_GROUP:
                return new GroupViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_group_local_file2, parent, false));
            case ITEM_NORMAL:
                return new NormalItemViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_normal_local_file2, parent, false));
        }
        throw new IndexOutOfBoundsException("unrecognized viewType: " + viewType + " does not " +
                "correspond to ITEM_NORMAL, GROUP_ITEM");
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

        public abstract void bandData(LocalFileItem item);
    }

    class NormalItemViewHolder extends ViewHolder {
        private ImageView mIvIcon;
        private TextView mTvName;
        private TextView mTvSubText;

        NormalItemViewHolder(View itemView) {
            super(itemView);
            mIvIcon = itemView.findViewById(R.id.iv_icon);
            mTvName = itemView.findViewById(R.id.tv_name);
            mTvSubText = itemView.findViewById(R.id.tv_sub_text);

            if (mOnItemClickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = getLayoutPosition();
                        if (pos < 0 || pos > mData.size() - 1) {
                            return;
                        }
                        mOnItemClickListener.onItemClick(mData.get(pos).getLocalFile(), pos);
                    }
                });
            }
        }

        @Override
        public void bandData(LocalFileItem item) {
            ILocalFile file = item.getLocalFile();
            mTvName.setText(file.getFile().getName());
            if (file.isFolder()) {
                mIvIcon.setImageResource(R.drawable.local_file_folder_icon);
                mTvSubText.setVisibility(View.GONE);
            } else {
                mTvSubText.setVisibility(View.VISIBLE);
                mTvSubText.setText(String.format("%s%s", FileUtils.transparentFileSize(file.getFile().length()).concat("    "),
                        TimeUtil.formatData(file.getFile().lastModified())));

                if (new ImageFileFilter().accept(file.getFile())) {
                    mIvIcon.setImageResource(R.drawable.home_file_icon);
                    ImageLoader.getDefault().loadImage(file.getFile().getPath(), mIvIcon);
                } else {
                    mIvIcon.setImageResource(R.drawable.home_file_icon);
                }
            }
        }
    }

    class GroupViewHolder extends NormalItemViewHolder {
        private final TextView mTvTitle;

        GroupViewHolder(View itemView) {
            super(itemView);
            mTvTitle = itemView.findViewById(R.id.tv_title);
        }

        @Override
        public void bandData(LocalFileItem item) {
            super.bandData(item);
            mTvTitle.setText(item.getTitle());
        }
    }

    private static class ImageFileFilter implements FileFilter {
        private final String[] supported = new String[]{"jpg", "png", "gif", "jpeg"};

        public boolean accept(File file) {
            for (String ext : supported) {
                if (file.getName().toLowerCase().endsWith(ext)) {
                    return true;
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ILocalFile f, int pos);
    }
}
