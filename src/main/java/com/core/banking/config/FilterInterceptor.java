package com.core.banking.config;

import com.core.banking.dto.UserMetaData;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;

public class FilterInterceptor implements WebFilter {

    private static final String SECRET_KEY = "mysecretkey12345678901234567890123456789012";

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getPath().value();

        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        String header = request.getHeaders().getFirst("Authorization-key");
        if (header == null || header.isBlank()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            byte[] bytes = "Missing or invalid Authorization-key header".getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }

        String token = header.trim();
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

            // Set userMetaData ke attribute exchange agar bisa diakses downstream
            exchange.getAttributes().put("userMetaData", userMetaData);
            return chain.filter(exchange);
        } catch (JwtException e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            byte[] bytes = ("Invalid token: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }
    }

    private boolean isExcludedPath(String path) {
        return path.equals("/login")
                || path.startsWith("/public/")
                || path.startsWith("/actuator")
                || path.equals("/error");
    }
}
