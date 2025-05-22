package com.core.banking.service.impl;

import com.core.banking.entity.DepositoProfitSharing;
import com.core.banking.repository.DepositoProfitSharingRepository;
import com.core.banking.service.DepositoProfitSharingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepositoProfitSharingServiceImpl implements DepositoProfitSharingService {
    @Autowired
    private DepositoProfitSharingRepository depositoProfitSharingRepository;

    @Override
    public List<DepositoProfitSharing> findAll() {
        return depositoProfitSharingRepository.findAll();
    }
}
