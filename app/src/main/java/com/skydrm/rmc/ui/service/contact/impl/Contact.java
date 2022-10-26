package com.skydrm.rmc.ui.service.contact.impl;

import android.database.Cursor;
import android.provider.ContactsContract;

import com.skydrm.rmc.utils.sort.IBaseSortable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Contact implements IBaseSortable {
    private String mName;
    private List<Detail> mDetails;

    public Contact(String name, List<Detail> details) {
        this.mName = name;
        this.mDetails = details;
    }

    public String getName() {
        return mName;
    }

    public List<Detail> getDetails() {
        return mDetails;
    }

    @Override
    public String getSortableName() {
        return mName;
    }

    @Override
    public long getSortableSize() {
        return 0;
    }

    @Override
    public long getSortableTime() {
        return 0;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    public static class Detail {
        String email;
        boolean checked;

        public Detail() {
        }

        public Detail(String email, boolean checked) {
            this.email = email;
            this.checked = checked;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Detail detail = (Detail) o;
            return checked == detail.checked &&
                    email.equals(detail.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(email, checked);
        }
    }

    static List<Contact> valuesOf(Cursor cursor) {
        List<Contact> ret = new ArrayList<>();
        if (cursor == null) {
            return ret;
        }

        Map<String, List<String>> container = new HashMap<>();
        while (cursor.moveToNext()) {
            String displayName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA)).trim();

            if (container.containsKey(displayName)) {
                container.get(displayName).add(email);
            } else {
                ArrayList<String> emails = new ArrayList<>();
                emails.add(email);
                container.put(displayName, emails);
            }
        }

        if (container.size() != 0) {
            Set<String> names = container.keySet();
            for (String name : names) {
                List<Detail> details = new ArrayList<>();
                List<String> emails = container.get(name);
                for (String e : emails) {
                    if (!details.contains(new Detail(e,false))){
                        details.add(new Detail(e, false));
                    }
                }
                ret.add(new Contact(name, details));
            }
        }

        return ret;
    }
}
