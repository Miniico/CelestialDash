package com.minico.celestialdash;

final class ConfigValueValidator {

    private ConfigValueValidator() {
    }

    static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static long clampNonNegative(long value, long max) {
        return Math.max(0L, Math.min(max, value));
    }

    static double clampNonNegative(double value, double max) {
        return Math.max(0.0, Math.min(max, value));
    }
}
