package com.core.banking.service;

import com.core.banking.dto.EscrowAccountDetailRequest;
import com.core.banking.dto.EscrowAccountDetailResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.EscrowAccountDetail;
import java.util.List;

public interface EscrowAccountDetailService {
    String createEscrowAccountDetail (EscrowAccountDetailRequest request, UserMetaData userMetaData);
    List<EscrowAccountDetailResponse> getAll();
    String updateEscrowAccountDetail (String id, EscrowAccountDetailRequest request);
    String deleteEscrowAccountDetail(String id);
}
