package com.core.banking.service;

import com.core.banking.dto.DepositAccountRequest;
import com.core.banking.dto.EarlyWithdrawalResponse;
import com.core.banking.dto.UserMetaData;

import java.util.Map;

public interface EarlyWithdrawalService {
    EarlyWithdrawalResponse processEarlyWithdrawal(DepositAccountRequest DepositAccountRequest, Long depositAccountId, String savingAccountId, UserMetaData userMetaData);
    Map<String, Object> calculateEarlyWithdrawalPenalty(Long depositAccountId);
}
