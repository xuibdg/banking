package com.core.banking.service.impl;

import com.core.banking.entity.DepositAccountDetail;
import com.core.banking.repository.DepositAccountDetailRepository;
import com.core.banking.service.DepositAccountDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepositAccountDetailServiceImpl implements DepositAccountDetailService {
    @Autowired
    private DepositAccountDetailRepository depositAccountDetailRepository;

    @Override
    public List<DepositAccountDetail> findAll() {
        return depositAccountDetailRepository.findAll();
    }
}
