package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.EscrowAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.enums.EscrowAccountStatus;
import com.core.banking.service.EscrowAccountService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

@RestController
@RequestMapping("/api/escrow-accounts")
public class EscrowAccountController {

    @Autowired
    private EscrowAccountService escrowAccountService;

    @PostMapping("/add")
    BaseResponse<String> createEscrowAccount(@RequestBody EscrowAccountRequest request,
                                             @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(escrowAccountService.createEscrowAccount(request, userMetaData));
    }

    @GetMapping("/get-all")
    public List<EscrowAccountResponse> getAll() {
        return escrowAccountService.getAll();
    }

    @GetMapping("/filter")
    public List<EscrowAccountResponse> findByNeedData(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false)EscrowAccountStatus accountStatus) {
        List<EscrowAccountResponse> filter = escrowAccountService.filterData(id, start, end, accountStatus);
        return filter;
    }

    @PutMapping("/{id}")
    BaseResponse<String> updateEscrowAccount (@PathVariable String id, @RequestBody EscrowAccountRequest request,
                                @CurrentUser UserMetaData userMetaData){
        return buildSuccessResponse(escrowAccountService.updateEscrowAccount(id, request, userMetaData));
    }

    @DeleteMapping("/{id}")
    BaseResponse<String> deleteEscrowAccount(@PathVariable String id,
                               @CurrentUser UserMetaData userMetaData){
        return buildSuccessResponse(escrowAccountService.deleteEscrowAccount(id, userMetaData));
    }
}
