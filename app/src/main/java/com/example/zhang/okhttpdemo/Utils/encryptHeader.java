package com.example.zhang.okhttpdemo.Utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 工具类 - 加密post请求的header中的时间戳
 */
public class encryptHeader {

    public static String getEncryptedText(String key, String text) throws Exception {
        String result = null;
        byte[] encrypted = getEncryptedText(key, text.getBytes("UTF-8"));
        StringBuffer encryptedCode = new StringBuffer(encrypted.length * 2);
        String hexNumber;
        for (int i = 0; i < encrypted.length; i++) {
            hexNumber = "0" + Integer.toHexString(0xff & encrypted[i]);
            encryptedCode.append(hexNumber.substring(hexNumber.length() - 2));
        }
        result = encryptedCode.toString();
        return result;
    }

    public static byte[] getEncryptedText(String key, byte[] textBytes) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        SecretKeySpec spec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, spec);
        return cipher.doFinal(textBytes);
    }

}
