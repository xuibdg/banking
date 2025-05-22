package com.core.banking.service.impl;

import com.core.banking.entity.DepositoType;
import com.core.banking.repository.DepositoTypeRepository;
import com.core.banking.service.DepositoTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepositoTypeServiceImpl implements DepositoTypeService {
    @Autowired
    private DepositoTypeRepository depositoTypeRepository;

    @Override
    public List<DepositoType> findAll() {
        return depositoTypeRepository.findAll();
    }
}
