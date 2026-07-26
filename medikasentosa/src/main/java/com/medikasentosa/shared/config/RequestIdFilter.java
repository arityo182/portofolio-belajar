package com.medikasentosa.shared.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter yang menyisipkan X-Request-ID ke setiap request/response.
 * ID ini digunakan untuk tracing error ke log.
 */
public class RequestIdFilter implements Filter {

    private static final String HEADER = "X-Request-ID";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;

        // Ambil dari header masuk atau buat baru
        String requestId = httpReq.getHeader(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        // Simpan ke MDC buat logging + response header + request attribute
        MDC.put("requestId", requestId);
        httpRes.setHeader(HEADER, requestId);
        req.setAttribute("requestId", requestId);

        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear();
        }
    }
}
