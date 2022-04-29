package com.roiquery.cloudconfig.utils;


import android.util.Base64;

import java.security.AlgorithmParameters;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES对称加密算法
 */
public class AESCoder {

    private static final String KEY_ALGORITHM = "AES";

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";

    private static byte[] ivBytes = null;
    //hardcoded or read me from a file
    private static char[] pass = "coludconfigpassword".toCharArray();
    // for more confusion
    private static byte[] salt = new byte[20];
    // vs brute force
    private static final int PASSWORD_ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    /**
     * 解密数据
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return byte[] 解密后的数据
     */
    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 欢迎密钥

        Cipher cipher = createCipher(false, key);
        // 执行操作
        return cipher.doFinal(data);
    }

    /**
     * 加密数据
     *
     * @param data 待加密数据
     * @param key  密钥
     * @return byte[] 加密后的数据
     */
    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        Cipher cipher = createCipher(true, key);
        // 执行操作
        return cipher.doFinal(data);
    }

    private static Cipher createCipher(boolean encryptMode, byte[] secret) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        int mode = encryptMode ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;

        Key key = toKey(secret);
        if (ivBytes == null) {

            cipher.init(mode, key);
            AlgorithmParameters params = cipher.getParameters();
            ivBytes = params.getParameterSpec(IvParameterSpec.class).getIV();

        } else {

            cipher.init(mode, key, new IvParameterSpec(ivBytes));
        }

        return cipher;
    }

    /**
     * 生成密钥，java6只支持56位密钥，bouncycastle支持64位密钥
     *
     * @return byte[] 二进制密钥
     */
    // TODO: 2022/4/27 ANR
    public static byte[] initKey() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(pass, salt, PASSWORD_ITERATIONS, KEY_LENGTH);
        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
        return secret.getEncoded();
    }

    /**
     * 转换密钥
     *
     * @param key 二进制密钥
     * @return Key 密钥
     */
    public static Key toKey(byte[] key) throws Exception {
        // 实例化DES密钥
        // 生成密钥
        SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
        return secretKey;
    }

    /**
     * @throws Exception
     */
    public static void main() throws Exception {
        // 初始化密钥
//        byte[] key = Base64.decode(UUID.randomUUID().toString(),Base64.NO_WRAP);
        byte[] key = AESCoder.initKey();
        System.out.println("密钥：" + Base64.encodeToString(key, Base64.NO_WRAP));

        for (int i = 0; i < 100; i++) {
            String str = "我是一个快乐的人-" + i;
            System.out.println("原文：" + str);
            // 加密数据
            byte[] data = AESCoder.encrypt(str.getBytes(), key);
            System.out.println("加密后：" + Base64.encodeToString(data, Base64.NO_WRAP));
            // 解密数据
            data = AESCoder.decrypt(data, key);
            System.out.println("解密后：" + new String(data));
            System.out.println("========================");
        }
    }

}