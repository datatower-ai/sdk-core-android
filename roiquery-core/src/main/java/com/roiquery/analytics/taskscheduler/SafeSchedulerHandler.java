package com.roiquery.analytics.taskscheduler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


/**
 * a safe Handler avoid crash
 */
class SafeSchedulerHandler extends Handler{

    private static final String TAG = "SafeSchedulerHandler";
     SafeSchedulerHandler(Looper looper) {
        super(looper);
    }

     SafeSchedulerHandler() {
        super();
    }


    @Override
    public void dispatchMessage(Message msg) {
        try {
            super.dispatchMessage(msg);
        } catch (Exception e) {
            Log.d(TAG, "dispatchMessage Exception " + msg + " , " + e);
        } catch (Error error) {
            Log.d(TAG, "dispatchMessage error " + msg + " , " + error);
        }
    }
}
