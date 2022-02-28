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

package com.roiquery.analytics.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.webkit.WebSettings;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

public final class DataUtils {

    private static final String marshmallowMacAddress = "02:00:00:00:00:00";
    private static final String SHARED_PREF_EDITS_FILE = "sensorsdata";
    private static final String SHARED_PREF_DEVICE_ID_KEY = "sensorsdata.device.id";
    private static final String SHARED_PREF_USER_AGENT_KEY = "sensorsdata.user.agent";
    private static final String SHARED_PREF_APP_VERSION = "sensorsdata.app.version";
    private static final Map<String, String> sCarrierMap = new HashMap<String, String>() {
        {
            //中国移动
            put("46000", "中国移动");
            put("46002", "中国移动");
            put("46007", "中国移动");
            put("46008", "中国移动");

            //中国联通
            put("46001", "中国联通");
            put("46006", "中国联通");
            put("46009", "中国联通");

            //中国电信
            put("46003", "中国电信");
            put("46005", "中国电信");
            put("46011", "中国电信");

            //中国卫通
            put("46004", "中国卫通");

            //中国铁通
            put("46020", "中国铁通");

        }
    };

    private static final List<String> mInvalidAndroidId = new ArrayList<String>() {
        {
            add("9774d56d682e549c");
            add("0123456789abcdef");
        }
    };
    private static final String TAG = "SA.SensorsDataUtils";

    private static String getJsonFromAssets(String fileName, Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bf = null;
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            LogUtils.printStackTrace(e);
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    LogUtils.printStackTrace(e);
                }
            }
        }
        return stringBuilder.toString();
    }

    public static String getUUID() {
        SecureRandom random = new SecureRandom();
        String uuid = String.valueOf(random.nextLong());
        return uuid.replace("-", "");
    }


    public static String getSession() {
        String uuid = "";
        for (int i = 0; i < 16; i++) {
            uuid = uuid + Integer.toHexString(new Random().nextInt(16));
        }
        return uuid;
    }

    /**
     * 此方法谨慎修改
     * 插件配置 disableCarrier 会修改此方法
     * 获取运营商信息
     *
     * @param context Context
     * @return 运营商信息
     */
    public static String getCarrier(Context context) {
        try {
            if (checkHasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                try {
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context
                            .TELEPHONY_SERVICE);
                    if (telephonyManager != null) {
                        String operator = telephonyManager.getSimOperator();
                        String alternativeName = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            CharSequence tmpCarrierName = telephonyManager.getSimCarrierIdName();
                            if (!TextUtils.isEmpty(tmpCarrierName)) {
                                alternativeName = tmpCarrierName.toString();
                            }
                        }
                        if (TextUtils.isEmpty(alternativeName)) {
                            if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
                                alternativeName = telephonyManager.getSimOperatorName();
                            } else {
                                alternativeName = "未知";
                            }
                        }
                        if (!TextUtils.isEmpty(operator)) {
                            return operatorToCarrier(context, operator, alternativeName);
                        }
                    }
                } catch (Exception e) {
                    LogUtils.printStackTrace(e);
                }
            }
        } catch (Exception e) {
            LogUtils.printStackTrace(e);
        } catch (Error error) {
            //针对酷派 B770 机型抛出的 IncompatibleClassChangeError 错误进行捕获
            LogUtils.i(TAG, error.toString());
        }
        return null;
    }

    /**
     * 获取 Activity 的 title
     *
     * @param activity Activity
     * @return Activity 的 title
     */
    public static String getActivityTitle(Activity activity) {
        try {
            if (activity != null) {
                try {
                    String activityTitle = null;

                    if (Build.VERSION.SDK_INT >= 11) {
                        String toolbarTitle = getToolbarTitle(activity);
                        if (!TextUtils.isEmpty(toolbarTitle)) {
                            activityTitle = toolbarTitle;
                        }
                    }

                    if (TextUtils.isEmpty(activityTitle)) {
                        activityTitle = activity.getTitle().toString();
                    }

                    if (TextUtils.isEmpty(activityTitle)) {
                        PackageManager packageManager = activity.getPackageManager();
                        if (packageManager != null) {
                            ActivityInfo activityInfo = packageManager.getActivityInfo(activity.getComponentName(), 0);
                            if (!TextUtils.isEmpty(activityInfo.loadLabel(packageManager))) {
                                activityTitle = activityInfo.loadLabel(packageManager).toString();
                            }
                        }
                    }

                    return activityTitle;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            LogUtils.printStackTrace(e);
            return null;
        }
    }

    /**
     * 根据 operator，获取本地化运营商信息
     *
     * @param context         context
     * @param operator        sim operator
     * @param alternativeName 备选名称
     * @return local carrier name
     */
    private static String operatorToCarrier(Context context, String operator, String alternativeName) {
        try {
            if (TextUtils.isEmpty(operator)) {
                return alternativeName;
            }
            if (sCarrierMap.containsKey(operator)) {
                return sCarrierMap.get(operator);
            }
            String carrierJson = getJsonFromAssets("sa_mcc_mnc_mini.json", context);
            if (TextUtils.isEmpty(carrierJson)) {
                sCarrierMap.put(operator, alternativeName);
                return alternativeName;
            }
            JSONObject jsonObject = new JSONObject(carrierJson);
            String carrier = getCarrierFromJsonObject(jsonObject, operator);
            if (!TextUtils.isEmpty(carrier)) {
                sCarrierMap.put(operator, carrier);
                return carrier;
            }
        } catch (Exception e) {
            LogUtils.printStackTrace(e);
        }
        return alternativeName;
    }

    private static String getCarrierFromJsonObject(JSONObject jsonObject, String mccMnc) {
        if (jsonObject == null || TextUtils.isEmpty(mccMnc)) {
            return null;
        }
        return jsonObject.optString(mccMnc);

    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREF_EDITS_FILE, Context.MODE_PRIVATE);
    }

    @TargetApi(11)
    static String getToolbarTitle(Activity activity) {
        try {
            if ("com.tencent.connect.common.AssistActivity".equals(activity.getClass().getCanonicalName())) {
                if (!TextUtils.isEmpty(activity.getTitle())) {
                    return activity.getTitle().toString();
                }
                return null;
            }
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                if (!TextUtils.isEmpty(actionBar.getTitle())) {
                    return actionBar.getTitle().toString();
                }
            } else {
                try {
                    Class<?> appCompatActivityClass = compatActivity();
                    if (appCompatActivityClass != null && appCompatActivityClass.isInstance(activity)) {
                        Method method = activity.getClass().getMethod("getSupportActionBar");
                        Object supportActionBar = method.invoke(activity);
                        if (supportActionBar != null) {
                            method = supportActionBar.getClass().getMethod("getTitle");
                            CharSequence charSequence = (CharSequence) method.invoke(supportActionBar);
                            if (charSequence != null) {
                                return charSequence.toString();
                            }
                        }
                    }
                } catch (Exception e) {
                    //ignored
                }
            }
        } catch (Exception e) {
            LogUtils.printStackTrace(e);
        }
        return null;
    }

    private static Class<?> compatActivity() {
        Class<?> appCompatActivityClass = null;
        try {
            appCompatActivityClass = Class.forName("android.support.v7.app.AppCompatActivity");
        } catch (Exception e) {
            //ignored
        }
        if (appCompatActivityClass == null) {
            try {
                appCompatActivityClass = Class.forName("androidx.appcompat.app.AppCompatActivity");
            } catch (Exception e) {
                //ignored
            }
        }
        return appCompatActivityClass;
    }

    /**
     * 尝试读取页面 title
     *
     * @param properties JSONObject
     * @param activity   Activity
     */
    public static void getScreenNameAndTitleFromActivity(JSONObject properties, Activity activity) {
        if (activity == null || properties == null) {
            return;
        }

        try {
            properties.put("$screen_name", activity.getClass().getCanonicalName());

            String activityTitle = null;
            if (!TextUtils.isEmpty(activity.getTitle())) {
                activityTitle = activity.getTitle().toString();
            }

            String toolbarTitle = getToolbarTitle(activity);
            if (!TextUtils.isEmpty(toolbarTitle)) {
                activityTitle = toolbarTitle;
            }

            if (TextUtils.isEmpty(activityTitle)) {
                PackageManager packageManager = activity.getPackageManager();
                if (packageManager != null) {
                    ActivityInfo activityInfo = packageManager.getActivityInfo(activity.getComponentName(), 0);
                    activityTitle = activityInfo.loadLabel(packageManager).toString();
                }
            }
            if (!TextUtils.isEmpty(activityTitle)) {
                properties.put("$title", activityTitle);
            }
        } catch (Exception e) {
            LogUtils.printStackTrace(e);
        }
    }

    public static void mergeJSONObject(final JSONObject source, JSONObject dest) {
        try {
            Iterator<String> superPropertiesIterator = source.keys();

            while (superPropertiesIterator.hasNext()) {
                String key = superPropertiesIterator.next();
                Object value = source.get(key);
                dest.put(key, String.valueOf(value));
            }
        } catch (Exception ex) {
            // 此处报错并不会导致事件上报终止，可能会导致事件属性不全
            LogUtils.printStackTrace(ex);
        }
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

    public static JSONObject clearJSONObjectKey(final JSONObject source) throws JSONException{
        JSONObject object = new JSONObject();
        Iterator<String> sourceIterator = source.keys();
        while (sourceIterator.hasNext()) {
            String key = sourceIterator.next();
            Object value = source.get(key);
            if (key.contains("#")) {
                key = key.replace("#","");
            }
            object.put(key,value);
        }
        return object;
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

    /**
     * 合并、去重公共属性
     *
     * @param source 新加入或者优先级高的属性
     * @param dest   本地缓存或者优先级低的属性，如果有重复会删除该属性
     * @return 合并后的属性
     */
    public static JSONObject mergeSuperJSONObject(JSONObject source, JSONObject dest) {
        if (source == null) {
            source = new JSONObject();
        }
        if (dest == null) {
            return source;
        }

        try {
            Iterator<String> sourceIterator = source.keys();
            while (sourceIterator.hasNext()) {
                String key = sourceIterator.next();
                Iterator<String> destIterator = dest.keys();
                while (destIterator.hasNext()) {
                    String destKey = destIterator.next();
                    if (!TextUtils.isEmpty(key) && key.equalsIgnoreCase(destKey)) {
                        destIterator.remove();
                    }
                }
            }
            //重新遍历赋值，如果在同一次遍历中赋值会导致同一个 json 中大小写不一样的 key 被删除
            mergeJSONObject(source, dest);
        } catch (Exception ex) {
            LogUtils.printStackTrace(ex);
        }
        return dest;
    }

    /**
     * 获取 UA 值
     *
     * @param context Context
     * @return 当前 UA 值
     */
    @Deprecated
    public static String getUserAgent(Context context) {
        try {
            final SharedPreferences preferences = getSharedPreferences(context);
            String userAgent = preferences.getString(SHARED_PREF_USER_AGENT_KEY, null);
            if (TextUtils.isEmpty(userAgent)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    try {
                        Class webSettingsClass = Class.forName("android.webkit.WebSettings");
                        Method getDefaultUserAgentMethod = webSettingsClass.getMethod("getDefaultUserAgent", Context.class);
                        if (getDefaultUserAgentMethod != null) {
                            userAgent = WebSettings.getDefaultUserAgent(context);
                        }
                    } catch (Exception e) {
                        LogUtils.i(TAG, "WebSettings NoSuchMethod: getDefaultUserAgent");
                    }
                }

                if (TextUtils.isEmpty(userAgent)) {
                    userAgent = System.getProperty("http.agent");
                }

                if (!TextUtils.isEmpty(userAgent)) {
                    final SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(SHARED_PREF_USER_AGENT_KEY, userAgent);
                    editor.apply();
                }
            }

            return userAgent;
        } catch (Exception e) {
            LogUtils.printStackTrace(e);
            return null;
        }
    }

    /**
     * 检测权限F
     *
     * @param context    Context
     * @param permission 权限名称
     * @return true:已允许该权限; false:没有允许该权限
     */
    public static boolean checkHasPermission(Context context, String permission) {
        try {
            Class<?> contextCompat = null;
            try {
                contextCompat = Class.forName("android.support.v4.content.ContextCompat");
            } catch (Exception e) {
                //ignored
            }

            if (contextCompat == null) {
                try {
                    contextCompat = Class.forName("androidx.core.content.ContextCompat");
                } catch (Exception e) {
                    //ignored
                }
            }

            if (contextCompat == null) {
                return true;
            }

            Method checkSelfPermissionMethod = contextCompat.getMethod("checkSelfPermission", Context.class, String.class);
            int result = (int) checkSelfPermissionMethod.invoke(null, new Object[]{context, permission});
            if (result != PackageManager.PERMISSION_GRANTED) {
                LogUtils.i(TAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n"
                        + "<uses-permission android:name=\"" + permission + "\" />");
                return false;
            }

            return true;
        } catch (Exception e) {
            LogUtils.i(TAG, e.toString());
            return true;
        }
    }


    /**
     * 此方法谨慎修改
     * 插件配置 disableAndroidID 会修改此方法
     * 获取 Android ID
     *
     * @param context Context
     * @return androidID
     */
    @SuppressLint("HardwareIds")
    public static String getAndroidID(Context context) {
        String androidID = "";
        try {
            androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            LogUtils.printStackTrace(e);
        }
        return androidID;
    }

    private static String getMacAddressByInterface() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if ("wlan0".equalsIgnoreCase(nif.getName())) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }

        } catch (Exception e) {
            //ignore
        }
        return null;
    }

    /**
     * 此方法谨慎修改
     * 插件配置 disableMacAddress 会修改此方法
     * 获取手机的 Mac 地址
     *
     * @param context Context
     * @return String 当前手机的 Mac 地址
     */
    @SuppressLint("HardwareIds")
    public static String getMacAddress(Context context) {
        try {
            if (!checkHasPermission(context, Manifest.permission.ACCESS_WIFI_STATE)) {
                return "";
            }

            WifiManager wifiMan = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiMan == null) {
                return "";
            }

            WifiInfo wifiInfo = wifiMan.getConnectionInfo();

            if (wifiInfo != null && marshmallowMacAddress.equals(wifiInfo.getMacAddress())) {
                String result;
                try {
                    result = getMacAddressByInterface();
                    if (result != null) {
                        return result;
                    }
                } catch (Exception e) {
                    //ignore
                }
            } else {
                if (wifiInfo != null && wifiInfo.getMacAddress() != null) {
                    return wifiInfo.getMacAddress();
                } else {
                    return "";
                }
            }
            return marshmallowMacAddress;
        } catch (Exception e) {
            //ignore
        }
        return "";
    }

    public static boolean isValidAndroidId(String androidId) {
        if (TextUtils.isEmpty(androidId)) {
            return false;
        }

        return !mInvalidAndroidId.contains(androidId.toLowerCase(Locale.getDefault()));
    }

    /**
     * 检查版本是否经过升级
     *
     * @param context     context
     * @param currVersion 当前 SDK 版本
     * @return true，老版本升级到新版本。false，当前已是最新版本
     */
    public static boolean checkVersionIsNew(Context context, String currVersion) {
        try {
            SharedPreferences appVersionPref = getSharedPreferences(context);
            String localVersion = appVersionPref.getString(SHARED_PREF_APP_VERSION, "");

            if (!TextUtils.isEmpty(currVersion) && !currVersion.equals(localVersion)) {
                appVersionPref.edit().putString(SHARED_PREF_APP_VERSION, currVersion).apply();
                return true;
            }
        } catch (Exception ex) {
            LogUtils.printStackTrace(ex);
            return true;
        }
        return false;
    }


}
