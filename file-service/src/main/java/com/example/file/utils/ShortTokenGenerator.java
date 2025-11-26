package com.example.file.utils;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class ShortTokenGenerator {
    private ShortTokenGenerator() {}
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(int length) {
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            token.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return token.toString();
    }
}
