package com.core.banking.service;

import com.core.banking.dto.EscrowAccountDetailRequest;
import com.core.banking.dto.EscrowAccountDetailResponse;
import com.core.banking.dto.UserMetaData;

import java.time.LocalDate;
import java.util.List;

public interface EscrowAccountDetailService {
    String createEscrowAccountDetail (EscrowAccountDetailRequest request, UserMetaData userMetaData);
    List<EscrowAccountDetailResponse> getAll();
    String updateEscrowAccountDetail (String id, EscrowAccountDetailRequest request, UserMetaData userMetaData);
    String deleteEscrowAccountDetail(String id, UserMetaData userMetaData);
}
