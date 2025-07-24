package com.twogether.deokhugam.dashboard.entity;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public enum RankingPeriod {

    DAILY {
        @Override
        public Instant getStartTime(Instant now) {
            return now.atZone(ZONE).toLocalDate().atStartOfDay(ZONE).toInstant();
        }

        @Override
        public Instant getEndTime(Instant now) {
            return now.atZone(ZONE).toLocalDate().atTime(LocalTime.MAX).atZone(ZONE).toInstant();
        }
    },

    WEEKLY {
        @Override
        public Instant getStartTime(Instant now) {
            return now.atZone(ZONE).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toLocalDate().atStartOfDay(ZONE).toInstant();
        }

        @Override
        public Instant getEndTime(Instant now) {
            return now.atZone(ZONE).toLocalDate().atTime(LocalTime.MAX).atZone(ZONE).toInstant();
        }
    },

    MONTHLY {
        @Override
        public Instant getStartTime(Instant now) {
            return now.atZone(ZONE).withDayOfMonth(1).toLocalDate().atStartOfDay(ZONE).toInstant();
        }

        @Override
        public Instant getEndTime(Instant now) {
            return now.atZone(ZONE).toLocalDate().atTime(LocalTime.MAX).atZone(ZONE).toInstant();
        }
    },

    ALL_TIME {
        @Override
        public Instant getStartTime(Instant now) {
            return LocalDate.of(2000, 1, 1).atStartOfDay(ZONE).toInstant();
        }

        @Override
        public Instant getEndTime(Instant now) {
            return now;
        }
    };

    private static final ZoneId ZONE = ZoneId.systemDefault();

    public abstract Instant getStartTime(Instant now);
    public abstract Instant getEndTime(Instant now);
}