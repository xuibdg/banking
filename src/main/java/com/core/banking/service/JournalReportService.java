package com.core.banking.service;

import com.core.banking.dto.JournalReportDto;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface JournalReportService {
    List<JournalReportDto> getJournalByReference(String referenceNumber);
}
