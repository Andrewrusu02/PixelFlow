package com.unical.pixelflow.utility;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.io.File;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class Encrypter {
    private final Cipher ecipher;
    private final Cipher dcipher;
    private final SecretKey sk;
    private static int bcrypt_salt = 12;

    public Encrypter(SecretKey key) throws Exception {
        sk = key;
        ecipher = Cipher.getInstance("AES");
        dcipher = Cipher.getInstance("AES");
        ecipher.init(Cipher.ENCRYPT_MODE, key);
        dcipher.init(Cipher.DECRYPT_MODE, key);
    }

    public Encrypter(String key) throws Exception {
        SecretKey k = new SecretKeySpec(key.getBytes(), "AES");
        sk = k;
        ecipher = Cipher.getInstance("AES");
        dcipher = Cipher.getInstance("AES");
        ecipher.init(Cipher.ENCRYPT_MODE, k);
        dcipher.init(Cipher.DECRYPT_MODE, k);
    }

    public String encrypt_string(String str) throws Exception {
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        byte[] enc = ecipher.doFinal(utf8);
        return Base64.getEncoder().encodeToString(enc);
    }

    public String decrypt_string(String str) throws Exception {
        byte[] dec = Base64.getDecoder().decode(str);
        byte[] utf8 = dcipher.doFinal(dec);
        return new String(utf8, StandardCharsets.UTF_8);
    }

    public void encrypt_file(String input_path, String output_path) throws Exception {
        encrypt_file(input_path, output_path, false);
    }
    public void encrypt_file(String input_path, String output_path, boolean delete_input_file) throws Exception {
        FileInputStream in = new FileInputStream(input_path);
        FileOutputStream out = new FileOutputStream(output_path);
        out.write(ecipher.doFinal(in.readAllBytes()));
        in.close();
        out.close();
        if (delete_input_file)
            if (!new File(input_path).delete())
                throw new IOException("Failed to delete input file");
    }

    public void decrypt_file(String input_path, String output_path) throws Exception {
        decrypt_file(input_path, output_path, false);
    }
    public void decrypt_file(String input_path, String output_path, boolean delete_input_file) throws Exception {
        FileInputStream in = new FileInputStream(input_path);
        FileOutputStream out = new FileOutputStream(output_path);
        out.write(dcipher.doFinal(in.readAllBytes()));
        in.close();
        out.close();
        if (delete_input_file)
            if (!new File(input_path).delete())
                throw new IOException("Failed to delete input file");
    }

    public void setBcrypt_salt(int salt) {
        bcrypt_salt = salt;
    }

    public static String encrypt_string_bcrypt(String str) {
        return BCrypt.hashpw(str, BCrypt.gensalt(bcrypt_salt));
    }

    public static boolean check_string_bcrypt(String str, String hash) {
        return BCrypt.checkpw(str, hash);
    }
}