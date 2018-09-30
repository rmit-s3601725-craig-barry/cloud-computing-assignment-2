package com.example.craig.myapplication.util;

import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5 {

    private MD5() {}

    /**
     * Hashes a string using MD5 hash
     * @param input - string to hash
     * @param outLen - the character length of output
     * @return md5 hash string
     */
    public static String hash(String input, int outLen)
    {
        try {

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());

            BigInteger num = new BigInteger(1, messageDigest);

            String hashtext = num.toString(16);
            while (hashtext.length() < outLen) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Hashes a string using MD5 hash using the default hash length
     * @param input - string to hash
     * @return md5 hash string
     */
    public static String hash(String input)
    {
        return hash(input, DEFAULT_HASH_LEN);
    }

    private static final int DEFAULT_HASH_LEN = 32;
}
