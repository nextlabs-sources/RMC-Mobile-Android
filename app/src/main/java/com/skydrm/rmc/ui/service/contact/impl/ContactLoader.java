package com.skydrm.rmc.ui.service.contact.impl;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.CursorLoader;

public class ContactLoader extends CursorLoader {
    private static final Uri QUERY_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
    private static final String[] PROJECTION =
            {
                    ContactsContract.CommonDataKinds.Email._ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Email.DATA
//                    ContactsContract.CommonDataKinds.Email.TYPE,
//                    ContactsContract.CommonDataKinds.Email.LABEL
            };

    private ContactLoader(Context context) {
        super(context, QUERY_URI, PROJECTION, "", null, "");
    }

    @Override
    public Cursor loadInBackground() {
        return super.loadInBackground();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }

    public static ContactLoader newInstance(Context context) {
        return new ContactLoader(context);
    }
}
