package com.core.banking.controller;

import com.core.banking.entity.DepositoType;
import com.core.banking.service.DepositoTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/deposito-types")
public class DepositoTypeController {
    @Autowired
    private DepositoTypeService depositoTypeService;

    @GetMapping
    public List<DepositoType> getAll() {
        return depositoTypeService.findAll();
    }
}
