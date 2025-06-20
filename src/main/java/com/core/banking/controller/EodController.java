package com.core.banking.controller;

import com.core.banking.service.EodReportingService;
import com.core.banking.utils.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

@RestController
@RequestMapping("api/closing")
@RequiredArgsConstructor
public class EodController {

    @Autowired
    private final EodReportingService eodReportingService;

    @PostMapping("add")
    BaseResponse<String> generateEod() {
        eodReportingService.generateEodReporting();
        return buildSuccessResponse("Tutup Buku Berhasil diproses");
    }
}
