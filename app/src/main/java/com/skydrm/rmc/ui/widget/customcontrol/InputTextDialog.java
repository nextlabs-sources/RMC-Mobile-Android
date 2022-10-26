package com.skydrm.rmc.ui.widget.customcontrol;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.IBinder;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.skydrm.rmc.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jrzhou on 4/6/2017.
 */

public abstract class InputTextDialog {
    protected Context mContext;
    private boolean validateFolderName;
    //Verify the folder name of regular expressions
    private String regularExpression = "^[\\u00C0-\\u1FFF\\u2C00-\\uD7FF\\w \\x22\\x23\\x27\\x2C\\x2D]+$";
    private Pattern pattern = Pattern.compile(regularExpression);
    private Matcher matcher;
    private ProgressDialog mProgressDialog;
    private AlertDialog dialog;
    private TextInputLayout textInputLayout;

    //Verify the folder name is in accordance with the rules
    public boolean validateFolderName(String folderName) {
        matcher = pattern.matcher(folderName);
        return matcher.matches();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMessage("Waiting...");
        }
        mProgressDialog.show();
    }

    private void hideKeyBoard(IBinder windowToken) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(windowToken, 0);
    }

    public void showDialog(String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        View view = LayoutInflater.from(mContext).inflate(R.layout.alertdialog_folder_name_layout, null);
        alertDialog.setView(view);
        dialog = alertDialog.create();
        dialog.show();
        textInputLayout = (TextInputLayout) view.findViewById(R.id.alert_dialog_textInputLayout);
        final EditText alertDialogEditText = (EditText) view.findViewById(R.id.alert_dialog_editText);
        Button alertDialogYes = (Button) view.findViewById(R.id.alert_dialog_yes);
        Button alertDialogNo = (Button) view.findViewById(R.id.alert_dialog_no);

        validateFolderName = false;
        alertDialogEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateFolderName = validateFolderName(s.toString());
                if (!validateFolderName) {
                    if (TextUtils.isEmpty(s.toString())) {
                        textInputLayout.setError("");
                    } else {
                        textInputLayout.setError(mContext.getResources().getString(R.string.folder_name_rules));
                    }
                } else {
                    textInputLayout.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        alertDialogYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String folderName = textInputLayout.getEditText().getText().toString().trim();
                if (!TextUtils.isEmpty(folderName)) {
                    if (validateFolderName) {
                        hideKeyBoard(alertDialogEditText.getWindowToken());
                        textInputLayout.setErrorEnabled(false);
                        showProgressDialog();
                        OnClickYes(folderName);
                    }
                } else {
                    hideKeyBoard(alertDialogEditText.getWindowToken());
                    inputTextIsNull();
                }
            }
        });

        alertDialogNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard(alertDialogEditText.getWindowToken());
                OnClickNo();
            }
        });
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void dismissProgressDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void setErrorMessage(String errorMessage) {
        textInputLayout.setError(errorMessage);
    }

    public void setRegularExpression(String regularExpression) {
        this.regularExpression = regularExpression;
    }

    protected InputTextDialog(Context context) {
        this.mContext = context;
    }

    protected abstract void inputTextIsNull();

    protected abstract void OnClickYes(String folderName);

    protected abstract void OnClickNo();

}
