package com.core.banking.controller;

import com.core.banking.dto.SavingConfRequest;
import com.core.banking.dto.SavingConfResponse;
import com.core.banking.dto.SavingTypeRequest;
import com.core.banking.dto.SavingTypeResponse;
import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.service.SavingTypeConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saving-type-configs")
@RequiredArgsConstructor
public class SavingTypeConfigController {
    @Autowired
    private SavingTypeConfigService savingTypeConfigService;

    @GetMapping
    public List<SavingTypeConfig> getAll() {
        return savingTypeConfigService.findAll();
    }

    @PostMapping
    public ResponseEntity<SavingTypeResponse> createOrUpdateConfiguration(@RequestBody @Valid SavingTypeRequest request) {
        SavingTypeResponse response = savingTypeConfigService.createOrUpdateConfiguration(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SavingTypeResponse>> getAllConfigurations() {
        List<SavingTypeResponse> responses = savingTypeConfigService.getAllConfigurations();
        return ResponseEntity.ok(responses);
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<SavingTypeResponse> getConfigurationById(@PathVariable Long id) {
        SavingTypeResponse response = savingTypeConfigService.getConfigurationById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping({"/{id}"})
    public ResponseEntity<SavingTypeResponse> updateConfiguration(@PathVariable Long id, @RequestBody @Valid SavingTypeRequest request) {
        SavingTypeResponse response = savingTypeConfigService.updateConfiguration(id, request);
        return ResponseEntity.ok(response);
    }
}
