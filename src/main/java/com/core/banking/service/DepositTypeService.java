package com.core.banking.service;

import com.core.banking.entity.DepositoType;
import java.util.List;

public interface DepositoTypeService {
    List<DepositoType> findAll();
}
