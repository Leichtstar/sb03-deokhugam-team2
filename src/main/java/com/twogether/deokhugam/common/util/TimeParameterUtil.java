package com.twogether.deokhugam.common.util;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TimeParameterUtil {

    private TimeParameterUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Instant parseNowOrDefault(String nowString) {
        try {
            return Instant.parse(nowString);
        } catch (DateTimeParseException | NullPointerException e) {
            log.warn("[TimeParameterUtil] 'now' 파라미터 파싱 실패. Instant.now()를 사용합니다. nowString={}", nowString, e);
            return Instant.now();
        }
    }
}
