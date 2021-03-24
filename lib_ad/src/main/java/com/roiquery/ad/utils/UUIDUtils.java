package com.roiquery.ad.utils;

import java.util.Random;

public class UUIDUtils {
    public static String generateUUID() {
        String uuid = "";
        for (int i = 0; i < 16; i++) {
            uuid = uuid + Integer.toHexString(new Random().nextInt(16));
        }
        return uuid;
    }
}
