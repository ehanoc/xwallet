package com.bytetobyte.xwallet.util;

import android.content.Context;

import com.bytetobyte.xwallet.R;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import se.simbio.encryption.Encryption;

/**
 * Created by bruno on 07.04.17.
 */
public abstract class EncryptUtils {

    public static String KSEED = "";

    /**
     *
     * @param context
     * @return
     */
    public static Encryption getEncryptor(Context context) {
        String key = context.getString(R.string.skey);
        String salt = context.getString(R.string.ksalt);
        byte[] iv = new byte[16];
        Encryption encryptor = Encryption.getDefault(key, salt, iv);

        return encryptor;
    }

    private final static String algorithm = "PBKDF2WithHmacSHA1";

    private final static String HEX = "0123456789ABCDEF";

    private static final String CP_ALGORITH = "AES";
    private static final String CP_KEY = "PUTsomeKEYinHere";

    public static String cipher(String cipherKey, String data) throws NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
        KeySpec spec = new PBEKeySpec(cipherKey.toCharArray(), cipherKey.getBytes(), 128, 256);
        SecretKey tmp = skf.generateSecret(spec);
        SecretKey key = new SecretKeySpec(tmp.getEncoded(), CP_ALGORITH);
        Cipher cipher = Cipher.getInstance(CP_ALGORITH);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return toHex(cipher.doFinal(data.getBytes()));
    }

    public static String decipher(String cipherKey, String data) throws NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
        KeySpec spec = new PBEKeySpec(cipherKey.toCharArray(), cipherKey.getBytes(), 128, 256);
        SecretKey tmp = skf.generateSecret(spec);
        SecretKey key = new SecretKeySpec(tmp.getEncoded(), CP_ALGORITH);
        Cipher cipher = Cipher.getInstance(CP_ALGORITH);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(toByte(data)));
    }

    private static byte[] toByte(String data) throws NullPointerException{
        int len = data.length()/2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(data.substring(2*i, 2*i+2), 16).byteValue();
        return result;
    }

    private static String toHex(byte[] doFinal) {
        StringBuffer result = new StringBuffer(2*doFinal.length);
        for (int i = 0; i < doFinal.length; i++) {
            result.append(HEX.charAt((doFinal[i]>>4)&0x0f)).append(HEX.charAt(doFinal[i]&0x0f));
        }
        return result.toString();
    }
}
