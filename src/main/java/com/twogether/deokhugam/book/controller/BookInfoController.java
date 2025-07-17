package com.twogether.deokhugam.book.controller;

import com.twogether.deokhugam.apiclient.NaverBookClient;
import com.twogether.deokhugam.book.dto.NaverBookDto;
import com.twogether.deokhugam.book.exception.NaverBookException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books/info")
@RequiredArgsConstructor
public class BookInfoController {

    private final NaverBookClient naverBookClient;

    @GetMapping
    public ResponseEntity<NaverBookDto> getBookInfo(@RequestParam("isbn") String isbn) {
        if (!isbn.matches("\\d{1,13}")) {
            throw new NaverBookException(ErrorCode.INVALID_ISBN);
        }
        NaverBookDto dto = naverBookClient.fetchInfoByIsbn(isbn);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
}
