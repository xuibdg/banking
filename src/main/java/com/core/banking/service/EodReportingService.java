package com.core.banking.service;

import java.time.LocalDate;

public interface EodReportingService {
    void generateEodReporting (LocalDate systemAt);
}
