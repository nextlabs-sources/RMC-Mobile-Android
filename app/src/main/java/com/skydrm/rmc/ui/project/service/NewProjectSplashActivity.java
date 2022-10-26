package com.skydrm.rmc.ui.project.service;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.activity.splash.SplashPagerAdapter;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.widget.autoscrollviewpager.AutoScrollViewPager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewProjectSplashActivity extends BaseActivity {
    // for features splash section
    @BindView(R.id.splash_view_pager)
    AutoScrollViewPager featuresViewPager;

    private List<View> featureViewLists = new ArrayList<>();
    private List<ImageView> scrollableDots = new ArrayList<>();
    private Button splashCreateAProject;
    private Toolbar newProjectSplashToolbar3;
    private Button splashCreateProjectCancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project_splash3);
        ButterKnife.bind(this);
        initView();
        initEvent();
    }

    private void initView() {
        // get the width and height of screen.
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        View featurePage1 = LayoutInflater.from(this).inflate(R.layout.layout_splash_create_project_page1, null);
        View featurePage2 = LayoutInflater.from(this).inflate(R.layout.layout_splash_create_project_page2, null);
        View featurePage3 = LayoutInflater.from(this).inflate(R.layout.layout_splash_create_project_page3, null);
        View featurePage4 = LayoutInflater.from(this).inflate(R.layout.layout_splash_create_project_page4, null);
        View featurePage5 = LayoutInflater.from(this).inflate(R.layout.layout_splash_create_project_page5, null);

        featureViewLists.add(featurePage1);
        featureViewLists.add(featurePage2);
        featureViewLists.add(featurePage3);
        featureViewLists.add(featurePage4);
        featureViewLists.add(featurePage5);

        ImageView scrollDot1 = findViewById(R.id.scroll_dot1);
        ImageView scrollDot2 = findViewById(R.id.scroll_dot2);
        ImageView scrollDot3 = findViewById(R.id.scroll_dot3);
        ImageView scrollDot4 = findViewById(R.id.scroll_dot4);
        ImageView scrollDot5 = findViewById(R.id.scroll_dot5);

        scrollableDots.add(scrollDot1);
        scrollableDots.add(scrollDot2);
        scrollableDots.add(scrollDot3);
        scrollableDots.add(scrollDot4);
        scrollableDots.add(scrollDot5);

        featuresViewPager.setAdapter(new SplashPagerAdapter(featureViewLists));
        featuresViewPager.setCurrentItem(0);
        scrollableDots.get(0).setVisibility(View.VISIBLE);

        featuresViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int newPos = position % featureViewLists.size();
                for (int i = 0; i < scrollableDots.size(); i++) {
                    scrollableDots.get(i).setVisibility(newPos == i ?
                            View.VISIBLE :
                            View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        featuresViewPager.startAutoScroll();

        splashCreateAProject = findViewById(R.id.splash_create_a_project);
        newProjectSplashToolbar3 = findViewById(R.id.new_project_splash_toolbar3);
        splashCreateProjectCancel = findViewById(R.id.splash_create_project_cancel);
    }

    private void initEvent() {
        splashCreateAProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent newProjectIntent = new Intent(NewProjectSplashActivity.this, NewProjectActivity.class);
                NewProjectSplashActivity.this.startActivity(newProjectIntent);
            }
        });
        newProjectSplashToolbar3.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        splashCreateProjectCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (featuresViewPager != null) {
            featuresViewPager.startAutoScroll();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (featuresViewPager != null) {
            featuresViewPager.stopAutoScroll();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (featuresViewPager != null) {
            featuresViewPager.stopAutoScroll();
        }
    }
}
