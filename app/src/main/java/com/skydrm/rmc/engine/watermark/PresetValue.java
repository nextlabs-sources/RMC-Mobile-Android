package com.skydrm.rmc.engine.watermark;

/**
 * Created by aning on 11/3/2017.
 *
 * The preset value when user edit watermark
 */

public interface PresetValue {
     String PRESET_EMAIL_ID = "EmailID";
     String PRESET_LINE_BREAK = "Line Break";
     String PRESET_DATE = "Date";
     String PRESET_TIME = "Time";

     String DOLLAR_USER = "$(User)";
     String DOLLAR_BREAK = "$(Break)";
     String DOLLAR_DATE = "$(Date)";
     String DOLLAR_TIME = "$(Time)";

     // ImageSpan wrapped preset value
     // Note: leading and trailing has a whitespace respectively, or else will error when user input in edittext.
     String SPAN_PRESET_EMAIL_ID = " EmailID ";
     String SPAN_PRESET_LINE_BREAK = " Line Break ";
     String SPAN_PRESET_DATE = " Date ";
     String SPAN_PRESET_TIME = " Time ";
}
