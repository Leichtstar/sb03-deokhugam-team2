package com.twogether.deokhugam.apiclient;

import com.twogether.deokhugam.book.dto.NaverBookDto;
import org.springframework.web.multipart.MultipartFile;

public interface NaverBookClient {
    NaverBookDto fetchInfoByIsbn(String isbn);
    String extractIsbnFromImage(MultipartFile image);
}


