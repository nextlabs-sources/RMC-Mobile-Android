package com.skydrm.rmc.ui.widget.customcontrol;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.project.service.adapter.RecyclingPagerAdapter;
import com.skydrm.rmc.utils.commonUtils.DensityHelper;


public class ScaleViewPage extends LinearLayout implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private ClipViewPager scaleViewPager;
    private FlowLayout point_container;
    private RecyclingPagerAdapter mRecyclingPagerAdapter;
    private boolean mIsPlayPointContainer = true;
    private Context mContext;
    private int diameter;
    private int leftMargin;
    private int prePosition = 0;
    private boolean scaleViewPage_isLeftDisplay;
    private boolean centerDisplay;
    private boolean rightDisplay;
    private RelativeLayout scaleViewPage_progress;
    private RelativeLayout emptyView;
    private RelativeLayout networkAnomaliesView;


    public ScaleViewPage(Context context) {
        this(context, null);
    }

    public ScaleViewPage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleViewPage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScaleViewPage);
        scaleViewPage_isLeftDisplay = typedArray.getBoolean(R.styleable.ScaleViewPage_isLeftDisplay, false);
        centerDisplay = typedArray.getBoolean(R.styleable.ScaleViewPage_isCenterDisplay, false);
        rightDisplay = typedArray.getBoolean(R.styleable.ScaleViewPage_isRightDisplay, false);
        typedArray.recycle();
        setClickable(true);
        this.mContext = context;
        diameter = DensityHelper.dip2px(mContext, 15);
        leftMargin = DensityHelper.dip2px(mContext, 10);
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.real_scale_viewpage, this, true);
        initView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        scaleViewPager.addOnPageChangeListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        scaleViewPager.removeOnPageChangeListener(this);
    }


    public ClipViewPager getClipViewPager() {
        return this.scaleViewPager;
    }

    private void initView() {
        emptyView = (RelativeLayout) findViewById(R.id.scaleViewPage_empty_view);
        networkAnomaliesView = (RelativeLayout) findViewById(R.id.scaleViewPage_network_anomalies_view);

        scaleViewPager = (ClipViewPager) findViewById(R.id.scaleViewPager);
        findViewById(R.id.scaleViewPager_container).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return scaleViewPager.dispatchTouchEvent(event);
            }
        });
        scaleViewPage_progress = (RelativeLayout) findViewById(R.id.scaleViewPage_progressBar);
        point_container = (FlowLayout) findViewById(R.id.point_container);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scaleViewPager.getLayoutParams();
        if (scaleViewPage_isLeftDisplay) {

        } else if (centerDisplay) {
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        } else if (rightDisplay) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            params.setMarginEnd(DensityHelper.dip2px(mContext, 15));
        }
        scaleViewPager.setLayoutParams(params);
//        scaleViewPager.setPageMargin(30);

        HorizontalScrollView scaleViewPage_horizontalScrollView = (HorizontalScrollView) findViewById(R.id.scaleViewPage_horizontalScrollView);
        scaleViewPage_horizontalScrollView.setHorizontalScrollBarEnabled(false);
    }

    public void showEmptyView() {
        emptyView.setVisibility(VISIBLE);
        scaleViewPage_progress.setVisibility(View.GONE);
        scaleViewPager.setVisibility(GONE);
        networkAnomaliesView.setVisibility(View.GONE);
        point_container.setVisibility(GONE);
    }

    public void showNormalView() {
        scaleViewPager.setVisibility(VISIBLE);
        point_container.setVisibility(VISIBLE);
        emptyView.setVisibility(GONE);
        scaleViewPage_progress.setVisibility(View.GONE);
        networkAnomaliesView.setVisibility(View.GONE);
    }

    public void goneEmptyView() {
        emptyView.setVisibility(View.GONE);
    }

    public void showNetworkAnomaliesView() {
        networkAnomaliesView.setVisibility(View.VISIBLE);
        scaleViewPage_progress.setVisibility(View.GONE);
        scaleViewPager.setVisibility(GONE);
        emptyView.setVisibility(View.GONE);
        point_container.setVisibility(GONE);
    }

    public void goneNetworkAnomaliesView() {
        networkAnomaliesView.setVisibility(View.GONE);
    }

    public void visibleScaleViewPageProgress() {
        scaleViewPage_progress.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        networkAnomaliesView.setVisibility(View.GONE);
    }

    public void goneScaleViewPageProgress() {
        scaleViewPage_progress.setVisibility(View.GONE);
    }

    public void setScaleViewPageAdapter(RecyclingPagerAdapter adapter) {
        this.mRecyclingPagerAdapter = adapter;
        int count = adapter.getCount();
        setPointContainer(count);
        setScaleViewPageProperties(count);
        scaleViewPager.setAdapter(adapter);
    }

    private void setScaleViewPageProperties(int count) {
        scaleViewPager.setPageTransformer(true, new ScalePageTransformer());
        scaleViewPager.setOffscreenPageLimit(count);
    }

    public RecyclingPagerAdapter getAdapter() {
        return mRecyclingPagerAdapter;
    }

    public FlowLayout getPoint_container() {
        return point_container;
    }

    public void notificationRefreshPointContainer() {
        int pointCount = mRecyclingPagerAdapter.getCount();
        scaleViewPager.setOffscreenPageLimit(pointCount);

        if (mIsPlayPointContainer) {
            point_container.removeAllViews();
            setPointContainer(pointCount);
//            int currentItem = scaleViewPager.getCurrentItem();
            scaleViewPager.setCurrentItem(0);
//            point_container.getChildAt(0).setEnabled(false);
            prePosition = 0;
        }
    }

    public void setPointContainer(int pointCount) {
        if (!mIsPlayPointContainer) {
            return;
        }
        for (int i = 0; i < pointCount; i++) {
            View point = new View(mContext);
            point.setBackgroundResource(R.drawable.point_selector);
            point.setTag(i);
            point.setOnClickListener(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(diameter, diameter);
            params.leftMargin = leftMargin;
            point.setLayoutParams(params);
            if (i == 0) {
                point.setEnabled(false);
            }
            point_container.addView(point);
        }
    }

    public void setIsPlayPointContainer(boolean isPlayPointContainer) {
        this.mIsPlayPointContainer = isPlayPointContainer;
        if (isPlayPointContainer) {
            point_container.setVisibility(View.VISIBLE);
        } else {
            point_container.setVisibility(GONE);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (prePosition != position && mIsPlayPointContainer) {
            point_container.getChildAt(position).setEnabled(false);
            point_container.getChildAt(prePosition).setEnabled(true);
            prePosition = position;
            mPageSelectedListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View v) {
        int tag = (int) v.getTag();
        scaleViewPager.setCurrentItem(tag);
    }

    public interface ScaleViewPagerCurrentChangedItemListener {
        void onScaleViewPagerCurrentChanged(ClipViewPager scaleViewPager);
    }

    private ScaleViewPagerCurrentChangedItemListener scaleViewPagerCurrentChangedItemListener;

    public void setScaleViewPagerCurrentChangedItemListener(ScaleViewPagerCurrentChangedItemListener scaleViewPagerCurrentChangedItemListener) {
        this.scaleViewPagerCurrentChangedItemListener = scaleViewPagerCurrentChangedItemListener;
    }

    private mPageSelectedListener mPageSelectedListener=new mPageSelectedListener() {
        @Override
        public void onPageSelected(int position) {

        }
    };

    public void setmPageSelectedListener(ScaleViewPage.mPageSelectedListener mPageSelectedListener) {
        this.mPageSelectedListener = mPageSelectedListener;
    }

    public interface mPageSelectedListener{
        void onPageSelected(int position);
    }
}
