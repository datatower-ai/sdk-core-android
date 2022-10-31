package com.roiquery.analytics.utils;

import static android.content.Context.ACTIVITY_SERVICE;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.view.Choreographer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class MemoryUtils {

    static  long firstVsync;
    static  long secondVsync;
    static  volatile int fps;

    /**
     * 获取FPS.
     * */
    public static int getFPS() {
        if (fps == 0) {
            fps = 60;
        }
        return fps;
    }

    /**
     * 监听FPS.
     * */
    public static void listenFPS() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Choreographer.FrameCallback secondCallBack = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {
                    secondVsync = frameTimeNanos;
                    if (secondVsync <= firstVsync) {
                        fps = 60;
                    } else {
                        long hz = 1000000000 / (secondVsync - firstVsync);
                        if (hz > 70) {
                            fps = 60;
                        } else {
                            fps = (int) hz;
                        }
                    }
                }
            };

            final Choreographer.FrameCallback firstCallBack = new Choreographer.FrameCallback() {
                @Override
                public void doFrame(long frameTimeNanos) {
                    firstVsync = frameTimeNanos;
                    Choreographer.getInstance().postFrameCallback(secondCallBack);
                }
            };
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    handler.postDelayed(this, 500);
                    Choreographer.getInstance().postFrameCallback(firstCallBack);
                }
            };
            handler.postDelayed(runnable, 500);
        }
    }


    private static String getMemoryUsed(Context context) {
        StorageBean bean = new StorageBean();
        getMemoryInfo(context, bean);
        return bean.getUsedMemory() + " / " + bean.getTotalMemory();
    }

    private static String getStorageUsed(Context context) {
        StorageBean bean = new StorageBean();
        getStoreInfo(context, bean);
        return bean.getUsedStore() + " / " + bean.getTotalStore();
    }

    /**
     * 读取内存信息
     *
     * @return
     */
    public static void getMemoryInfo(Context context, StorageBean bean) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            manager.getMemoryInfo(info);
            long totalMem = info.totalMem;
            long availMem = info.availMem;
            long usedMem = totalMem - availMem;
            String total = readableStorageSize(totalMem);
            String usable = readableStorageSize(usedMem);
            String free = readableStorageSize(availMem);
            bean.setTotalMemory(total);
            bean.setFreeMemory(free);
            bean.setUsedMemory(usable);
            int ratio = (int) ((availMem / (double) totalMem) * 100);
            bean.setRatioMemory(ratio);
            double v = totalMem / 1024 / 1024 / 1024.0;
            String ram;
            if (v <= 1) {
                ram = "1 GB";
            } else if (v <= 2) {
                ram = "2 GB";
            } else if (v <= 4) {
                ram = "4 GB";
            } else if (v <= 6) {
                ram = "6 GB";
            } else if (v <= 8) {
                ram = "8 GB";
            } else if (v <= 12) {
                ram = "12 GB";
            } else {
                ram = "16 GB";
            }
            bean.setMemInfo(ram);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取 sd 卡存储信息
     *
     * @param context
     * @param bean
     */
    public static void getStoreInfo(Context context, StorageBean bean) {
        File card = Environment.getExternalStorageDirectory();
        bean.setStorePath(card.getAbsolutePath());
        long totalSpace = card.getTotalSpace();
        long freeSpace = card.getFreeSpace();
        long usableSpace = totalSpace - freeSpace;
        String total = readableStorageSize(totalSpace);
        String usable = readableStorageSize(usableSpace);
        String free = readableStorageSize(freeSpace);
        bean.setTotalStore(total);
        bean.setFreeStore(free);
        bean.setUsedStore(usable);
        int ratio = (int) ((usableSpace / (double) totalSpace) * 100);
        bean.setRatioStore(ratio);
        bean.setRomSize(getRealStorage(context));
    }


    @SuppressLint("DiscouragedPrivateApi")
    public static String getRealStorage(Context context) {
        long total = 0L;
        try {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            int version = Build.VERSION.SDK_INT;
            float unit = version >= Build.VERSION_CODES.O ? 1000 : 1024;
            if (version < Build.VERSION_CODES.M) {
                Method getVolumeList = StorageManager.class.getDeclaredMethod("getVolumeList");
                StorageVolume[] volumeList = (StorageVolume[]) getVolumeList.invoke(storageManager);
                if (volumeList != null) {
                    Method getPathFile = null;
                    for (StorageVolume volume : volumeList) {
                        if (getPathFile == null) {
                            getPathFile = volume.getClass().getDeclaredMethod("getPathFile");
                        }
                        File file = (File) getPathFile.invoke(volume);
                        total += file.getTotalSpace();
                    }
                }
            } else {
                @SuppressLint("PrivateApi") Method getVolumes = StorageManager.class.getDeclaredMethod("getVolumes");
                List<Object> getVolumeInfo = (List<Object>) getVolumes.invoke(storageManager);
                for (Object obj : getVolumeInfo) {
                    Field getType = obj.getClass().getField("type");
                    int type = getType.getInt(obj);
                    if (type == 1) {
                        long totalSize = 0L;
                        if (version >= Build.VERSION_CODES.O) {
                            Method getFsUuid = obj.getClass().getDeclaredMethod("getFsUuid");
                            String fsUuid = (String) getFsUuid.invoke(obj);
                            totalSize = getTotalSize(context, fsUuid);
                        } else if (version >= Build.VERSION_CODES.N_MR1) {
                            Method getPrimaryStorageSize = StorageManager.class.getMethod("getPrimaryStorageSize");
                            totalSize = (long) getPrimaryStorageSize.invoke(storageManager);
                        }
                        Method isMountedReadable = obj.getClass().getDeclaredMethod("isMountedReadable");
                        boolean readable = (boolean) isMountedReadable.invoke(obj);
                        if (readable) {
                            Method file = obj.getClass().getDeclaredMethod("getPath");
                            File f = (File) file.invoke(obj);
                            if (totalSize == 0) {
                                totalSize = f.getTotalSpace();
                            }
                            total += totalSize;
                        }
                    } else if (type == 0) {
                        Method isMountedReadable = obj.getClass().getDeclaredMethod("isMountedReadable");
                        boolean readable = (boolean) isMountedReadable.invoke(obj);
                        if (readable) {
                            Method file = obj.getClass().getDeclaredMethod("getPath");
                            File f = (File) file.invoke(obj);
                            total += f.getTotalSpace();
                        }
                    }
                }
            }
            return getUnit(total, unit);
        } catch (Exception ignore) {

        }
        return null;
    }

    private static String[] units = {"B", "KB", "MB", "GB", "TB"};

    /**
     * 进制转换
     */
    private static String getUnit(float size, float base) {
        int index = 0;
        while (size > base && index < 4) {
            size = size / base;
            index++;
        }
        return String.format(Locale.getDefault(), "%.2f %s ", size, units[index]);
    }

    /**
     * API 26 android O
     * 获取总共容量大小，包括系统大小
     */
    @SuppressLint("NewApi")
    private static long getTotalSize(Context context, String fsUuid) {
        try {
            UUID id;
            if (fsUuid == null) {
                id = StorageManager.UUID_DEFAULT;
            } else {
                id = UUID.fromString(fsUuid);
            }
            StorageStatsManager stats = context.getSystemService(StorageStatsManager.class);
            return stats.getTotalBytes(id);
        } catch (NoSuchFieldError | NoClassDefFoundError | NullPointerException | IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 将byte转换为更加友好的单位
     *
     * @param sizeInB byte
     * @return 更加友好的单位（KB、GB等）
     */
    public static String readableStorageSize(long sizeInB) {
        float floatSize = sizeInB;
        int index = 0;
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};

        while (floatSize > 1000 && index < 5) {
            index++;
            floatSize /= 1024;
        }

        String capacityText = new DecimalFormat("###,###,###.##").format(floatSize);
        return String.format(Locale.ENGLISH, "%s%s", capacityText, units[index]);
    }



    /**
     * 获取 手机 RAM 信息.
     * */
    @NonNull
    public static String getRAM(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ActivityManager activityManager = (ActivityManager) context
                        .getSystemService(ACTIVITY_SERVICE);
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                activityManager.getMemoryInfo(memoryInfo);
                long totalSize = memoryInfo.totalMem;
                long availableSize = memoryInfo.availMem;
                double total = formatNumber(totalSize / 1024.0 / 1024.0 / 1024.0);
                double available = formatNumber(availableSize / 1024.0 / 1024.0 / 1024.0);
                return available + "/" + total;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";

    }

    /**
     * 判断SD是否挂载.
     */
    public boolean isSDCardMount() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 通过反射调用获取内置存储和外置sd卡根路径(通用)
     * HarmonyOS 正常获取
     * ANDROID 11 接口有变动.
     *
     * @param mContext    上下文
     * @param isRemovable 是否可移除，false返回内部存储，true返回外置sd卡
     * @return Path
     */
    @Nullable
    private static String getStoragePath(Context mContext, boolean isRemovable) {
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = null;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                getPath = storageVolumeClazz.getMethod("getPath");
            } else {
                getPath = storageVolumeClazz.getMethod("getDirectory");
            }
            Method isRemovableMethod = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = "";
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    path = (String) getPath.invoke(storageVolumeElement);
                } else {
                    path = ((File) getPath.invoke(storageVolumeElement)).getAbsolutePath();
                }
                boolean removable = (Boolean) isRemovableMethod.invoke(storageVolumeElement);
                if (isRemovable == removable) {
                    return path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String mStoragePath; //保存手机外置卡路径

    public static String getDisk(Context context, boolean isExternal) {
        try {
            if (TextUtils.isEmpty(mStoragePath)) {
                mStoragePath = getStoragePath(context, isExternal);
            }
            if (TextUtils.isEmpty(mStoragePath)) {
                return "0";
            }
            File file = new File(mStoragePath);
            if (!file.exists()) {
                return "0";
            }
            StatFs statFs = new StatFs(file.getPath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                long blockCount = statFs.getBlockCountLong();
                long blockSize = statFs.getBlockSizeLong();
                long totalSpace = blockSize * blockCount;
                long availableBlocks = statFs.getAvailableBlocksLong();
                long availableSpace = availableBlocks * blockSize;
                double total = formatNumber(totalSpace / 1024.0 / 1024.0 / 1024.0);
                double available = formatNumber(availableSpace / 1024.0 / 1024.0 / 1024.0);
                return available + "/" + total;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";

    }
    /**
     * 保留一位小数.
     *
     * @param num double
     * @return 一位小数double
     */
    public static double formatNumber(double num) {
        return (double) Math.round(num * 10) / 10;
    }

}
