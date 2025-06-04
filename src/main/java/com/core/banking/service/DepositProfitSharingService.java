package com.core.banking.service;

import com.core.banking.dto.DepositProfitSharingRequest;
import com.core.banking.dto.DepositProfitSharingResponse;
import com.core.banking.entity.DepositProfitSharing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DepositProfitSharingService {

    void processProfitSharing(LocalDate profitPeriodStart, LocalDate profitPeriodEnd);

    BigDecimal totalProfitBank();

    DepositProfitSharingResponse update(String id, DepositProfitSharingRequest request);

    void delete(String id);

    List<DepositProfitSharing> findAll();
}
