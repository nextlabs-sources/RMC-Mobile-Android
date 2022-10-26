package com.skydrm.rmc.ui.widget.customcontrol;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jrzhou on 2/11/2017.
 */

public class SwipeRecyclerView extends RecyclerView {


    public SwipeRecyclerView(Context context) {
        super(context, null);
    }

    public SwipeRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    private boolean releaseTouchEvent;

    private float xDistance, yDistance, xLast, yLast;

    public SwipeRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    private View mEmptyView;

    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
    }

    private boolean isLoading;

    public boolean isLoading() {
        return isLoading;
    }

    private boolean isEmptyData;

    public boolean isEmptyData() {
        return isEmptyData;
    }

    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {

            Adapter<?> adapter = getAdapter();
            if (adapter != null && mEmptyView != null) {

                if (adapter.getItemCount() == 0) {

                    mEmptyView.setVisibility(View.VISIBLE);
                    isEmptyData = true;
                    SwipeRecyclerView.this.setVisibility(View.GONE);
                } else {

                    mEmptyView.setVisibility(View.GONE);
                    isEmptyData = false;
                    SwipeRecyclerView.this.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }
        emptyObserver.onChanged();
    }

}
