package com.core.banking.service.impl;

import com.core.banking.entity.DepositoAccountDetail;
import com.core.banking.repository.DepositoAccountDetailRepository;
import com.core.banking.service.DepositAccountDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepositAccountDetailServiceImpl implements DepositAccountDetailService {
    @Autowired
    private DepositoAccountDetailRepository depositoAccountDetailRepository;

    @Override
    public List<DepositoAccountDetail> findAll() {
        return depositoAccountDetailRepository.findAll();
    }
}
