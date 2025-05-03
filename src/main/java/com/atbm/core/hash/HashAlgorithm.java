package com.atbm.core.hash;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class HashAlgorithm {
    public static String hashText(String text, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] hashBytes = md.digest(text.getBytes("UTF-8"));
        return bytesToHex(hashBytes);
    }

    public static String hashFile(File file, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        return bytesToHex(md.digest());
    }

    public static String[] getSupportedAlgorithms() {
        return new String[] { "MD5", "SHA-1", "SHA-256", "SHA-384", "SHA-512" };
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}