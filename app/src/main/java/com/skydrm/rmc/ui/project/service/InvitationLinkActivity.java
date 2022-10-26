package com.skydrm.rmc.ui.project.service;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.skydrm.rmc.R;
import com.skydrm.rmc.engine.FileOperation;
import com.skydrm.rmc.ui.base.BaseActivity;
import com.skydrm.sdk.exception.RmsRestAPIException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jrzhou on 6/12/2017.
 */

public class InvitationLinkActivity extends BaseActivity {
    @BindView(R.id.ignore_invited)
    Button mBtIgnoreInvited;
    @BindView(R.id.accept_invitation)
    Button mBtAcceptInvitation;
    private int invitationId;
    private String code;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.click_email_to_proejcts);
        ButterKnife.bind(this);
        initListener();
        initData();
    }

    private void initData() {
        try {
            Uri uri = Uri.parse(getIntent().getStringExtra("INVITATION"));
            invitationId = Integer.valueOf(uri.getQueryParameter("id"));
            code = uri.getQueryParameter("code");
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void initListener() {
        mBtAcceptInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptInvitation();
            }
        });
        mBtIgnoreInvited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleIgnore();
            }
        });
    }

    private View ignoreView;

    // handle ignore invitation of a project.
    private void handleIgnore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ignoreView = LayoutInflater.from(this).inflate(R.layout.layout_ignore_invite_dialog, null);
        final EditText et_ignore = (EditText) ignoreView.findViewById(R.id.ev_ignoreReason);
        builder.setCancelable(false);
        builder.setView(ignoreView);
        builder.setPositiveButton(this.getResources().getString(R.string.c_Ignore), null);
        builder.setNegativeButton(this.getResources().getString(R.string.common_cancel_initcap), null);
        builder.setTitle(com.skydrm.sdk.R.string.app_name);
        builder.setMessage(this.getResources().getString(R.string.hint_msg_ask_ignore_reason));
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reason = et_ignore.getText().toString();
                declineInvitation(reason);
                hindSoftKeyBoard(ignoreView);
                dialog.dismiss();
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void hindSoftKeyBoard(View view) {
        // hidden soft-keyboard
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            // the first para is windowToken, which can be any current view's window token.
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void declineInvitation(String reason) {
        FileOperation.projectIgnoreInvitation(this, invitationId, code, reason, new FileOperation.IIgnoreInvitation() {
            @Override
            public void onIgnoreInvitation(boolean result) {
                if (result) {
                    Toast.makeText(InvitationLinkActivity.this, getString(R.string.ignore_success), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(RmsRestAPIException e) {
            }
        });
    }


    private void acceptInvitation() {
        FileOperation.projectAcceptInvitation(this, invitationId, code, new FileOperation.IAcceptInvitation() {
            @Override
            public void onAcceptInvitation(final String projectId) {
                if (!TextUtils.isEmpty(projectId)) {
                    Toast.makeText(InvitationLinkActivity.this, getString(R.string.accept_invitation_succeed), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(RmsRestAPIException e) {

            }
        });
    }
}
