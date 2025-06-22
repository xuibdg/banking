package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.JournalDetailRequest;
import com.core.banking.dto.JournalRequest;
import com.core.banking.dto.JournalResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.service.JournalLedgerService;
import com.core.banking.utils.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;


@RestController
@RequestMapping("api/journals")
public class JournalController {

    @Autowired
    private JournalLedgerService journalLedgerService;

    @PostMapping
    public BaseResponse<JournalResponse> createJournal(
            @RequestBody @Validated JournalRequest request,
            @CurrentUser UserMetaData userMetaData) {

        return buildSuccessResponse(journalLedgerService.createJournal(request, userMetaData));
    }
}
