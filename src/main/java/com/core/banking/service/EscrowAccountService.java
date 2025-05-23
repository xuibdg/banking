package com.core.banking.service;

import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.EscrowAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.Customer;
import com.core.banking.entity.EscrowAccount;
import java.util.List;

public interface EscrowAccountService {
    String createEscrowAccount (EscrowAccountRequest request, UserMetaData userMetaData);
    List<EscrowAccountResponse> getAll();
    String updateEscrowAccount (String id, EscrowAccountRequest request);
    String deleteEscrowAccount (String id);
}
