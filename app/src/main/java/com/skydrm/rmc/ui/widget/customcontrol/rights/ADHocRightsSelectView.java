package com.skydrm.rmc.ui.widget.customcontrol.rights;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.engine.eventBusMsg.ChangeExpiryDateEvent;
import com.skydrm.rmc.engine.watermark.EditWatermarkHelper;
import com.skydrm.rmc.exceptions.ExceptionDialog;
import com.skydrm.rmc.ui.activity.ExpiryWindow;
import com.skydrm.rmc.ui.base.IDisplayWatermark;
import com.skydrm.rmc.ui.widget.customcontrol.EditWatermarkWidget;
import com.skydrm.rmc.utils.commonUtils.CalenderUtils;
import com.skydrm.rmc.utils.commonUtils.ExpiryChecker;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.user.User;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ADHocRightsSelectView extends ScrollView {
    // expiry date type
    private static final String EXPIRY_NEVER_EXPIRE = "Never Expire";
    private static final String EXPIRY_RELATIVE = "Relative";
    private static final String EXPIRY_DATE_RANGE = "Date Range";
    private static final String EXPIRY_ABSOLUTE_DATE = "Absolute Date";
    private Context mCtx;

    private SwitchCompat mSwView;
    private SwitchCompat mSwPrint;
    private SwitchCompat mSwEdit;
    private SwitchCompat mSwSaveAs;
    private SwitchCompat mSwReShare;
    private SwitchCompat mSwWatermark;
    private RelativeLayout mRlWatermarkChangeLayout;
    private TextView mTvWatermark;
    private TextView mTvChange;

    private String mWatermarkValue;
    private IDisplayWatermark mWatermark = new IDisplayWatermark() {
        @Override
        public String getValue() {
            return SkyDRMApp.getInstance()
                    .getSession()
                    .getUserPreference()
                    .getWatermarkValue();
        }
    };
    private EditWatermarkWidget mEditWatermarkWidget;

    // for expiry date
    private User.IExpiry mExpiry;
    private LinearLayout mLlChangeCommonDate;
    private LinearLayout mLlChangeAbsoluteDate;
    private LinearLayout mLlChangeNeverDate;

    private TextView mTvFromMonth;
    private TextView mTvFromWeek;
    private TextView mTvFromDay;

    private TextView mTvToMonth;
    private TextView mTvToWeek;
    private TextView mTvToDay;
    private TextView mTvCountDay;

    private TextView mTvAbsToMonth;
    private TextView mTvAbsToWeek;
    private TextView mTvAbsToDay;
    private TextView mTvAbsCountDay;

    // calendar
    private Calendar mAbsoluteEndDate;
    private Calendar mRangeStartDate;
    private Calendar mRangeEndDate;
    private View mRoot;

    private boolean isSwExtractChecked;

    public ADHocRightsSelectView(Context context) {
        this(context, null);
    }

    public ADHocRightsSelectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ADHocRightsSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setBackgroundColor(Color.parseColor("#F2F3F5"));
        initView(context);
    }

    public void setWatermark(IDisplayWatermark watermark) {
        mWatermark = watermark;
        mWatermarkValue = watermark.getValue();
    }

    public void setExpiry(String expiryRaw) {
        if (expiryRaw == null || expiryRaw.isEmpty()) {
            return;
        }
        try {
            JSONObject expiry = null;
            expiry = new JSONObject(expiryRaw);
            if (expiry.has("option")) {
                int option = expiry.getInt("option");
                switch (option) {
                    case 0:
                        mExpiry = new User.IExpiry() {
                            @Override
                            public int getOption() {
                                return 0;
                            }
                        };
                        break;
                    case 1:
                        final JSONObject relativeDay = expiry.getJSONObject("relativeDay");
                        final int mYears = relativeDay.optInt("year");
                        final int mMonths = relativeDay.optInt("month");
                        final int mWeeks = relativeDay.optInt("week");
                        final int mDays = relativeDay.optInt("day");
                        mExpiry = new User.IRelative() {
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
                    case 2:
                        final long absoluteEndDate = expiry.getLong("endDate");
                        mExpiry = new User.IAbsolute() {
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
                    case 3:
                        final long rangeStartDate = expiry.getLong("startDate");
                        final long rangeEndDate = expiry.getLong("endDate");
                        mExpiry = new User.IRange() {
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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // init default expiry date.
        displayExpiryDate(mRoot);
    }

    public void setExpiry(User.IExpiry expiry) {
        mExpiry = expiry;
        // init default expiry date.
        displayExpiryDate(mRoot);
    }

    /**
     * get rights from UI for normal file
     */
    public Rights getRights() {
        Rights rights = new Rights();
        if (mSwView.isChecked()) {
            rights.setView(true);
        }
        if (mSwPrint.isChecked()) {
            rights.setPrint(true);
        }
        if (mSwEdit.isChecked()) {
            rights.setEdit(true);
        }
        if (mSwSaveAs.isChecked()) {
            rights.setDownload(true);
        }
        if (mSwReShare.isChecked()) {
            rights.setShare(true);
        }
        // added for sharing local file
        if (mSwWatermark.isChecked()) {
            rights.setWatermark(true);
        }
        if (isSwExtractChecked) {
            rights.setDecrypt(true);
        }
        return rights;
    }

    /**
     * get obligations from UI for normal file, now only for overlay.
     */
    public Obligations getObligations() {
        Map<String, String> map = new HashMap<>();
        Obligations nxObligations = new Obligations();
        if (mSwWatermark.isChecked()) {
            map.put(Constant.RIGHTS_WATERMARK, mWatermarkValue);
        } else {
            map.put(Constant.RIGHTS_WATERMARK, null); // means no watermark
        }
        nxObligations.setObligation(map);

        return nxObligations;
    }

    /**
     * Build expiry and write file header when protect or share
     *
     * @return
     */
    public Expiry getExpiry() {
        // for share nxl file, will pass null
        if (mExpiry == null) {
            return null;
        }
        int option = mExpiry.getOption();
        switch (option) {
            case 0:
                return new Expiry.Builder().never().build();
            case 1:
                User.IRelative iRelative = (User.IRelative) mExpiry;
                return new Expiry.Builder().relative()
                        .setYear(iRelative.getYear())
                        .setMonth(iRelative.getMonth())
                        .setWeek(iRelative.getWeek())
                        .setDay(iRelative.getDay())
                        .build();
            case 2:
                User.IAbsolute iAbsolute = (User.IAbsolute) mExpiry;
                return new Expiry.Builder().absolute()
                        .setEndDate(iAbsolute.endDate())
                        .build();
            case 3:
                User.IRange iRange = (User.IRange) mExpiry;
                return new Expiry.Builder().range()
                        .setStartDate(iRange.startDate())
                        .setEndDate(iRange.endDate())
                        .build();
            default:
                throw new RuntimeException("Can not access this!");
        }
    }

    public boolean isExpired() {
        return mExpiry != null && isExpired(mExpiry);
    }

    public void setSwExtractChecked(boolean checked) {
        isSwExtractChecked = checked;
    }

    public boolean isSwExtractChecked() {
        return isSwExtractChecked;
    }

    private void initView(Context ctx) {
        mCtx = ctx;
        mRoot = LayoutInflater.from(mCtx).inflate(R.layout.layout_adhoc_rights_select_view,
                this, true);
        mSwView = mRoot.findViewById(R.id.toggle_view);

        TextView tvPrint = mRoot.findViewById(R.id.textPrint);
        mSwPrint = mRoot.findViewById(R.id.toggle_print);

        TextView tvEdit = mRoot.findViewById(R.id.textEdit);
        mSwEdit = mRoot.findViewById(R.id.toggle_edit);

        TextView tvSaveAs = mRoot.findViewById(R.id.textDownload);
        mSwSaveAs = mRoot.findViewById(R.id.toggle_download);

        TextView tvReShare = mRoot.findViewById(R.id.textShare);
        mSwReShare = mRoot.findViewById(R.id.toggle_share);

        TextView tvWatermark = mRoot.findViewById(R.id.textWatermark);
        mSwWatermark = mRoot.findViewById(R.id.toggle_watermark);

        initRightsTextClick(tvPrint, mSwPrint);
        initRightsTextClick(tvEdit, mSwEdit);
        initRightsTextClick(tvSaveAs, mSwSaveAs);
        initRightsTextClick(tvReShare, mSwReShare);
        initRightsTextClick(tvWatermark, mSwWatermark);

        initWaterMarkView(mRoot);
        initExpiryView(mRoot);
    }

    private void initWaterMarkView(View root) {
        mRlWatermarkChangeLayout = root.findViewById(R.id.change_watermark_layout);
        mTvWatermark = root.findViewById(R.id.watermark_value);
        mTvChange = root.findViewById(R.id.change_label);

        mWatermarkValue = mWatermark.getValue();
        mSwWatermark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mRlWatermarkChangeLayout.setVisibility(View.VISIBLE);
                    // display watermark
                    mTvWatermark.setText(""); // reset
                    EditWatermarkHelper.string2imageSpanForDisplay(mCtx, mWatermarkValue, mTvWatermark);
                    // set underline
                    mTvChange.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                } else {
                    mRlWatermarkChangeLayout.setVisibility(View.GONE);
                }
            }
        });

        mTvChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
                RelativeLayout layout = (RelativeLayout) LayoutInflater.from(mCtx).inflate(R.layout.change_watermark_value_layout, null);
                builder.setView(layout);
                final AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

                mEditWatermarkWidget = layout.findViewById(R.id.edit_watermark_widget);
                mEditWatermarkWidget.setEditValue(mWatermarkValue);
                // cancel button
                Button btnEditWatermarkCancel = layout.findViewById(R.id.btn_cancel);
                btnEditWatermarkCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                // ok button
                Button btnEditWatermarkOk = layout.findViewById(R.id.btn_ok);
                btnEditWatermarkOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get the edited watermark value
                        EditText editText = mEditWatermarkWidget.getEditText();

                        // prevent user input empty watermark.
                        if (editText.getEditableText().toString().trim().equals("")) {
                            ToastUtil.showToast(mCtx, mCtx.getResources().getString(R.string.watermark_can_not_is_empty));
                            return;
                        }
                        if (!mEditWatermarkWidget.checkValidity()) {
                            return;
                        }

                        mWatermarkValue = EditWatermarkHelper.imageSpan2StringEx(editText);
                        // display watermark
                        mTvWatermark.setText(""); // reset
                        EditWatermarkHelper.string2imageSpanForDisplay(mCtx, mWatermarkValue, mTvWatermark);

                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void initExpiryView(View root) {
        // init controls
        mLlChangeCommonDate = root.findViewById(R.id.change_common_layout);
        mLlChangeAbsoluteDate = root.findViewById(R.id.change_absolute_layout);
        mLlChangeNeverDate = root.findViewById(R.id.change_never_layout);

        mTvFromMonth = root.findViewById(R.id.from_month);
        mTvFromWeek = root.findViewById(R.id.from_week);
        mTvFromDay = root.findViewById(R.id.from_day);

        mTvToMonth = root.findViewById(R.id.to_month);
        mTvToWeek = root.findViewById(R.id.to_week);
        mTvToDay = root.findViewById(R.id.to_day);

        mTvCountDay = root.findViewById(R.id.count_day);
        // Absolute date
        mTvAbsToMonth = root.findViewById(R.id.abs_to_month);
        mTvAbsToWeek = root.findViewById(R.id.abs_to_week);
        mTvAbsToDay = root.findViewById(R.id.abs_to_day);
        mTvAbsCountDay = root.findViewById(R.id.abs_count_day);
    }

    public void displayExpiryDate(View root) {
        if (mExpiry == null || root == null) {
            return;
        }
        switch (mExpiry.getOption()) {
            case 0: // Never expiry
                mLlChangeCommonDate.setVisibility(View.GONE);
                mLlChangeAbsoluteDate.setVisibility(View.GONE);
                mLlChangeNeverDate.setVisibility(View.VISIBLE);
                // change operate
                TextView neverChange = root.findViewById(R.id.never_change);
                initExpiryChange(neverChange, EXPIRY_NEVER_EXPIRE);
                break;
            case 1: // relative
                mLlChangeCommonDate.setVisibility(View.VISIBLE);
                mLlChangeAbsoluteDate.setVisibility(View.GONE);
                mLlChangeNeverDate.setVisibility(View.GONE);
                // set data
                User.IRelative relative = (User.IRelative) mExpiry;
                updateRelativeEndDate(relative.getYear(), relative.getMonth(), relative.getDay(), relative.getWeek());

                // change operate
                TextView relativeChange = root.findViewById(R.id.change);
                initExpiryChange(relativeChange, EXPIRY_RELATIVE);
                break;
            case 2: // absolute
                mLlChangeCommonDate.setVisibility(View.GONE);
                mLlChangeAbsoluteDate.setVisibility(View.VISIBLE);
                mLlChangeNeverDate.setVisibility(View.GONE);

                // set the "Expire on" text style
                TextView expireOn = root.findViewById(R.id.expire_on);
                Spannable spannable = new SpannableString(mCtx.getResources().getString(R.string.Rights_will_expire_on));
                spannable.setSpan(new ForegroundColorSpan(mCtx.getResources().getColor(R.color.green_light)), 12, 21, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spannable.setSpan(new StyleSpan(Typeface.BOLD), 12, 21, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                expireOn.setText(spannable);

                User.IAbsolute absolute = (User.IAbsolute) mExpiry;
                // end date
                long date = absolute.endDate();
                mAbsoluteEndDate = Calendar.getInstance(Locale.getDefault());
                mAbsoluteEndDate.setTime(new Date(date));
                updateAbsoluteEndDateText(mAbsoluteEndDate);
                // change operate
                TextView absChange = root.findViewById(R.id.abs_change);
                initExpiryChange(absChange, EXPIRY_ABSOLUTE_DATE);
                break;
            case 3: // date range
                mLlChangeCommonDate.setVisibility(View.VISIBLE);
                mLlChangeAbsoluteDate.setVisibility(View.GONE);
                mLlChangeNeverDate.setVisibility(View.GONE);

                // set data --- convert mills to Calendar
                User.IRange range = (User.IRange) mExpiry;
                // start date
                long startDate = range.startDate();
                mRangeStartDate = Calendar.getInstance(Locale.getDefault());
                mRangeStartDate.setTime(new Date(startDate));
                // end date
                long endDate = range.endDate();
                mRangeEndDate = Calendar.getInstance(Locale.getDefault());
                mRangeEndDate.setTime(new Date(endDate));
                updateRelativeOrRangeEndDateText(mRangeStartDate, mRangeEndDate);
                // change operate
                TextView rangChange = root.findViewById(R.id.change);
                initExpiryChange(rangChange, EXPIRY_DATE_RANGE);
                break;
            default:
                break;
        }
    }

    private void initExpiryChange(TextView change, final String selectItem) {
        change.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mCtx, ExpiryWindow.class);
                // post expiry value to ExpiryWindow to change
                EventBus.getDefault().postSticky(new ChangeExpiryDateEvent(mExpiry));
                intent.putExtra("select_item", selectItem);
                intent.putExtra("use_default", false);
                mCtx.startActivity(intent);
            }
        });
    }

    // update Relative date.
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

    private void updateRelativeOrRangeEndDateText(Calendar startDate, Calendar endDate) {
        //for from side
        mTvFromMonth.setText(CalenderUtils.getMonthLabel(startDate));
        mTvFromWeek.setText(CalenderUtils.getWeekLabel(startDate) + " " + startDate.get(Calendar.YEAR));
        mTvFromDay.setText(String.valueOf(startDate.get(Calendar.DAY_OF_MONTH)));
        //for to side
        mTvToMonth.setText(CalenderUtils.getMonthLabel(endDate));
        mTvToWeek.setText(CalenderUtils.getWeekLabel(endDate) + " " + endDate.get(Calendar.YEAR));
        mTvToDay.setText(String.valueOf(endDate.get(Calendar.DAY_OF_MONTH)));
        mTvCountDay.setText(String.valueOf(CalenderUtils.countDays(startDate, endDate)));
        mTvCountDay.append(" days");
    }

    private void updateAbsoluteEndDateText(Calendar endDate) {
        mTvAbsToMonth.setText(CalenderUtils.getMonthLabel(endDate));
        mTvAbsToWeek.setText(CalenderUtils.getWeekLabel(endDate) + " " + endDate.get(Calendar.YEAR));
        mTvAbsToDay.setText(String.valueOf(endDate.get(Calendar.DAY_OF_MONTH)));
        mTvAbsCountDay.setText(String.valueOf(CalenderUtils.countDays(Calendar.getInstance(), endDate)));
        mTvAbsCountDay.append(" days");
    }

    //User click the rights text, also can set the checked to be true or false.
    private void initRightsTextClick(TextView textView, final SwitchCompat toggle) {
        if (textView == null) {
            return;
        }
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle.setChecked(!toggle.isChecked());
            }
        });
    }

    private boolean isExpired(User.IExpiry expiry) {
        ExpiryChecker expiryChecker = new ExpiryChecker();
        if (!expiryChecker.isValidate(expiry)) {
            ExceptionDialog.showSimpleUI(mCtx, mCtx.getString(R.string.hint_msg_valid_period));
            return true;
        }
        return false;
    }
}
