package com.core.banking.config;

import com.core.banking.dto.UserMetaData;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

public class FilterInterceptor implements Filter {

    private static final String SECRET_KEY = "mysecretkey12345678901234567890123456789012";

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();

        // Allow anonymous paths
        if (path.equals("/login") || path.startsWith("/public/") || path.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization-key");
        if (header == null || header.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization-key header");
            return;
        }

        String token = header.trim(); // langsung pakai token

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            UserMetaData userMetaData = UserMetaData.builder()
                    .userId(claims.get("userId", String.class))
                    .username(claims.getSubject())
                    .email(claims.get("email", String.class))
                    .roleId(claims.get("roleId", String.class))
                    .authenticationKey(token)
                    .sessionId(claims.get("sessionId", String.class))
                    .build();

            request.setAttribute("userMetaData", userMetaData);
            chain.doFilter(request, response);

        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token: " + e.getMessage());
        }
    }


    private boolean isExcludedPath(String path) {
        return path.equals("/login")
                || path.startsWith("/public/")
                || path.startsWith("/actuator")
                || path.equals("/error");
    }


}
