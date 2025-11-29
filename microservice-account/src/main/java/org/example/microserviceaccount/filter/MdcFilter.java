package org.example.microserviceaccount.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MdcFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException{

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String traceId = httpRequest.getHeader("X-Trace-Id");

        if (traceId != null) {
            MDC.put("X-Trace-Id", traceId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("X-Trace-Id");
        }
    }
}
