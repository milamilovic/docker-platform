package com.dockerplatform.backend.configs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String username = "unauthorized user";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            username = auth.getName();
        }

        log.info("Incoming request: {} {} from user {}",
                request.getMethod(),
                request.getRequestURI(),
                username);

        return true; // nastavlja dalje ka kontroleru
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {

        String username = "unauthorized user";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            username = auth.getName();
        }

        log.info("Response status: {} for {} {} by user {}",
                response.getStatus(),
                request.getMethod(),
                request.getRequestURI(),
                username);

        if (ex != null) {
            log.error("Exception occurred for user {}: ", username, ex);
        }
    }
}