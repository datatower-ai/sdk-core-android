package ai.datatower.analytics.utils;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresPermission;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ai.datatower.analytics.taskqueue.MainQueue;


/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2016/08/02
 *     desc  : utils about network
 * </pre>
 */
public final class NetworkUtil {

    private NetworkUtil() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public enum NetworkType {
        NETWORK_ETHERNET,
        NETWORK_WIFI,
        NETWORK_5G,
        NETWORK_4G,
        NETWORK_3G,
        NETWORK_2G,
        NETWORK_UNKNOWN,
        NETWORK_NO
    }

    public static String convertNetworkTypeToString(NetworkType networkType){
        if (networkType == NetworkType.NETWORK_ETHERNET) {
            return "e";
        }
        if (networkType == NetworkType.NETWORK_WIFI) {
            return "wifi";
        }
        if (networkType == NetworkType.NETWORK_5G) {
            return "5g";
        }
        if (networkType == NetworkType.NETWORK_4G) {
            return "4g";
        }
        if (networkType == NetworkType.NETWORK_3G) {
            return "3g";
        }
        if (networkType == NetworkType.NETWORK_2G) {
            return "2g";
        }
        if (networkType == NetworkType.NETWORK_UNKNOWN) {
            return "unknown_network";
        }
        if (networkType == NetworkType.NETWORK_NO) {
            return "none_network";
        }
        return "unknown_network";
    }

    public static String getNetworkTypeString(Application context) {
        return convertNetworkTypeToString(getNetworkType(context));
    }

    /**
     * Return type of network.
     * <p>Must hold {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return type of network
     * <ul>
     * <li>{@link NetworkType#NETWORK_ETHERNET} </li>
     * <li>{@link NetworkType#NETWORK_WIFI    } </li>
     * <li>{@link NetworkType#NETWORK_4G      } </li>
     * <li>{@link NetworkType#NETWORK_3G      } </li>
     * <li>{@link NetworkType#NETWORK_2G      } </li>
     * <li>{@link NetworkType#NETWORK_UNKNOWN } </li>
     * <li>{@link NetworkType#NETWORK_NO      } </li>
     * </ul>
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static NetworkType getNetworkType(Application context) {
        if (isEthernet(context)) {
            return NetworkType.NETWORK_ETHERNET;
        }
        NetworkInfo info = getActiveNetworkInfo(context);
        if (info != null && info.isAvailable()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return NetworkType.NETWORK_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return NetworkType.NETWORK_2G;

                    case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return NetworkType.NETWORK_3G;

                    case TelephonyManager.NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return NetworkType.NETWORK_4G;

                    case TelephonyManager.NETWORK_TYPE_NR:
                        return NetworkType.NETWORK_5G;
                    default:
                        String subtypeName = info.getSubtypeName();
                        if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                                || subtypeName.equalsIgnoreCase("WCDMA")
                                || subtypeName.equalsIgnoreCase("CDMA2000")) {
                            return NetworkType.NETWORK_3G;
                        } else {
                            return NetworkType.NETWORK_UNKNOWN;
                        }
                }
            } else {
                return NetworkType.NETWORK_UNKNOWN;
            }
        }
        return NetworkType.NETWORK_NO;
    }

    /**
     * Return whether using ethernet.
     * <p>Must hold
     * {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />}</p>
     *
     * @return {@code true}: yes<br>{@code false}: no
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    private static boolean isEthernet(Context context) {
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        final NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        if (info == null) return false;
        NetworkInfo.State state = info.getState();
        if (null == state) return false;
        return state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING;
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    private static NetworkInfo getActiveNetworkInfo(Application context) {
        ConnectivityManager cm =
                (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return null;
        return cm.getActiveNetworkInfo();
    }



    /**
     * Return the ip address.
     * <p>Must hold {@code <uses-permission android:name="android.permission.INTERNET" />}</p>
     *
     * @param useIPv4 True to use ipv4, false otherwise.
     * @return the ip address
     */
    @RequiresPermission(INTERNET)
    public static String getIPAddress(final boolean useIPv4) {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            LinkedList<InetAddress> adds = new LinkedList<>();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                // To prevent phone of xiaomi return "10.0.2.15"
                if (!ni.isUp() || ni.isLoopback()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    adds.addFirst(addresses.nextElement());
                }
            }
            for (InetAddress add : adds) {
                if (!add.isLoopbackAddress()) {
                    String hostAddress = add.getHostAddress();
                    boolean isIPv4 = hostAddress.indexOf(':') < 0;
                    if (useIPv4) {
                        if (isIPv4) return hostAddress;
                    } else {
                        if (!isIPv4) {
                            int index = hostAddress.indexOf('%');
                            return index < 0
                                    ? hostAddress.toUpperCase(Locale.ENGLISH)
                                    : hostAddress.substring(0, index).toUpperCase(Locale.ENGLISH);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Return the ip address of broadcast.
     *
     * @return the ip address of broadcast
     */
    public static String getBroadcastIpAddress() {
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            LinkedList<InetAddress> adds = new LinkedList<>();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                if (!ni.isUp() || ni.isLoopback()) continue;
                List<InterfaceAddress> ias = ni.getInterfaceAddresses();
                for (int i = 0, size = ias.size(); i < size; i++) {
                    InterfaceAddress ia = ias.get(i);
                    InetAddress broadcast = ia.getBroadcast();
                    if (broadcast != null) {
                        return broadcast.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Return the domain address.
     * <p>Must hold {@code <uses-permission android:name="android.permission.INTERNET" />}</p>
     *
     * @param domain The name of domain.
     * @return the domain address
     */
    @RequiresPermission(INTERNET)
    public static String getDomainAddress(final String domain) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(domain);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "";
        }
    }


    /**
     * Register the status of network changed listener.
     *
     * @param listener The status of network changed listener
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    public static void registerNetworkStatusChangedListener(Application context,final OnNetworkStatusChangedListener listener) {
        NetworkChangedReceiver.getInstance(context).registerListener(listener);
    }

    /**
     * Return whether the status of network changed listener has been registered.
     *
     * @param listener The listener
     * @return true to registered, false otherwise.
     */
    public static boolean isRegisteredNetworkStatusChangedListener(Application context,final OnNetworkStatusChangedListener listener) {
        return NetworkChangedReceiver.getInstance(context).isRegistered(listener);
    }

    /**
     * Unregister the status of network changed listener.
     *
     * @param listener The status of network changed listener.
     */
    public static void unregisterNetworkStatusChangedListener(Application context,final OnNetworkStatusChangedListener listener) {
        NetworkChangedReceiver.getInstance(context).unregisterListener(listener);
    }



    public static final class NetworkChangedReceiver extends BroadcastReceiver {

        public NetworkChangedReceiver(Application context) {
            mContext = context;
        }

        private static NetworkChangedReceiver getInstance(Application context) {
            mContext = context;
            return LazyHolder.INSTANCE;
        }

        private NetworkType                         mType;
        private final Set<OnNetworkStatusChangedListener> mListeners = new HashSet<>();
        private static Application mContext;

        @RequiresPermission(ACCESS_NETWORK_STATE)
        void registerListener(final OnNetworkStatusChangedListener listener) {
            if (listener == null) return;
//            ThreadUtils.runOnUiThread(new Runnable() {
//                @Override
//                @RequiresPermission(ACCESS_NETWORK_STATE)
//                public void run() {
//                    int preSize = mListeners.size();
//                    mListeners.add(listener);
//                    if (preSize == 0 && mListeners.size() == 1) {
//                        mType = getNetworkType(mContext);
//                        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//                        mContext.registerReceiver(NetworkChangedReceiver.getInstance(mContext), intentFilter);
//                    }
//                }
//            });

            MainQueue.get().postTask(new Runnable() {

                @Override
                @RequiresPermission(ACCESS_NETWORK_STATE)
                public void run() {
                    int preSize = mListeners.size();
                    mListeners.add(listener);
                    if (preSize == 0 && mListeners.size() == 1) {
                        mType = getNetworkType(mContext);
                        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                        mContext.registerReceiver(NetworkChangedReceiver.getInstance(mContext), intentFilter);
                    }
                }
            });
        }

        boolean isRegistered(final OnNetworkStatusChangedListener listener) {
            if (listener == null) return false;
            return mListeners.contains(listener);
        }

        void unregisterListener(final OnNetworkStatusChangedListener listener) {
            if (listener == null) return;
            MainQueue.get().postTask(() -> {
                int preSize = mListeners.size();
                mListeners.remove(listener);
                if (preSize == 1 && mListeners.size() == 0) {
                    mContext.unregisterReceiver(NetworkChangedReceiver.getInstance(mContext));
                }
            });
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                MainQueue.get().postTask(() -> {
                    NetworkType networkType = NetworkUtil.getNetworkType(mContext);
                    if (mType == networkType) return;
                    mType = networkType;
                    if (networkType == NetworkType.NETWORK_NO) {
                        for (OnNetworkStatusChangedListener listener : mListeners) {
                            listener.onDisconnected();
                        }
                    } else {
                        for (OnNetworkStatusChangedListener listener : mListeners) {
                            listener.onConnected(networkType);
                        }
                    }
                });

                // debouncing
//                ThreadUtils.runOnUiThreadDelayed(new Runnable() {
//                    @Override
//                    @RequiresPermission(ACCESS_NETWORK_STATE)
//                    public void run() {
//                        NetworkType networkType = NetworkUtil.getNetworkType(mContext);
//                        if (mType == networkType) return;
//                        mType = networkType;
//                        if (networkType == NetworkType.NETWORK_NO) {
//                            for (OnNetworkStatusChangedListener listener : mListeners) {
//                                listener.onDisconnected();
//                            }
//                        } else {
//                            for (OnNetworkStatusChangedListener listener : mListeners) {
//                                listener.onConnected(networkType);
//                            }
//                        }
//                    }
//                }, 1000);
            }
        }

        private static class LazyHolder {
            private static final NetworkChangedReceiver INSTANCE = new NetworkChangedReceiver(mContext);
        }
    }

    public interface OnNetworkStatusChangedListener {
        void onDisconnected();

        void onConnected(NetworkType networkType);
    }


}
