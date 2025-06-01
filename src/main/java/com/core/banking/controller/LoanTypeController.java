package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanTypeRequest;
import com.core.banking.entity.LoanType;
import com.core.banking.service.LoanTypeService;
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
@RequestMapping("/loan-types")
public class LoanTypeController {

    @Autowired
    private LoanTypeService loanTypeService;

    @GetMapping
    BaseResponse<List<LoanType>> getAll() {
        return buildSuccessResponse(loanTypeService.findAll());
    }

    @PostMapping("/create")
    BaseResponse<String> createLoanType (@RequestBody LoanTypeRequest request,
                                         @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanTypeService.createLoanType(request, userMetaData));
    }

    @PutMapping("/{loanTypeId}")
    BaseResponse<String> updateLoanType (@PathVariable String loanTypeId,
                                         @RequestBody LoanTypeRequest request,
                                         @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanTypeService.updateLoanType(loanTypeId, request, userMetaData));
    }

    @DeleteMapping("/{loanTypeId}")
    BaseResponse<String> deleteLoanType (@PathVariable String loanTypeId,
                                         @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanTypeService.deleteLoanType(loanTypeId, userMetaData));
    }
}
