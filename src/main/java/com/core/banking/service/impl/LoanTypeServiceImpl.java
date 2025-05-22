package com.core.banking.service.impl;

import com.core.banking.entity.LoanType;
import com.core.banking.repository.LoanTypeRepository;
import com.core.banking.service.LoanTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LoanTypeServiceImpl implements LoanTypeService {
    @Autowired
    private LoanTypeRepository loanTypeRepository;

    @Override
    public List<LoanType> findAll() {
        return loanTypeRepository.findAll();
    }
}
