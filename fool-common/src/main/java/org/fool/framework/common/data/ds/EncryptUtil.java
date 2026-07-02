package org.fool.framework.common.data.ds;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class EncryptUtil {
    private EncryptUtil() {
    }

    public static String toMD5(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(value.getBytes(StandardCharsets.UTF_16LE));
            StringBuilder result = new StringBuilder();
            for (byte item : digest) {
                result.append(Integer.toHexString(item & 0xff));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
