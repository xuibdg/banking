package com.core.banking.service.impl;

import com.core.banking.entity.DepositProfitSharing;
import com.core.banking.repository.DepositProfitSharingRepository;
import com.core.banking.service.DepositProfitSharingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DepositProfitSharingServiceImpl implements DepositProfitSharingService {
    @Autowired
    private DepositProfitSharingRepository depositProfitSharingRepository;

    @Override
    public List<DepositProfitSharing> findAll() {
        return depositProfitSharingRepository.findAll();
    }
}
