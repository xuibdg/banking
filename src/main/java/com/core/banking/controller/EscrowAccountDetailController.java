package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.EscrowAccountDetailRequest;
import com.core.banking.dto.EscrowAccountDetailResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.service.EscrowAccountDetailService;
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
    public List<EscrowAccountDetailResponse> getAll() {
        return escrowAccountDetailService.getAll();
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
