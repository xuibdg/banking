package com.core.banking.controller;

import com.core.banking.dto.DepositProfitSharingRequest;
import com.core.banking.service.DepositProfitSharingService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deposit-profit-sharings")
public class DepositProfitSharingController extends BaseCRUDController {

    @Autowired
    private DepositProfitSharingService depositProfitSharingService;

    @PostMapping("/process")
    public BaseResponse processProfitSharing(@RequestBody DepositProfitSharingRequest request) {
        depositProfitSharingService.processProfitSharing(request.getProfitPeriodStartDate(), request.getProfitPeriodEndDate());
        return buildSuccessResponse("Profit sharing processed successfully.");
    }

    // Uncomment and use BaseResponse for findAll
//    @GetMapping
//    public BaseResponse findAll() {
//        return buildSuccessResponse(depositProfitSharingService.findAll());
//    }

    @PutMapping("/{id}")
    public BaseResponse update(@PathVariable String id, @RequestBody DepositProfitSharingRequest request) {
        return buildSuccessResponse(depositProfitSharingService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public BaseResponse delete(@PathVariable String id) {
        depositProfitSharingService.delete(id);
        return buildSuccessResponse("Deleted successfully.");
    }
}