package com.skydrm.rmc.ui.widget.popupwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.adapter.HomeRepoAdapter;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.sort.SortType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhu on 5/5/2017.
 */

public class MySpaceSortMenu extends PopupWindow {
    private static SortType mSortType = SortType.NAME_ASCEND;
    private final CheckedTextView mCtvA2Z;
    private final CheckedTextView mCtvSize;
    private final CheckedTextView mCtvZ2A;
    private final CheckedTextView mCtvDate;
    private final CheckedTextView mCtvRepo;
    private View mRepoView;
    private OnRepoListItemClickListener mOnRepoListItemClickListener;
    private OnSortByItemSelectListener mOnSortByItemSelectListener;

    public MySpaceSortMenu(Context context, boolean myDrive, final View.OnClickListener onClickListener) {
        mRepoView = LayoutInflater.from(context).inflate(R.layout.layout_repo_helper_popup_window, null);
        Button mBtApply = mRepoView.findViewById(R.id.bt_apply);
        mCtvA2Z = mRepoView.findViewById(R.id.ctv_a2z);
        mCtvSize = mRepoView.findViewById(R.id.ctv_size);
        mCtvZ2A = mRepoView.findViewById(R.id.ctv_z2a);
        mCtvDate = mRepoView.findViewById(R.id.ctv_date);
        mCtvRepo = mRepoView.findViewById(R.id.ctv_repository);
        initializeSelectItem();
        if (!myDrive) {
            final ListView mRepoList = mRepoView.findViewById(R.id.lv_active_repo_list);
            final List<BoundService> userLinkedRepos = CommonUtils.getFilteredBoundServiceByAccountType();

            HomeRepoAdapter mHomeRepoAdapter = new HomeRepoAdapter(context, R.layout.list_home_repo_item, userLinkedRepos);
            mRepoList.setAdapter(mHomeRepoAdapter);
            mHomeRepoAdapter.setOnRepoSelectListener(new HomeRepoAdapter.OnRepoSelectListener() {
                @Override
                public void onRepoSelect(boolean isChecked, int position) {
                    try {
                        BoundService boundService = userLinkedRepos.get(position);
                        if (boundService != null) {
                            if (isChecked) {
                                boundService.selected = 1;
                                SkyDRMApp.getInstance().getRepoSystem().activateRepo(boundService);
                            } else {
                                boundService.selected = 0;
                                SkyDRMApp.getInstance().getRepoSystem().deactivateRepo(boundService);
                            }
                            SkyDRMApp.getInstance().dbUpdateRepoSelected(boundService);
                        }
                        if (mOnRepoListItemClickListener != null) {
                            mOnRepoListItemClickListener.onUpdateUI(mSortType);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            LinearLayout repositoryContainer = mRepoView.findViewById(R.id.ll_bottom);
            repositoryContainer.setVisibility(View.INVISIBLE);
//            mCtvRepo.setVisibility(View.GONE);
        }
        mBtApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // notify user
                onClickListener.onClick(v);
                // dismiss this
                MySpaceSortMenu.this.dismiss();
            }
        });
        //sort select item
        mCtvA2Z.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvA2Z);
                mSortType = SortType.NAME_ASCEND;
                if (mOnSortByItemSelectListener != null) {
                    mOnSortByItemSelectListener.onItemSelected(SortType.NAME_ASCEND);
                }
            }
        });
        mCtvSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvSize);
                mSortType = SortType.SIZE_ASCEND;
                if (mOnSortByItemSelectListener != null) {
                    mOnSortByItemSelectListener.onItemSelected(SortType.SIZE_ASCEND);
                }
            }
        });
        mCtvZ2A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvZ2A);
                mSortType = SortType.NAME_DESCEND;
                if (mOnSortByItemSelectListener != null) {
                    mOnSortByItemSelectListener.onItemSelected(SortType.NAME_DESCEND);
                }
            }
        });
        mCtvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvDate);
                mSortType = SortType.TIME_DESCEND;
                if (mOnSortByItemSelectListener != null) {
                    mOnSortByItemSelectListener.onItemSelected(SortType.TIME_DESCEND);
                }
            }
        });
        mCtvRepo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvRepo);
                mSortType = SortType.DRIVER_TYPE;
                if (mOnSortByItemSelectListener != null) {
                    mOnSortByItemSelectListener.onItemSelected(SortType.DRIVER_TYPE);
                }
            }
        });
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setContentView(mRepoView);
        this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        ColorDrawable mDrawable = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(mDrawable);
        mRepoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = mRepoView.findViewById(R.id.ll_bottom).getBottom();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (y > height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    public void setSortType(SortType sortType) {
        mSortType = sortType;
        //initializeSelectItem();
    }

    private void initializeSelectItem() {
        switch (mSortType) {
            case NAME_ASCEND:
                setSelectItem(mCtvA2Z);
                break;
            case NAME_DESCEND:
                setSelectItem(mCtvZ2A);
                break;
            case TIME_DESCEND:
                setSelectItem(mCtvDate);
                break;
            case DRIVER_TYPE:
                setSelectItem(mCtvRepo);
                break;
            case SIZE_ASCEND:
                setSelectItem(mCtvSize);
                break;
        }
    }

    private void setSelectItem(CheckedTextView selectedItem) {
        mCtvA2Z.setChecked(false);
        mCtvZ2A.setChecked(false);
        mCtvDate.setChecked(false);
        mCtvRepo.setChecked(false);
        mCtvSize.setChecked(false);
        selectedItem.setChecked(true);
    }

    public void setOnRepoListItemClickListener(OnRepoListItemClickListener onRepoListItemClickListener) {
        this.mOnRepoListItemClickListener = onRepoListItemClickListener;
    }

    public void setOnSortByItemSelectListener(OnSortByItemSelectListener onSortByItemSelectListener) {
        this.mOnSortByItemSelectListener = onSortByItemSelectListener;
    }

    public interface OnRepoListItemClickListener {
        void onUpdateUI(SortType sortType);
    }

    public interface OnSortByItemSelectListener {
        void onItemSelected(SortType sortType);
    }

}
