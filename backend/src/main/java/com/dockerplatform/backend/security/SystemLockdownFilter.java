package com.dockerplatform.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class SystemLockdownFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        Path secretFile = Path.of("secrets", "super_admin.txt");

        if (Files.exists(secretFile) && !path.contains("/auth")){
            response.setStatus(503);
            response.getWriter().write("System initialization in progress.");
            return;
        }
        filterChain.doFilter(request,response);
    }
}
