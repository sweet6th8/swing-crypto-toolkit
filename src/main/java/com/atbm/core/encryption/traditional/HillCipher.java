package com.atbm.core.encryption.traditional;

import java.security.Key;
import java.nio.charset.StandardCharsets;

public class HillCipher extends TraditionalEncryption {
    public HillCipher() {
        super("Hill");
    }

    private int modInverse(int a, int m) {
        a = a % m;
        for (int x = 1; x < m; x++) {
            if ((a * x) % m == 1)
                return x;
        }
        throw new IllegalArgumentException("Không có nghịch đảo modular!");
    }

    private int[][] parseKey(String key) {
        String[] nums = key.split(",");
        if (nums.length != 4)
            throw new IllegalArgumentException("Key phải có 4 số cho ma trận 2x2!");
        int[][] matrix = new int[2][2];
        for (int i = 0; i < 4; i++) {
            matrix[i / 2][i % 2] = Integer.parseInt(nums[i].trim());
        }
        return matrix;
    }

    public String encrypt(String plainText, String key) {
        int[][] k = parseKey(key);
        StringBuilder result = new StringBuilder();
        String filtered = plainText.replaceAll("[^A-Z]", "").toUpperCase();
        if (filtered.length() % 2 != 0)
            filtered += "X";
        for (int i = 0; i < filtered.length(); i += 2) {
            int[] vec = { filtered.charAt(i) - 'A', filtered.charAt(i + 1) - 'A' };
            int c1 = (k[0][0] * vec[0] + k[0][1] * vec[1]) % 26;
            int c2 = (k[1][0] * vec[0] + k[1][1] * vec[1]) % 26;
            if (c1 < 0)
                c1 += 26;
            if (c2 < 0)
                c2 += 26;
            result.append((char) ('A' + c1));
            result.append((char) ('A' + c2));
        }
        return result.toString();
    }

    public String decrypt(String cipherText, String key) {
        int[][] k = parseKey(key);
        int det = (k[0][0] * k[1][1] - k[0][1] * k[1][0]) % 26;
        if (det < 0)
            det += 26;
        int detInv = modInverse(det, 26);
        int[][] inv = {
                { k[1][1] * detInv % 26, (-k[0][1] + 26) * detInv % 26 },
                { (-k[1][0] + 26) * detInv % 26, k[0][0] * detInv % 26 }
        };
        StringBuilder result = new StringBuilder();
        String filtered = cipherText.replaceAll("[^A-Z]", "").toUpperCase();
        for (int i = 0; i < filtered.length(); i += 2) {
            int[] vec = { filtered.charAt(i) - 'A', filtered.charAt(i + 1) - 'A' };
            int p1 = (inv[0][0] * vec[0] + inv[0][1] * vec[1]) % 26;
            int p2 = (inv[1][0] * vec[0] + inv[1][1] * vec[1]) % 26;
            if (p1 < 0)
                p1 += 26;
            if (p2 < 0)
                p2 += 26;
            result.append((char) ('A' + p1));
            result.append((char) ('A' + p2));
        }
        String res = result.toString();
        if (res.endsWith("X") && cipherText.replaceAll("[^A-Z]", "").length() % 2 != 0) {
            res = res.substring(0, res.length() - 1);
        }
        return res;
    }

    @Override
    public byte[] encrypt(byte[] data, Key key) {
        String keyStr = new String(key.getEncoded(), StandardCharsets.UTF_8);
        String plainText = new String(data, StandardCharsets.UTF_8);
        return encrypt(plainText, keyStr).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] decrypt(byte[] data, Key key) {
        String keyStr = new String(key.getEncoded(), StandardCharsets.UTF_8);
        String cipherText = new String(data, StandardCharsets.UTF_8);
        return decrypt(cipherText, keyStr).getBytes(StandardCharsets.UTF_8);
    }
}