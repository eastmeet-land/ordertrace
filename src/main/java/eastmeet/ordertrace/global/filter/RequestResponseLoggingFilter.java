package eastmeet.ordertrace.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (isSwaggerRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 1024);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        String traceId = UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            String requestBody = compactJson(
                new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8));
            String responseBody = compactJson(
                new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8));

            int status = responseWrapper.getStatus();

            if (status >= 500) {
                log.error("[{}] {} {} | status={} | {}ms | req={} | res={}",
                    traceId, request.getMethod(), request.getRequestURI(),
                    status, duration, requestBody, responseBody);
            } else if (status >= 400) {
                log.warn("[{}] {} {} | status={} | {}ms | req={} | res={}",
                    traceId, request.getMethod(), request.getRequestURI(),
                    status, duration, requestBody, responseBody);
            } else {
                log.info("[{}] {} {} | status={} | {}ms | req={} | res={}",
                    traceId, request.getMethod(), request.getRequestURI(),
                    status, duration, requestBody, responseBody);
            }

            responseWrapper.copyBodyToResponse();
        }
    }

    private boolean isSwaggerRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("/swagger") || uri.contains("/v3/api-docs");
    }

    private String compactJson(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }
        return json.replaceAll("\\s+", " ").trim();
    }
}