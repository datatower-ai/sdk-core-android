/*
 * Created by wangzhuozhou on 2015/08/01.
 * Copyright 2015－2020 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.datatower.analytics.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public final class DataUtils {


    public static String getUUID() {
        SecureRandom random = new SecureRandom();
        String uuid = String.valueOf(random.nextLong());
        return uuid.replace("-", "");
    }


    public static String getSession() {
        StringBuilder uuid = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            uuid.append(Integer.toHexString(new Random().nextInt(16)));
        }
        return uuid.toString();
    }

    // 返回当前时区偏移，单位毫秒
    public static double getTimezoneOffset(long time, TimeZone timeZone) {
        TimeZone tz = (null == timeZone) ? TimeZone.getDefault() : timeZone;
        return tz.getOffset(time) / (1000.0 * 60 * 60);
    }

    public static void mergeJSONObject(final JSONObject source, JSONObject dest, TimeZone timeZone)
            throws JSONException {
        Iterator<String> sourceIterator = source.keys();
        while (sourceIterator.hasNext()) {
            String key = sourceIterator.next();
            Object value = source.get(key);
            if (value instanceof Date) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);
                if (null != timeZone) {
                    dateFormat.setTimeZone(timeZone);
                }
                dest.put(key, dateFormat.format((Date) value));
            } else if (value instanceof JSONArray) {
                dest.put(key, formatJSONArray((JSONArray) value, timeZone));
            } else if (value instanceof JSONObject) {
                dest.put(key, formatJSONObject((JSONObject) value, timeZone));
            } else {
                dest.put(key, value);
            }
        }
    }



    public static JSONObject formatJSONObject(JSONObject jsonObject, TimeZone timeZone) {
        JSONObject result = new JSONObject();
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = null;
            try {
                value = jsonObject.get(key);
                if (value instanceof Date) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);
                    if (null != timeZone) {
                        dateFormat.setTimeZone(timeZone);
                    }
                    result.put(key, dateFormat.format((Date) value));
                } else if (value instanceof JSONArray) {
                    result.put(key, formatJSONArray((JSONArray) value, timeZone));
                } else if (value instanceof JSONObject) {
                    result.put(key, formatJSONObject((JSONObject) value, timeZone));
                } else {
                    result.put(key, value);
                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }

        }
        return result;

    }

    public static JSONArray formatJSONArray(JSONArray jsonArr, TimeZone timeZone) {
        JSONArray result = new JSONArray();
        for (int i = 0; i < jsonArr.length(); i++) {
            Object value = jsonArr.opt(i);
            if (value != null) {
                if (value instanceof Date) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);
                    if (null != timeZone) {
                        dateFormat.setTimeZone(timeZone);
                    }
                    result.put(dateFormat.format((Date) value));
                } else if (value instanceof JSONArray) {
                    result.put(formatJSONArray((JSONArray) value, timeZone));
                } else if (value instanceof JSONObject) {
                    JSONObject newObject = formatJSONObject((JSONObject) value, timeZone);
                    result.put(newObject);
                } else {
                    result.put(value);
                }
            }

        }
        return result;
    }


}
