package com.core.banking.service;

import com.core.banking.dto.JournalRequest;
import com.core.banking.dto.JournalResponse;
import com.core.banking.dto.UserMetaData;

import java.math.BigDecimal;

public interface JournalLedgerService {
    JournalResponse createJournal(JournalRequest request, UserMetaData userMetaData);
    String createJournalHeader(BigDecimal amount, String mutationType, String status, String description, String referenceNumber);
    String createJournalDetail(String coaCode, BigDecimal amount, String mutationType, Long journalLedgerId);
    String generateNewTransactionReference();
}