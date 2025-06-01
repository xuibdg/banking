package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.EarlyWithdrawalRequest;
import com.core.banking.dto.EarlyWithdrawalResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.service.EarlyWithdrawalService;
import com.core.banking.utils.exception.BaseResponse;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

@RestController
@RequestMapping("/api/deposit-accounts")
@NoArgsConstructor
public class EarlyWithdrawalController {
    @Autowired
    private EarlyWithdrawalService earlyWithdrawalService;

    @PostMapping("/{id}/early-withdrawal")
    BaseResponse<EarlyWithdrawalResponse> processEarlyWithdrawal(@PathVariable("id") Long depositAccountId, @RequestBody(required = false)EarlyWithdrawalRequest earlyWithdrawalRequest, @CurrentUser UserMetaData userMetaData) {
        EarlyWithdrawalResponse earlyWithdrawalResponse = earlyWithdrawalService.processEarlyWithdrawal(depositAccountId, userMetaData);
        return buildSuccessResponse(earlyWithdrawalResponse);
    }

    @GetMapping("/{id}/early-withdrawal/calculate")
    BaseResponse<Map<String, Object>> calculateEarlyWithdrawalPenalty(@PathVariable("id") Long depositAccountId, @CurrentUser UserMetaData userMetaData) {
        Map <String, Object> result = earlyWithdrawalService.calculateEarlyWithdrawalPenalty(depositAccountId);
        return buildSuccessResponse(result);
    }
}
