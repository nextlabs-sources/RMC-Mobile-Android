package com.skydrm.rmc.dbbridge.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.skydrm.rmc.dbbridge.IOwner;

import org.json.JSONException;
import org.json.JSONObject;

public class Owner implements IOwner, Parcelable {
    private int userId;
    private String name;
    private String email;

    private Owner() {

    }

    private Owner(Parcel in) {
        userId = in.readInt();
        name = in.readString();
        email = in.readString();
    }

    public static final Creator<Owner> CREATOR = new Creator<Owner>() {
        @Override
        public Owner createFromParcel(Parcel in) {
            return new Owner(in);
        }

        @Override
        public Owner[] newArray(int size) {
            return new Owner[size];
        }
    };


    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    public static Owner newByJson(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        if (raw.equals("{}")) {
            return null;
        }
        Owner ret = new Owner();
        try {
            JSONObject ownerObj = new JSONObject(raw);
            ret.userId = ownerObj.optInt("user_id");
            ret.name = ownerObj.optString("name");
            ret.email = ownerObj.optString("email");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static Owner createOwner(int userId, String name, String email) {
        Owner o = new Owner();
        o.userId = userId;
        o.name = name;
        o.email = email;
        return o;
    }

    public static String generateRawJson(int userId, String name, String email) {
        String ret = "{}";
        JSONObject ownObj = new JSONObject();
        try {
            ownObj.put("user_id", userId);
            ownObj.put("name", name);
            ownObj.put("email", email);
            ret = ownObj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeString(name);
        dest.writeString(email);
    }
}
