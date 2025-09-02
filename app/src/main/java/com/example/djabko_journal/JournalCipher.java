package com.example.djabko_journal;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
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
    private KeyStore keystore;
    private String alias;
    private boolean initialized = false;
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";

    private static final int AES_KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private void initCipher() throws Exception {
        if (initialized) return;

        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        decipher = Cipher.getInstance("AES/GCM/NoPadding");
        keystore = KeyStore.getInstance(ANDROID_KEYSTORE);
        random = new SecureRandom();

        initialized = true;
    }

    public JournalCipher(String keyAlias, SecretKey key) throws Exception {
        initCipher();


        keystore.load(null);

        if (key != null);

        else if (keystore.containsAlias(keyAlias)) {
            key = (SecretKey) keystore.getKey(keyAlias, null);

        } else {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(AES_KEY_SIZE)
                    //.setRandomizedEncryptionRequired(false)
                    .build();

            KeyGenerator keygen = KeyGenerator.getInstance("AES", ANDROID_KEYSTORE);
            keygen.init(spec);
            key = keygen.generateKey();
            alias = keyAlias;
        }

        this.key = key;
    }

    public void setKey(SecretKey key) {
        this.key = key;
    }

    public JournalCipherError setKey(String base64Key){

        try {
            byte[] key = Base64.decode(base64Key, Base64.NO_WRAP);
            this.key = new SecretKeySpec(key, "AES");
            keystore.setKeyEntry(alias, key, null);

        } catch (KeyStoreException e) {
            Log.println(Log.ERROR, this.getClass().toString(), Log.getStackTraceString(e));
            return JournalCipherError.KEYSTORE_EXCEPTION;

        } catch (IllegalArgumentException e) {
            Log.println(Log.ERROR, this.getClass().toString(), Log.getStackTraceString(e));
            return JournalCipherError.BASE64_DECODING_ERROR;
        }

        return JournalCipherError.OK;
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

        /*
        byte[] iv = new byte[IV_SIZE];
        random.nextBytes(iv);
         */

        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] iv = cipher.getIV();
        if (iv == null) throw new Exception("IV is null.");

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
