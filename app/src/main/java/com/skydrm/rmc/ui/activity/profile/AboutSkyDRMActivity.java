package com.skydrm.rmc.ui.activity.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.utils.AppVersionHelper;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hhu on 5/9/2017.
 */

public class AboutSkyDRMActivity extends BaseActivity {
    @BindView(R.id.to_nextlabs)
    TextView mTvJumpLink;
    @BindView(R.id.subview_container)
    FrameLayout subviewContainer;
    private ProfileSubviewLoader subviewLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_skydrm);
        ButterKnife.bind(this);
        initViewAndEvents();
    }

    private void initViewAndEvents() {
        //tool bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // app version
        TextView tvAppVersion = findViewById(R.id.tv_app_version);
        tvAppVersion.setText(String.format(Locale.getDefault(), getResources().getString(R.string.version_),
                AppVersionHelper.getVersionName(this)));
        mTvJumpLink.setText(String.format(Locale.getDefault(), getString(R.string.copy_rights),
                getCurrentYear()));
        mTvJumpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subviewLoader = new ProfileSubviewLoader(AboutSkyDRMActivity.this,
                        ProfileSubviewLoader.SubviewType.HELP_VIEW,
                        subviewContainer);
                subviewLoader.dispatchSubview();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (subviewLoader != null) {
                return subviewLoader.handleBackKey() || super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private int getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

}
