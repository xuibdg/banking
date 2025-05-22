package com.core.banking.service;

import com.core.banking.entity.DepositAccountDetail;
import java.util.List;

public interface DepositAccountDetailService {
    List<DepositAccountDetail> findAll();
}
