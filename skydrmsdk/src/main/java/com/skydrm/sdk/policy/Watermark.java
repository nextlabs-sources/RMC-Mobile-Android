package com.skydrm.sdk.policy;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class Watermark {
    private String serialNumber;
    // content
    private String text;
    private int transparentRatio;
    private String fontName;
    private int fontSize;
    private String fontColor;
    private String rotation;
    private boolean bRepeat;
    private String density;

    private String rawContent;
    private boolean bIsBuildSucceed = false;

    public Watermark() {
        clear();
    }

    public Watermark(String json) {
        clear();
        if (buildFromJson(json)) {
            bIsBuildSucceed = true;
        }
    }

    public Watermark(JSONArray obligations) {
        clear();
        if (buildFromJsonArray(obligations)) {
            bIsBuildSucceed = true;
        }
    }

    static public Watermark buildDefault() {
        Watermark w = new Watermark();
        w.text = "$(User)\n$(Date) $(Time)";
        w.density = "dense";
        w.fontColor = "#008000";
        w.fontName = "Sitka Text";
        w.fontSize = 26;
        w.serialNumber = "edbd53a";
        w.rotation = "Anticlockwise";
        w.bRepeat = true;
        w.transparentRatio = 70;
        return w;
    }

    public void clear() {
        text = null;
        transparentRatio = -1;
        fontName = null;
        fontSize = -1;
        fontColor = null;
        rotation = null;
        bRepeat = false;
        density = null;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTransparentRatio() {
        return transparentRatio;
    }

    public void setTransparentRatio(int transparentRatio) {
        this.transparentRatio = transparentRatio;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

    public boolean isbRepeat() {
        return bRepeat;
    }

    public void setbRepeat(boolean bRepeat) {
        this.bRepeat = bRepeat;
    }

    public String getDensity() {
        return density;
    }

    public void setDensity(String density) {
        this.density = density;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public boolean isbIsBuildSucceed() {
        return bIsBuildSucceed;
    }

    private boolean buildFromJson(@NonNull String json) {
        boolean bRet = true;
        setRawContent(json);
        try {
            JSONObject jsData = new JSONObject(json);
            JSONObject results = jsData.getJSONObject("results");
            String watermarkConfig = results.getString("watermarkConfig");

            JSONObject watermarkConfigJSO = new JSONObject(watermarkConfig);
            setSerialNumber(watermarkConfigJSO.getString("serialNumber"));

            String watermarkContent = watermarkConfigJSO.getString("content");
            JSONObject watermarkContentJSO = new JSONObject(watermarkContent);
            // content
            setText(watermarkContentJSO.getString("text"));
            setTransparentRatio(watermarkContentJSO.getInt("transparentRatio"));
            setFontName(watermarkContentJSO.getString("fontName"));
            setFontSize(watermarkContentJSO.getInt("fontSize"));
            setFontColor(watermarkContentJSO.getString("fontColor"));
            setRotation(watermarkContentJSO.getString("rotation"));
            setbRepeat(watermarkContentJSO.getBoolean("repeat"));
            setDensity(watermarkContentJSO.getString("density"));
        } catch (JSONException e) {
            e.printStackTrace();
            bRet = false;
        }

        return bRet;
    }

    private boolean buildFromJsonArray(JSONArray obligations) {
        if (obligations == null) {
            return false;
        }
        for (int i = 0; i < obligations.length(); i++) {
            try {
                JSONObject jsonObject = obligations.getJSONObject(i);
                if (jsonObject.has("attributes")
                        && !jsonObject.isNull("attributes")) {
                    JSONArray attributesArray =
                            jsonObject.getJSONArray("attributes");
                    for (int j = 0; j < attributesArray.length(); j++) {
                        JSONObject attributesObjContent = attributesArray.getJSONObject(j);
                        String name = attributesObjContent.getString("name");
                        String value = attributesObjContent.getString("value");
                        if (TextUtils.equals(name, "Text")) {
                            setText(value);
                        }
                        if (TextUtils.equals(name, "Transparency")) {
                            setTransparentRatio(Integer.parseInt(value));
                        }
                        if (TextUtils.equals(name, "FontName")) {
                            setFontName(value);
                        }
                        if (TextUtils.equals(name, "FontSize")) {
                            setFontSize(Integer.parseInt(value));
                        }
                        if (TextUtils.equals(name, "TextColor")) {
                            setFontColor(value);
                        }
                        if (TextUtils.equals(name, "Rotation")) {
                            setRotation(value);
                        }
//                        if (TextUtils.equals(name, "Placement")) {
//                            setbRepeat();
//                        }
                        if (TextUtils.equals(name, "Density")) {
                            setDensity(value);
                        }
//                        if (TextUtils.equals(name, "policy_model_id")) {
//                            setSerialNumber(value);
//                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
