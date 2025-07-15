package com.twogether.deokhugam.dashboard.dto.request;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PopularRankingSearchRequest {

    private String period;
    private Long cursor;
    private LocalDateTime after;
}
