package com.roiquery.analytics.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class GaidHelper {

    private static final String GP_PACKAGE = "com.android.vending";
    private static final String GP_PACKAGE_NOT_FOUND = "package 'com.android.vending' not found";
    private static final String GMS_ACTION = "com.google.android.gms.ads.identifier.service.START";
    private static final String GMS_PACKAGE = "com.google.android.gms";


    public interface IExecutor {
        void post(Runnable runnable);
    }

    /**
     * Executes Runnable on current thread
     */
    public static class CurrentThreadExecutor implements IExecutor {

        public CurrentThreadExecutor() {
            //nothing special to construct
        }

        @Override
        public void post(Runnable runnable) {
            runnable.run();
        }
    }

    /**
     * Executes Runnable on UI Thread
     */
    public static class UiThreadExecutor implements IExecutor {

        private final Handler mHandler;
        public UiThreadExecutor() {
            this.mHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void post(Runnable runnable) {
            mHandler.post(runnable);
        }
    }

    private final GaidListener mListener;
    private final IExecutor mExecutor;

    protected GaidHelper(GaidListener listener, IExecutor executor){
        this.mListener = listener;
        this.mExecutor = executor;
    }

    protected void getGoogleAdIdInfo(Context context){
        PackageManager packageManager = context.getPackageManager();
        try {
            // is google play installed
            packageManager.getPackageInfo(GP_PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e){
            // google play is not installed
            PackageManager.NameNotFoundException exception =
                    new PackageManager.NameNotFoundException(GP_PACKAGE_NOT_FOUND);
            onErrorOnExecutable(exception);
            return;
        }

        // service binding intent
        Intent intent = new Intent(GMS_ACTION);
        intent.setPackage(GMS_PACKAGE);
        AdIdConnection serviceConnection = new AdIdConnection();
        try {
            // if connection is successful
            if (context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)) {
                AdIdInterface adIdInterface = new AdIdInterface(serviceConnection.getBinder());
                String adId = adIdInterface.getAdId();
                boolean isAdTrackingEnabled = adIdInterface.isAdIdTrackingEnabled();
                if (TextUtils.isEmpty(adId)) {
                    // empty ad id, something went wrong
                    onErrorOnExecutable(new IllegalStateException("Ad ID is null or empty"));
                } else {
                    // everything is ok, call listener
                    onSuccessOnExecutable(new AdIdInfo(adId, isAdTrackingEnabled));
                }
            } else {
                // connection to service was not successful
                onErrorOnExecutable(new IllegalStateException("Bad GMS service connection"));
            }
        } catch (Exception e){
            // can't process IBinder object
            onErrorOnExecutable(e);
        } finally {
            // finally unbind from service
            context.unbindService(serviceConnection);
        }
    }

    protected void onSuccessOnExecutable(final AdIdInfo info){
        mExecutor.post(new Runnable() {
            @Override
            public void run() {
                mListener.onSuccess(info);
            }
        });
    }

    protected void onErrorOnExecutable(final Exception exception){
        mExecutor.post(new Runnable() {
            @Override
            public void run() {
                mListener.onException(exception);
            }
        });
    }

    /**
     * <p>
     *      Retrieve 'Ad ID' and 'Is Limited Ad Tracking' flag.
     *      Listener methods are invoked on the UI thread
     *</p>
     *
     * @param context Application context
     * @param gaidListener listener
     */
    public static void getAdInfo(final Context context, final GaidListener gaidListener){
        getAdInfo(context, true, gaidListener);
    }

    /**
     * <p>
     *     Retrieve 'Ad ID' and 'Is Limited Ad Tracking' flag.
     *     Listener methods are invoked based on boolean parameter.
     * </p>
     * @param context Application context
     * @param callbackOnUI receive Ad Info on UIThread if it is true and on invoker's thread(current thread) if it is false
     * @param gaidListener listener
     */
    public static void getAdInfo(final Context context, final boolean callbackOnUI,
                                 final GaidListener gaidListener){
        IExecutor executor = callbackOnUI ? new UiThreadExecutor() : new CurrentThreadExecutor();
        getAdInfo(context, executor, gaidListener);
    }

    /**
     * <p>
     *     Retrieve 'Ad ID' and 'Is Limited Ad Tracking' flag.
     *     Listener methods are invoked as designed by executor implementation passed in
     * </p>
     * @param context Application context
     * @param executor IExecutor implementation to run listener's methods in
     * @param gaidListener listener
     */
    public static void getAdInfo(final Context context, final IExecutor executor,
                                 final GaidListener gaidListener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                new GaidHelper(gaidListener, executor).getGoogleAdIdInfo(context);
            }
        }).start();
    }

    /**
     * <p>
     *      Client side callback interface
     * </p>
     */
    public interface GaidListener {
        /**`
         * Successfully retrieved Ad info
         * @param info AdIdInfo object containing Ad ID and Tracking flag
         */
        void onSuccess(AdIdInfo info);

        /**
         * Something happened trying to get Ad info
         * @param exception Exception object that was thrown on error
         */
        void onException(Exception exception);
    }

    /**
     * Holds 'Ad ID and 'Is Limited Ad Tracking' flag
     */
    public static class AdIdInfo {
        private final String adId;
        private final boolean isAdTrackingEnabled;

        private AdIdInfo(String adId, boolean isAdTrackingEnabled){
            this.adId = adId;
            this.isAdTrackingEnabled = isAdTrackingEnabled;
        }

        public String getAdId() {
            return adId;
        }

        public boolean isAdTrackingEnabled() {
            return isAdTrackingEnabled;
        }
    }

    /**
     * Service connection that retrieves Binder object from connected service
     */
    private static class AdIdConnection implements ServiceConnection{

        private final BlockingQueue<IBinder> queue = new ArrayBlockingQueue<>(1);

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) throws IllegalStateException {
            try{
                this.queue.put(iBinder);
            }
            catch (InterruptedException ex){
                throw new IllegalStateException("Exception trying to parse GMS connection");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }

        public IBinder getBinder() throws IllegalStateException {
            try{
                return queue.take();
            }
            catch (InterruptedException e){
                throw new IllegalStateException("Exception trying to retrieve GMS connection");
            }
        }
    }

    /**
     * Interface that deals with advertising service's Binder
     */
    private static class AdIdInterface implements IInterface{

        private static final String INTERFACE_TOKEN = "com.google.android.gms.ads.identifier.internal.IAdvertisingIdService";
        private static final int AD_ID_TRANSACTION_CODE = 1;
        private static final int AD_TRACKING_TRANSACTION_CODE = 2;

        private final IBinder mIBinder;
        private AdIdInterface(IBinder binder){
            this.mIBinder = binder;
        }

        @Override
        public IBinder asBinder() {
            return mIBinder;
        }

        private String getAdId() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            String adId;
            try {
                data.writeInterfaceToken(INTERFACE_TOKEN);
                mIBinder.transact(AD_ID_TRANSACTION_CODE, data, reply, 0);
                reply.readException();
                adId = reply.readString();
            }
            finally {
                data.recycle();
                reply.recycle();
            }
            return adId;
        }

        private boolean isAdIdTrackingEnabled() throws RemoteException{
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            boolean limitedTrackingEnabled;
            try {
                data.writeInterfaceToken(INTERFACE_TOKEN);
                data.writeInt(1);
                mIBinder.transact(AD_TRACKING_TRANSACTION_CODE, data, reply, 0);
                reply.readException();
                limitedTrackingEnabled = 0 != reply.readInt();
            }
            finally {
                data.recycle();
                reply.recycle();
            }
            return limitedTrackingEnabled;
        }
    }
}
