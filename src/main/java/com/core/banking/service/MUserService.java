package com.core.banking.service;

import com.core.banking.dto.MUserRequest;
import com.core.banking.dto.MUserResponse;
import com.core.banking.dto.UserMetaData;

import java.util.List;

public interface MUserService {

    String createUser(MUserRequest request, UserMetaData userMetaData);
    List<MUserResponse> getAll(String name);
    String updateUser(String id, MUserRequest request);
    String deletedUser(String id);
}