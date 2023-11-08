/*
 * Copyright (C) 2022 ThinkingData
 */

package ai.datatower.analytics.core;


import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import ai.datatower.analytics.Constant;
import ai.datatower.analytics.config.AnalyticsConfig;
import ai.datatower.analytics.utils.LogUtils;
import ai.datatower.analytics.utils.MemoryUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class DTActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = Constant.LOG_TAG;
    private boolean resumeFromBackground = false;
    private final Object mActivityLifecycleCallbacksLock = new Object();
    private volatile Boolean isLaunch =  true;
    private WeakReference<Activity> mCurrentActivity;
    private final List<WeakReference<Activity>> mStartedActivityList = new ArrayList<>();
    //标识是否采集end事件
    private boolean shouldTrackEndEvent = true;


    public Activity currentActivity() {
        if (mCurrentActivity != null) {
            return mCurrentActivity.get();
        }
        return  null;
    }

    public void trackSessionStart(){
        if (AnalyticsConfig.Companion.getInstance().isSdkDisable()) {
            return;
        }
        if (isLaunch || resumeFromBackground) {
            isLaunch = false;
            PropertyManager.Companion.getInstance().updateIsForeground(true, resumeFromBackground, getStartReason());
        }
    }

    private void trackSessionEnd(){
        if (AnalyticsConfig.Companion.getInstance().isSdkDisable()) {
            return;
        }
        PropertyManager.Companion.getInstance().updateIsForeground(false, resumeFromBackground,"");
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        mCurrentActivity = new WeakReference<>(activity);
        MemoryUtils.toggleShouldListenFps(true);
    }

    private boolean notStartedActivity(Activity activity, boolean remove) {
        synchronized (mActivityLifecycleCallbacksLock) {
            Iterator<WeakReference<Activity>> it = mStartedActivityList.iterator();
            while (it.hasNext()) {
                WeakReference<Activity> current = it.next();
                if (current.get() == activity) {
                    if (remove) {
                        it.remove();
                    }
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        mCurrentActivity = new WeakReference<>(activity);
        MemoryUtils.toggleShouldListenFps(true);
        try {
            synchronized (mActivityLifecycleCallbacksLock) {
                if (mStartedActivityList.size() == 0) {
                    trackSessionStart();
                }
                if (notStartedActivity(activity, false)) {
                    mStartedActivityList.add(new WeakReference<>(activity));
                } else {
                    LogUtils.w(TAG, "Unexpected state. The activity might not be stopped correctly: " + activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResumed(Activity activity) {
        synchronized (mActivityLifecycleCallbacksLock) {
            MemoryUtils.toggleShouldListenFps(true);
            if (notStartedActivity(activity, false)) {
                LogUtils.i(TAG, "onActivityResumed: the SDK was initialized after the onActivityStart of " + activity);
                mStartedActivityList.add(new WeakReference<>(activity));
                if (mStartedActivityList.size() == 1) {
                    trackSessionStart();
                }
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        synchronized (mActivityLifecycleCallbacksLock) {
            MemoryUtils.toggleShouldListenFps(false);
            if (notStartedActivity(activity, false)) {
                LogUtils.i(TAG, "onActivityPaused: the SDK was initialized after the onActivityStart of " + activity);
                mStartedActivityList.add(new WeakReference<>(activity));
                if (mStartedActivityList.size() == 1) {
                    trackSessionStart();
                }
            }
        }
    }



    void onAppStartEventEnabled() {
        synchronized (mActivityLifecycleCallbacksLock) {
            if (isLaunch) {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        if (isLaunch) {
                            isLaunch = false;
                            PropertyManager.Companion.getInstance().updateIsForeground(true, resumeFromBackground, getStartReason());
                        }
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 100); //100ms后执行TimeTask的run方法
            }
        }
    }

    public static Object wrap(Object o) {
        if (o == null) {
            return JSONObject.NULL;
        }
        if (o instanceof JSONArray || o instanceof JSONObject) {
            return o;
        }

        if (o.equals(JSONObject.NULL)) {
            return o;
        }
        try {
            if (o instanceof Collection) {
                return new JSONArray((Collection) o);
            } else if (o.getClass().isArray()) {
                return toJSONArray(o);
            }
            if (o instanceof Map) {
                return new JSONObject((Map) o);
            }
            if (o instanceof Boolean
                    || o instanceof Byte
                    || o instanceof Character
                    || o instanceof Double
                    || o instanceof Float
                    || o instanceof Integer
                    || o instanceof Long
                    || o instanceof Short
                    || o instanceof String) {
                return o;
            }
            if (o.getClass().getPackage().getName().startsWith("java.")) {
                return o.toString();
            }
        } catch (Exception ignored) {
            //ignored
        }

        return null;

    }

    public static JSONArray toJSONArray(Object array) throws JSONException {
        JSONArray result = new JSONArray();
        if (!array.getClass().isArray()) {
            throw new JSONException("Not a primitive array: " + array.getClass());
        }
        final int length = Array.getLength(array);
        for (int i = 0; i < length; ++i) {
            result.put(wrap(Array.get(array, i)));
        }
        return result;
    }

    String getStartReason() {
        JSONObject object = new JSONObject();
        JSONObject data = new JSONObject();
        if (mCurrentActivity != null) {
            try {
                Activity activity = mCurrentActivity.get();
                Intent intent = activity.getIntent();
                if (intent != null) {
                    String uri = intent.getDataString();
                    if (!TextUtils.isEmpty(uri)) {
                        object.put("url", uri);
                    }
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        Set<String> keys = bundle.keySet();
                        for (String key : keys) {
                            Object value = bundle.get(key);
                            Object supportValue = wrap(value);
                            if (supportValue != null && supportValue != JSONObject.NULL) {
                                data.put(key, wrap(value));
                            }
                        }
                        object.put("data", data);
                    }
                }
            } catch (Exception exception) {
                //exception.printStackTrace();
                return object.toString();
            }
        }
        return  object.toString();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        try {
            synchronized (mActivityLifecycleCallbacksLock) {
                if (notStartedActivity(activity, true)) {
                    LogUtils.i(TAG, "onActivityStopped: the SDK might be initialized after the onActivityStart of " + activity);
                    return;
                }
                if (mStartedActivityList.size() == 0) {
                    mCurrentActivity = null;
                    if (shouldTrackEndEvent) {
                        try {
                            trackSessionEnd();
                            resumeFromBackground = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }



}
