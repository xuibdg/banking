package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.DepositMaturityResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.service.DepositMaturityService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

@RestController
@RequestMapping("/api/deposit-account/maturity")
public class DepositMaturityController {
    @Autowired
    DepositMaturityService depositMaturityService;

    @PostMapping("/{id}/process-deposit")
    BaseResponse<DepositMaturityResponse> processMaturity(@PathVariable ("id") Long depositoAccountId, @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(depositMaturityService.processMaturity(depositoAccountId));
    }

    @GetMapping("/get-list")
    BaseResponse<List<DepositMaturityResponse>> getAllMaturedDeposits(@RequestParam(required = false) LocalDate maturityDate, @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(depositMaturityService.getAllMaturedDeposits(maturityDate));
    }
}
