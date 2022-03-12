package ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr;

import org.apache.commons.lang3.StringUtils;

public interface Validatable {
    void validate();

    static void notNull(Object o, String name) {
        if (o == null) {
            throw new NullPointerException(name);
        }
    }

    static void notEmpty(String s, String name) {
        if (s == null) {
            throw new NullPointerException(name);
        }
        if (StringUtils.isEmpty(s)) {
            throw new IllegalArgumentException("blank " + name);
        }
    }

    static void notNegative(int i, String name) {
        if (i < 0) {
            throw new IllegalArgumentException("negative " + name);
        }
    }
}
