package com.core.banking.controller;

import com.core.banking.entity.SavingAccountDetail;
import com.core.banking.service.SavingAccountDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/saving-account-details")
public class SavingAccountDetailController {
    @Autowired
    private SavingAccountDetailService savingAccountDetailService;

    @GetMapping
    public List<SavingAccountDetail> getAll() {
        return savingAccountDetailService.findAll();
    }
}
