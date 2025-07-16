package com.twogether.deokhugam.common.exception.dto;

import java.util.List;

public record CursorPageResponse<T>(List<T> content, String nextCursor) {}

