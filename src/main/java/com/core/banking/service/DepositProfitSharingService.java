package com.core.banking.service;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.DepositProfitSharingRequest;
import com.core.banking.dto.DepositProfitSharingResponse;
import com.core.banking.dto.UserMetaData;

import java.util.List;

public interface DepositProfitSharingService {

    List<DepositProfitSharingResponse> createCalculateDepositSharing(DepositProfitSharingRequest depositProfitSharingRequest, @CurrentUser UserMetaData userMetaData);

    String updateDepositProfitSharing(Long id, DepositProfitSharingRequest request, UserMetaData userMetaData);

    String deleteDepositProfitSharing(Long id, UserMetaData userMetaData);

    List<DepositProfitSharingResponse> findAll();
}
