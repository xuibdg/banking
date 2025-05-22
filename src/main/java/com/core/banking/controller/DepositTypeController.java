package com.core.banking.controller;


import com.core.banking.entity.DepositType;
import com.core.banking.service.DepositTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/deposit-types")
public class DepositTypeController {
    @Autowired
    private DepositTypeService depositTypeService;

    @GetMapping
    public List<DepositType> getAll() {
        return depositTypeService.findAll();
    }
}
