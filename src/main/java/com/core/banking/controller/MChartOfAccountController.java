package com.core.banking.controller;

import com.core.banking.dto.MChartOfAccountRequest;
import com.core.banking.dto.MChartOfAccountResponse;
import com.core.banking.service.MChartOfAccountService;
import com.core.banking.utils.exception.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/m-chart-of-account")
public class MChartOfAccountController {
    @Autowired
    private MChartOfAccountService mChartOfAccountService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED) // Tetap gunakan @ResponseStatus untuk mengatur HTTP code header
    public BaseResponse<MChartOfAccountResponse> createCoa(@Valid @RequestBody MChartOfAccountRequest mChartOfAccountRequest) {
        MChartOfAccountResponse mChartOfAccountResponse = mChartOfAccountService.create(mChartOfAccountRequest);

        return BaseResponse.<MChartOfAccountResponse>builder()
                .status(HttpStatus.CREATED.value()) // 201
                .message("Chart of Account created successfully")
                .data(mChartOfAccountResponse)
                .build();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<List<MChartOfAccountResponse>> getAll() {
        List<MChartOfAccountResponse> mChartOfAccountResponses = mChartOfAccountService.getAll();

        return BaseResponse.<List<MChartOfAccountResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully retrieved all Charts of Account")
                .data(mChartOfAccountResponses)
                .build();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<MChartOfAccountResponse> getById(@PathVariable String id) {
        MChartOfAccountResponse coaResponse = mChartOfAccountService.getById(id);

        return BaseResponse.<MChartOfAccountResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully retrieved Chart of Account")
                .data(coaResponse)
                .build();
    }
}
