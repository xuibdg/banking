package com.core.banking.service.impl;

import com.core.banking.entity.DepositType;
import com.core.banking.repository.DepositTypeRepository;
import com.core.banking.service.DepositTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepositTypeServiceImpl implements DepositTypeService {
    @Autowired
    private DepositTypeRepository depositTypeRepository;

    @Override
    public List<DepositType> findAll() {
        return depositTypeRepository.findAll();
    }
}
