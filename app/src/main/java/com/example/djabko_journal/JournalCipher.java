package com.example.djabko_journal;

import android.util.Base64;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class JournalCipher {

    private Cipher cipher;
    private Cipher decipher;
    private SecureRandom random;
    private SecretKey key;
    private static boolean initialized = false;

    private static final int AES_KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private void initCipher() throws Exception {
        if (initialized) throw new Exception("Double Cipher initialization...");

        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        decipher = Cipher.getInstance("AES/GCM/NoPadding");
        random = new SecureRandom();

        initialized = true;
    }

    public JournalCipher() throws Exception {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");

        keygen.init(AES_KEY_SIZE);
        this.key = keygen.generateKey();

        initCipher();
    }

    public JournalCipher(SecretKey key) throws Exception {
        this.key = key;

        initCipher();
    }

    public void setKey(SecretKey key) {
        this.key = key;
    }

    public void setKey(String base64Key) throws IllegalArgumentException {
        byte[] key = Base64.decode(base64Key, Base64.NO_WRAP);
        this.key = new SecretKeySpec(key, "AES");
    }

    public SecretKey getKey() {
        if (!initialized) return null;

        return key;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder s = new StringBuilder();

        for (byte b: bytes) {
            s.append(String.format("%02x", b));
        }

        return s.toString();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length() / 2;
        byte[] bytes = new byte[len];

        for (int i = 0; i < len; i++) {
            int a = Character.digit(hex.charAt(i * 2), 16) << 4;
            int b = Character.digit(hex.charAt(i * 2 + 1), 16);

            bytes[i] = (byte) (a + b);
        }

        return bytes;
    }

    String encrypt(String plaintext) throws Exception {
        if (!initialized) throw new Exception("Cipher not initialized...");

        byte[] iv = new byte[IV_SIZE];
        random.nextBytes(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        byte[] cipherbytes = cipher.doFinal(plaintext.getBytes());
        String nonce = bytesToHex(iv);
        String ciphertext = Base64.encodeToString(cipherbytes, Base64.NO_WRAP);

        return nonce + ":" + ciphertext;
    }

    String decrypt(String ciphertext) throws Exception {
        if (!initialized) throw new Exception("Cipher not initialized...");

        String nonce = ciphertext.substring(0, IV_SIZE * 2);
        ciphertext = ciphertext.substring(IV_SIZE * 2 + 1);

        byte[] iv = hexToBytes(nonce);
        byte[] cipherbytes = Base64.decode(ciphertext, Base64.NO_WRAP);

        decipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        return new String(decipher.doFinal(cipherbytes));
    }
}
