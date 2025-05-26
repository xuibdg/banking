package com.core.banking.controller;

import com.core.banking.dto.SavingTypeRequest;
import com.core.banking.entity.SavingType;
import com.core.banking.service.SavingTypeService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

@RestController
@RequestMapping("/api/saving-types")
public class SavingTypeController {

    @Autowired
    private SavingTypeService savingTypeService;

    @PostMapping
    public BaseResponse<SavingType> createSavingType(@RequestBody SavingTypeRequest savingType) {
        return buildSuccessResponse(savingTypeService.createSavingType(savingType));
    }

    @GetMapping
    public BaseResponse<List<SavingType>> getAllSavingTypes() {
        return buildSuccessResponse(savingTypeService.getAllSavingTypes());
    }

    @GetMapping("/{id}")
    public BaseResponse<SavingType> getSavingTypeById(@PathVariable String id) {
        Optional<SavingType> savingType = savingTypeService.getSavingTypeById(id);
        return buildSuccessResponse(savingType);
    }

    @PutMapping("/{id}")
    public BaseResponse<SavingType> updateSavingType(
            @PathVariable String id,
            @RequestBody SavingTypeRequest updatedSavingType) {
        return buildSuccessResponse(savingTypeService.updateSavingType(id, updatedSavingType));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteSavingType(@PathVariable String savingTypeId) {
        savingTypeService.deleteSavingType(savingTypeId);
        return buildSuccessResponse(ResponseEntity.noContent().build());
    }


}
