package com.skydrm.rmc.ui.project.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.rmc.ui.project.ProjectActivity;
import com.skydrm.rmc.ui.project.service.contact.INewProjectContact;
import com.skydrm.rmc.ui.project.service.contact.NewProjectPresenter;
import com.skydrm.rmc.ui.widget.customcontrol.CommentWidget;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewProjectActivity extends BaseActivity implements TextWatcher,
        View.OnKeyListener, INewProjectContact.IView {
    //Verify the project name of regular expressions
    private static final String FOLDER_NAME_PATTERN = "^[\\u00C0-\\u1FFF\\u2C00-\\uD7FF\\w \\x22\\x23\\x27\\x2C\\x2D]+$";

    @BindView(R.id.edText_name_of_the_project)
    EditText mEtName;
    @BindView(R.id.editText_description)
    EditText mEtDescription;
    @BindView(R.id.create_project)
    Button mBtCreateProject;

    @BindView(R.id.edText_name_textInputLayout)
    TextInputLayout mInputNameLayout;
    @BindView(R.id.editText_description_textInputLayout)
    TextInputLayout mInputDescriptionLayout;

    @BindView(R.id.new_project_et_email_address)
    EditText mEtEmailAddress;
    @BindView(R.id.new_project_flowLayout)
    FlowLayout mFlEmailContainer;
    @BindView(R.id.comment_widget)
    CommentWidget mCommentWidget;

    private boolean validateProjectName;
    private EmailEditText mEmailEditText;

    private ProgressDialog mDialog;

    private INewProjectContact.IPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project3);
        ButterKnife.bind(this);
        initViewsAndEvents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultcode, Intent data) {
        super.onActivityResult(requestCode, resultcode, data);
        if (resultcode == RESULT_OK && requestCode == Constant.REQUEST_CODE_SELECT_EMAILS) {
            mFlEmailContainer.wrapEmailFromContact(data);
        }
    }

    private void initViewsAndEvents() {
        mEtEmailAddress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Drawable drawable = mEtEmailAddress.getCompoundDrawables()[2];
                if (drawable == null) return false;
                if (motionEvent.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (motionEvent.getX() > mEtEmailAddress.getWidth() - mEtEmailAddress.getPaddingLeft() - drawable.getIntrinsicWidth()) {
                    mEtEmailAddress.setFocusableInTouchMode(false);
                    mEtEmailAddress.setFocusable(false);
                    lunchContactPageWithResult(Constant.REQUEST_CODE_SELECT_EMAILS);
                } else {
                    mEtEmailAddress.setFocusableInTouchMode(true);
                    mEtEmailAddress.setFocusable(true);
                }
                return NewProjectActivity.super.onTouchEvent(motionEvent);
            }
        });
        mPresenter = new NewProjectPresenter(this);

        findViewById(R.id.new_project_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hidden soft-keyboard
                InputMethodManager imm = (InputMethodManager) NewProjectActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    // the first para is windowToken, which can be any current view's window token.
                    imm.hideSoftInputFromWindow(mBtCreateProject.getWindowToken(), 0);
                }
                finish();
            }
        });
        mEtDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mInputDescriptionLayout.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEtName.addTextChangedListener(this);
        mEmailEditText = new EmailEditText();
        mEtEmailAddress.addTextChangedListener(mEmailEditText);
        mEtEmailAddress.setOnKeyListener(this);

        // resolve the issue that input box can't slid up and down when ScrollView layout embedded editText(fix bug 42766)
        mEtDescription.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // disallow it's parent and ancestors view intercept touch event
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        // recover intercept
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
        mBtCreateProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProject();
            }
        });
        mFlEmailContainer.setOnClearInputEmailListener(new FlowLayout.OnClearInputEmailListener() {
            @Override
            public void onClear() {
                mEtEmailAddress.setText("");
            }
        });
    }

    private void createProject() {
        String name = mEtName.getText().toString().trim();
        String desc = mEtDescription.getText().toString().trim();

        // check project name
        if (TextUtils.isEmpty(name)) {
            mInputNameLayout.setError(getResources().getString(R.string.please_input_project_name));
            return;
        }
        // check project description
        if (TextUtils.isEmpty(desc)) {
            mInputDescriptionLayout.setError(getResources().getString(R.string.please_input_brief_for_project));
            return;
        }

        if (!TextUtils.isEmpty(mEtEmailAddress.getText().toString())) {
            mFlEmailContainer.wrapEmail(mEtEmailAddress.getText().toString(), true);
        }

        List<String> validEmails = getValidEmails();
        if (null == validEmails) {
            return;
        }
        if (!validateProjectName) {
            return;
        }

        // hidden soft-keyboard
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            // the first para is windowToken, which can be any current view's window token.
            imm.hideSoftInputFromWindow(mBtCreateProject.getWindowToken(), 0);
        }

        //clean hint message
        mInputNameLayout.setErrorEnabled(false);
        mInputDescriptionLayout.setErrorEnabled(false);

        String invitationMsg = mCommentWidget.getText().toString().trim();

        mPresenter.newProject(name, desc, validEmails, invitationMsg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEtName != null) {
            mEtName.removeTextChangedListener(this);
            mEtName = null;
        }
        if (mEtEmailAddress != null) {
            mEtEmailAddress.removeTextChangedListener(mEmailEditText);
            mEtEmailAddress = null;
            mEmailEditText = null;
        }
        if (mPresenter != null) {
            mPresenter.onDestroy();
            mPresenter = null;
        }
    }

    //Verify the project name is in accordance with the rules
    public boolean validateProjectName(String projectName) {
        Matcher matcher = Pattern.compile(FOLDER_NAME_PATTERN).matcher(projectName);
        return matcher.matches();
    }

    private List<String> getValidEmails() {
        List<String> validEmails = new ArrayList<>();
        List<String> invalidEmails = new ArrayList<>();
        List<String> emailList = mFlEmailContainer.getEmailList();
        if (emailList.size() > 0) {
            try {
                for (String oneEmail : emailList) {
                    if (!CommonUtils.checkEmailAddress(oneEmail)) {
                        invalidEmails.add(oneEmail);
                    } else {
                        validEmails.add(oneEmail);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (invalidEmails.size() > 0) {
                CommonUtils.handleErrorEmail(this, invalidEmails);
                return null;
            }
        }
        return validEmails;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        validateProjectName = validateProjectName(s.toString());
        if (validateProjectName) {
            mInputNameLayout.setError("");
        } else {
            if (TextUtils.isEmpty(s.toString())) {
                mInputNameLayout.setError("");
            } else {
                mInputNameLayout.setError(getResources().getString(R.string.project_name_rules));
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        boolean b = false;
        if (keyCode == KeyEvent.KEYCODE_ENTER && KeyEvent.ACTION_DOWN == event.getAction()) {
            if (null != mEtEmailAddress && !TextUtils.isEmpty(mEtEmailAddress.getText().toString())) {
                mFlEmailContainer.wrapEmail(mEtEmailAddress.getText().toString(), true);
                b = true;
            }
        }
        return b;
    }

    @Override
    public void onCreatingProject() {
        showLoadDialog();
    }

    @Override
    public void onCreateSucceed(IProject project) {
        dismissDialog();
        toProjectActivity(project);
    }

    private void toProjectActivity(IProject project) {
        Intent i = new Intent(NewProjectActivity.this,
                ProjectActivity.class);
        Bundle b = new Bundle();
        b.putParcelable(Constant.PROJECT_DETAIL, (Parcelable) project);
        b.putInt(Constant.PROJECT_INDEX, 0);
        i.putExtras(b);
        startActivity(i);
    }

    @Override
    public void onCreateFailed(Exception e) {
        dismissDialog();
        ExceptionHandler.handleException(this, e);
    }

    private void showLoadDialog() {
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getResources().getString(R.string.waite_creating));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    private void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    class EmailEditText implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mFlEmailContainer.wrapEmail(s.toString(), false);
        }
    }
}
