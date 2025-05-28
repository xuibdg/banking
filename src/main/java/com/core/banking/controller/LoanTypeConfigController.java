package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanTypeConfigRequest;
import com.core.banking.dto.LoanTypeConfigResponse;
import com.core.banking.service.LoanTypeConfigService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

import java.util.List;

@RestController
@RequestMapping("/loan-type-configs")
public class LoanTypeConfigController {
    @Autowired
    private LoanTypeConfigService loanTypeConfigService;

    @GetMapping
    public List<LoanTypeConfigResponse> getAll() {
        return loanTypeConfigService.findAll();
    }

    @PostMapping("/create")
    BaseResponse<String> createLoanTypeConfig (@RequestBody LoanTypeConfigRequest request,
                                               @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanTypeConfigService.createLoanTypeConfig(request, userMetaData));
    }

    @PutMapping("/{loanTypeConfigId}")
    BaseResponse<String> updateLoanTypeConfig (@PathVariable String loanTypeConfigId, @RequestBody LoanTypeConfigRequest request,
                                                @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanTypeConfigService.updateLoanTypeConfig(loanTypeConfigId, request,userMetaData));
    }

    @DeleteMapping("/{loanTypeConfigId}")
    BaseResponse<String> deleteLoanTypeConfig (@PathVariable String loanTypeConfigId,
                                                @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanTypeConfigService.deleteLoanTypeConfig(loanTypeConfigId, userMetaData));
    }
}
