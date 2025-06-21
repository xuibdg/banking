package com.core.banking.service;

import com.core.banking.dto.MChartOfAccountRequest;
import com.core.banking.dto.MChartOfAccountResponse;
import com.core.banking.dto.UserMetaData;

import java.util.List;

public interface MChartOfAccountService {
    MChartOfAccountResponse create(MChartOfAccountRequest request, UserMetaData userMetaData);
    MChartOfAccountResponse getById(String id, UserMetaData userMetaData);
    List<MChartOfAccountResponse> getAll();
    MChartOfAccountResponse update(MChartOfAccountRequest request, String id, UserMetaData userMetaData);
}
