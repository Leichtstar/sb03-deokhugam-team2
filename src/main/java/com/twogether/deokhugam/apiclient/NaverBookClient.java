package com.twogether.deokhugam.apiclient;

import com.twogether.deokhugam.book.dto.NaverBookDto;

public interface NaverBookClient {
    NaverBookDto fetchInfoByIsbn(String isbn);
}


