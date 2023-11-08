package ai.datatower.ad.utils;

import java.util.Random;

public class UUIDUtils {
    public static String generateUUID() {
        StringBuilder uuid = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            uuid.append(Integer.toHexString(new Random().nextInt(16)));
        }
        return uuid.toString();
    }
}
