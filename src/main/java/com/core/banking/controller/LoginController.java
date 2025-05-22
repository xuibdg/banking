package com.core.banking.controller;

import com.core.banking.entity.MUser;
import com.core.banking.repository.MUserRepository;
import com.core.banking.utils.exception.BaseResponse;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController extends BaseCRUDController{

    @Autowired
    private MUserRepository mUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String SECRET_KEY = "mysecretkey12345678901234567890123456789012"; // 256-bit
    private static final long EXPIRATION_TIME = 3600000; // 1 jam

    @PostMapping("/login")
    public BaseResponse<Map<String, String>> login(@RequestParam String username, @RequestParam String password) {
        MUser mUser = mUserRepository.findByNameOptional(username).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, mUser.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, GlobalErrorMapping.INVALID_CREDENTIAL);
        }

//        if (!"SUPERADMIN".equalsIgnoreCase(mUser.getUserRole().getRoleName())) {
//            throw new BusinessException(HttpStatus.UNAUTHORIZED, GlobalErrorMapping.UNAUTHORIZED_ACCESS);
//        }

        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        String jwt = Jwts.builder()
                .setSubject(username)
                .setIssuer("myapp")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        return buildSuccessResponse(response);
    }
}

