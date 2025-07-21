package com.twogether.deokhugam.apiclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twogether.deokhugam.apiclient.dto.NaverBookItem;
import com.twogether.deokhugam.apiclient.dto.NaverBookSearchResponse;
import com.twogether.deokhugam.book.dto.NaverBookDto;
import com.twogether.deokhugam.book.exception.NaverBookException;
import com.twogether.deokhugam.common.exception.ErrorCode;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverBookClientImpl implements NaverBookClient {

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    @Value("${naver.api.ocr-secret}")
    private String ocrSecret;

    @Value("${naver.api.ocr-url}")
    private String ocrUrl;

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
    @Override
    public String extractIsbnFromImage(MultipartFile image) {
        try {
            // OCR 요청 메시지 구성
            Map<String, Object> message = new HashMap<>();
            message.put("version", "V2");
            message.put("requestId", UUID.randomUUID().toString());
            message.put("timestamp", System.currentTimeMillis());

            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("format", "jpg");
            imageMap.put("name", "image");

            message.put("images", List.of(imageMap));

            // HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("X-OCR-SECRET", ocrSecret);

            // multipart body 생성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("message", new ObjectMapper().writeValueAsString(message));
            body.add("file", new MultipartInputResource(image));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // OCR 요청
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<String> response = restTemplate.postForEntity(ocrUrl, requestEntity, String.class);

            // OCR 결과에서 ISBN 추출
            return parseIsbnFromOcrJson(response.getBody());

        } catch (Exception e) {
            throw new NaverBookException(ErrorCode.NAVER_API_CONNECTION_FAILED);
        }

    }

    private String parseIsbnFromOcrJson(String jsonText) {
        try {
            JsonNode root = new ObjectMapper().readTree(jsonText);
            JsonNode fields = root.path("images").get(0).path("fields");

            StringBuilder collected = new StringBuilder();
            boolean isbnStarted = false;

            for (JsonNode field : fields) {
                String text = field.path("inferText").asText();

                // "ISBN"이 포함되면 추출 시작
                if (!isbnStarted && text.toLowerCase().contains("isbn")) {
                    isbnStarted = true;
                    continue;
                }

                if (isbnStarted) {
                    // ISBN 추정 텍스트가 끝나는 조건
                    if (text.contains("세트")) break;

                    collected.append(text.replaceAll("[^0-9]", "")); // 숫자만 누적
                }
            }

            // 정규식으로 ISBN 13자리 추출
            Matcher matcher = Pattern.compile("\\d{13}").matcher(collected.toString());
            if (matcher.find()) {
                return matcher.group(0);
            }

            return null;
        } catch (Exception e) {
            throw new NaverBookException(ErrorCode.NAVER_OCR_ISBN_NOT_FOUND);
        }
    }
    private static class MultipartInputResource extends ByteArrayResource {
        private final String filename;

        public MultipartInputResource(MultipartFile file) throws IOException {
            super(file.getBytes());
            this.filename = file.getOriginalFilename();
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
    private LocalDate parseDate(String pubdate) {
        try {
            return LocalDate.parse(pubdate, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }
}
