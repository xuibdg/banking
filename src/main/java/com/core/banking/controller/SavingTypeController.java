package com.core.banking.controller;

import com.core.banking.entity.SavingType;
import com.core.banking.service.SavingTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/saving-types")
public class SavingTypeController {
    @Autowired
    private SavingTypeService savingTypeService;

    @GetMapping
    public List<SavingType> getAll() {
        return savingTypeService.findAll();
    }
}
