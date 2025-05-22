package com.core.banking.controller;

import com.core.banking.entity.DepositoAccountDetail;
import com.core.banking.service.DepositAccountDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/deposito-account-details")
public class DepositoAccountDetailController {
    @Autowired
    private DepositAccountDetailService depositoAccountDetailService;

    @GetMapping
    public List<DepositAccountDetail> getAll() {
        return depositoAccountDetailService.findAll();
    }
}
