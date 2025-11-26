package com.example.file.utils;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class BCryptUtils {

    private BCryptUtils() {}

    private static final int WORK_FACTOR = 10; // bcrypt cost factor

    public static String hash(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean matches(String raw, String hashed) {
        return BCrypt.checkpw(raw, hashed);
    }
}
