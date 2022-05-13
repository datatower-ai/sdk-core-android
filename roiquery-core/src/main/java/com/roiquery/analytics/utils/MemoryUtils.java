package com.roiquery.analytics.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.format.Formatter;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by chensongsong on 2020/6/2.
 */
public class MemoryUtils {


    public static String getMemoryUsed(Context context) {
        StorageBean bean = new StorageBean();
        getMemoryInfo(context, bean);
        return bean.getUsedMemory() + " / " + bean.getTotalMemory();
    }

    public static String getStorageUsed(Context context) {
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
}
