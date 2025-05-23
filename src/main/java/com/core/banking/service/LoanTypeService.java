package com.core.banking.service;

import com.core.banking.dto.LoanTypeRequest;
import com.core.banking.entity.LoanType;
import java.util.List;

public interface LoanTypeService {
    List<LoanType> findAll();
    String createLoanType (LoanTypeRequest request);
    String updateLoanType (String loanTypeId, LoanTypeRequest request);
    String deleteLoanType (String loanTypeId);
}
