package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.EscrowAccountDetailRequest;
import com.core.banking.dto.EscrowAccountDetailResponse;
import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.EscrowAccountDetail;
import com.core.banking.service.EscrowAccountDetailService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/get-all")
    public List<EscrowAccountDetailResponse> getAll(@CurrentUser UserMetaData userMetaData) {
        return escrowAccountDetailService.getAll();
    }

    @PutMapping("/{id}")
    String updateEscrowAccountDetail (@PathVariable String id, @RequestBody EscrowAccountDetailRequest request,
                                      @CurrentUser UserMetaData userMetaData){
        return escrowAccountDetailService.updateEscrowAccountDetail(id, request);
    }

    @DeleteMapping("/{id}")
    String deleteEscrowAccountDetail(@PathVariable String id,
                                     @CurrentUser UserMetaData userMetaData){
        return escrowAccountDetailService.deleteEscrowAccountDetail(id);
    }
}
