package com.core.banking.controller;

import com.core.banking.dto.SavingTypeConfRequest;
import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.service.impl.SavingTypeConfigServiceImpl;
import com.core.banking.utils.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import java.util.Optional;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

@RestController
@RequestMapping("/api/saving-type-configs")
@RequiredArgsConstructor
public class SavingTypeConfigController {
    @Autowired
    private SavingTypeConfigServiceImpl service;

    @PostMapping
    public BaseResponse<SavingTypeConfig> createSavingTypeConfig(@RequestBody SavingTypeConfRequest savingTypeConfig) {
        return buildSuccessResponse(service.createSavingTypeConfig(savingTypeConfig));
    }

    @GetMapping("/get-all-data")
    public BaseResponse<List<SavingTypeConfig>> getAllConfigs() {
        return buildSuccessResponse(service.getAllConfigs());
    }

    @GetMapping("/{id}")
    public BaseResponse<SavingTypeConfig> getConfigById(@PathVariable String id) {
        Optional<SavingTypeConfig> config = service.getConfigById(id);
        return buildSuccessResponse(config);
    }

    @GetMapping("/by-saving-type/{savingTypeId}")
    public BaseResponse<List<SavingTypeConfig>> getConfigsBySavingTypeId(
            @PathVariable String savingTypeId) {
        return buildSuccessResponse(service.getConfigsBySavingTypeId(savingTypeId));
    }

    @GetMapping("/active")
    public BaseResponse<List<SavingTypeConfig>> getActiveConfigs() {
        return buildSuccessResponse(service.getActiveConfigs());
    }

    @PutMapping("/{id}")
    public BaseResponse<SavingTypeConfig> updateSavingTypeConfig(
            @PathVariable String id,
            @RequestBody SavingTypeConfRequest updatedConfig) {
        return buildSuccessResponse(service.updateSavingTypeConfig(id, updatedConfig));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteSavingTypeConfig(@PathVariable String id) {
        service.deleteSavingType(id);
        return buildSuccessResponse("SUCCESSFULLY DELETED");
    }
}
