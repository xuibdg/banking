package com.core.banking.service;

import com.core.banking.dto.EarlyWithdrawalResponse;
import com.core.banking.dto.UserMetaData;

import java.util.Map;

public interface EarlyWithdrawalService {
    EarlyWithdrawalResponse processEarlyWithdrawal(Long depositAccountId, UserMetaData userMetaData);
    Map<String, Object> calculateEarlyWithdrawalPenalty(Long depositAccountId);
}
