package com.twogether.deokhugam.apiclient;

import com.twogether.deokhugam.apiclient.dto.NaverBookItem;
import com.twogether.deokhugam.apiclient.dto.NaverBookSearchResponse;
import com.twogether.deokhugam.book.dto.NaverBookDto;
import com.twogether.deokhugam.book.exception.NaverBookException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverBookClientImpl implements NaverBookClient {

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public NaverBookDto fetchInfoByIsbn(String isbn) {
        if(isbn == null || isbn.isEmpty()){
            throw new NaverBookException(ErrorCode.INVALID_ISBN);
        }
        String url = "https://openapi.naver.com/v1/search/book.json?query=" + isbn;

        // 네이버 책 API용 Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        // RestTemplate 생성
        RestTemplate restTemplate = restTemplateBuilder.build();

            try {
                ResponseEntity<NaverBookSearchResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NaverBookSearchResponse.class
                );

                NaverBookSearchResponse body = response.getBody();
                if (body == null || body.items() == null || body.items().isEmpty()) {
                    return null;
                }
                List<NaverBookItem> items = body.items();

                var item = items.get(0);
                return new NaverBookDto(
                    item.title(),
                    item.author(),
                    item.description(),
                    item.publisher(),
                    parseDate(item.pubdate()),
                    isbn,
                    downloadImageAsBase64(item.image())
                );
            } catch (Exception e) {
                throw new NaverBookException(ErrorCode.NAVER_API_CONNECTION_FAILED);
            }
    }
    private String downloadImageAsBase64(String imageUrl) {
        try {
            ResponseEntity<byte[]> response = restTemplateBuilder.build()
                .getForEntity(imageUrl, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Base64.getEncoder().encodeToString(response.getBody());
            }
        } catch (Exception e) {
            throw new NaverBookException(ErrorCode.NAVER_API_THUMBNAIL_NOT_FOUND);
        }

      return null;
    }
    private LocalDate parseDate(String pubdate) {
        try {
            return LocalDate.parse(pubdate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }
}
