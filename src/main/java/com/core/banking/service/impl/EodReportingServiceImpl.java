package com.core.banking.service.impl;

import com.core.banking.dto.EodReporting;
import com.core.banking.entity.BalanceSheet;
import com.core.banking.entity.MChartOfAccount;
import com.core.banking.entity.MSystem;
import com.core.banking.entity.ProfitLoss;
import com.core.banking.repository.*;
import com.core.banking.service.EodReportingService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EodReportingServiceImpl implements EodReportingService {

    private final JournalLedgerDetailRepository journalLedgerDetailRepository;
    private final BalanceSheetRepository balanceSheetRepository;
    private final ProfitLossRepository profitLossRepository;
    private final MChartOfAccountRepository mChartOfAccountRepository;
    private final MSystemRepository mSystemRepository;

    @Override
    @Transactional
    public void generateEodReporting() {
        MSystem mSystem = mSystemRepository.findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SYSTEM_RECORD_NOT_FOUND));

        LocalDate systemAt = mSystem.getSystemAt();
        boolean isEndOfMonth = systemAt.equals(systemAt.withDayOfMonth(systemAt.lengthOfMonth()));
        boolean isEndOfYear = systemAt.getMonth() == Month.DECEMBER && systemAt.getDayOfMonth() == 31;

        if (isEndOfYear) {
            processYearClosing(systemAt);
        } else if (isEndOfMonth) {
            processMonthlyClosing(systemAt);
        } else {
            processDailyClosing(systemAt);
        }

        mSystem.setSystemAt(systemAt.plusDays(1));
        mSystem.setUpdateAt(LocalDateTime.now());
        mSystemRepository.save(mSystem);
    }

    private void processDailyClosing(LocalDate systemAt) {
        LocalDate startDay = systemAt;
        LocalDate endDay = systemAt.plusDays(1);
        generateProfitLossAndBalanceSheet(startDay, endDay, systemAt);
    }

    private void processMonthlyClosing(LocalDate systemAt) {
        LocalDate startOfMonth = systemAt.withDayOfMonth(1);
        LocalDate endOfMonth = systemAt.plusDays(1);
        generateProfitLossAndBalanceSheet(startOfMonth, endOfMonth, systemAt);
    }

    private void processYearClosing(LocalDate systemAt) {
        LocalDate startOfYear = systemAt.withDayOfYear(1);
        LocalDate endOfYear = systemAt.plusDays(1);
        generateProfitLossAndBalanceSheet(startOfYear, endOfYear, systemAt);
    }

    private void generateProfitLossAndBalanceSheet(LocalDate startDate, LocalDate endDate, LocalDate systemAt) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atStartOfDay();
        List<EodReporting> balanceSheet = journalLedgerDetailRepository.findEodReport(
                start, end, List.of("ASSET", "LIABILITY", "EQUITY"));
        List<EodReporting> profitLoss = journalLedgerDetailRepository.findEodReport(
                start, end, List.of("REVENUE", "EXPENSE"));

        List<BalanceSheet> balanceSheetList = balanceSheet.stream()
                .map(dto -> {
                    MChartOfAccount coa = mChartOfAccountRepository.findByCode(dto.getCoaCode())
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.COA_MISSING));
                    return BalanceSheet.builder()
                            .systemAt(systemAt)
                            .amount(dto.getAmount())
                            .coaCode(coa)
                            .coaName(coa.getName())
                            .build();
                })
                .toList();
        balanceSheetRepository.saveAll(balanceSheetList);

        List<ProfitLoss> profitLossList = profitLoss.stream()
                .map(dto -> {
                    MChartOfAccount coa = mChartOfAccountRepository.findByCode(dto.getCoaCode())
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.COA_MISSING));
                    return ProfitLoss.builder()
                            .systemAt(systemAt)
                            .amount(dto.getAmount())
                            .coaCode(coa)
                            .coaName(coa.getName())
                            .build();

                })
                .toList();
        profitLossRepository.saveAll(profitLossList);
    }
}
