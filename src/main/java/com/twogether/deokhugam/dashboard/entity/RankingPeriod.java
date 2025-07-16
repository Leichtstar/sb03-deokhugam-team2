package com.twogether.deokhugam.dashboard.entity;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

public enum RankingPeriod {

    DAILY {
        @Override
        public LocalDateTime getStartTime(LocalDateTime now) {
            return now.with(LocalTime.MIN);
        }

        @Override
        public LocalDateTime getEndTime(LocalDateTime now) {
            return now.with(LocalTime.MAX);
        }
    },

    WEEKLY {
        @Override
        public LocalDateTime getStartTime(LocalDateTime now) {
            return now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
        }

        @Override
        public LocalDateTime getEndTime(LocalDateTime now) {
            return now.with(LocalTime.MAX);
        }
    },

    MONTHLY {
        @Override
        public LocalDateTime getStartTime(LocalDateTime now) {
            return now.withDayOfMonth(1).with(LocalTime.MIN);
        }

        @Override
        public LocalDateTime getEndTime(LocalDateTime now) {
            return now.with(LocalTime.MAX);
        }
    },

    ALL_TIME {
        @Override
        public LocalDateTime getStartTime(LocalDateTime now) {
            return LocalDateTime.of(2000, 1, 1, 0, 0);
        }

        @Override
        public LocalDateTime getEndTime(LocalDateTime now) {
            return now;
        }
    };

    public abstract LocalDateTime getStartTime(LocalDateTime now);
    public abstract LocalDateTime getEndTime(LocalDateTime now);
}