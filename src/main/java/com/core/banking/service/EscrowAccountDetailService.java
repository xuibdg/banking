package com.core.banking.service;

import com.core.banking.dto.EscrowAccountDetailRequest;
import com.core.banking.dto.EscrowAccountDetailResponse;
import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.UserMetaData;
import com.core.banking.enums.EscrowTransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EscrowAccountDetailService {
    String createEscrowAccountDetail (EscrowAccountDetailRequest request, UserMetaData userMetaData);
    String createAndReleaseEscrowAccount (EscrowAccountRequest escrowRequest, BigDecimal nominalTransaction, String releaseAccountNumber, String description, UserMetaData userMetaData);
    List<EscrowAccountDetailResponse> getAll();
    List<EscrowAccountDetailResponse> filterData(String id, LocalDate startDate, LocalDate endDate, EscrowTransactionType transactionType);
    String updateEscrowAccountDetail (String id, EscrowAccountDetailRequest request, UserMetaData userMetaData);
    String deleteEscrowAccountDetail(String id, UserMetaData userMetaData);
}
