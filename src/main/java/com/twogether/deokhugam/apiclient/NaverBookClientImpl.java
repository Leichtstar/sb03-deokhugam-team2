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
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class NaverBookClientImpl implements NaverBookClient {

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    @Value("${naver.api.ocr-secret}")
    private String ocrSecret;

    @Value("${naver.api.ocr-url}")
    private String ocrUrl;

    private final RestTemplate restTemplate;

    public NaverBookClientImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public NaverBookDto fetchInfoByIsbn(String isbn) {
        log.info("[NaverBookClient] ISBN으로 책 정보 가져오기 요청 : isbn = {}", isbn);
        if(isbn == null || isbn.isEmpty()){
            throw new NaverBookException(ErrorCode.INVALID_ISBN);
        }
        String url = "https://openapi.naver.com/v1/search/book.json?query=" + isbn;

        // 네이버 책 API용 Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<?> entity = new HttpEntity<>(headers);
        log.debug("네이버 책 API 접속 시도 : url = {}", url);
        long requestTime = System.currentTimeMillis();

            try {
                ResponseEntity<NaverBookSearchResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NaverBookSearchResponse.class
                );
                long elapsedTime = System.currentTimeMillis() - requestTime;
                log.debug("네이버 책 API에서 정보 가져오기 성공 : Status = {}, 응답시간 = {}", response.getStatusCode(), elapsedTime);

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
        log.debug("썸네일 이미지 다운로드 요청 : imageUrl = {}", imageUrl);
        try {
            ResponseEntity<byte[]> response = restTemplate
                .getForEntity(imageUrl, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("썸네일 이미지 가져오기 성공 : Status = {}",response.getStatusCode());
                return Base64.getEncoder().encodeToString(response.getBody());
            }
        } catch (Exception e) {
            throw new NaverBookException(ErrorCode.NAVER_API_THUMBNAIL_NOT_FOUND);
        }

      return null;
    }
    @Override
    public String extractIsbnFromImage(MultipartFile image) {
        log.info("[NaverBookClient] 이미지에서 ISBN 추출 요청 : 이미지 = {}", image.getOriginalFilename());
        try {
            // OCR 요청 메시지 구성
            Map<String, Object> message = new HashMap<>();
            message.put("version", "V2");
            message.put("requestId", UUID.randomUUID().toString());
            message.put("timestamp", System.currentTimeMillis());

            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("name", "image");
            String contentType = image.getContentType();
            String format = contentType != null && contentType.startsWith("image/") ? contentType.substring(6) : "jpg";
            imageMap.put("format",format);

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
            ResponseEntity<String> response;
            log.debug("CLOVA OCR API 호출 : image = {}", image.getOriginalFilename());
            long requestTime = System.currentTimeMillis(); // 요청시간 기록
            try {
                response = restTemplate.postForEntity(ocrUrl, requestEntity, String.class);
            }catch (RestClientException ex) {
                throw new NaverBookException(ErrorCode.NAVER_API_CONNECTION_FAILED);
            }
            long elapsedTime = System.currentTimeMillis() - requestTime; // 응답시간 기록
            // 응답 코드 체크
            if(response.getStatusCode().is4xxClientError()){
                throw new NaverBookException(ErrorCode.NAVER_API_UNAUTHORIZED);
            } else if(response.getStatusCode().is5xxServerError()){
                throw new NaverBookException(ErrorCode.NAVER_OCR_SERVER_ERROR);
            }
            log.debug("OCR 추출 응답 수신 : 처리시간 = {} ms", elapsedTime );
            // OCR 결과에서 ISBN 추출
            String result = parseIsbnFromOcrJson(response.getBody());
            if (result == null || result.isBlank()) {
                throw new NaverBookException(ErrorCode.NAVER_OCR_ISBN_NOT_FOUND);
            }
            return result;

        } catch (NaverBookException e) {
            throw e;
        } catch (Exception e){
            throw new NaverBookException(ErrorCode.NAVER_API_UNKNOWN_ERROR);
        }

    }

    private String parseIsbnFromOcrJson(String jsonText) {
        log.debug("응답으로부터 ISBN 추출 시작 : parseIsbnFromOcrJson");
        try {
            JsonNode root = new ObjectMapper().readTree(jsonText);
            JsonNode fields = root.path("images").get(0).path("fields");

            //inferText 필드를 숫자 혹은 X인 값만 추출한 리스트 생성
            List<String> digitFragments = new ArrayList<>();
            for(JsonNode field : fields) {
                String text = field.path("inferText").asText();
                String digits = text.replaceAll("[^0-9Xx]", "");//정규식으로 숫자와 X 외의 값 공백처리
                if(!digits.isEmpty()){
                    digitFragments.add(digits); // 추출된 숫자와 X값을 리스트에 추가
                }
            }
            // 추출값 직렬화하여 978/979로 시작하는 ISBN 코드 추출
            StringBuilder buffer = new StringBuilder();
            for(int i = 0; i < digitFragments.size(); i++){
                buffer.setLength(0); // 초기화

                for (int j = i; j < digitFragments.size(); j++) {
                    buffer.append(digitFragments.get(j));
                    if (buffer.length() >= 13) break;
                }
                String candidate = buffer.toString();
                // 13자리, 978/979 시작, 마지막 자리는 숫자
                if (candidate.matches("97[89]\\d{9}[\\d]") && isValidIsbn13(candidate)) {
                    log.info("ISBN-13 코드 추출 성공 : isbn = {}", candidate);
                    return candidate;
                }
            }
            log.debug("ISBN 추출 실패 : 조건에 맞는 코드가 존재하지 않습니다.");
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

    private boolean isValidIsbn13(String isbn) {
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            int digit = Character.getNumericValue(isbn.charAt(i));
            if (digit < 0 || digit > 9) return false;
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return sum % 10 == 0;
    }

}

