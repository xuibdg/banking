package com.core.banking.controller;

import com.core.banking.entity.DepositTypeConfig;
import com.core.banking.service.DepositTypeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/deposit-type-configs")
public class DepositTypeConfigController {
    @Autowired
    private DepositTypeConfigService depositTypeConfigService;

    @GetMapping
    public List<DepositTypeConfig> getAll() {
        return depositTypeConfigService.findAll();
    }
}
