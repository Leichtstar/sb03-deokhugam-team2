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
    }

    /**
     * 클라이언트의 실제 IP 주소를 추출하는 메서드
     * 프록시나 로드밸런서를 거치는 경우도 고려
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String clientIp = null;

        // X-Forwarded-For 헤더 확인 (프록시 서버를 거친 경우)
        clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            // Proxy-Client-IP 헤더 확인
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            // WL-Proxy-Client-IP 헤더 확인 (WebLogic)
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            // HTTP_CLIENT_IP 헤더 확인
            clientIp = request.getHeader("HTTP_CLIENT_IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            // HTTP_X_FORWARDED_FOR 헤더 확인
            clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            // 직접 연결된 경우의 IP
            clientIp = request.getRemoteAddr();
        }

        // X-Forwarded-For에 여러 IP가 있는 경우 첫 번째 IP 사용
        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }

        return clientIp;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 요청 처리 후 MDC 데이터 정리
        log.debug("Request completed");
        MDC.clear();
    }
} 