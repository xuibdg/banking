package com.core.banking.controller;

import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.service.SavingTypeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/saving-type-configs")
public class SavingTypeConfigController {
    @Autowired
    private SavingTypeConfigService savingTypeConfigService;

    @GetMapping
    public List<SavingTypeConfig> getAll() {
        return savingTypeConfigService.findAll();
    }
}
