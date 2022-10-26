package com.skydrm.rmc.ui.myspace.myvault.model.domain;

import android.graphics.Bitmap;

/**
 * Created by hhu on 5/12/2017.
 */

public class Recipient {
    private Bitmap recipientAvatar;
    private String recipientName;
    private String recipientEmail;

    public Bitmap getRecipientAvatar() {
        return recipientAvatar;
    }

    public void setRecipientAvatar(Bitmap recipientAvatar) {
        this.recipientAvatar = recipientAvatar;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
}
