package com.core.banking.service;

import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.EscrowAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.EscrowAccount;

import java.time.LocalDate;
import java.util.List;

public interface EscrowAccountService {
    String createEscrowAccount (EscrowAccountRequest request, UserMetaData userMetaData);
    List<EscrowAccountResponse> getAll();
    List<EscrowAccountResponse> filterData(String id, LocalDate startDate, LocalDate endDate);
    String updateEscrowAccount (String id, EscrowAccountRequest request, UserMetaData userMetaData);
    String deleteEscrowAccount (String id, UserMetaData userMetaData);
}
