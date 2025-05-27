package com.core.banking.controller;

import com.core.banking.dto.DepositMaturityResponse;
import com.core.banking.service.DepositMaturityService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deposit-account/maturity")
public class DepositMaturityController {
    @Autowired
    DepositMaturityService depositMaturityService;

//    @PostMapping("/{id}")
//    BaseResponse<DepositMaturityResponse> depositMaturity(@PathVariable )
}
