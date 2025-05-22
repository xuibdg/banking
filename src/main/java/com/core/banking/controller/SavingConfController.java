package com.core.banking.controller;

import com.core.banking.dto.SavingConfRequest;
import com.core.banking.dto.SavingConfResponse;
import com.core.banking.service.impl.SavingConfServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saving-config")
@RequiredArgsConstructor
public class SavingConfController {

    private final SavingConfServiceImpl service;

    @PostMapping
    public ResponseEntity<SavingConfResponse> createOrUpdateConfiguration(@RequestBody @Valid SavingConfRequest request) {
        SavingConfResponse response = service.createOrUpdateConfiguration(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SavingConfResponse>> getAllConfigurations() {
        List<SavingConfResponse> responses = service.getAllConfigurations();
        return ResponseEntity.ok(responses);
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<SavingConfResponse> getConfigurationById(@PathVariable Long id) {
        SavingConfResponse response = service.getConfigurationById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping({"/{id}"})
    public ResponseEntity<SavingConfResponse> updateConfiguration(@PathVariable Long id, @RequestBody @Valid SavingConfRequest request) {
        SavingConfResponse response = this.service.updateConfiguration(id, request);
        return ResponseEntity.ok(response);
    }

}
