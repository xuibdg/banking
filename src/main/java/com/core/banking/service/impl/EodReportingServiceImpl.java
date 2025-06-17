package com.core.banking.service.impl;

import com.core.banking.dto.EodReporting;
import com.core.banking.entity.BalanceSheet;
import com.core.banking.entity.MChartOfAccount;
import com.core.banking.entity.ProfitLoss;
import com.core.banking.enums.ReportPeriodType;
import com.core.banking.repository.BalanceSheetRepository;
import com.core.banking.repository.JournalLedgerDetailRepository;
import com.core.banking.repository.MChartOfAccountRepository;
import com.core.banking.repository.ProfitLossRepository;
import com.core.banking.service.EodReportingService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EodReportingServiceImpl implements EodReportingService {

    private final JournalLedgerDetailRepository journalLedgerDetailRepository;
    private final BalanceSheetRepository balanceSheetRepository;
    private final ProfitLossRepository profitLossRepository;
    private final MChartOfAccountRepository mChartOfAccountRepository;

    @Override
    public void generateEodReporting(LocalDate systemAt) {
        LocalDateTime start = systemAt.atStartOfDay();
        LocalDateTime end = systemAt.plusDays(1).atStartOfDay();
        List<EodReporting> neraca = journalLedgerDetailRepository.findEodReport(
                start, end, List.of("ASSET", "LIABILITY", "EQUITY")
        );
        List<EodReporting> labaRugi = journalLedgerDetailRepository.findEodReport(
                start, end, List.of("REVENUE", "EXPENSE")
        );


        List<BalanceSheet> balanceSheetList = neraca.stream()
                .map(dto -> {
                    MChartOfAccount coa = mChartOfAccountRepository.findByCode(dto.getCoaCode())
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
                    return BalanceSheet.builder()
                            .systemAt(systemAt)
                            .amount(dto.getAmount())
                            .coaCode(coa)
                            .coaName(coa.getName())
                            .build();
                })
                .toList();
        balanceSheetRepository.saveAll(balanceSheetList);

        List<ProfitLoss> profitLossList = labaRugi.stream()
                .map(dto -> {
                    MChartOfAccount coa = mChartOfAccountRepository.findByCode(dto.getCoaCode())
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
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


//    public record DateRange(LocalDateTime start, LocalDateTime end) {}
//
//    private DateRange getDateRange(LocalDate baseDate, ReportPeriodType periodType) {
//        switch (periodType) {
//            case DAILY:
//                return new DateRange(baseDate.atStartOfDay(), baseDate.plusDays(1).atStartOfDay());
//            case WEEKLY:
//                LocalDate weekStart = baseDate.with(DayOfWeek.MONDAY);
//                LocalDate weekEnd = weekStart.plusDays(7);
//                return new DateRange(weekStart.atStartOfDay(), weekEnd.atStartOfDay());
//            case MONTHLY:
//        }
//    }
}
