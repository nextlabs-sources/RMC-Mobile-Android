package com.skydrm.rmc.ui.project.feature.centralpolicy;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skydrm.rmc.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hhu on 4/9/2018.
 */

public class ClassifyAdapter extends RecyclerView.Adapter<ClassifyAdapter.ViewHolder> {
    private List<ClassifyItem> mData = new ArrayList<>();
    private OnLabelSelectListener mOnLabelSelectListener;
    private boolean allowDefaultSelect;
    private boolean allowInheritanceSelect;

    public void setAllowDefaultSelect(boolean allowDefaultSelect) {
        this.allowDefaultSelect = allowDefaultSelect;
    }

    public void setAllowInheritanceSelect(boolean allowInheritanceSelect) {
        this.allowInheritanceSelect = allowInheritanceSelect;
    }

    public void setData(List<ClassifyItem> data) {
        mData.clear();
        if (data != null) {
            mData.addAll(data);
        }
        notifyDataSetChanged();
    }

    public List<String> getSingleSelectCategories() {
        List<String> retVal = new ArrayList<>();
        if (mData.size() == 0) {
            return retVal;
        }
        for (ClassifyItem item : mData) {
            if (item == null) {
                continue;
            }
            if (!item.isMultiSelect()) {
                retVal.add(item.getCategoryName());
            }
        }
        return retVal;
    }

    public void selectTarget(Map<String, Set<String>> tags) {
        // before select should reset-selected.
        resetAllSelected();
        if (tags == null || tags.size() == 0) {
            return;
        }
        Set<String> categories = tags.keySet();
        if (categories.size() == 0) {
            return;
        }
        for (String category : categories) {
            if (category == null || category.isEmpty()) {
                continue;
            }
            int index = -1;
            for (int i = 0; i < mData.size(); i++) {
                ClassifyItem item = mData.get(i);
                if (item.getCategoryName().equals(category)) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                continue;
            }
            Set<String> labels = tags.get(category);
            if (labels == null || labels.size() == 0) {
                continue;
            }
            List<ClassifyItem.LabelsBean> uiLabels = mData.get(index).getLabels();
            for (ClassifyItem.LabelsBean l : uiLabels) {
                l.defaultX = labels.contains(l.name);
//                if (!mData.get(index).isMultiSelect()) {
//                    break;
//                }
            }
        }
        notifyDataSetChanged();
    }

    private void resetAllSelected() {
        if (mData == null || mData.size() == 0) {
            return;
        }
        for (ClassifyItem item : mData) {
            List<ClassifyItem.LabelsBean> labels = item.getLabels();
            for (ClassifyItem.LabelsBean labelsBean : labels) {
                labelsBean.defaultX = false;
            }
        }
    }

    public void setOnLabelSelectListener(OnLabelSelectListener listener) {
        this.mOnLabelSelectListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_classify, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.bandData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvTitle;
        private AutoFlowLayout mAfl;

        ViewHolder(View itemView) {
            super(itemView);
            mTvTitle = itemView.findViewById(R.id.tv_section_title);
            mAfl = itemView.findViewById(R.id.afl_section_items);
        }

        private void bandData(final ClassifyItem item) {
            if (item.isMandatory()) {
                mAfl.setOnMandatoryListener(new AutoFlowLayout.OnMandatoryListener() {
                    @Override
                    public void onSelectedItemExist(boolean selected) {
                        mTvTitle.setText(generateSpannable(item.getCategoryName() + " (Mandatory)",
                                selected ? "#828282" : "#ffcc0000"));
                    }
                });
            } else {
                mAfl.setOnMandatoryListener(null);
                mTvTitle.setText(item.getCategoryName());
            }

            mAfl.setSelectMode(item.isMultiSelect() ? AutoFlowLayout.SelectMode.MULTIPLE : AutoFlowLayout.SelectMode.SINGLE);
            mAfl.setAllowDefaultSelect(allowDefaultSelect);
            mAfl.setAllowInheritanceSelect(allowInheritanceSelect);
            mAfl.setData(item.getLabels());
            mAfl.setOnItemClickListener(new AutoFlowLayout.OnItemClickListener() {
                @Override
                public void onItemClick(boolean selected, String name) {
                    if (mOnLabelSelectListener != null) {
                        mOnLabelSelectListener.onLabelSelect(item, name, selected, item.isMandatory());
                    }
                }
            });
        }

        private Spannable generateSpannable(String text, String color) {
            Spannable spannable = new SpannableString(text);
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor(color)),
                    text.indexOf("("), text.indexOf(")") + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            return spannable;
        }
    }


    public interface OnLabelSelectListener {
        void onLabelSelect(ClassifyItem classifyItem, String label, boolean selected, boolean mandatory);
    }
}