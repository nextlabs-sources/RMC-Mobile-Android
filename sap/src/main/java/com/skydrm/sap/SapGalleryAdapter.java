package com.skydrm.sap;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sap.ve.SDVLImage;
import com.sap.ve.SDVLProcedure;
import com.sap.ve.SDVLProceduresInfo;
import com.sap.ve.SDVLStep;

import java.util.ArrayList;
import java.util.List;


public class SapGalleryAdapter extends
        RecyclerView.Adapter<SapGalleryAdapter.ViewHolder> {
    private List<Integer> mSelected = new ArrayList<>();
    private List<SDVLImage> mData = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    public void setData(List<SDVLImage> data) {
        mData.clear();
        if (data != null) {
            mData.clear();
        }
        mData.addAll(data);

        notifyDataSetChanged();
    }

    public void setData(SAPViewer viewer, SDVLProceduresInfo proceduresInfo) {
        if (viewer == null || proceduresInfo == null) {
            return;
        }
        setData(getThumbnailData(viewer, proceduresInfo));
    }

    private List<SDVLImage> getThumbnailData(SAPViewer viewer, SDVLProceduresInfo proceduresInfo) {
        List<SDVLImage> ret = new ArrayList<>();
        if (viewer == null || proceduresInfo == null) {
            return ret;
        }
        List<SDVLProcedure> procedures = proceduresInfo.procedures;
        if (procedures == null || procedures.size() == 0) {
            return ret;
        }
        List<SDVLStep> steps = procedures.get(0).steps;
        if (steps == null || steps.size() == 0) {
            return ret;
        }
        for (SDVLStep step : steps) {
            SDVLImage image = new SDVLImage();
            viewer.retrieveThumbnail(step.id, image);
            ret.add(image);
        }
        return ret;
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setSelected(int index) {
        if (index < 0 || index >= mData.size()) {
            return;
        }
        mSelected.clear();
        mSelected.add(index);

        notifyItemChanged(index);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sap_gallery,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.bandData(mData.get(position), position);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        SapRecycleImageView mImg;

        public ViewHolder(final View itemView) {
            super(itemView);
            mImg = itemView.findViewById(R.id.id_index_gallery_item_image);

            if (mOnItemClickListener != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = getLayoutPosition();
                        mOnItemClickListener.onItemClick(itemView, pos);
                        mImg.setColor(Color.RED);
                        mImg.setBorderWidth(10);

                        if (mSelected.isEmpty()) {
                            mSelected.add(pos);
                        } else {
                            int oldSelected = mSelected.get(0);
                            mSelected.clear();
                            mSelected.add(pos);
                            // we do not notify that an item has been selected
                            // because that work is done here.  we instead send
                            // notifications for items to be deselected

                            notifyItemChanged(oldSelected);
                        }
                        notifyItemChanged(pos);
                    }
                });
            }
        }

        public void bandData(SDVLImage image, int pos) {
            final byte[] data = image.data;
            mImg.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
            if (mSelected.contains(pos)) {
                // view not selected
                mImg.setColor(Color.RED);
                mImg.setBorderWidth(10);
            } else {
                // view is selected
                mImg.setColor(Color.WHITE);
                mImg.setBorderWidth(0);
            }
        }
    }
}
