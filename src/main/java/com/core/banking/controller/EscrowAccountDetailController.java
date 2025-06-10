package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.EscrowAccountDetailRequest;
import com.core.banking.dto.EscrowAccountDetailResponse;
import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.UserMetaData;
import com.core.banking.enums.EscrowTransactionType;
import com.core.banking.service.EscrowAccountDetailService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

@RestController
@RequestMapping("/api/escrow-account-details")
public class EscrowAccountDetailController {

    @Autowired
    private EscrowAccountDetailService escrowAccountDetailService;

    @PostMapping("/add")
    BaseResponse<String> createEscrowAccountDetail(@RequestBody EscrowAccountDetailRequest request,
                                           @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(escrowAccountDetailService.createEscrowAccountDetail(request, userMetaData));
    }

    @PostMapping("/add-escrow")
    BaseResponse<String> createAndReleaseEscrowAccount(@RequestBody EscrowAccountRequest request,
                                                       @RequestParam BigDecimal nominalTransaction,
                                                       @RequestParam String releaseAccountNumber,
                                                       @RequestParam String description,
                                                       @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(escrowAccountDetailService.createAndReleaseEscrowAccount(request, nominalTransaction, releaseAccountNumber, description, userMetaData));
    }

    @GetMapping("/get-all")
    public List<EscrowAccountDetailResponse> getAll() {
        return escrowAccountDetailService.getAll();
    }

    @GetMapping("/filter")
    public List<EscrowAccountDetailResponse> findByNeedData(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate endDate,
            @RequestParam(required = false)EscrowTransactionType transactionType) {
        List<EscrowAccountDetailResponse> filter = escrowAccountDetailService.filterData(id, startDate, endDate, transactionType);
        return filter;
    }

    @PutMapping("/{id}")
    BaseResponse <String> updateEscrowAccountDetail (@PathVariable String id, @RequestBody EscrowAccountDetailRequest request,
                                      @CurrentUser UserMetaData userMetaData){
        return buildSuccessResponse(escrowAccountDetailService.updateEscrowAccountDetail(id, request, userMetaData));
    }

    @DeleteMapping("/{id}")
    BaseResponse <String> deleteEscrowAccountDetail(@PathVariable String id,
                                     @CurrentUser UserMetaData userMetaData){
        return buildSuccessResponse(escrowAccountDetailService.deleteEscrowAccountDetail(id, userMetaData));
    }
}
