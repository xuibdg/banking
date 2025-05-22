package com.core.banking.service;

import com.core.banking.entity.DepositType;
import java.util.List;

public interface DepositTypeService {
    List<DepositType> findAll();
}
