package com.skydrm.rmc.utils.emailUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.TextView;
import android.widget.Toast;

import com.skydrm.rmc.SkyDRMApp;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this is custom Editable control, used to input multiple email address when do sharing.
 */
public class QCEmailTextArea extends android.support.v7.widget.AppCompatMultiAutoCompleteTextView {
    private static final boolean DEBUG = SkyDRMApp.getInstance().isDebug();
    public static final String TAG = "QCEmailTextArea";
    private int mDirtyTextBgColor;
    private int mDirtySelectedTextBgColor;
    private int mDirtyTextFgColor;
    private int mDirtySelectedTextFgColor;

    private int mValidTextBgColor;
    private int mValidSelectedTextBgColor;
    private int mValidTextFgColor;
    private int mValidSelectedTextFgColor;

    private int mTextSize;
    private int mMinValidLength;
    private boolean mIsAllowInputText = true;
    private boolean isInPutValue = true;

    private int mRoundRectPaddingLeft;
    private int mRoundRectPaddingRight;
    private int mRoundRectPaddingTop;
    private int mRoundRectPaddingBottom;

    private int mRoundRectRadiusX;
    private int mRoundRectRadiusY;

    // added by aning
    private DrawableLeftListener mLeftListener;
    private DrawableRightListener mRightListener;

    final int DRAWABLE_LEFT = 0;
    final int DRAWABLE_TOP = 1;
    final int DRAWABLE_RIGHT = 2;
    final int DRAWABLE_BOTTOM = 3;

    // use space as separator between emails
    private char customChar = ' ';

    private ArrayList<ChooseObjEntity> mChooseObjList;
    private OnDeleteObjListener mDeleteObjListener;
    private OnAddObjListener mAddObjListener;
    private AutoClickableImageSpan[] image;
    private boolean bTextInput = false;
    private boolean bSelectionRemoved = false;
    private ChooseObjEntity objEntity;

    public QCEmailTextArea(Context context) {
        this(context, null);
    }

    public QCEmailTextArea(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.autoCompleteTextViewStyle);
    }

    public QCEmailTextArea(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        registerListener();
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        Editable editable = getEditableText();
        setSelection(editable.length());
//        //that means user touch the edittext need focus to input text
//        if (!bTextInput && editable.length() == 0) {
//            setSelection(editable.length());
//            bTextInput = false;
//        }
        //current cursor position is not the editable texts'length need hide the cursor
//        if (selEnd != editable.length() && editable.length() != 0) {
//            Selection.removeSelection(editable);
//            bSelectionRemoved = true;
//        }

        //reset all selected status when user input text again
//        if (mChooseObjList == null || mChooseObjList.size() == 0)
//            return;
//        if (bTextInput && !bSelectionRemoved && editable.length() != 0) {
//            resetAllWidget();
//            for (ChooseObjEntity entity : mChooseObjList) {
//                entity.isSelected = false;
//            }
//        }
    }

    // class ZanyInputConnection used to listen software DEL key, note: it is different for google native nexus series phone
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection inputConnection = super.onCreateInputConnection(outAttrs);
        if (inputConnection == null) {
            return null;
        }
        return new ZanyInputConnection(inputConnection, true);
    }

    private class ZanyInputConnection extends InputConnectionWrapper {

        public ZanyInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            Editable editable = getEditableText();
            //This is used to judge the LastEntity whether under condition that still under input status or not
            //if under input status then request focus by setSelection(editable.length())
//            if (mChooseObjList.size() >= 1) {
//                ChooseObjEntity lastEntity = mChooseObjList.get(mChooseObjList.size() - 1);
//                if (lastEntity.end != editable.length()) {
//                    QCEmailTextArea.this.setSelection(editable.length());
//                }
//            }
            if (editable.length() > 0 && !editable.toString().startsWith(" ")
                    && (editable.length() > 1 && editable.toString().charAt(editable.length() - 1) == ' ' && editable.toString().charAt(editable.length() - 2) != ' ')
                    && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                return delete();
            }
            // fix bug 37338 -- handle ENTER key for some android platform.
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                addDirtyObjEx(true);
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            Editable editable = getEditableText();
            if (editable != null
                    && editable.length() > 0 && !editable.toString().startsWith(" ")
                    && (editable.length() > 1 && editable.toString().charAt(editable.length() - 1) == ' '
                    && editable.toString().charAt(editable.length() - 2) != ' ')) {
                if (beforeLength == 1 && afterLength == 0) {
                    return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_DEL))
                            && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                            KeyEvent.KEYCODE_DEL));
                }
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    private void init() {
        this.setThreshold(2);
        recoverDefaultValues();
        mChooseObjList = new ArrayList<ChooseObjEntity>();
        setTokenizer(new CustomTokenizer(DefaultGlobal.SPACE_TOKENIZER));
        allowInputTextControl();
    }

    public ArrayList<ChooseObjEntity> getmChooseObjList() {
        return mChooseObjList;
    }

    private void registerListener() {
        addTextChangedListener(new AutoTextChangedListener());
        setMovementMethod(new AutoLinkMovementMethod());  // maybe not use now!
        setOnFocusChangeListener(new AutoFocusChangeListener()); // this is critical
    }

    public void appendContent(String[] names, String[] outKeys) {
        if (names != null && outKeys != null) {
            Editable editable = getEditableText();
            forceAllowInputText();
            for (int i = 0; i < names.length && i < outKeys.length; i++) {
                isInPutValue = false;
                SpannableString sp = getSpannableString(false, names[i], null,
                        null);
                editable.append(sp);
            }
            allowInputTextControl();
        }
    }

    // used to add email address by phone contracts
    public void appendContent(String name, String outKey) {
        if (name != null && outKey != null) {
            Editable editable = getEditableText();
            forceAllowInputText();

            isInPutValue = false;
            SpannableString sp = getSpannableString(false, name, null,
                    null);
            editable.append(sp);

            allowInputTextControl();
        }
    }

    public void setContent(String[] names, String[] outKeys) {
        clearContent();
        appendContent(names, outKeys);
    }

    public void clearContent() {
        for (ChooseObjEntity objEntity : mChooseObjList)
            objEntity.isSelected = true;
        int size = mChooseObjList.size();
        for (int i = 0; i < size; i++)
            delete();
        getEditableText().clear();
    }

    public String[] getOutKeys() {
        if (mChooseObjList.isEmpty())
            return null;
        else {
            String[] outKeys = new String[mChooseObjList.size()];
            for (int i = 0; i < mChooseObjList.size(); i++) {
                ChooseObjEntity objEntity = mChooseObjList.get(i);
                outKeys[i] = objEntity.outKey;
            }
            return outKeys;
        }
    }

    public void setAllowInputText(boolean isAllow) {
        mIsAllowInputText = isAllow;
        allowInputTextControl();
    }

    public boolean isAllowInputText() {
        return mIsAllowInputText;
    }

    // -------------- begin do some register ------------
    public void setOnDeleteObjListener(OnDeleteObjListener listener) {
        mDeleteObjListener = listener;
    }

    public void setDrawableLeftListener(DrawableLeftListener listener) {
        this.mLeftListener = listener;
    }

    public void setDrawableRightListener(DrawableRightListener listener) {
        this.mRightListener = listener;
    }

    public void setOnAddObjListener(OnAddObjListener listener) {
        mAddObjListener = listener;
    }

    public void setDirtyTextBgColor(int dirtyTextBgColor,
                                    int dirtySelectedTextBgColor) {
        mDirtyTextBgColor = dirtyTextBgColor;
        mDirtySelectedTextBgColor = dirtySelectedTextBgColor;
    }

    public void setValidTextBgColor(int validTextBgColor,
                                    int validSelectedTextBgColor) {
        mValidTextBgColor = validTextBgColor;
        mValidSelectedTextBgColor = validSelectedTextBgColor;
    }

    public void setDirtyTextFgColor(int dirtyTextFgColor,
                                    int dirtySelectedTextFgColor) {
        mDirtyTextFgColor = dirtyTextFgColor;
        mDirtySelectedTextFgColor = dirtySelectedTextFgColor;
    }

    public void setValidTextFgColor(int validTextFgColor,
                                    int validSelectedTextFgColor) {
        mValidTextFgColor = validTextFgColor;
        mValidSelectedTextFgColor = validSelectedTextFgColor;
    }

    public void setRoundRectPadding(int left, int top, int right, int bottom) {
        mRoundRectPaddingLeft = left;
        mRoundRectPaddingRight = right;
        mRoundRectPaddingTop = top;
        mRoundRectPaddingBottom = bottom;
    }

    public void setRoundRectRadius(int x, int y) {
        mRoundRectRadiusX = x;
        mRoundRectRadiusY = y;
    }

    public void setTextSizeOfRoundRect(int spSize) {
        mTextSize = spSize;
    }

    public void setMinValidLength(int len) {
        mMinValidLength = len;
    }
    // -------------- end do some register ------------

    public void recoverDefaultValues() {
        mDirtyTextBgColor = DefaultGlobal.DIRTY_TEXT_BG_COLOR;
        mDirtySelectedTextBgColor = DefaultGlobal.DIRTY_SELECTED_TEXT_BG_COLOR;
        mDirtyTextFgColor = DefaultGlobal.TEXT_FG_COLOR;
        mDirtySelectedTextFgColor = DefaultGlobal.SELECTED_TEXT_FG_COLOR;

        mValidTextBgColor = DefaultGlobal.VALID_TEXT_BG_COLOR;
        mValidSelectedTextBgColor = DefaultGlobal.VALID_SELECTED_TEXT_BG_COLOR;
        mValidTextFgColor = DefaultGlobal.TEXT_FG_COLOR;
        mValidSelectedTextFgColor = DefaultGlobal.SELECTED_TEXT_FG_COLOR;

        mRoundRectPaddingLeft = DefaultGlobal.ROUND_RECT_PADDING_LEFT;
        mRoundRectPaddingRight = DefaultGlobal.ROUND_RECT_PADDING_RIGHT;
        mRoundRectPaddingTop = DefaultGlobal.ROUND_RECT_PADDING_TOP;
        mRoundRectPaddingBottom = DefaultGlobal.ROUND_RECT_PADDING_BOTTOM;

        mRoundRectRadiusX = DefaultGlobal.ROUND_RECT_RADIUS_X;
        mRoundRectRadiusY = DefaultGlobal.ROUND_RECT_RADIUS_Y;

        mTextSize = DefaultGlobal.TEXT_SIZE;
        mMinValidLength = DefaultGlobal.MIN_VALID_LENGTH;
    }

    public void onDestroy() {
        for (ChooseObjEntity objEntity : mChooseObjList) {
            objEntity.drawable.setCallback(null);
            objEntity.drawable = null;
        }
        mChooseObjList.clear();
        mChooseObjList = null;
    }

    private void forceAllowInputText() {
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(
                DefaultGlobal.MAX_INPUT_LENGTH)});
    }

    private void allowInputTextControl() {
        int len = DefaultGlobal.MAX_INPUT_LENGTH;
        if (!mIsAllowInputText)
            len = getEditableText().length();
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(len)});
    }


    // -------------- begin,get Spannable String --------------------
    private SpannableString getSpannableString(SpannableString spanStr) {
        return getSpannableString(spanStr, DefaultGlobal.SPACE_TOKENIZER);
    }

    private SpannableString getSpannableString(SpannableString spanStr, char customChar) {
        SpannableString sp = new SpannableString(spanStr + (customChar + ""));
        TextUtils.copySpansFrom((Spanned) spanStr, 0, spanStr.length(),
                Object.class, sp, 0);
        return sp;
    }

    private SpannableString getSpannableString(boolean isDirty, String name, ChooseObjEntity objEntity) {
        return getSpannableString(isDirty, name, null, objEntity);
    }

    private SpannableString getSpannableString(boolean isDirty, String name,
                                               TextDrawable drawable, ChooseObjEntity objEntity) {
        String value = getValue(name);
        SpannableString spanStr = getUncompleteSpanStr(isDirty, value,
                drawable, objEntity);
        return getSpannableString(spanStr);
    }

    private SpannableString getUncompleteSpanStr(boolean isDirty, String value,
                                                 TextDrawable drawable, ChooseObjEntity objEntity) {
        return getUncompleteSpanStr(isDirty, value, null, drawable, objEntity);
    }

    private SpannableString getUncompleteSpanStr(boolean isDirty, String value,
                                                 String outKey, TextDrawable drawable, ChooseObjEntity objEntity) {
        if (drawable == null)
            drawable = getTextDrawable(isDirty, value);
        SpannableString spanStr;
        if (TextUtils.isEmpty(outKey))
            spanStr = new SpannableString(value);
        else
            spanStr = new SpannableString(value + outKey);
        AutoClickableImageSpan imageSpan = new AutoClickableImageSpan(drawable);
        imageSpan.setDirty(isDirty);
        imageSpan.setChooseObjEntity(objEntity);
        spanStr.setSpan(imageSpan, 0, value.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return spanStr;
    }
    // -------------- end,get Spannable String --------------------


    private String getValue(String name) {
        return name;
    }

    public String getOutKey(SpannableString spanStr) {
        String str = spanStr.toString();
        return str.substring(str.lastIndexOf(DefaultGlobal.SEPARATOR_RIGHT) + 1);
    }

    private TextDrawable getTextDrawable(boolean isDirty, String value) {
        // may change the height of the drawable if contains ENTER key.
        if (!TextUtils.isEmpty(value) && value.contains("\n")) {
            value = value.replace("\n", "");
        }
        //fix bug the value is too long to display the Bounds will show only the left RectRadiusBounds
        TextDrawable drawable = new TextDrawable(getContext(), value);
//        TextDrawable drawable = new TextDrawable(getContext(), value, this.getWidth() -
//                DefaultGlobal.ROUND_RECT_MARGIN_LEFT - DefaultGlobal.ROUND_RECT_MARGIN_RIGHT
//                - DefaultGlobal.ROUND_RECT_PADDING_LEFT - DefaultGlobal.ROUND_RECT_PADDING_RIGHT,
//                ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
//                        .getDefaultDisplay().getHeight());

//        TextDrawable drawable = new TextDrawable(getContext(), value, this.getWidth() -
//                DefaultGlobal.ROUND_RECT_MARGIN_LEFT - DefaultGlobal.ROUND_RECT_MARGIN_RIGHT
//                - DefaultGlobal.ROUND_RECT_PADDING_LEFT - DefaultGlobal.ROUND_RECT_PADDING_RIGHT,
//                PxConvertUtil.dp2px(getContext(), 100));
        if (isDirty) {
            drawable.setTextBgColor(mDirtyTextBgColor);
            drawable.setTextFgColor(mDirtyTextFgColor);
        } else {
            drawable.setTextBgColor(mValidTextBgColor);
            drawable.setTextFgColor(mValidTextFgColor);
        }

        drawable.setTextSize(mTextSize);
        drawable.setPadding(mRoundRectPaddingLeft, mRoundRectPaddingRight,
                mRoundRectPaddingTop, mRoundRectPaddingBottom);
        drawable.setRoundRectRadius(mRoundRectRadiusX, mRoundRectRadiusY);
        drawable.setBounds();
        return drawable;
    }

    private void confirmInput(SpannableString spanStr, String outKey) {
        String value = spanStr.toString();
        ChooseObjEntity objEntity = new ChooseObjEntity();
        objEntity.name = value;
        objEntity.outKey = outKey;
        if (mChooseObjList.isEmpty())
            objEntity.start = 0;
        else {
            ChooseObjEntity lastEntity = mChooseObjList.get(mChooseObjList.size() - 1);
            objEntity.start = lastEntity.end;
        }
        objEntity.end = objEntity.start + value.length();
        mChooseObjList.add(objEntity);

        AutoClickableImageSpan[] imageSpan = spanStr.getSpans(0, value.length(), AutoClickableImageSpan.class);
        objEntity.drawable = (TextDrawable) imageSpan[0].getDrawable();
        objEntity.isDirty = imageSpan[0].isDirty();
        imageSpan[0].setChooseObjEntity(objEntity);
    }

    // execute this fun when enter DEL key
    private synchronized boolean delete() {
        Editable editable = getEditableText();
        if (!mChooseObjList.isEmpty()) {
            for (ChooseObjEntity objEntity : mChooseObjList) {
                if (objEntity.isSelected) {
                    if (mDeleteObjListener != null)
                        mDeleteObjListener.delete(objEntity.outKey);
                    // delete
                    deleteTextAndObj(editable, objEntity);
                    resetChooseObjList();
                    allowInputTextControl();
                    //  return directly if have selected!
                    return true;
                }
            }

            // execute go head if have not selected
            ChooseObjEntity lastEntity = mChooseObjList.get(mChooseObjList.size() - 1);
            // means is a completed email before cursor
            if (lastEntity.end == editable.length()) {
                if (lastEntity.isSelected) {
                    // delete
                    deleteTextAndObj(editable, lastEntity);
                } else {
                    // to select
                    updateText(lastEntity);
                }
                return true;
            }
        }
        return false;
    }

    private synchronized void deleteTextAndObj(Editable editable, ChooseObjEntity objEntity) {
        deleteText(editable, objEntity);
        objEntity.drawable.setCallback(null);
        objEntity.drawable = null;
        mChooseObjList.remove(objEntity);
    }

    private synchronized void deleteText(Editable editable, ChooseObjEntity objEntity) {
        if (DEBUG) {
            Log.d(TAG, "deleteText: " + editable.toString() + "editable.length()-->" + editable.length());
        }
        removeSpan(editable, objEntity); // remove the span image
        String str = editable.toString();
        if (!TextUtils.isEmpty(str) && objEntity.start < editable.length()) {
            editable.delete(objEntity.start, objEntity.end); // delete the text.
        }
    }

    private void removeSpan(Editable editable, ChooseObjEntity objEntity) {
        AutoClickableImageSpan[] imageSpans = editable.getSpans(objEntity.start, objEntity.end, AutoClickableImageSpan.class);
        for (int i = 0; imageSpans != null && i < imageSpans.length; i++) {
            editable.removeSpan(imageSpans[i]);
        }
    }

    private void resetChooseObjList() {
        if (!mChooseObjList.isEmpty()) {
            int start = 0;
            for (ChooseObjEntity objEntity : mChooseObjList) {
                int len = objEntity.end - objEntity.start;
                objEntity.start = start;
                objEntity.end = start + len;
                start = objEntity.end;
            }
        }
    }

    private synchronized void dealDirty() {
        addDirtyObjEx(false);
        clearDirtyData();
    }

    private void addDirtyObjEx(boolean bIsEnterKey) {
        Editable editable = getEditableText(); // get editText content.
        if (TextUtils.isEmpty(editable.toString())) {
            return;
        }
        if (bIsEnterKey)
            editable.append("\n");
        int length = editable.length();
        int tpLen = 0;
        if (length > 1) {
            char endCh1 = editable.charAt(length - 1); // the last char
            char endCh2 = editable.charAt(length - 2);

            String dirty = null;
            if (mChooseObjList.isEmpty()) {
                if ((endCh1 == DefaultGlobal.SPACE_TOKENIZER || endCh1 == '\n') && endCh2 != DefaultGlobal.SPACE_TOKENIZER && endCh2 != '\n') {
                    dirty = editable.subSequence(0, length - 1).toString().trim(); // trim() -- remove the space before and after it
                    editable.delete(0, length); // delete the text content first then will add spanning string below
                }
            } else {
                ChooseObjEntity lastEntity = mChooseObjList.get(mChooseObjList.size() - 1);
                tpLen = lastEntity.end;
                if ((endCh1 == DefaultGlobal.SPACE_TOKENIZER || endCh1 == '\n')
                        && (endCh2 != DefaultGlobal.SPACE_TOKENIZER)
                        && endCh2 != '\n'
                        && lastEntity.end < length - 1) {
                    // ----- will remove "\n"
                    dirty = editable.subSequence(tpLen, length - 1).toString().trim();
                    // delete the text content first then will add spanning string below
                    editable.delete(lastEntity.end, length);
                }
            }

            if (dirty != null && !dirty.equals("")) {
                boolean isDirty = !checkEmailAddress(dirty); // check email format
                SpannableString sp = getSpannableString(isDirty, dirty, null);
                String data = sp.toString();
                // check the email if have existed!
                boolean flag = checkExistEmailAddress(data);
                //  means have deleted text content style above,but have not executed below editable.append(sp), -- so delete it when have existed!!
                if (flag) {
                    return;
                }
                // construct ChooseObjEntity and add it
                confirmInput(sp, dirty);
                // add email in the style of  SpannableString (have deleted the text style above)
                editable.append(sp);
                if (DEBUG) {
                    Log.d(TAG, "addDirtyObjEx:editable.append " + editable.toString() + "editable.length()-->" + editable.length());
                }
                // user input's value -- not add by contracts
                if (isInPutValue) {
                    String backStr = data.substring(0, data.length() - 1);
                    if (mAddObjListener != null)
                        mAddObjListener.add(backStr, backStr);
                }

                isInPutValue = true;
            }
        }
    }

    public void addDirtyObj(boolean bDirectClickShare) {
        // get editText content.
        Editable editable = getEditableText();
        if (TextUtils.isEmpty(editable.toString())) {
            return;
        }
        int length = editable.length();
        int tpLen = 0;
        if (length > 1) {
            // the last char
            char endCh1 = editable.charAt(length - 1);
            char endCh2 = editable.charAt(length - 2);
            String dirty = null;
            if (mChooseObjList.isEmpty()) {
                if ((endCh1 == DefaultGlobal.SPACE_TOKENIZER || endCh1 == '\n' || bDirectClickShare) && endCh2 != DefaultGlobal.SPACE_TOKENIZER) {
                    dirty = editable.subSequence(0, length).toString().trim();
                    // delete the text content first then will add spanning string below
                    editable.delete(0, length);
                }
            } else {
                ChooseObjEntity lastEntity = mChooseObjList.get(mChooseObjList.size() - 1);
                tpLen = lastEntity.end;
                if ((endCh1 == DefaultGlobal.SPACE_TOKENIZER || endCh1 == '\n' || bDirectClickShare)
                        && endCh2 != DefaultGlobal.SPACE_TOKENIZER
                        && lastEntity.end <= length - 1) {
                    dirty = editable.subSequence(tpLen, length).toString().trim();
                    // delete the text content first then will add spanning string below
                    editable.delete(lastEntity.end, length);
                }
            }
            if (dirty != null && !dirty.equals("")) {
                // check email format
                boolean isDirty = !checkEmailAddress(dirty);
                SpannableString sp = getSpannableString(isDirty, dirty, null);
                String data = sp.toString();
                // check the email if have existed!
                boolean flag = checkExistEmailAddress(data);
                //  means have deleted text content style above,but have not executed below editable.append(sp), -- so delete it when have existed!!
                if (flag) {
                    return;
                }
                // construct ChooseObjEntity and add it
                confirmInput(sp, dirty);
                // add email in the style of  SpannableString (have deleted the text style above)
                editable.append(sp);
                // user input's value -- not add by contracts
                if (isInPutValue) {
                    String backStr = data.substring(0, data.length() - 1);
                    if (mAddObjListener != null)
                        mAddObjListener.add(backStr, backStr);
                }
                isInPutValue = true;
            }
        }
    }

    // for check email format
    public boolean checkEmailAddress(String inPutValue) {
        boolean flag = false;
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        + "([a-zA-Z0-9]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inPutValue);
        if (matcher.matches()) {
            flag = true;
        }
        return flag;
    }

    // check email if have existed!
    private boolean checkExistEmailAddress(String inPutValue) {
        boolean flag = false;
        ChooseObjEntity lastEntity = null;
        for (int i = 0; i < mChooseObjList.size(); i++) {
            lastEntity = mChooseObjList.get(i);
            if (lastEntity.name.equals(inPutValue)) {
                flag = true;
                Toast.makeText(getContext(), "this email have existed!", Toast.LENGTH_LONG).show();
                break;
            }
        }
        return flag;
    }

    // now have not understood this very clearly
    private void clearDirtyData() {
        Editable editable = getEditableText();
        if (!mChooseObjList.isEmpty()) {
            int length = editable.length();
            if (mChooseObjList.size() == 1) {
                String data = editable.toString();
                if (data.startsWith(DefaultGlobal.SPACE_TOKENIZER + "")) {
                    ChooseObjEntity lastEntity = mChooseObjList.get(0);
                    if (length > lastEntity.end)
                        editable.delete(0, length - lastEntity.end);
                }
            } else {
                ChooseObjEntity lastEntity = mChooseObjList.get(mChooseObjList.size() - 1);
                if (length > lastEntity.end) {
                    String data = editable.subSequence(lastEntity.start, length).toString();
                    if (data.startsWith(DefaultGlobal.SPACE_TOKENIZER + ""))
                        editable.delete(lastEntity.start, lastEntity.start + (length - lastEntity.end));
                }
            }
        }

    }

    private void updateText(ChooseObjEntity objEntity) {
        if (objEntity == null) {
            return;
        }
        if (objEntity.isSelected) {
            objEntity.isSelected = false;
            if (objEntity.isDirty) {
                // todo,can popup a window to let user to edit the error email address
                objEntity.drawable.setTextBgColor(mDirtyTextBgColor);
                objEntity.drawable.setTextFgColor(mDirtyTextFgColor);
            } else {
                objEntity.drawable.setTextBgColor(mValidTextBgColor);
                objEntity.drawable.setTextFgColor(mValidTextFgColor);
            }
        } else {
            objEntity.isSelected = true;
            if (objEntity.isDirty) {
                objEntity.drawable.setTextBgColor(mDirtySelectedTextBgColor);
                objEntity.drawable.setTextFgColor(mDirtySelectedTextFgColor);
            } else {
                objEntity.drawable.setTextBgColor(mValidSelectedTextBgColor);
                objEntity.drawable.setTextFgColor(mValidSelectedTextFgColor);
            }
        }

        String tpname = objEntity.name;
        String rpstr = DefaultGlobal.SPACE_TOKENIZER + "";
        if (tpname.contains(rpstr)) {
            tpname = tpname.replaceAll(rpstr, "");
        }

        SpannableString sp = getSpannableString2(objEntity.isDirty, tpname, objEntity.drawable, objEntity);
        Editable editable = getEditableText();
        removeSpan(editable, objEntity);

        editable.replace(objEntity.start, objEntity.end, sp);
    }

    private SpannableString getSpannableString2(boolean isDirty, String name,
                                                TextDrawable drawable, ChooseObjEntity objEntity) {
        String value = getValue(name);
        SpannableString spanStr = getUncompleteSpanStr(isDirty, value,
                drawable, objEntity);
        return getSpannableString2(spanStr); // means copy one
    }

    private SpannableString getSpannableString2(SpannableString spanStr) {
        return getSpannableString2(spanStr, DefaultGlobal.SPACE_TOKENIZER);
    }

    private SpannableString getSpannableString2(SpannableString spanStr, char customChar) {
        SpannableString sp = new SpannableString(spanStr);
        TextUtils.copySpansFrom((Spanned) spanStr, 0, spanStr.length(), Object.class, sp, 0);
        return sp;
    }

    private void drawable2Text() {
        setSingleLine(true);
        Editable editable = getEditableText();
        for (ChooseObjEntity objEntity : mChooseObjList) {
            objEntity.isSelected = false;
            if (objEntity.isDirty) {
                objEntity.drawable.setTextBgColor(mDirtyTextBgColor);
                objEntity.drawable.setTextFgColor(mDirtyTextFgColor);
            } else {
                objEntity.drawable.setTextBgColor(mValidTextBgColor);
                objEntity.drawable.setTextFgColor(mValidTextFgColor);
            }
            removeSpan(editable, objEntity);
        }
    }

    private void text2Drawable() {
        setSingleLine(false);
        Editable editable = getEditableText();
        SpannableString sp = new SpannableString(editable.toString());
        for (ChooseObjEntity objEntity : mChooseObjList)
            addSpan(sp, objEntity);
        editable.replace(0, sp.length(), sp);
        setSelection(editable.length());
    }

    private void addSpan(SpannableString sp, ChooseObjEntity objEntity) {
        AutoClickableImageSpan imageSpan = new AutoClickableImageSpan(objEntity.drawable);
        imageSpan.setDirty(objEntity.isDirty);
        imageSpan.setChooseObjEntity(objEntity);
        sp.setSpan(imageSpan, objEntity.start, objEntity.end - 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    public interface OnDeleteObjListener {
        void delete(String outKey);
    }

    public interface OnAddObjListener {
        void add(String objName, String outKey);
    }

    private class AutoFocusChangeListener implements OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus)
                drawable2Text();
            else
                text2Drawable();
        }
    }


    private class AutoTextChangedListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            bTextInput = true;
            if (bSelectionRemoved) {
                Editable editableText = getEditableText();
                setSelection(editableText.length());
                bSelectionRemoved = false;
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s)) {
                return;
            }
            bTextInput = true;
            dealDirty();
        }
    }


    // ************************** begin CustomTokenizer ********************************
    private class CustomTokenizer implements Tokenizer {
        CustomTokenizer(char ch) {
            customChar = ch;
        }

        @Override
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;
            String tps = text.toString();
            int tpLen = tps.length();
            while (i > 0 && (i - 1 < tpLen) && text.charAt(i - 1) != customChar) {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }
            return i;
        }

        @Override
        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();
            while (i < len) {
                if (text.charAt(i) == customChar) {
                    return i;
                } else {
                    i++;
                }
            }
            return len;
        }

        @Override
        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();
            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }
            if (i > 0 && text.charAt(i - 1) == customChar) {
                return text;
            } else {
                if (text instanceof Spanned) {
                    String outKey = getOutKey((SpannableString) text);
                    text = text.subSequence(0, text.length() - outKey.length());
                    SpannableString sp = getSpannableString(
                            (SpannableString) text, customChar);
                    confirmInput(sp, outKey);
                    return sp;
                } else {
                    return text + (customChar + "");
                }
            }
        }
    }
    // ************************** end CustomTokenizer ********************************

    // ************************** begin AutoClickableImageSpan ********************************
    private class AutoClickableImageSpan extends ImageSpan {

        private boolean isDirty;
        private ChooseObjEntity objEntity;

        AutoClickableImageSpan(Drawable d) {
            super(d);
        }

        public void onClick(View v) {
            updateText(objEntity);
        }

        void setDirty(boolean flag) {
            isDirty = flag;
        }

        boolean isDirty() {
            return isDirty;
        }

        void setChooseObjEntity(ChooseObjEntity obj) {
            objEntity = obj;
        }

    }
    // ************************** end AutoClickableImageSpan ********************************


    // ************************** begin AutoLinkMovementMethod ********************************
    private class AutoLinkMovementMethod extends LinkMovementMethod {

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer,
                                    MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                image = buffer.getSpans(off, off,
                        AutoClickableImageSpan.class);
                if (image.length != 0) {
                    if (action == MotionEvent.ACTION_DOWN) {
                        image[0].onClick(widget);
//                        Selection.removeSelection(buffer);
                    }
//                    else if (action == MotionEvent.ACTION_DOWN) {
//                        Selection.setSelection(buffer,
//                                buffer.getSpanStart(image[0]),
//                                buffer.getSpanEnd(image[0]));
//                    }
                    return true;
                } else {
                    Selection.removeSelection(buffer);
                }
            }
            return super.onTouchEvent(widget, buffer, event);
        }
    }
    // ************************** end AutoLinkMovementMethod ********************************

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (mRightListener != null) {
                    Drawable drawableRight = getCompoundDrawables()[DRAWABLE_RIGHT];
                    if (drawableRight != null && event.getRawX() >= (getRight() - drawableRight.getBounds().width())) {
                        mRightListener.onDrawableRightClick(this);
                        return true;
                    }
                }

                if (mLeftListener != null) {
                    Drawable drawableLeft = getCompoundDrawables()[DRAWABLE_LEFT];
                    if (drawableLeft != null && event.getRawX() <= (getLeft() + drawableLeft.getBounds().width())) {
                        mLeftListener.onDrawableLeftClick(this);
                        return true;
                    }
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    interface DrawableLeftListener {
        void onDrawableLeftClick(View view);
    }

    interface DrawableRightListener {
        void onDrawableRightClick(View view);
    }
}

