package com.skydrm.rmc.utils.emailUtils;

/**
 * this class used to label a email address (separated by space) -- contains right email address and error email format address(is dirty)
 */
public class ChooseObjEntity {
    // the email content
    public String name = null;
    // start position of one email in Editable
    public int start = 0;
    // end position of one email in Editable
    public int end = 0;
    // if the email is dirty(error email format address)
    public boolean isDirty = false;
    // if this email is selected
    public boolean isSelected = false;
    // the bg image of one email
    public TextDrawable drawable = null;
    // the content without space
    public String outKey = null;
}

