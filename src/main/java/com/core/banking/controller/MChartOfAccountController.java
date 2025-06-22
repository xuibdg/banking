package com.core.banking.controller;

import com.core.banking.dto.MChartOfAccountRequest;
import com.core.banking.dto.MChartOfAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.service.MChartOfAccountService;
import com.core.banking.utils.exception.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

@RestController
@RequestMapping("/api/m-chart-of-account")
public class MChartOfAccountController {
    @Autowired
    private MChartOfAccountService mChartOfAccountService;

    @PostMapping("/create")
    public BaseResponse<MChartOfAccountResponse> createCoa(@Valid @RequestBody MChartOfAccountRequest mChartOfAccountRequest, UserMetaData userMetaData) {
        return buildSuccessResponse(mChartOfAccountService.create(mChartOfAccountRequest, userMetaData));
    }

    @GetMapping
    public BaseResponse<List<MChartOfAccountResponse>> getAll() {
        return buildSuccessResponse(mChartOfAccountService.getAll());
    }

    @GetMapping("/{id}")
    public BaseResponse<MChartOfAccountResponse> getById(@PathVariable String id, UserMetaData userMetaData) {
        return buildSuccessResponse(mChartOfAccountService.getById(id, userMetaData));
    }

    @PutMapping("/update/{id}")
    public BaseResponse<MChartOfAccountResponse> update(@RequestBody MChartOfAccountRequest mChartOfAccountRequest, @PathVariable String id, UserMetaData userMetaData){
        return buildSuccessResponse(mChartOfAccountService.update(mChartOfAccountRequest, id, userMetaData));
    }
}
