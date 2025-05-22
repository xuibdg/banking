package com.core.banking.controller;

import com.core.banking.entity.DepositoTypeConfig;
import com.core.banking.service.DepositoTypeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/deposito-type-configs")
public class DepositoTypeConfigController {
    @Autowired
    private DepositoTypeConfigService depositoTypeConfigService;

    @GetMapping
    public List<DepositoTypeConfig> getAll() {
        return depositoTypeConfigService.findAll();
    }
}
