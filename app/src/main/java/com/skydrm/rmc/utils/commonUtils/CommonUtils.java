package com.skydrm.rmc.utils.commonUtils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.ui.widget.customcontrol.CustomRelativeLayout;
import com.skydrm.rmc.ui.widget.customcontrol.DrawableTextView;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;
import com.skydrm.rmc.ui.widget.popupwindow.OperateCompleteWindow3;
import com.skydrm.rmc.utils.emailUtils.ChooseObjEntity;
import com.skydrm.rmc.utils.emailUtils.QCEmailTextArea;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hhu on 11/10/2016.
 */

public class CommonUtils {

    private static final String TAG = "CommonUtils";
    private static Random mRandom = new Random();

    /**
     * The method is used to change the statusbar background color
     *
     * @param activity
     * @param color
     */
    public static void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            View statusView = createStatusView(activity, color);

            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(statusView);
            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            rootView.setFitsSystemWindows(true);
            rootView.setClipToPadding(true);
        }
    }

    /**
     * @param activity
     * @param color
     * @return
     */
    private static View createStatusView(Activity activity, int color) {
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);

        View statusView = new View(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                statusBarHeight);
        statusView.setLayoutParams(params);
        statusView.setBackgroundColor(color);
        return statusView;
    }

    /**
     * judge the location of touch if is inner of the view
     *
     * @param view
     * @param ev
     */
    public static boolean isInRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getX() < x || ev.getX() > (x + view.getWidth()) || ev.getY() < y || ev.getY() > (y + view.getHeight())) {
            return false;
        }
        return true;
    }

    /**
     * The method is used to change TimeMillis to Days:Hours:Minutes:Seconds:Millis
     *
     * @param timeMillis
     * @return
     */
    public static String format(long timeMillis) {
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;
        long day = timeMillis / dd;
        long hour = (timeMillis - day * dd) / hh;
        long minute = (timeMillis - day * dd - hour * hh) / mi;
        long second = (timeMillis - day * dd - hour * hh - minute * mi) / ss;
        long milliSecond = timeMillis - day * dd - hour * hh - minute * mi - second * ss;
        String strDay = day < 10 ? "0" + day : "" + day;
        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;
        String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" + milliSecond;
        strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" + strMilliSecond;
        return strDay + " days " + strHour + " hours " + strMinute + " minutes ";
    }

    /**
     * Get Screen Height
     *
     * @param activity
     * @return
     */
    public static int getScreenHeight(Activity activity) {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            return dm.heightPixels;
        } catch (Exception e) {
            Log.e(TAG, "getScreenHeight: " + e);
            throw new NullPointerException("the params of activity should not be null!");
        }
    }

    public static int getScreenWidth(Activity activity) {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            return dm.widthPixels;
        } catch (Exception e) {
            Log.e(TAG, "getScreenWidth: " + e);
            throw new NullPointerException("the params of activity should not be null!");
        }
    }

    /**
     * @return
     */
    public static boolean isRunOnUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static Handler getHandler() {
        return SkyDRMApp.getInstance().getGlobalUIHandler();
    }

    /**
     * @param runnable
     */
    public static void runOnUiThread(Runnable runnable) {
        if (isRunOnUiThread()) {
            Log.e(TAG, "runOnUiThread: ");
            runnable.run();
        } else {
            Log.e(TAG, "runOnUiThread: getHandler");
            getHandler().post(runnable);
        }
    }

    /**
     * write byte[] into output streame
     *
     * @param data
     * @param os
     */
    public static void writeOutputStreamByte(OutputStream os, byte[] data) {
        int total = data.length;
        int blockSize = 0x100;
        int current = 0;
        try {
            while (current < total) {
                if (current + blockSize <= total) {
                    os.write(data, current, blockSize);
                    current += blockSize;
                } else {
                    os.write(data, current, total - current);
                    current += total - current;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // for check email format
    public static boolean checkEmailAddress(String inPutValue) {
        boolean flag = false;
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        + "([a-zA-Z0-9]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
//        if (inPutValue.matches("[a-zA-Z0-9._-]+@[a-z0-9]+\\.+[a-z0-9.]+")) {
//            flag = true;
//        }
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inPutValue);
        if (matcher.matches()) {
            flag = true;
        }
        return flag;
    }

    // check the email address if has existed.
    public static boolean isEmailExisted(String email, List<String> emailList) {
        for (String one : emailList) {
            if (one.equals(email)) {
                return true;
            }
        }
        return false;
    }

    // handle the wrong emails
    public static void handleErrorEmail(Context context, List<String> dirtyList) {
        // have error format email address
        if (dirtyList.size() > 0) {
            String strErrorEmail = null;
            for (int i = 0; i < dirtyList.size(); ++i) {
                if (i == 0)
                    strErrorEmail = dirtyList.get(i);
                else
                    strErrorEmail += ("," + dirtyList.get(i));
            }
            GenericError.showSimpleUI(context, context.getString(R.string.error_email_format) + "  " + strErrorEmail);
        }
    }

    private static void handleErrorEmail(Context context, List<String> emailList, List<String> dirtyList) {
        // have error format email address
        if (dirtyList.size() > 0) {
            String strErrorEmail = null;
            for (int i = 0; i < dirtyList.size(); ++i) {
                if (i == 0)
                    strErrorEmail = dirtyList.get(i);
                else
                    strErrorEmail += ("," + dirtyList.get(i));
            }
            GenericError.showSimpleUI(context, context.getString(R.string.error_email_format) + "  " + strErrorEmail);
            emailList.clear();
        }
    }

    // get valid emails
    public static List<String> getValidEmails(Context context, List<String> emailList) {
        List<String> validEmails = new ArrayList<>();
        List<String> invalidEmails = new ArrayList<>();
        if (emailList.size() > 0) {
            for (String oneEmail : emailList) {
                if (!checkEmailAddress(oneEmail)) {
                    invalidEmails.add(oneEmail);
                } else {
                    validEmails.add(oneEmail);
                }
            }

            if (invalidEmails.size() > 0) {
                handleErrorEmail(context, invalidEmails);
                return null;
            }
        } else {
            GenericError.showSimpleUI(context, context.getString(R.string.please_input_email));
        }

        return validEmails;
    }

    // used to wrap email
    public static void wrapEmail(Context context,
                                 String contentText,
                                 boolean bClickShareButton,
                                 final List<String> emailList,
                                 final FlowLayout flowLayout,
                                 EditText editText) {
        if (!TextUtils.isEmpty(contentText)) {
            String spaceString = contentText.substring(contentText.length() - 1, contentText.length());

            if (" ".equals(spaceString) || bClickShareButton || contentText.contains(" ") || "\n".equals(spaceString)) {
                int spaceStringIndexOf = contentText.indexOf(" ");
                String tag = contentText;

                if (!bClickShareButton && spaceStringIndexOf != -1) {
                    tag = contentText.substring(0, spaceStringIndexOf);
                }

                // remove the "\n" of the end of string if have this
                if (tag.endsWith("\n")) {
                    tag = tag.substring(0, tag.lastIndexOf("\n"));
                }

                if (!TextUtils.isEmpty(tag)) {
                    DrawableTextView drawableTextView = new DrawableTextView(context, true);
                    drawableTextView.setText(tag);

                    int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    drawableTextView.measure(w, h);

                    int drawableTextViewMeasuredWidth = drawableTextView.getMeasuredWidth();

                    int flowLayoutWidth = flowLayout.getWidth();
                    CustomRelativeLayout.MarginLayoutParams marginLayoutParams = (CustomRelativeLayout.MarginLayoutParams) flowLayout.getLayoutParams();

                    int drawableTextVieMargin = DensityHelper.dip2px(context, 4);
                    int drawableTextViewMaxWidth = flowLayoutWidth - drawableTextVieMargin * 2;
                    if (drawableTextViewMeasuredWidth > drawableTextViewMaxWidth) {
                        drawableTextView.setMaxLines(1);
                        drawableTextView.setEllipsize(TextUtils.TruncateAt.END);

                        FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(10, ViewGroup.LayoutParams.WRAP_CONTENT);

                        drawableTextView.setLayoutParams(params);
                        drawableTextView.setPadding(20, 0, 20, 0);
                        drawableTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                    } else {
                        drawableTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(drawableTextVieMargin, drawableTextVieMargin, drawableTextVieMargin, drawableTextVieMargin);
                    drawableTextView.setLayoutParams(params);

                    drawableTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            flowLayout.removeView(v);
                            emailList.remove(((DrawableTextView) v).getText().toString());
                        }
                    });

                    // judge if have the same email exist, if have, will not add it again
                    if (!isEmailExisted(drawableTextView.getText().toString(), emailList)) {
                        // set the background of dirty email
                        if (!checkEmailAddress(drawableTextView.getText().toString())) {
                            drawableTextView.setBackgroundResource(R.drawable.dirty_email_bg);
                        }
                        flowLayout.addView(drawableTextView);
                        emailList.add(drawableTextView.getText().toString());
                    }

                }
                editText.setText("");
            }

        }
    }

    // used to handle input emails when invite in project.
    public static List<String> getValidEmails(Context context, QCEmailTextArea qCEmailTextArea) {
        List<String> emailList = new ArrayList<>();
        List<String> dirtyList = new ArrayList<>();
        Editable editable = qCEmailTextArea.getEditableText();
        if (editable != null && editable.length() > 0) {
            ArrayList<ChooseObjEntity> emailsObjEntity = qCEmailTextArea.getmChooseObjList();
            if (emailsObjEntity != null && !emailsObjEntity.isEmpty()) {
                int length = editable.length();
                ChooseObjEntity lastEntity = emailsObjEntity.get(emailsObjEntity.size() - 1);
                // handle the case that directly click invite button instead of input space key after input.
                if (lastEntity.end < length) {
                    qCEmailTextArea.addDirtyObj(true);
                    ArrayList<ChooseObjEntity> emailsObj = qCEmailTextArea.getmChooseObjList();
                    for (ChooseObjEntity one : emailsObj) {
                        if (!one.isDirty) {
                            emailList.add(one.outKey);
                        } else {
                            dirtyList.add(one.outKey);
                        }
                    }
                } else {
                    for (ChooseObjEntity one : emailsObjEntity) {
                        if (!one.isDirty) {
                            emailList.add(one.outKey);
                        } else {
                            dirtyList.add(one.outKey);
                        }
                    }
                }

                handleErrorEmail(context, emailList, dirtyList);
            } else {
                // handle the case that directly click button instead of input space key after input.
                qCEmailTextArea.addDirtyObj(true);
                ArrayList<ChooseObjEntity> emailsObj = qCEmailTextArea.getmChooseObjList();
                for (ChooseObjEntity one : emailsObj) {
                    if (!one.isDirty) {
                        emailList.add(one.outKey);
                    } else {
                        dirtyList.add(one.outKey);
                    }
                }

                handleErrorEmail(context, emailList, dirtyList);
            }
        } else {
            GenericError.showSimpleUI(context, context.getString(R.string.please_input_email));
        }

        return emailList;
    }

    // used to handle input emails when create project and invite in project.
    public static List<String> getValidEmailsForCreateProject(Context context, QCEmailTextArea qCEmailTextArea, List<String> dirtyList) {
        List<String> emailList = new ArrayList<>();
        Editable editable = qCEmailTextArea.getEditableText();
        if (editable != null && editable.length() > 0) {
            ArrayList<ChooseObjEntity> emailsObjEntity = qCEmailTextArea.getmChooseObjList();
            if (emailsObjEntity != null && !emailsObjEntity.isEmpty()) {
                int length = editable.length();
                ChooseObjEntity lastEntity = emailsObjEntity.get(emailsObjEntity.size() - 1);
                // handle the case that directly click invite button instead of input space key after input.
                if (lastEntity.end < length) {
                    qCEmailTextArea.addDirtyObj(true);
                    ArrayList<ChooseObjEntity> emailsObj = qCEmailTextArea.getmChooseObjList();
                    for (ChooseObjEntity one : emailsObj) {
                        if (!one.isDirty) {
                            emailList.add(one.outKey);
                        } else {
                            dirtyList.add(one.outKey);
                        }
                    }
                } else {
                    for (ChooseObjEntity one : emailsObjEntity) {
                        if (!one.isDirty) {
                            emailList.add(one.outKey);
                        } else {
                            dirtyList.add(one.outKey);
                        }
                    }
                }

                handleErrorEmail(context, emailList, dirtyList);
            } else {
                // handle the case that directly click button instead of input space key after input.
                qCEmailTextArea.addDirtyObj(true);
                ArrayList<ChooseObjEntity> emailsObj = qCEmailTextArea.getmChooseObjList();
                for (ChooseObjEntity one : emailsObj) {
                    if (!one.isDirty) {
                        emailList.add(one.outKey);
                    } else {
                        dirtyList.add(one.outKey);
                    }
                }

                handleErrorEmail(context, emailList, dirtyList);
            }
        }

        return emailList;
    }

    public static void setSearchView(Activity context, SearchView searchView) {
        searchView.setIconifiedByDefault(false);
        final int closeImgId = context.getResources().getIdentifier("search_close_btn", "id", context.getPackageName());
        ImageView closeImg = (ImageView) searchView.findViewById(closeImgId);
        if (closeImg != null) {
            LinearLayout.LayoutParams paramsImg = (LinearLayout.LayoutParams) closeImg.getLayoutParams();
            paramsImg.topMargin = DensityHelper.dip2px(context, -2);
            closeImg.setImageResource(R.drawable.icon_delete);
            closeImg.setLayoutParams(paramsImg);
        }
        final int editViewId = context.getResources().getIdentifier("search_src_text", "id", context.getPackageName());
        EditText mEdit = (SearchView.SearchAutoComplete) searchView.findViewById(editViewId);
        if (mEdit != null) {
            mEdit.setHintTextColor(context.getResources().getColor(android.R.color.darker_gray));
            mEdit.setTextColor(context.getResources().getColor(android.R.color.white));
            mEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            mEdit.setHint(context.getResources().getString(R.string.search_hint_tip));
        }
        LinearLayout rootView = (LinearLayout) searchView.findViewById(R.id.search_bar);
        rootView.setBackgroundResource(R.drawable.bg_share_button_2);
        rootView.setClickable(true);
        LinearLayout editLayout = (LinearLayout) searchView.findViewById(R.id.search_plate);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) editLayout.getLayoutParams();
        LinearLayout tipLayout = (LinearLayout) searchView.findViewById(R.id.search_edit_frame);
        LinearLayout.LayoutParams tipParams = (LinearLayout.LayoutParams) tipLayout.getLayoutParams();
        tipParams.leftMargin = 0;
        tipParams.rightMargin = 0;
        tipLayout.setLayoutParams(tipParams);
        ImageView icTip = (ImageView) searchView.findViewById(R.id.search_mag_icon);
        icTip.setImageResource(R.drawable.icon_search);
        params.topMargin = DensityHelper.dip2px(context, 10);
        params.bottomMargin = DensityHelper.dip2px(context, 4);
        params.rightMargin = DensityHelper.dip2px(context, 4);
        editLayout.setLayoutParams(params);
        searchView.setSubmitButtonEnabled(false);
    }

    public static int generateTextColor(int randomColor) {

        int avgColor = (Color.red(randomColor) + Color.blue(randomColor) + Color.blue(randomColor)) / 3;

        if (avgColor > 0xff / 2) {

            return Color.BLACK;
        } else {

            return Color.WHITE;
        }

    }

    public static int generateRandomColor() {

        //0~255
        return Color.argb(0xff, mRandom.nextInt(0xfff), mRandom.nextInt(0xfff), mRandom.nextInt(0xfff));
    }

    // get the height of status.
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    // get view height dynamically(can get it from onCreate of some Activity)
    public static int getViewHeight(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return view.getMeasuredWidth();
    }

    /**
     * Only one method among all hide methods
     *
     * @param token
     */
    public static void hideSoftInput(Context context, IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * getBackgroundColor
     *
     * @param context
     * @param name
     * @return
     */
    public static int selectionBackgroundColor(Context context, String name) {
        if (TextUtils.isEmpty(name)) {
            return context.getResources().getColor(R.color.r_background);
        }
        switch (name.substring(0, 1).toUpperCase()) {
            case "A":
                return context.getResources().getColor(R.color.a_background);

            case "B":
                return context.getResources().getColor(R.color.b_background);

            case "C":
                return context.getResources().getColor(R.color.c_background);

            case "D":
                return context.getResources().getColor(R.color.d_background);

            case "E":
                return context.getResources().getColor(R.color.e_background);

            case "F":
                return context.getResources().getColor(R.color.f_background);

            case "G":
                return context.getResources().getColor(R.color.g_background);

            case "H":
                return context.getResources().getColor(R.color.h_background);

            case "I":
                return context.getResources().getColor(R.color.i_background);

            case "J":
                return context.getResources().getColor(R.color.j_background);

            case "K":
                return context.getResources().getColor(R.color.k_background);

            case "L":
                return context.getResources().getColor(R.color.l_background);

            case "M":
                return context.getResources().getColor(R.color.m_background);

            case "N":
                return context.getResources().getColor(R.color.n_background);

            case "O":
                return context.getResources().getColor(R.color.o_background);

            case "P":
                return context.getResources().getColor(R.color.p_background);

            case "Q":
                return context.getResources().getColor(R.color.q_background);

            case "R":
                return context.getResources().getColor(R.color.r_background);

            case "S":
                return context.getResources().getColor(R.color.s_background);


            case "T":
                return context.getResources().getColor(R.color.t_background);


            case "U":
                return context.getResources().getColor(R.color.u_background);


            case "V":
                return context.getResources().getColor(R.color.v_background);


            case "W":
                return context.getResources().getColor(R.color.w_background);


            case "X":
                return context.getResources().getColor(R.color.x_background);


            case "Y":
                return context.getResources().getColor(R.color.y_background);


            case "Z":
                return context.getResources().getColor(R.color.z_background);


            case "0":
                return context.getResources().getColor(R.color.background_0);


            case "1":
                return context.getResources().getColor(R.color.background_1);


            case "2":
                return context.getResources().getColor(R.color.background_2);


            case "3":
                return context.getResources().getColor(R.color.background_3);

            case "4":
                return context.getResources().getColor(R.color.background_4);


            case "5":
                return context.getResources().getColor(R.color.background_5);


            case "6":
                return context.getResources().getColor(R.color.background_6);

            case "7":
                return context.getResources().getColor(R.color.background_7);

            case "8":
                return context.getResources().getColor(R.color.background_8);

            case "9":
                return context.getResources().getColor(R.color.background_9);

            default:
                return context.getResources().getColor(R.color.default_background);
        }
    }


    /**
     * get textColor
     *
     * @param context
     * @param name
     * @return
     */
    public static int selectionTextColor(Context context, String name) {
        if (TextUtils.isEmpty(name)) {
            return context.getResources().getColor(R.color.a_textColor);
        }
        switch (name.substring(0, 1).toUpperCase()) {
            case "A":
                return context.getResources().getColor(R.color.a_textColor);

            case "B":
                return context.getResources().getColor(R.color.b_textColor);

            case "C":
                return context.getResources().getColor(R.color.c_textColor);

            case "D":
                return context.getResources().getColor(R.color.d_textColor);

            case "E":
                return context.getResources().getColor(R.color.e_textColor);

            case "F":
                return context.getResources().getColor(R.color.f_textColor);

            case "G":
                return context.getResources().getColor(R.color.g_textColor);

            case "H":
                return context.getResources().getColor(R.color.h_textColor);

            case "I":
                return context.getResources().getColor(R.color.i_textColor);

            case "J":
                return context.getResources().getColor(R.color.j_textColor);

            case "K":
                return context.getResources().getColor(R.color.k_textColor);

            case "L":
                return context.getResources().getColor(R.color.l_textColor);

            case "M":
                return context.getResources().getColor(R.color.m_textColor);

            case "N":
                return context.getResources().getColor(R.color.n_textColor);

            case "O":
                return context.getResources().getColor(R.color.o_textColor);

            case "P":
                return context.getResources().getColor(R.color.p_textColor);

            case "Q":
                return context.getResources().getColor(R.color.q_textColor);

            case "R":
                return context.getResources().getColor(R.color.r_textColor);

            case "S":
                return context.getResources().getColor(R.color.s_textColor);


            case "T":
                return context.getResources().getColor(R.color.t_textColor);


            case "U":
                return context.getResources().getColor(R.color.u_textColor);


            case "V":
                return context.getResources().getColor(R.color.v_textColor);


            case "W":
                return context.getResources().getColor(R.color.w_textColor);


            case "X":
                return context.getResources().getColor(R.color.x_textColor);


            case "Y":
                return context.getResources().getColor(R.color.y_textColor);


            case "Z":
                return context.getResources().getColor(R.color.z_textColor);


            case "0":
                return context.getResources().getColor(R.color.textColor_0);


            case "1":
                return context.getResources().getColor(R.color.textColor_1);


            case "2":
                return context.getResources().getColor(R.color.textColor_2);


            case "3":
                return context.getResources().getColor(R.color.textColor_3);

            case "4":
                return context.getResources().getColor(R.color.textColor_4);


            case "5":
                return context.getResources().getColor(R.color.textColor_5);


            case "6":
                return context.getResources().getColor(R.color.textColor_6);

            case "7":
                return context.getResources().getColor(R.color.textColor_7);

            case "8":
                return context.getResources().getColor(R.color.textColor_8);

            case "9":
                return context.getResources().getColor(R.color.textColor_9);

            default:
                return context.getResources().getColor(R.color.default_textColor);

        }
    }

    /**
     * Judge current screen is portrait or landscape.
     * return true if is landscape, false is portrait.
     */
    private static boolean isLandscape(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * Judge current device is tablet or phone
     * return true if is tablet, false is phone
     */
    private static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private static int getOperateCompleteWindowHeight(Context context) {
        if (isTablet(context)) {
            return isLandscape(context) ? CommonUtils.getScreenHeight((Activity) context) / 3 : CommonUtils.getScreenHeight((Activity) context) / 4;
        } else {
            return isLandscape(context) ? CommonUtils.getScreenHeight((Activity) context) / 2 : CommonUtils.getScreenHeight((Activity) context) / 3;
        }
    }

    // popup window for share succeed
    public static void popupShareSucceedTip(Context context, String filename, View rootView, List<String> emailLists, boolean bIsNxl) {
        // popup succeed prompt window
        final View view = LayoutInflater.from(context).inflate(R.layout.share_succeed_tip3, null);
        TextView fileName = (TextView) view.findViewById(R.id.file_name);
        TextView emails = (TextView) view.findViewById(R.id.with_emails);
        if (bIsNxl) {
            TextView myVaultTip = (TextView) view.findViewById(R.id.nxl_to_myVault_tip);
            myVaultTip.setVisibility(View.GONE);
        }

        if (emailLists.size() == 1) {
            emails.setText(emailLists.get(0));
        } else if (emailLists.size() > 1) {
            String email = emailLists.get(0) + " " + context.getResources().getString(R.string.and_so_on);
            emails.setText(email);
        }
        fileName.setText(filename);
        OperateCompleteWindow3 popupWindow = new OperateCompleteWindow3((Activity) context, rootView, view, ViewGroup.LayoutParams.MATCH_PARENT, getOperateCompleteWindowHeight(context), true);
        popupWindow.setBackGroundColor(context.getResources().getColor(R.color.popup_window_bg));
        popupWindow.showWindow();
    }

    // popup windows for protect succeed
    public static void popupProtectSucceedTip(Context context, View rootView, String fileName) {
        final View view = LayoutInflater.from(context).inflate(R.layout.protect_succeed_tip3, null);
        TextView tvFileName = (TextView) view.findViewById(R.id.file_name);
        tvFileName.setText(fileName);
        final OperateCompleteWindow3 popupWindow = new OperateCompleteWindow3((Activity) context, rootView, view, ViewGroup.LayoutParams.MATCH_PARENT, getOperateCompleteWindowHeight(context), true);
        popupWindow.setBackGroundColor(context.getResources().getColor(R.color.popup_window_bg));
        popupWindow.showWindow();
    }

    public static void popupProjectShareToProjectSuccessTip(View root, String projectName, String fileName) {
        if (root == null) {
            return;
        }
        if (projectName == null || projectName.isEmpty()) {
            return;
        }
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        Context ctx = root.getContext();
        final View view = LayoutInflater.from(ctx).inflate(R.layout.add_to_project_succeed_tip, null);
        TextView tvFileName = view.findViewById(R.id.file_name);
        TextView tvSuccessTip = view.findViewById(R.id.success);
        tvFileName.setText(fileName);
        String tip = String.format(Locale.getDefault(), ctx.getString(R.string.add_to_project_success_tip), projectName);
        int fIdx = tip.lastIndexOf(projectName);
        tvSuccessTip.setText(StringUtils.getBoldStyle(tip, fIdx, fIdx + projectName.length()));

        final OperateCompleteWindow3 popupWindow = new OperateCompleteWindow3((Activity) ctx, root, view,
                ViewGroup.LayoutParams.MATCH_PARENT, getOperateCompleteWindowHeight(ctx), true);
        popupWindow.setBackGroundColor(ctx.getResources().getColor(R.color.popup_window_bg));
        popupWindow.showWindow();
    }

    public static void popupProjectAddFileSuccessTip(Context ctx, View root, String fileName) {
        if (ctx == null || root == null) {
            return;
        }
        final View view = LayoutInflater.from(ctx).inflate(R.layout.protect_succeed_tip3, null);
        TextView tvFileName = view.findViewById(R.id.file_name);
        TextView tvToMyVaultTip = view.findViewById(R.id.nxl_to_myVault_tip);
        tvToMyVaultTip.setVisibility(View.GONE);
        tvFileName.setText(fileName);
        final OperateCompleteWindow3 popupWindow = new OperateCompleteWindow3((Activity) ctx, root,
                view, ViewGroup.LayoutParams.MATCH_PARENT, getOperateCompleteWindowHeight(ctx), true);
        popupWindow.setBackGroundColor(ctx.getResources().getColor(R.color.popup_window_bg));
        popupWindow.showWindow();
    }

    public static void popupWorkSpaceAddFileSuccessTip(View root, String fileName) {
        if (root == null) {
            return;
        }
        Context ctx = root.getContext();
        final View view = LayoutInflater.from(ctx).inflate(R.layout.protect_succeed_tip3, null);
        TextView tvFileName = view.findViewById(R.id.file_name);
        TextView tvToMyVaultTip = view.findViewById(R.id.nxl_to_myVault_tip);
        tvToMyVaultTip.setVisibility(View.GONE);
        tvFileName.setText(fileName);
        final OperateCompleteWindow3 popupWindow = new OperateCompleteWindow3((Activity) ctx, root,
                view, ViewGroup.LayoutParams.MATCH_PARENT, getOperateCompleteWindowHeight(ctx), true);
        popupWindow.setBackGroundColor(ctx.getResources().getColor(R.color.popup_window_bg));
        popupWindow.showWindow();
    }

    public static void popupReShareSucceedTip(Context context, View rootView, String fileName,
                                              List<String> newSharedLists, List<String> alreadySharedLists) {
        View view = LayoutInflater.from(context).inflate(R.layout.reshare_succeed_tip, null);
        TextView tvFileName = (TextView) view.findViewById(R.id.file_name);
        TextView tvTip = (TextView) view.findViewById(R.id.success);
        tvFileName.setText(fileName);
        StringBuilder tipBuilder = new StringBuilder();
        if (newSharedLists != null && newSharedLists.size() != 0) {
            tipBuilder.append("Successfully shared the file and emailed the link to ");
            for (int i = 0; i < newSharedLists.size(); i++) {
                if (i == newSharedLists.size() - 1) {
                    tipBuilder.append(newSharedLists.get(i)).append(".");
                } else {
                    tipBuilder.append(newSharedLists.get(i)).append(",");
                }
            }
        }
        if (alreadySharedLists != null && alreadySharedLists.size() != 0) {
            tipBuilder.append("File has already been shared with ");
            for (int i = 0; i < alreadySharedLists.size(); i++) {
                if (i == alreadySharedLists.size() - 1) {
                    tipBuilder.append(alreadySharedLists.get(i));
                } else {
                    tipBuilder.append(alreadySharedLists.get(i)).append(",");
                }
            }
            tipBuilder.append(" and email will not be sent again.");
        }

        tvTip.setText(tipBuilder.toString());
        OperateCompleteWindow3 popupWindow = new OperateCompleteWindow3((Activity) context, rootView, view, ViewGroup.LayoutParams.MATCH_PARENT, getOperateCompleteWindowHeight(context));
        popupWindow.setStandingTme(4000);
        popupWindow.setBackGroundColor(context.getResources().getColor(R.color.popup_window_bg));
        popupWindow.showWindow();
    }

    public static void popupReClassifySuccessTip(Context context, View rootView, String fileName) {
        final View view = LayoutInflater.from(context).inflate(R.layout.layout_modify_success_tip, null);
        TextView tvFileName = view.findViewById(R.id.tv_file_name);
        tvFileName.setText(fileName);
        final OperateCompleteWindow3 popupWindow = new OperateCompleteWindow3((Activity) context, rootView, view, ViewGroup.LayoutParams.MATCH_PARENT, getOperateCompleteWindowHeight(context), true);
        popupWindow.setBackGroundColor(context.getResources().getColor(R.color.popup_window_bg));
        popupWindow.showWindow();
    }

    // get default uploaded boundService: myDrive
    public static BoundService getDefaultUploadedBoundService() {
        List<BoundService> lists = SkyDRMApp
                .getInstance()
                .getRepoSystem()
                .getStockedNotSpoiledServiceInRepoSystem();
        if (lists != null && lists.size() > 0) {
            for (BoundService one : lists) {
                if (one.type.equals(BoundService.ServiceType.MYDRIVE)) {
                    return one;
                }
            }
        }
        return null;
    }

    public static boolean isURLValidate(String url) {
        String regExp = "(ht|f)tp(s?)\\:\\/\\/[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:(0-9)*)*(\\/?)([a-zA-Z0-9\\-\\.\\?\\,\\'\\/\\\\\\+&amp;%\\$#_]*)?";
        Pattern pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    public static String generateWrapperURL(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String trimUrl = url.toLowerCase().trim();
        String wrapperUrl = "";
        if (trimUrl.contains("http://") || trimUrl.contains("https://")) {
            wrapperUrl = trimUrl;
        } else {
            wrapperUrl = wrapperUrl.concat("https://").concat(trimUrl);
        }
        return wrapperUrl;
    }

    public static void releaseResource(IDestroyable destroyable) {
        if (destroyable != null) {
            destroyable.onReleaseResource();
            destroyable = null;
        }
    }

    public static List<BoundService> getFilteredBoundServiceByAccountType() {
        List<BoundService> retVal = new ArrayList<>();
        SkyDRMApp app = SkyDRMApp.getInstance();
        List<BoundService> userLinkedRepos = app
                .getRepoSystem()
                .getStockedNotSpoiledServiceInRepoSystem();
        if (userLinkedRepos == null || userLinkedRepos.size() == 0) {
            return retVal;
        }
        for (BoundService service : userLinkedRepos) {
            if (service == null) {
                continue;
            }
            if (!service.isValidRepo()) {
                continue;
            }
            if (service.type == BoundService.ServiceType.MYDRIVE) {
                retVal.add(service);
            } else {
                if (!app.isOnPremise()) {
                    retVal.add(service);
                }
            }
        }
        return retVal;
    }

}
