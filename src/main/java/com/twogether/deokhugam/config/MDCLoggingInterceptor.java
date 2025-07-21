package com.twogether.deokhugam.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 요청마다 MDC에 컨텍스트 정보를 추가하는 인터셉터
 */
@Slf4j
public class MDCLoggingInterceptor implements HandlerInterceptor {
    
    /**
     * MDC 로깅에 사용되는 상수 정의
     */
    public static final String REQUEST_ID = "requestId";
    public static final String REQUEST_METHOD = "requestMethod";
    public static final String REQUEST_URI = "requestUri";
    public static final String CLIENT_IP = "clientIp";
    
    public static final String REQUEST_ID_HEADER = "Deokhugam-Request-ID";
    public static final String CLIENT_IP_HEADER = "Deokhugam-Client-IP";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 요청 ID 생성 (UUID)
            String requestId = UUID.randomUUID().toString().replaceAll("-", "");

            // 클라이언트 IP 주소 추출
            String clientIp = getClientIpAddress(request);

            // MDC에 컨텍스트 정보 추가
            MDC.put(REQUEST_ID, requestId);
            MDC.put(REQUEST_METHOD, request.getMethod());
            MDC.put(REQUEST_URI, request.getRequestURI());
            MDC.put(CLIENT_IP, clientIp);

            // 응답 헤더에 요청 ID 추가
            response.setHeader(REQUEST_ID_HEADER, requestId);
            response.setHeader(CLIENT_IP_HEADER, clientIp);

            log.debug("Request started");
            return true;
        } catch (Exception e) {
            log.error("Failed to setup MDC context", e);
            return true; // 인터셉터 오류가 요청 처리를 중단하지 않도록
        }
    }

    /**
     * 클라이언트의 실제 IP 주소를 추출하는 메서드
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String clientIp = "";

        // 신뢰할 수 있는 헤더 목록 (우선순위 순서로 배열)
        String[] headers = {
            "X-Forwarded-For",      // 가장 일반적인 프록시 헤더
            "Proxy-Client-IP",      // 일반 프록시 서버 헤더
            "WL-Proxy-Client-IP",   // WebLogic 프록시 헤더
            "HTTP_CLIENT_IP",       // HTTP 클라이언트 IP 헤더
            "HTTP_X_FORWARDED_FOR"  // HTTP X-Forwarded-For 헤더
        };

        // 헤더들을 순서대로 확인하여 유효한 IP 찾기
        for (String header : headers) {
            clientIp = request.getHeader(header);
            if (isValidIp(clientIp)) {
                break; // 유효한 IP를 찾으면 반복 중단
            }
        }

        // 헤더에서 찾지 못한 경우 직접 연결 IP 사용
        if (!isValidIp(clientIp)) {
            clientIp = request.getRemoteAddr();
        }

        // X-Forwarded-For에 여러 IP가 있는 경우 첫 번째 IP 사용
        // 예: "192.168.1.1, 10.0.0.1" -> "192.168.1.1"
        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }

        // null 방지를 위한 안전장치
        return clientIp != null ? clientIp : "unknown";
    }

    /**
     * IP 주소가 유효한지 검증하는 헬퍼 메서드
     * null, 빈 문자열, "unknown" 값을 무효한 IP로 판단
     */
    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 요청 처리 후 MDC 데이터 정리
        log.debug("Request completed");
        MDC.clear();
    }
} 