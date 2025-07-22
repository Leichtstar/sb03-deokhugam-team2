package com.twogether.deokhugam.book.controller;

import com.twogether.deokhugam.apiclient.NaverBookClient;
import com.twogether.deokhugam.book.dto.NaverBookDto;
import com.twogether.deokhugam.book.exception.NaverBookException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookInfoController {

    private final NaverBookClient naverBookClient;

    @GetMapping(value = "/info")
    public ResponseEntity<NaverBookDto> getBookInfo(@RequestParam("isbn") String isbn) {
        if (!isbn.matches("\\d{1,13}")) {
            throw new NaverBookException(ErrorCode.INVALID_ISBN);
        }
        NaverBookDto dto = naverBookClient.fetchInfoByIsbn(isbn);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }
    @PostMapping(value = "/isbn/ocr")
    public ResponseEntity<String> getIsbnFromOcr(@RequestPart(value = "image", required = true) MultipartFile bookImage){
    if (bookImage.isEmpty() || bookImage.getSize() == 0){
        throw new NaverBookException(ErrorCode.INVALID_IMAGE_FILE);
    }
    // 이미지 타입 검증
    String contentType = bookImage.getContentType();
    if (contentType==null || !contentType.startsWith("image/")){
        throw new NaverBookException(ErrorCode.INVALID_IMAGE_FILE);
    }
    String result = naverBookClient.extractIsbnFromImage(bookImage);
        if (result == null) {
            throw new NaverBookException(ErrorCode.NAVER_OCR_ISBN_NOT_FOUND);
        }
    return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
