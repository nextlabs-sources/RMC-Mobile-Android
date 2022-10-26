package com.skydrm.rmc.ui.widget.customcontrol.rights;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.ui.project.feature.centralpolicy.ClassifyAdapter;
import com.skydrm.rmc.ui.project.feature.centralpolicy.ClassifyItem;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.rms.types.ClassificationProfileRetrieveResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CentralTagSelectView extends FrameLayout implements IDestroyable {
    private Context mCtx;
    private TextView mTvDesc;
    private RecyclerView mRecyclerView;
    private ClassifyAdapter mClassifyAdapter;

    private Map<String, Set<String>> mDisplayTags = new HashMap<>();
    private Map<String, Set<String>> mSelectTags = new HashMap<>();
    private List<String> mMandatoryTags = new ArrayList<>();
    private Set<String> mGroupSelectLabels;

    private boolean allowDefaultSelect = true;
    private boolean allowInheritanceSelect;

    private OnTagSizeChangeListener mOnTagSizeChangeListener;

    public CentralTagSelectView(@NonNull Context context) {
        this(context, null);
    }

    public CentralTagSelectView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CentralTagSelectView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mCtx = context;
        initView();
    }

    public void setAllowDefaultSelect(boolean allowDefaultSelect) {
        this.allowDefaultSelect = allowDefaultSelect;
    }

    public void setAllowInheritanceSelect(boolean allowInheritanceSelect) {
        this.allowInheritanceSelect = allowInheritanceSelect;
    }

    public void setOnTagSizeChangeListener(OnTagSizeChangeListener listener) {
        this.mOnTagSizeChangeListener = listener;
    }

    public Map<String, Set<String>> getDisplayTags() {
        return mDisplayTags;
    }

    public Map<String, Set<String>> getSelectedTags() {
        return mSelectTags;
    }

    public boolean checkMandatory() {
        //Empty select tags
        if (mSelectTags == null || mSelectTags.isEmpty()) {
            //If mandatory list does not exist, then let protect continue.
            if (mMandatoryTags == null || mMandatoryTags.isEmpty()) {
                return true;
            }
            //Mandatory list exists means must be selected at least one label.
            if (mMandatoryTags.size() != 1) {
                ToastUtil.showToast(mCtx,
                        "Mandatory categories require at least one classification label.");
            } else {
                for (String key : mMandatoryTags) {
                    ToastUtil.showToast(mCtx, "Mandatory category " + key +
                            " require at least one classification label.");
                }
            }
            return false;
        }
        if (mMandatoryTags == null || mMandatoryTags.isEmpty()) {
            return true;
        }
        int mandatoryCheck = 0;
        for (String key : mMandatoryTags) {
            if (mSelectTags.containsKey(key)) {
                Set<String> valueLabels = mSelectTags.get(key);
                if (valueLabels == null || valueLabels.size() == 0) {
                    ToastUtil.showToast(mCtx, "Mandatory category " + key
                            + " require at least one classification label.");
                    return false;
                } else {
                    mandatoryCheck++;
                }
            } else {
                ToastUtil.showToast(mCtx, "Mandatory category " + key
                        + " require at least one classification label.");
                return false;
            }
        }
        return mMandatoryTags.size() == mandatoryCheck;
    }

    public boolean checkNeedReSelect() {
        List<String> singleSelectCategories = mClassifyAdapter.getSingleSelectCategories();
        if (singleSelectCategories == null || singleSelectCategories.size() == 0) {
            return false;
        }
        for (String category : singleSelectCategories) {
            if (mSelectTags.containsKey(category)) {
                Set<String> labels = mSelectTags.get(category);
                if (labels.size() != 1) {
                    ToastUtil.showToast(mCtx, "Category " + category
                            + " require only one classification label.");

                    return true;
                }
            }
        }
        return false;
    }

    public void displayTags(String classificationRaw) {
        if (classificationRaw == null || classificationRaw.isEmpty() || classificationRaw.equals("{}")) {
            notifySizeChange(true);
            return;
        }
        ClassificationProfileRetrieveResult result = new Gson().fromJson(classificationRaw,
                ClassificationProfileRetrieveResult.class);
        parseAndDisplay(result);
    }

    public void selectTargetTags(Map<String, Set<String>> tags) {
        if (tags == null || tags.size() == 0) {
            return;
        }
        mSelectTags.clear();

        Set<String> displayCategories = mDisplayTags.keySet();
        Set<String> needSelectedCategories = tags.keySet();
        for (String category : needSelectedCategories) {
            if (displayCategories.contains(category)) {

                Set<String> targetLabels = new HashSet<>();
                Set<String> fileLabels = tags.get(category);
                if (fileLabels != null && fileLabels.size() != 0) {
                    for (String label : fileLabels) {
                        if (label == null || label.isEmpty()) {
                            continue;
                        }
                        Set<String> projectLabels = mDisplayTags.get(category);
                        if (projectLabels.contains(label)) {
                            targetLabels.add(label);
                        }
                    }
                }

                if (targetLabels.size() != 0) {
                    mSelectTags.put(category, targetLabels);
                }
            }
        }

        mClassifyAdapter.setAllowInheritanceSelect(allowInheritanceSelect);
        mClassifyAdapter.selectTarget(mSelectTags);
    }

    private void initView() {
        View root = LayoutInflater.from(mCtx).inflate(R.layout.specify_center_policy_rights_project_add_file,
                this, true);
        mTvDesc = root.findViewById(R.id.rights);
        mRecyclerView = root.findViewById(R.id.rv_central_policy_rights);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mCtx));
        mRecyclerView.setNestedScrollingEnabled(false);
        mClassifyAdapter = new ClassifyAdapter();
        mRecyclerView.setAdapter(mClassifyAdapter);

        mClassifyAdapter.setOnLabelSelectListener(new ClassifyAdapter.OnLabelSelectListener() {
            @Override
            public void onLabelSelect(ClassifyItem classifyItem,
                                      String label, boolean selected,
                                      boolean mandatory) {
                if (selected) {
                    if (!mSelectTags.containsKey(classifyItem.getCategoryName())) {
                        mGroupSelectLabels = new HashSet<>();
                        mGroupSelectLabels.add(label);
                        mSelectTags.put(classifyItem.getCategoryName(), mGroupSelectLabels);
                    } else {
                        Set<String> labels = mSelectTags.get(classifyItem.getCategoryName());
                        labels.add(label);
                        mSelectTags.put(classifyItem.getCategoryName(), labels);
                    }
                    if (mandatory) {
                        mMandatoryTags.remove(classifyItem.getCategoryName());
                    }
                } else {
                    if (mSelectTags.containsKey(classifyItem.getCategoryName())) {
                        Set<String> labels = mSelectTags.get(classifyItem.getCategoryName());
                        labels.remove(label);
                        if (labels.isEmpty()) {
                            mSelectTags.remove(classifyItem.getCategoryName());
                        }
                    }
                    if (mandatory) {
                        String categoryName = classifyItem.getCategoryName();
                        if (!mMandatoryTags.contains(categoryName)) {
                            mMandatoryTags.add(categoryName);
                        }
                    }
                }
            }
        });
    }

    private void parseAndDisplay(ClassificationProfileRetrieveResult result) {
        if (result == null) {
            notifySizeChange(true);
            return;
        }
        List<ClassifyItem> classifyItems = new ArrayList<>();
        mDisplayTags.clear();
        mSelectTags.clear();
        mMandatoryTags.clear();
        ClassificationProfileRetrieveResult.ResultsBean results = result.getResults();
        if (results == null) {
            notifySizeChange(true);
            return;
        }
        List<ClassificationProfileRetrieveResult.ResultsBean.CategoriesBean> categories = results.getCategories();
        if (categories == null || categories.size() == 0) {
            notifySizeChange(true);
            return;
        }
        for (ClassificationProfileRetrieveResult.ResultsBean.CategoriesBean categoriesBean : categories) {
            if (categoriesBean == null) {
                continue;
            }
            Set<String> categoryLabels = new HashSet<>();

            mGroupSelectLabels = new HashSet<>();
            String categoryName = categoriesBean.getName();
            boolean multiSelect = categoriesBean.isMultiSelect();
            boolean mandatory = categoriesBean.isMandatory();
            if (mandatory) {
                mMandatoryTags.add(categoryName);
            }
            List<ClassifyItem.LabelsBean> labelsBeans = new ArrayList<>();

            List<ClassificationProfileRetrieveResult.ResultsBean.CategoriesBean.LabelsBean> labels = categoriesBean.getLabels();
            if (labels == null || labels.size() == 0) {
                continue;
            }
            for (ClassificationProfileRetrieveResult.ResultsBean.CategoriesBean.LabelsBean labelsBean : labels) {
                if (labelsBean == null) {
                    continue;
                }
                labelsBeans.add(new ClassifyItem.LabelsBean(labelsBean.getName(), labelsBean.isDefaultX()));
                categoryLabels.add(labelsBean.getName());
                if (allowDefaultSelect & labelsBean.isDefaultX()) {
                    mGroupSelectLabels.add(labelsBean.getName());
                }
            }

            ClassifyItem classifyItem = new ClassifyItem();
            classifyItem.setCategoryName(categoryName);
            classifyItem.setMultiSelect(multiSelect);
            classifyItem.setMandatory(mandatory);
            classifyItem.setLabels(labelsBeans);

            classifyItems.add(classifyItem);
            if (mGroupSelectLabels != null && mGroupSelectLabels.size() != 0) {
                mSelectTags.put(categoryName, mGroupSelectLabels);
            }
            mDisplayTags.put(categoryName, categoryLabels);
        }
        if (classifyItems.size() == 0) {
            notifySizeChange(true);
        } else {
            notifySizeChange(false);
        }
        mClassifyAdapter.setAllowDefaultSelect(allowDefaultSelect);
        mClassifyAdapter.setData(classifyItems);
    }

    private void notifySizeChange(boolean empty) {
        if (mOnTagSizeChangeListener == null) {
            return;
        }
        mOnTagSizeChangeListener.onSizeChange(empty);
    }

    @Override
    public void onReleaseResource() {
        if (mOnTagSizeChangeListener != null) {
            mOnTagSizeChangeListener = null;
        }
    }

    public interface OnTagSizeChangeListener {
        void onSizeChange(boolean empty);
    }
}
