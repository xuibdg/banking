package com.core.banking.service;

import com.core.banking.entity.LoanType;
import java.util.List;

public interface LoanTypeService {
    List<LoanType> findAll();
}
