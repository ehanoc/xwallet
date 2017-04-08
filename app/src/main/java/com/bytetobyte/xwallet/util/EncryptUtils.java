package com.bytetobyte.xwallet.util;

import android.content.Context;

import com.bytetobyte.xwallet.R;

import se.simbio.encryption.Encryption;

/**
 * Created by bruno on 07.04.17.
 */
public abstract class EncryptUtils {

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

}
