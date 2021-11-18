package com.roiquery.analytics.utils;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;

public class EventUtils {

    private static final String TAG = "ROIQuery.EventUtils";
    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z\\d_]{0,49}$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidEventName(String name)  {
        if(TextUtils.isEmpty(name)){
            LogUtils.e("Empty event name is not allowed.") ;
            return false;
        }
         if(!KEY_PATTERN.matcher(name).matches()){
             LogUtils.e("event name[" + name + "] is not valid. The property KEY must be string that starts with English letter, " +
                     "and contains letter, number, and '_'. The max length of the property KEY is 50. ");
             return false;
        }
         return true;
    }

    public static boolean isValidProperty(JSONObject properties){

        if(properties != null ) {
            for (Iterator iterator = properties.keys(); iterator.hasNext(); ) {
                String key = (String) iterator.next();

                if (TextUtils.isEmpty(key)) {
                    LogUtils.e(TAG, "Empty property name is not allowed.");
                    return false;
                }

                if (!(KEY_PATTERN.matcher(key).matches())) {
                    LogUtils.e(TAG, "Property name[" + key + "] is not valid. The property KEY must be string that starts with English letter, " +
                            "and contains letter, number, and '_'. The max length of the property KEY is 50. ");
                    return false;
                }

                try {
                    Object value = properties.get(key);
                    if (!(value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Date || value instanceof JSONArray || value instanceof JSONObject)) {
                        LogUtils.e(TAG, "Property value must be type String, Number, Boolean, Date, JSONObject or JSONArray");
                        return false;
                    }
                    if (value instanceof Number) {
                        double number = ((Number) value).doubleValue();
                        if (number > 9999999999999.999 || number < -9999999999999.999) {
                            LogUtils.e(TAG, "The number value [" + value + "] is invalid.");
                            return false;
                        }
                    }
                } catch (JSONException e) {
                    LogUtils.e(TAG, "Unexpected parameters." + e);
                    return false;
                }
            }
        }
        return true;
    }

    // cut string by byte limitations
    public static byte[] cutToBytes(String s, int charLimit) throws UnsupportedEncodingException {
        byte[] utf8 = s.getBytes("UTF-8");
        if (utf8.length <= charLimit) {
            return utf8;
        }
        if ((utf8[charLimit] & 0x80) == 0) {
            // the limit doesn't cut an UTF-8 sequence
            return Arrays.copyOf(utf8, charLimit);
        }
        int i = 0;
        while ((utf8[charLimit-i-1] & 0x80) > 0 && (utf8[charLimit-i-1] & 0x40) == 0) {
            ++i;
        }
        if ((utf8[charLimit-i-1] & 0x80) > 0) {
            // we have to skip the starter UTF-8 byte
            return Arrays.copyOf(utf8, charLimit-i-1);
        } else {
            // we passed all UTF-8 bytes
            return Arrays.copyOf(utf8, charLimit-i);
        }
    }

}
