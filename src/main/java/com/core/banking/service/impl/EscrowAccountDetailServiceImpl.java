package com.core.banking.service.impl;

import com.core.banking.entity.EscrowAccountDetail;
import com.core.banking.repository.EscrowAccountDetailRepository;
import com.core.banking.service.EscrowAccountDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EscrowAccountDetailServiceImpl implements EscrowAccountDetailService {
    @Autowired
    private EscrowAccountDetailRepository escrowAccountDetailRepository;

    @Override
    public List<EscrowAccountDetail> findAll() {
        return escrowAccountDetailRepository.findAll();
    }
}
