package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanRepaymentScheduleRequest;
import com.core.banking.dto.LoanRepaymentScheduleResponse;
import com.core.banking.service.LoanRepaymentScheduleService;
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
@RequestMapping("/loan-repayment-schedules")
public class LoanRepaymentScheduleController {

    @Autowired
    private LoanRepaymentScheduleService loanRepaymentScheduleService;

    @GetMapping
    public List<LoanRepaymentScheduleResponse> getAll() {
        return loanRepaymentScheduleService.findAll();
    }

    @PostMapping("/create")
    BaseResponse<String> createLoanRepaymentSchedule (@RequestBody LoanRepaymentScheduleRequest request,@CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanRepaymentScheduleService.createLoanRepaymentSchedule(request, userMetaData));
    }

    @PostMapping("/repayment")
    BaseResponse<LoanRepaymentScheduleResponse> loanRepayment (@RequestBody LoanRepaymentScheduleRequest request,@CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanRepaymentScheduleService.loanRepayment(request, userMetaData));
    }

    @PutMapping("/{loanRepaymentScheduleId}")
    BaseResponse<String> updateLoanRepaymentSchedule (@PathVariable String loanRepaymentScheduleId,@RequestBody LoanRepaymentScheduleRequest request,@CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanRepaymentScheduleService.updateLoanRepaymentSchedule(loanRepaymentScheduleId,request,userMetaData));
    }

    @DeleteMapping("/{loanRepaymentScheduleId}")
    BaseResponse<String> deleteLoanRepaymentSchedule (@PathVariable String loanRepaymentScheduleId,@CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(loanRepaymentScheduleService.deleteLoanRepaymentSchedule(loanRepaymentScheduleId, userMetaData));
    }
}
