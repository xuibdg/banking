package com.core.banking.controller;

import com.core.banking.entity.DepositoAccountDetail;
import com.core.banking.service.DepositoAccountDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/deposito-account-details")
public class DepositoAccountDetailController {
    @Autowired
    private DepositoAccountDetailService depositoAccountDetailService;

    @GetMapping
    public List<DepositoAccountDetail> getAll() {
        return depositoAccountDetailService.findAll();
    }
}
