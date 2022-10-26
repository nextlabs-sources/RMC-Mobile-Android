package com.skydrm.rmc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.engine.eventBusMsg.ChangeExpiryDateEvent;
import com.skydrm.rmc.ui.adapter.CalendarRecyclerAdapter;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.widget.LimitedEditText;
import com.skydrm.rmc.ui.widget.calendar.CalendarView;
import com.skydrm.rmc.ui.widget.calendar.SelectMode;
import com.skydrm.rmc.ui.widget.popupwindow.SelectExpiryWindow;
import com.skydrm.rmc.utils.commonUtils.CalenderUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.rms.rest.user.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by aning on 11/8/2017.
 */

public class ExpiryWindow extends BaseActivity {
    private static final String EXPIRY_NEVER_EXPIRE = "Never Expire";
    private static final String EXPIRY_RELATIVE = "Relative";
    private static final String EXPIRY_DATE_RANGE = "Date Range";
    private static final String EXPIRY_ABSOLUTE_DATE = "Absolute Date";

    private TextView expirySelect;
    private Context context;
    private View rootView;

    private RecyclerView recyclerView;
    private CalendarRecyclerAdapter adapter;

    //for from side show text widget
    private TextView fromDay;
    private TextView fromMonth;
    private TextView fromWeek;

    //for to side show text widget
    private TextView toDay;
    private TextView toMonth;
    private TextView toWeek;
    //count mount of days widget
    private TextView countDay;

    private int mYears;
    private int mMonths;
    private int mWeeks;
    private int mDays;

    private Calendar absoluteEndDate;

    private Calendar rangeStartDate;
    private Calendar rangeEndDate;

    // for set expiry, it true(we will init using default date),
    // for "change" operation, it false, will init using original date
    private boolean bUseDefault;

    private User.IExpiry iExpiry;
    private List<Long> calendarDates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT == 26) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        EventBus.getDefault().register(this);
        context = this;

        Intent intent = getIntent();
        String expiryType = intent.getStringExtra("select_item");

        bUseDefault = intent.getBooleanExtra("use_default", true);

        switch (expiryType) {
            case EXPIRY_NEVER_EXPIRE:
                setContentView(R.layout.expiry_never_layout);
                initCommonLayout(expiryType, bUseDefault);
                initNeverLayout();
                break;
            case EXPIRY_RELATIVE:
                setContentView(R.layout.expiry_relative_layout);
                initCommonLayout(expiryType, bUseDefault);
                initRelativeLayout();
                break;
            case EXPIRY_DATE_RANGE:
                setContentView(R.layout.expiry_date_range_layout);
                initCommonLayout(expiryType, bUseDefault);
                initDateRangeLayout();
                break;
            case EXPIRY_ABSOLUTE_DATE:
                setContentView(R.layout.expiry_absolute_date_layout);
                initCommonLayout(expiryType, bUseDefault);
                initAbsoluteDateLayout();
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onChangeExpiryDateEvent(ChangeExpiryDateEvent event) {
        iExpiry = event.iExpiry;
    }

    public void initCommonLayout(final String expiryType, boolean bUseDefault) {
        this.bUseDefault = bUseDefault;
        rootView = (View) findViewById(R.id.root_layout);
        // expiry select
        expirySelect = (TextView) findViewById(R.id.specify_rights);
        expirySelect.setText(expiryType);
        expirySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int width = expirySelect.getWidth();
                SelectExpiryWindow popupWindow = new SelectExpiryWindow(context, rootView, expirySelect, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.showWindow();
            }
        });

        if (expiryType.equals(EXPIRY_RELATIVE) || expiryType.equals(EXPIRY_DATE_RANGE)) {
            fromDay = (TextView) findViewById(R.id.from_day);
            fromMonth = (TextView) findViewById(R.id.from_month);
            fromWeek = (TextView) findViewById(R.id.from_week);

            toDay = (TextView) findViewById(R.id.to_day);
            toMonth = (TextView) findViewById(R.id.to_month);
            toWeek = (TextView) findViewById(R.id.to_week);
            countDay = (TextView) findViewById(R.id.count_day);

            // init date
            if (bUseDefault) { // use default date to init(30 days)
                rangeStartDate = Calendar.getInstance();
                rangeEndDate = generateDefaultEndDate(Calendar.getInstance());
                updateRelativeOrRangeEndDateText(rangeStartDate, rangeEndDate);
            } else { // use the incoming value to init
                int option = iExpiry.getOption();
                if (option == 1) { // Relative
                    User.IRelative iRelative = (User.IRelative) iExpiry;
                    updateRelativeEndDate(iRelative.getYear(), iRelative.getMonth(), iRelative.getDay(), iRelative.getWeek());
                } else if (option == 3) { // Data range
                    User.IRange iRange = (User.IRange) iExpiry;
                    rangeStartDate = Calendar.getInstance(Locale.getDefault());
                    rangeStartDate.setTime(new Date(iRange.startDate()));
                    rangeEndDate = Calendar.getInstance(Locale.getDefault());
                    rangeEndDate.setTime(new Date(iRange.endDate()));
                    updateRelativeOrRangeEndDateText(rangeStartDate, rangeEndDate);
                }
            }

        } else if (expiryType.equals(EXPIRY_ABSOLUTE_DATE)) {
            toDay = (TextView) findViewById(R.id.to_day);
            toMonth = (TextView) findViewById(R.id.to_month);
            toWeek = (TextView) findViewById(R.id.to_week);
            countDay = (TextView) findViewById(R.id.count_day);

            // init date
            if (bUseDefault) { // use default date to init(30 days)
                absoluteEndDate = generateDefaultEndDate(Calendar.getInstance());
                updateAbsoluteEndDateText(absoluteEndDate);
            } else { // use the incoming value to init
                User.IAbsolute iAbsolute = (User.IAbsolute) iExpiry;
                absoluteEndDate = Calendar.getInstance(Locale.getDefault());
                absoluteEndDate.setTime(new Date(iAbsolute.endDate()));
                updateAbsoluteEndDateText(absoluteEndDate);
            }
        }

        Button btnOk = (Button) findViewById(R.id.bt_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // init "iExpiry" value
                String selectText = expirySelect.getText().toString();
                switch (selectText) {
                    case EXPIRY_NEVER_EXPIRE:
                        iExpiry = new User.IExpiry() {
                            @Override
                            public int getOption() {
                                return 0;
                            }
                        };
                        break;
                    case EXPIRY_RELATIVE:

                        if (mYears == 0 && mMonths == 0 && mWeeks == 0 && mDays == 0) {
                            ToastUtil.showToast(context, getResources().getString(R.string.Specify_relative_rights_validity_period));
                            return;
                        }

                        iExpiry = new User.IRelative() {

                            @Override
                            public int getYear() {
                                return mYears;
                            }

                            @Override
                            public int getMonth() {
                                return mMonths;
                            }

                            @Override
                            public int getWeek() {
                                return mWeeks;
                            }

                            @Override
                            public int getDay() {
                                return mDays;
                            }

                            @Override
                            public int getOption() {
                                return 1; // 1 means Relative
                            }

                        };
                        break;
                    case EXPIRY_ABSOLUTE_DATE:
                        iExpiry = new User.IAbsolute() {
                            @Override
                            public long endDate() {
                                return absoluteEndDate.getTimeInMillis();
                            }

                            @Override
                            public int getOption() {
                                return 2; // 2 means Absolute date
                            }
                        };
                        break;
                    case EXPIRY_DATE_RANGE:
                        iExpiry = new User.IRange() {
                            @Override
                            public long startDate() {
                                return rangeStartDate.getTimeInMillis();
                            }

                            @Override
                            public long endDate() {
                                return rangeEndDate.getTimeInMillis();
                            }

                            @Override
                            public int getOption() {
                                return 3; // 3 means Date Range
                            }
                        };
                        break;
                    default:
                        break;
                }

                EventBus.getDefault().post(new ChangeExpiryDateEvent(iExpiry));
                // close
                finish();
            }
        });

        Button btnCancel = (Button) findViewById(R.id.bt_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.scroll_view);
//        scrollToTop(scrollView);

    }

    public void initNeverLayout() {
        // set the "Never Expire" text style
        TextView neverExpire = (TextView) findViewById(R.id.rights_never_expire);
        Spannable spannable = new SpannableString(neverExpire.getText().toString());
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green_light)), 12, 24, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 12, 24, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        neverExpire.setText(spannable);
    }

    public void initRelativeLayout() {
        LimitedEditText year = (LimitedEditText) findViewById(R.id.et_year);
        LimitedEditText month = (LimitedEditText) findViewById(R.id.et_month);
        LimitedEditText week = (LimitedEditText) findViewById(R.id.et_week);
        LimitedEditText day = (LimitedEditText) findViewById(R.id.et_day);

        if (bUseDefault) {  // set default value is 1 month
            mMonths = 1;
            month.setText(String.valueOf(1));
        } else {
            User.IRelative iRelative = (User.IRelative) iExpiry;
            mYears = iRelative.getYear();
            mMonths = iRelative.getMonth();
            mWeeks = iRelative.getWeek();
            mDays = iRelative.getDay();
            year.setText(String.valueOf(iRelative.getYear()));
            month.setText(String.valueOf(iRelative.getMonth()));
            week.setText(String.valueOf(iRelative.getWeek()));
            day.setText(String.valueOf(iRelative.getDay()));
        }

        year.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    updateRelativeEndDate(0, mMonths, mDays, mWeeks);
                    mYears = 0;
                    return;
                }
                mYears = Integer.parseInt(s.toString());
                updateRelativeEndDate(mYears, mMonths, mDays, mWeeks);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        month.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    updateRelativeEndDate(mYears, 0, mDays, mWeeks);
                    mMonths = 0;
                    return;
                }
                mMonths = Integer.parseInt(s.toString());
                updateRelativeEndDate(mYears, mMonths, mDays, mWeeks);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        day.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    updateRelativeEndDate(mYears, mMonths, 0, mWeeks);
                    mDays = 0;
                    return;
                }
                mDays = Integer.parseInt(s.toString());
                updateRelativeEndDate(mYears, mMonths, mDays, mWeeks);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        week.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    updateRelativeEndDate(mYears, mMonths, mDays, 0);
                    mWeeks = 0;
                    return;
                }
                mWeeks = Integer.parseInt(s.toString());
                updateRelativeEndDate(mYears, mMonths, mDays, mWeeks);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void updateRelativeEndDate(int growthYears, int growthMonths, int growthDays, int growthWeeks) {
        Calendar calendar = Calendar.getInstance();
        //Get current year,month and day ,as for week(translate it to days to calc)
        int curYears = calendar.get(Calendar.YEAR);
        int curMonths = calendar.get(Calendar.MONTH);
        int curDays = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.set(Calendar.YEAR, curYears + growthYears);
        calendar.set(Calendar.MONTH, curMonths + growthMonths);
        calendar.set(Calendar.DAY_OF_MONTH, curDays + growthDays + growthWeeks * 7 - 1);
        calendar.set(Calendar.AM_PM, 1);
        calendar.set(Calendar.HOUR, calendar.getActualMaximum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        //update date show TextView
        updateRelativeOrRangeEndDateText(Calendar.getInstance(), calendar);
    }

    public void initDateRangeLayout() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new CalendarRecyclerAdapter(calendarDates);
        adapter.setRangeDates(rangeStartDate, rangeEndDate);
        adapter.setSelectMode(SelectMode.DOUBLE);
        //fill date range startDate and endDate.
        calendarDates.clear();
        calendarDates.add(rangeStartDate.getTimeInMillis());
//        calendarDates.add(rangeEndDate.getTimeInMillis());
        recyclerView.setAdapter(adapter);

        adapter.setOnDaySelectedListener(new CalendarRecyclerAdapter.OnDaySelectedListener() {
            @Override
            public void onDaySelected(CalendarView view, Calendar day, int position) {
                //only for absolute date select callback.
            }

            @Override
            public void onRangeSelected(CalendarView view, Calendar rangeStart, Calendar rangeEnd) {
                rangeStartDate = generateConfigedStartDate(rangeStart);
                rangeEndDate = generateConfigedEndDate(rangeEnd);
                updateRelativeOrRangeEndDateText(rangeStartDate, rangeEndDate);
            }
        });
    }

    public void initAbsoluteDateLayout() {
        log.d("-----------initAbsoluteDateLayout----------------");
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new CalendarRecyclerAdapter(calendarDates);
        adapter.setSelectMode(SelectMode.SINGLE);
        calendarDates.clear();
        calendarDates.add(absoluteEndDate.getTimeInMillis());
        recyclerView.setAdapter(adapter);

        // set the "Expire on" text style
        TextView expireOn = (TextView) findViewById(R.id.expire_on);
        Spannable spannable = new SpannableString(context.getResources().getString(R.string.Rights_will_expire_on));
        spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.green_light)), 12, 21, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 12, 21, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        expireOn.setText(spannable);

        adapter.setOnDaySelectedListener(new CalendarRecyclerAdapter.OnDaySelectedListener() {
            @Override
            public void onDaySelected(CalendarView view, Calendar day, int position) {
                absoluteEndDate = generateConfigedEndDate(day);
                updateAbsoluteEndDateText(absoluteEndDate);
            }

            @Override
            public void onRangeSelected(CalendarView view, Calendar rangeStart, Calendar rangeEnd) {
                //only for range date select callback
            }
        });
    }

    private Calendar generateConfigedStartDate(Calendar rangeStart) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, rangeStart.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, rangeStart.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, rangeStart.get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.AM_PM, 0);
        calendar.set(Calendar.HOUR, rangeStart.getActualMinimum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, rangeStart.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, rangeStart.getActualMinimum(Calendar.SECOND));
        return calendar;
    }

    private Calendar generateConfigedEndDate(Calendar rangeEnd) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, rangeEnd.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, rangeEnd.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, rangeEnd.get(Calendar.DAY_OF_MONTH));
        //must set am. pm. cause getActualMaximum is 11
        //if you are in am. the maximum time we set is 11:59:59 am. in order to avoid this we set our
        //maximum date is 11:59:59 pm.
        calendar.set(Calendar.AM_PM, 1);
        calendar.set(Calendar.HOUR, rangeEnd.getActualMaximum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, rangeEnd.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, rangeEnd.getActualMaximum(Calendar.SECOND));

        return calendar;
    }

    private void updateRelativeOrRangeEndDateText(Calendar startDate, Calendar endDate) {
        //for from side
        fromMonth.setText(CalenderUtils.getMonthLabel(startDate));
        fromWeek.setText(CalenderUtils.getWeekLabel(startDate) + " " + startDate.get(Calendar.YEAR));
        fromDay.setText(String.valueOf(startDate.get(Calendar.DAY_OF_MONTH)));
        //for to side
        toMonth.setText(CalenderUtils.getMonthLabel(endDate));

        toWeek.setText(CalenderUtils.getWeekLabel(endDate) + " " + endDate.get(Calendar.YEAR));
        toDay.setText(String.valueOf(endDate.get(Calendar.DAY_OF_MONTH)));
        countDay.setText(String.valueOf(CalenderUtils.countDays(startDate, endDate)));
        countDay.append(" days");
    }

    private void updateAbsoluteEndDateText(Calendar endDate) {
        toMonth.setText(CalenderUtils.getMonthLabel(endDate));
        toWeek.setText(CalenderUtils.getWeekLabel(endDate) + " " + endDate.get(Calendar.YEAR));
        toDay.setText(String.valueOf(endDate.get(Calendar.DAY_OF_MONTH)));
        countDay.setText(String.valueOf(CalenderUtils.countDays(Calendar.getInstance(), endDate)));
        countDay.append(" days");
    }

    private Calendar generateDefaultEndDate(Calendar currentDate) {
        if (currentDate == null) {
            return null;
        }
        Calendar defaultEndDate = Calendar.getInstance();
        defaultEndDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
        defaultEndDate.set(Calendar.MONTH, currentDate.get(Calendar.MONTH) + 1);
        defaultEndDate.set(Calendar.DAY_OF_MONTH, currentDate.get(Calendar.DAY_OF_MONTH) - 1);
        defaultEndDate.set(Calendar.AM_PM, 1);
        defaultEndDate.set(Calendar.HOUR, currentDate.getActualMaximum(Calendar.HOUR));
        defaultEndDate.set(Calendar.MINUTE, currentDate.getActualMaximum(Calendar.MINUTE));
        defaultEndDate.set(Calendar.SECOND, currentDate.getActualMaximum(Calendar.SECOND));
        return defaultEndDate;
    }
//    // control scrollview to scroll to layout top
//    private void scrollToTop(final ScrollView scrollView) {
//        scrollView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                scrollView.fullScroll(ScrollView.FOCUS_UP);
//            }
//        }, 100L);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().unregister(this);
    }
}
