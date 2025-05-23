package com.core.banking.service;

import com.core.banking.dto.LoanTypeConfigRequest;
import com.core.banking.entity.LoanTypeConfig;
import java.util.List;

public interface LoanTypeConfigService {
    String createLoanTypeConfig (LoanTypeConfigRequest request);
    List<LoanTypeConfig> findAll();
    String updateLoanTypeConfig (String loanTypeConfigId, LoanTypeConfigRequest request);
    String deleteLoanTypeConfig (String loanTypeConfigId);
}
