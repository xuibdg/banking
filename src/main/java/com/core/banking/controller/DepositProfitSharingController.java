package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.DepositProfitSharingRequest;
import com.core.banking.dto.DepositProfitSharingResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.service.DepositProfitSharingService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deposit-profit-sharing")
public class DepositProfitSharingController extends BaseCRUDController {

    @Autowired
    private DepositProfitSharingService depositProfitSharingService;

    @PostMapping("/monthly")
    BaseResponse<DepositProfitSharingResponse> calculateProfitSharing(@RequestBody @Validated DepositProfitSharingRequest request, @CurrentUser UserMetaData userMetaData) {
        List<DepositProfitSharingResponse> responses = depositProfitSharingService.createCalculateDepositSharing(request, userMetaData);
        return buildSuccessResponse(responses);
    }

    @GetMapping
    BaseResponse<List<DepositProfitSharingResponse>> getAll() {
        return buildSuccessResponse(depositProfitSharingService.findAll());
    }

    @PutMapping("/{id}")
    BaseResponse update(@PathVariable Long id, @RequestBody DepositProfitSharingRequest request, @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(depositProfitSharingService.updateDepositProfitSharing(id, request, userMetaData));
    }

    @DeleteMapping("/{id}")
    BaseResponse<String> delete(@PathVariable Long id, @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(depositProfitSharingService.deleteDepositProfitSharing(id, userMetaData));
    }
}