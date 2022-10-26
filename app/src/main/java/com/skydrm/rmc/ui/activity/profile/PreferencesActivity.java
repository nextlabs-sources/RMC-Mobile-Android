package com.skydrm.rmc.ui.activity.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.engine.eventBusMsg.ChangeExpiryDateEvent;
import com.skydrm.rmc.engine.watermark.EditWatermarkHelper;
import com.skydrm.rmc.engine.watermark.WatermarkSetInvalidEvent;
import com.skydrm.rmc.presenter.IUserPreferencePresenter;
import com.skydrm.rmc.presenter.impl.UserPreferencePresenter;
import com.skydrm.rmc.ui.activity.ExpiryWindow;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.widget.LoadingDialog2;
import com.skydrm.rmc.ui.widget.customcontrol.EditWatermarkWidget;
import com.skydrm.rmc.utils.commonUtils.CalenderUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.rms.rest.user.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hhu on 11/7/2017.
 */

public class PreferencesActivity extends BaseActivity implements IUserPreferenceView {
    private static DevLog log = new DevLog("PreferencesActivity");
    @BindView(R.id.root_layout)
    LinearLayout rootView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.edit_watermark_widget)
    EditWatermarkWidget watermarkWidget;
    @BindView(R.id.text_validity_period)
    TextView validityPeriod;
    @BindView(R.id.bt_ok)
    Button save;
    @BindView(R.id.specify_rights)
    TextView expirySelect;

    @BindView(R.id.timetable)
    LinearLayout timetable;
    @BindView(R.id.absolute_timetable)
    LinearLayout absoluteTimetable;

    //for from side show text widget
    @BindView(R.id.from_day)
    TextView fromDay;
    @BindView(R.id.from_month)
    TextView fromMonth;
    @BindView(R.id.from_week)
    TextView fromWeek;

    //for to side show text widget
    @BindView(R.id.to_day)
    TextView toDay;
    @BindView(R.id.to_month)
    TextView toMonth;
    @BindView(R.id.to_week)
    TextView toWeek;
    //count mount of days widget
    @BindView(R.id.count_day)
    TextView countDay;

    @BindView(R.id.expire_on)
    TextView expiredOn;
    //for to side show text widget
    @BindView(R.id.to_day_absolute)
    TextView toDayABS;
    @BindView(R.id.to_month_absolute)
    TextView toMonthABS;
    @BindView(R.id.to_week_absolute)
    TextView toWeekABS;
    @BindView(R.id.count_day_absolute)
    TextView countDayABS;

    private Context mContext;
    private static final String WATERMARK_VALUE_KEY = "watermark_value_key";
    private String watermarkValue = "$(User)$(Break)$(Date)$(Time)";
    private String validityPeriodText = "Access rights will Never Expire";
    private String subStr = "Never Expire";

    private static final String EXPIRY_NEVER_EXPIRE = "Never Expire";
    private static final String EXPIRY_RELATIVE = "Relative";
    private static final String EXPIRY_ABSOLUTE_DATE = "Absolute Date";
    private static final String EXPIRY_DATE_RANGE = "Date Range";


    private int mYears;
    private int mMonths = 1;
    private int mDays;
    private int mWeeks;

    private long relativeEndDate;
    private long absoluteEndDate;
    private long rangeStartDate;
    private long rangeEndDate;

    private static final String DEFAULT_FORMAT_PATTERN = "MMMM dd, yyyy";

    private IUserPreferencePresenter userPreference;
    private String mCheckedId = EXPIRY_NEVER_EXPIRE;

    private String[] expireSpinnerArray = new String[]{
            EXPIRY_NEVER_EXPIRE,
            EXPIRY_RELATIVE,
            EXPIRY_DATE_RANGE,
            EXPIRY_ABSOLUTE_DATE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        setUpToolbar(toolbar);

        mContext = this;
        userPreference = new UserPreferencePresenter(this);

        relativeEndDate = generateDefaultEndDate(Calendar.getInstance()).getTimeInMillis();
        absoluteEndDate = generateDefaultEndDate(Calendar.getInstance()).getTimeInMillis();

        rangeStartDate = Calendar.getInstance().getTimeInMillis();
        rangeEndDate = generateDefaultEndDate(Calendar.getInstance()).getTimeInMillis();

        neverExpire();
        expirySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ListPopupWindow popupWindow = new ListPopupWindow(mContext);
                ArrayAdapter<String> expireSpinnerAdapter = new ArrayAdapter<String>
                        (mContext, R.layout.item_list_popup, expireSpinnerArray);
                expireSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                popupWindow.setAdapter(expireSpinnerAdapter);
                popupWindow.setAnchorView(expirySelect);
                popupWindow.show();

                ListView listView = popupWindow.getListView();
                if (listView != null) {
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            switch (position) {
                                case 0:
                                    neverExpire();
                                    break;
                                case 1:
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
                                            return 1;
                                        }
                                    };
                                    //update time table text
                                    relative(iExpiry);
                                    // post expiry value to ExpiryWindow to change
                                    Intent relative = new Intent(mContext, ExpiryWindow.class);
                                    EventBus.getDefault().postSticky(new ChangeExpiryDateEvent(iExpiry));
                                    relative.putExtra("select_item", EXPIRY_RELATIVE);
                                    relative.putExtra("use_default", false);
                                    startActivity(relative);
                                    break;
                                case 2:
                                    iExpiry = new User.IRange() {
                                        @Override
                                        public long startDate() {
                                            return rangeStartDate;
                                        }

                                        @Override
                                        public long endDate() {
                                            return rangeEndDate;
                                        }

                                        @Override
                                        public int getOption() {
                                            return 3;
                                        }
                                    };
                                    //update time table text
                                    range(iExpiry);
                                    // post expiry value to ExpiryWindow to change
                                    Intent dataRange = new Intent(mContext, ExpiryWindow.class);
                                    EventBus.getDefault().postSticky(new ChangeExpiryDateEvent(iExpiry));
                                    dataRange.putExtra("select_item", EXPIRY_DATE_RANGE);
                                    dataRange.putExtra("use_default", false);
                                    startActivity(dataRange);
                                    break;
                                case 3:
                                    iExpiry = new User.IAbsolute() {
                                        @Override
                                        public long endDate() {
                                            return absoluteEndDate;
                                        }

                                        @Override
                                        public int getOption() {
                                            return 2;
                                        }
                                    };
                                    //update time table text
                                    absolute(iExpiry);
                                    // post expiry value to ExpiryWindow to change
                                    Intent absolute = new Intent(mContext, ExpiryWindow.class);
                                    EventBus.getDefault().postSticky(new ChangeExpiryDateEvent(iExpiry));
                                    absolute.putExtra("select_item", EXPIRY_ABSOLUTE_DATE);
                                    absolute.putExtra("use_default", false);
                                    startActivity(absolute);
                                    break;
                            }
                            popupWindow.dismiss();
                        }
                    });
                }
            }
        });

        userPreference.retrieveUserPreference();
    }

    private void range(User.IExpiry iExpiry) {
        if (iExpiry == null) {
            return;
        }
        validityPeriod.setVisibility(View.GONE);
        timetable.setVisibility(View.VISIBLE);
        absoluteTimetable.setVisibility(View.GONE);
        if (iExpiry instanceof User.IRange) {
            User.IRange range = (User.IRange) iExpiry;
            rangeStartDate = range.startDate();
            rangeEndDate = range.endDate();
            updateRelativeOrRangeEndDateText(convertFromTimeMillis(rangeStartDate),
                    convertFromTimeMillis(rangeEndDate));
        }
        mCheckedId = EXPIRY_DATE_RANGE;
        expirySelect.setText(EXPIRY_DATE_RANGE);
    }

    private void absolute(User.IExpiry iExpiry) {
        if (iExpiry == null) {
            return;
        }
        validityPeriod.setVisibility(View.GONE);
        timetable.setVisibility(View.GONE);
        absoluteTimetable.setVisibility(View.VISIBLE);
        Spannable spannable = new SpannableString(getString(R.string.Rights_will_expire_on));
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green_light)),
                12, 21, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 12, 21, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        expiredOn.setText(spannable);

        if (iExpiry instanceof User.IAbsolute) {
            User.IAbsolute absolute = (User.IAbsolute) iExpiry;
            absoluteEndDate = absolute.endDate();
            updateAbsoluteEndDateText(convertFromTimeMillis(absoluteEndDate));
        }
        mCheckedId = EXPIRY_ABSOLUTE_DATE;
        expirySelect.setText(EXPIRY_ABSOLUTE_DATE);
    }

    private Calendar convertFromTimeMillis(long timemillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timemillis);
        return calendar;
    }

    private void relative(User.IExpiry iExpiry) {
        if (iExpiry == null) {
            return;
        }
        validityPeriod.setVisibility(View.GONE);
        timetable.setVisibility(View.VISIBLE);
        absoluteTimetable.setVisibility(View.GONE);
        if (iExpiry instanceof User.IRelative) {
            User.IRelative relative = (User.IRelative) iExpiry;
            mYears = relative.getYear();
            mMonths = relative.getMonth();
            mDays = relative.getDay();
            mWeeks = relative.getWeek();
            updateRelativeOrRangeEndDateText(Calendar.getInstance(),
                    convertRelativeEndDate(mYears, mMonths,
                            mDays, mWeeks));
        }
        mCheckedId = EXPIRY_RELATIVE;
        expirySelect.setText(EXPIRY_RELATIVE);
    }

    private Calendar convertRelativeEndDate(int years, int months, int days, int weeks) {
        Calendar calendar = Calendar.getInstance();
        //Get current year,month and day ,as for week(translate it to days to calc)
        int curYears = calendar.get(Calendar.YEAR);
        int curMonths = calendar.get(Calendar.MONTH);
        int curDays = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.set(Calendar.YEAR, curYears + years);
        calendar.set(Calendar.MONTH, curMonths + months);
        calendar.set(Calendar.DAY_OF_MONTH, curDays + days + weeks * 7 - 1);
        calendar.set(Calendar.AM_PM, 1);
        calendar.set(Calendar.HOUR, calendar.getActualMaximum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        return calendar;
    }

    private void neverExpire() {
        validityPeriodText = "Access rights will Never Expire";
        subStr = "Never Expire";
        validityPeriod.setVisibility(View.VISIBLE);
        timetable.setVisibility(View.GONE);
        absoluteTimetable.setVisibility(View.GONE);

        updateValidityPeriod(0, 0, validityPeriodText);
        mCheckedId = EXPIRY_NEVER_EXPIRE;
        expirySelect.setText(EXPIRY_NEVER_EXPIRE);
    }

    private void setUpToolbar(Toolbar toolbar) {
        toolbar.setBackgroundColor(getResources().getColor(android.R.color.black));
        toolbar.setNavigationIcon(R.drawable.icon_back_white);
        toolbar.setTitle(R.string.preferences);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updateValidityPeriod(long startmillis, long endMillis, String validityText) {
        if (validityText.contains("[start]")) {
            validityText = validityText.replace("[start]", formatTime(startmillis, ""));
        }
        if (validityText.contains("[end]")) {
            validityText = validityText.replace("[end]", formatTime(endMillis, ""));
        }
        validityPeriod.setText(getSpannableString(validityText, subStr.equals("Never Expire") ?
                R.color.validity_text_color : android.R.color.black));
    }

    private SpannableStringBuilder getSpannableString(String validityText, int color) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(validityText);
        stringBuilder.setSpan(new ForegroundColorSpan(getResources().getColor(color)), validityText.indexOf(subStr),
                validityText.indexOf(subStr) + subStr.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), validityText.indexOf(subStr),
                validityText.indexOf(subStr) + subStr.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return stringBuilder;
    }

    private String formatTime(@NonNull long tmillis, @Nullable String formatPattern) {
        String pattern = TextUtils.isEmpty(formatPattern) ? DEFAULT_FORMAT_PATTERN : formatPattern;
        final DateFormat formatter = new SimpleDateFormat(pattern, Locale.getDefault());
        return formatter.format(tmillis);
    }

    /**
     * Get watermark value
     *
     * @return watermark value
     */
    public String getWatermarkValue() {
        return watermarkValue;
    }

    private User.IExpiry iExpiry;

    @OnClick(R.id.bt_ok)
    public void saveChange() {
        switch (mCheckedId) {
            case EXPIRY_NEVER_EXPIRE:
                iExpiry = new User.IExpiry() {
                    @Override
                    public int getOption() {
                        return 0;
                    }
                };
                break;
            case EXPIRY_RELATIVE:
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
                        return 1;
                    }
                };
                break;
            case EXPIRY_ABSOLUTE_DATE:
                iExpiry = new User.IAbsolute() {
                    @Override
                    public long endDate() {
                        return absoluteEndDate;
                    }

                    @Override
                    public int getOption() {
                        return 2;
                    }
                };
                break;
            case EXPIRY_DATE_RANGE:
                iExpiry = new User.IRange() {
                    @Override
                    public long startDate() {
                        return rangeStartDate;
                    }

                    @Override
                    public long endDate() {
                        return rangeEndDate;
                    }

                    @Override
                    public int getOption() {
                        return 3;
                    }
                };
                break;
        }
        watermarkValue = EditWatermarkHelper.imageSpan2StringEx(watermarkWidget.getEditText());
        userPreference.updateUserPreference(watermarkValue, iExpiry);
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
        toMonthABS.setText(CalenderUtils.getMonthLabel(endDate));
        toWeekABS.setText(CalenderUtils.getWeekLabel(endDate) + " " + endDate.get(Calendar.YEAR));
        toDayABS.setText(String.valueOf(endDate.get(Calendar.DAY_OF_MONTH)));
        countDayABS.setText(String.valueOf(CalenderUtils.countDays(Calendar.getInstance(), endDate)));
        countDayABS.append(" days");
    }

    private LoadingDialog2 mLoadingDialog = null;

    @Override
    public void loading(boolean done) {
        if (!done) {
            mLoadingDialog = LoadingDialog2.newInstance();
            mLoadingDialog.showModalDialog(this);
        } else {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismissDialog();
                mLoadingDialog = null;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(WATERMARK_VALUE_KEY, watermarkValue);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            watermarkValue = savedInstanceState.getString(WATERMARK_VALUE_KEY);
        }
    }

    @Override
    public void onUpdatePreference(String result) {
        ToastUtil.showToast(getApplicationContext(), "Operation success.");
        PreferencesActivity.this.finish();
    }

    @Override
    public void onRetrievePreference(String result) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("results")) {
                    JSONObject results = jsonObject.getJSONObject("results");
                    if (results.has("watermark")) {
                        watermarkValue = results.getString("watermark");
                        EditWatermarkHelper.string2imageSpanForPreference(mContext, watermarkValue,
                                watermarkWidget.getEditText(), watermarkWidget.getFlowLayout());
                        watermarkWidget.initFlowLayout();
                    }
                    if (results.has("expiry")) {
                        JSONObject expiry = results.getJSONObject("expiry");
                        if (expiry.has("option")) {
                            int option = expiry.getInt("option");
                            switch (option) {
                                case 0:
                                    neverExpire();
                                    break;
                                case 1:
                                    JSONObject relativeDay = expiry.getJSONObject("relativeDay");
                                    mYears = relativeDay.optInt("year");
                                    mMonths = relativeDay.optInt("month");
                                    mWeeks = relativeDay.optInt("week");
                                    mDays = relativeDay.optInt("day");

                                    relative(new User.IRelative() {
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
                                            return 1;
                                        }
                                    });
                                    break;
                                case 2:
                                    absoluteEndDate = expiry.getLong("endDate");
                                    absolute(new User.IAbsolute() {
                                        @Override
                                        public long endDate() {
                                            return absoluteEndDate;
                                        }

                                        @Override
                                        public int getOption() {
                                            return 2;
                                        }
                                    });
                                    break;
                                case 3:
                                    rangeStartDate = expiry.getLong("startDate");
                                    rangeEndDate = expiry.getLong("endDate");
                                    range(new User.IRange() {
                                        @Override
                                        public long startDate() {
                                            return rangeStartDate;
                                        }

                                        @Override
                                        public long endDate() {
                                            return rangeEndDate;
                                        }

                                        @Override
                                        public int getOption() {
                                            return 3;
                                        }
                                    });
                                    break;
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpdateFailed(Exception e) {
        if (e != null) {
            ToastUtil.showToast(getApplicationContext(), e.getMessage());
        }
        if (mLoadingDialog != null) {
            mLoadingDialog.dismissDialog();
            mLoadingDialog = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleInvalidWatermark(WatermarkSetInvalidEvent event) {
        save.setEnabled(!event.bSetInvalid);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateValidity(ChangeExpiryDateEvent expiryDateEvent) {
        iExpiry = expiryDateEvent.iExpiry;
        if (iExpiry != null) {
            parseIExpiry(iExpiry);
        }
    }

    private void parseIExpiry(User.IExpiry iExpiry) {
        if (iExpiry == null)
            return;
        switch (iExpiry.getOption()) {
            case 0://never expire
                neverExpire();
                break;
            case 1://relative
                relative(iExpiry);
                break;
            case 2://absolute
                absolute(iExpiry);
                break;
            case 3://range select
                range(iExpiry);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
