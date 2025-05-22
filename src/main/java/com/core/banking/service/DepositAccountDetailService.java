package com.core.banking.service;

import com.core.banking.entity.DepositoAccountDetail;
import java.util.List;

public interface DepositAccountDetailService {
    List<DepositoAccountDetail> findAll();
}
