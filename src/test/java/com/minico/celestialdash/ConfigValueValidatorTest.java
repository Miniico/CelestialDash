package com.minico.celestialdash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigValueValidatorTest {

    @Test
    void clampsIntegersBelowAndAboveTheAllowedRange() {
        assertEquals(0, ConfigValueValidator.clamp(-1, 0, 500));
        assertEquals(500, ConfigValueValidator.clamp(900, 0, 500));
        assertEquals(20, ConfigValueValidator.clamp(20, 0, 500));
    }

    @Test
    void clampsLongCooldownValues() {
        assertEquals(0L, ConfigValueValidator.clampNonNegative(-1L, 86_400L));
        assertEquals(86_400L, ConfigValueValidator.clampNonNegative(100_000L, 86_400L));
    }

    @Test
    void clampsDecimalValues() {
        assertEquals(0.0, ConfigValueValidator.clampNonNegative(-0.5, 1.0));
        assertEquals(1.0, ConfigValueValidator.clampNonNegative(1.2, 1.0));
        assertEquals(0.3, ConfigValueValidator.clampNonNegative(0.3, 1.0));
    }
}
