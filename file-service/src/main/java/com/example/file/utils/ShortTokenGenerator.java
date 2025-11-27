package com.example.file.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ShortTokenGenerator {
    private ShortTokenGenerator() {}
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGIT = "0123456789";
    private static final String ALL = UPPER + LOWER + DIGIT;

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(int length) {
        if (length < 3) {
            throw new IllegalArgumentException("Length must be at least 3 for complex token.");
        }

        List<Character> chars = new ArrayList<>(length);

        // Ensure at least one character from each category
        chars.add(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        chars.add(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        chars.add(DIGIT.charAt(RANDOM.nextInt(DIGIT.length())));

        for (int i = 3; i < length; i++) {
            chars.add(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }

        // Shuffle to remove predictable placement
        Collections.shuffle(chars, RANDOM);

        StringBuilder token = new StringBuilder(length);
        for (char c : chars) token.append(c);

        return token.toString();
    }
}
