package com.twogether.deokhugam.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimeParameterUtilTest {

    @Test
    @DisplayName("now 파라미터가 null 또는 blank인 경우 현재 시간 반환")
    void parseNowOrDefault_nullOrBlank_returnNow() {
        assertNotNull(TimeParameterUtil.parseNowOrDefault(null));
        assertNotNull(TimeParameterUtil.parseNowOrDefault(""));
        assertNotNull(TimeParameterUtil.parseNowOrDefault("   "));
    }

    @Test
    @DisplayName("now 파라미터가 정상적인 ISO-8601 문자열이면 파싱된 Instant 반환")
    void parseNowOrDefault_validString_returnParsed() {
        String now = "2025-07-24T10:00:00Z";
        Instant parsed = TimeParameterUtil.parseNowOrDefault(now);
        assertEquals(Instant.parse(now), parsed);
    }
}