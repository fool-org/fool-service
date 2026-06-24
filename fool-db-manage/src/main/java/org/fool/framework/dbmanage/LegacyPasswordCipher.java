package org.fool.framework.dbmanage;

import lombok.Data;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class LegacyPasswordCipher {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DES_TRANSFORMATION = "DES/CBC/PKCS5Padding";

    private LegacyPasswordCipher() {
    }

    public static EncryptedPassword encrypt(String password) {
        byte[] iv = new byte[8];
        byte[] key = new byte[8];
        RANDOM.nextBytes(iv);
        RANDOM.nextBytes(key);

        byte[] ivIndex = randomIndex();
        byte[] keyIndex = randomIndex();
        byte[] shuffledIv = shuffle(iv, ivIndex);
        byte[] shuffledKey = shuffle(key, keyIndex);
        String payload = encryptToBase64(password == null ? "" : password, key, iv);

        return new EncryptedPassword(shuffledKey, keyIndex, shuffledIv, ivIndex, payload);
    }

    public static String decrypt(EncryptedPassword encryptedPassword) {
        byte[] iv = restore(encryptedPassword.getInitializationVector(), encryptedPassword.getInitializationVectorIndex());
        byte[] key = restore(encryptedPassword.getEncryptedKey(), encryptedPassword.getEncryptedKeyIndex());
        return decryptFromBase64(encryptedPassword.getEncryptedPassword(), key, iv);
    }

    private static String encryptToBase64(String value, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(DES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "DES"), new IvParameterSpec(iv));
            return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to encrypt legacy password.", e);
        }
    }

    private static String decryptFromBase64(String value, byte[] key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(DES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "DES"), new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(value));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to decrypt legacy password.", e);
        }
    }

    private static byte[] randomIndex() {
        List<Byte> source = new ArrayList<>();
        for (byte i = 0; i < 8; i++) {
            source.add(i);
        }
        byte[] result = new byte[8];
        for (int i = 0; i < result.length; i++) {
            int sourceIndex = RANDOM.nextInt(source.size());
            result[i] = source.remove(sourceIndex);
        }
        return result;
    }

    private static byte[] shuffle(byte[] value, byte[] index) {
        byte[] result = new byte[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = value[Byte.toUnsignedInt(index[i])];
        }
        return result;
    }

    private static byte[] restore(byte[] shuffled, byte[] index) {
        byte[] result = new byte[shuffled.length];
        for (int i = 0; i < shuffled.length; i++) {
            result[Byte.toUnsignedInt(index[i])] = shuffled[i];
        }
        return result;
    }

    @Data
    public static class EncryptedPassword {
        private final byte[] encryptedKey;
        private final byte[] encryptedKeyIndex;
        private final byte[] initializationVector;
        private final byte[] initializationVectorIndex;
        private final String encryptedPassword;
    }
}
