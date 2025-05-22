package com.core.banking.service.impl;

import com.core.banking.entity.SavingAccountDetail;
import com.core.banking.repository.SavingAccountDetailRepository;
import com.core.banking.service.SavingAccountDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SavingAccountDetailServiceImpl implements SavingAccountDetailService {
    @Autowired
    private SavingAccountDetailRepository savingAccountDetailRepository;

    @Override
    public List<SavingAccountDetail> findAll() {
        return savingAccountDetailRepository.findAll();
    }
}
