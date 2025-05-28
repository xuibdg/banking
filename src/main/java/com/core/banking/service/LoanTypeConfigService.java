package com.core.banking.service;

import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanTypeConfigRequest;
import com.core.banking.dto.LoanTypeConfigResponse;
import java.util.List;

public interface LoanTypeConfigService {
    String createLoanTypeConfig (LoanTypeConfigRequest request, UserMetaData userMetaData);
    List<LoanTypeConfigResponse> findAll();
    String updateLoanTypeConfig (String loanTypeConfigId, LoanTypeConfigRequest request, UserMetaData userMetaData);
    String deleteLoanTypeConfig (String loanTypeConfigId, UserMetaData userMetaData);
}
