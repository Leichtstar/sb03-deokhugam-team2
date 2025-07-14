package com.twogether.deokhugam.apiclient;

import com.twogether.deokhugam.apiclient.dto.NaverBookItem;
import com.twogether.deokhugam.apiclient.dto.NaverBookSearchResponse;
import com.twogether.deokhugam.book.dto.NaverBookDto;
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

    //  @Value("${naver.api.client-id}")  보안값으로 개발중 생략
    private String clientId = "id";

    //  @Value("${naver.api.client-secret}")  보안값으로 개발중 생략
    private String clientSecret = "secret";

    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public NaverBookDto fetchInfoByIsbn(String isbn) {
        if(isbn == null || isbn.isEmpty()){
            throw new IllegalArgumentException("Isbn cannot be null or empty");
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
              System.err.println("Naver API 호출 실패: " + e.getMessage());
              return null;
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
            log.warn("이미지 다운로드 실패: IRL={}, 오류={}",imageUrl,e.getMessage());
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
