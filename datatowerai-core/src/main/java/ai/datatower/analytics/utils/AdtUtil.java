// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package ai.datatower.analytics.utils;

import android.app.Application;

import java.lang.ref.SoftReference;

/**
 *
 */
public class AdtUtil {
    private SoftReference<Application> mContext = null;

    private AdtUtil() {
    }

    private static final class Holder {
        private static final AdtUtil INSTANCE = new AdtUtil();
    }

    public static AdtUtil getInstance() {
        return Holder.INSTANCE;
    }

    public Application getApplicationContext() {
        if (mContext == null || mContext.get() == null) {
            Application application = currentApplication();
            if (application == null) {
                application = getInitialApplication();
            }
            mContext = new SoftReference<>(application);
        }
        return mContext.get();
    }

    private Application currentApplication() {
        Application application = null;
        try {
            application = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null, new Object[]{});
        } catch (Throwable ignored) {
        }
        return application;
    }

    private Application getInitialApplication() {
        Application application = null;
        try {
            application = (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null, new Object[]{});
        } catch (Throwable ignored) {
        }
        return application;
    }
}
