package com.core.banking.service;

import com.core.banking.dto.MChartOfAccountRequest;
import com.core.banking.dto.MChartOfAccountResponse;

import java.util.List;

public interface MChartOfAccountService {
    MChartOfAccountResponse create(MChartOfAccountRequest request);
    MChartOfAccountResponse getById(String id);
    List<MChartOfAccountResponse> getAll();
}
