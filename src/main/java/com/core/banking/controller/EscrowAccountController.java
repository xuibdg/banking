package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.EscrowAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.service.EscrowAccountService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public List<EscrowAccountResponse> getAll(@CurrentUser UserMetaData userMetaData) {
        return escrowAccountService.getAll();
    }

    @PutMapping("/{id}")
    String updateEscrowAccount (@PathVariable String id, @RequestBody EscrowAccountRequest request,
                                @CurrentUser UserMetaData userMetaData){
        return escrowAccountService.updateEscrowAccount(id, request);
    }

    @DeleteMapping("/{id}")
    String deleteEscrowAccount(@PathVariable String id,
                               @CurrentUser UserMetaData userMetaData){
        return escrowAccountService.deleteEscrowAccount(id);
    }
}
