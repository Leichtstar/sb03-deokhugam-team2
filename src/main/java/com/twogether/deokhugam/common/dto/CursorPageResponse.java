package com.twogether.deokhugam.common.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CursorPageResponse<T> {

    private List<T> content;
    private String nextCursor;
    private LocalDateTime nextAfter;
    private int size;
    private long totalElements;
    private boolean hasNext;

    public CursorPageResponse(List<T> content, String nextCursor, LocalDateTime nextAfter, int size, boolean hasNext) {
        this.content = content;
        this.nextCursor = nextCursor;
        this.nextAfter = nextAfter;
        this.size = size;
        this.hasNext = hasNext;
    }
}
