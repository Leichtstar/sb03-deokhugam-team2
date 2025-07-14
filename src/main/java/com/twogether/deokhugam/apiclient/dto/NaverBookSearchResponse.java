package com.twogether.deokhugam.apiclient.dto;

import java.util.List;

public record NaverBookSearchResponse(
    List<NaverBookItem> items
) {}

