package com.core.banking.service.impl;

import com.core.banking.dto.MUserRequest;
import com.core.banking.dto.MUserResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.MUser;
import com.core.banking.entity.UserRole;
import com.core.banking.repository.MUserRepository;
import com.core.banking.repository.UserRoleRepository;
import com.core.banking.service.MUserService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class MUserServiceImpl implements MUserService {

    @Autowired
    MUserRepository mUserRepository;

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String createUser(MUserRequest request, UserMetaData userMetaData) {

        UserRole userRole = userRoleRepository.findById(request.getRoleId()).orElseThrow(() ->  new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND));
        MUser mUser = MUser.builder()
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // ENKRIPSI juga di update
                .createdAt(Timestamp.from(Instant.now()))
                .createdBy(userMetaData.getUsername())
                .userRole(userRole)
                .build();

        mUserRepository.save(mUser);
        return "SUCCESS CREATED USER";
    }

    @Override
    public List<MUserResponse> getAll(String name) {

        List<MUserResponse> list = mUserRepository.findByName(name).stream().map(data -> {
            return MUserResponse.builder().userId(data.getUserId())
                    .name(data.getName())
                    .email(data.getEmail())
                    .createdAt(Timestamp.from(Instant.now()))
                    .roleName(data.getUserRole().getRoleName()).build();
        }).collect(Collectors.toList());

        return list;
    }

    @Override
    public String updateUser(String id, MUserRequest request) {
        mUserRepository.findById(id).map(data -> {
            UserRole userRole = userRoleRepository.findById(request.getRoleId()).orElseThrow(() ->  new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND));
            data.setName(request.getName());
            data.setEmail(request.getEmail());
            data.setPassword(passwordEncoder.encode(request.getPassword()));
            data.setUserRole(userRole);
            data.setUpdatedAt(Timestamp.from(Instant.now()));
            mUserRepository.save(data);
            return data;
        });

        return "SUCCESS UPDATED";
    }

    @Override
    public String deletedUser(String id) {
        mUserRepository.findById(id).map(data -> {
            data.setDeleted(true);
            mUserRepository.save(data);
            return data;
        });

        return "SUCCESS DELETED";
    }
}

