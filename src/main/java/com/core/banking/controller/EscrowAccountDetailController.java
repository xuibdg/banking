package com.core.banking.controller;

import com.core.banking.entity.EscrowAccountDetail;
import com.core.banking.service.EscrowAccountDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/escrow-account-details")
public class EscrowAccountDetailController {
    @Autowired
    private EscrowAccountDetailService escrowAccountDetailService;

    @GetMapping
    public List<EscrowAccountDetail> getAll() {
        return escrowAccountDetailService.findAll();
    }
}
