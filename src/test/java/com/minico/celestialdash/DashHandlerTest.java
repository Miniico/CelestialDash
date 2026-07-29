package com.minico.celestialdash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashHandlerTest {

    @Test
    void reportsOneSecondWhileAnyCooldownFractionRemains() {
        assertEquals(1L, DashHandler.calculateRemainingCooldownSeconds(1_000L, 1_001L, 1_000L));
        assertEquals(1L, DashHandler.calculateRemainingCooldownSeconds(1_000L, 1_999L, 1_000L));
    }

    @Test
    void reportsZeroWhenCooldownHasExpiredOrIsDisabled() {
        assertEquals(0L, DashHandler.calculateRemainingCooldownSeconds(1_000L, 2_000L, 1_000L));
        assertEquals(0L, DashHandler.calculateRemainingCooldownSeconds(1_000L, 1_001L, 0L));
    }
}
