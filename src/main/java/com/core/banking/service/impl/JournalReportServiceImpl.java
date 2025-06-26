package com.core.banking.service.impl;

import com.core.banking.dto.JournalReportDto;
import com.core.banking.entity.JournalLedger;
import com.core.banking.entity.JournalLedgerDetail;
import com.core.banking.entity.MChartOfAccount;
import com.core.banking.repository.JournalLedgerDetailRepository;
import com.core.banking.repository.JournalLedgerRepository;
import com.core.banking.repository.MChartOfAccountRepository;
import com.core.banking.service.JournalReportService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalReportServiceImpl implements JournalReportService {

    @Autowired
    private JournalLedgerRepository tJournalLedgerRepository;

    @Autowired
    private JournalLedgerDetailRepository tJournalLedgerDetailRepository;

    @Autowired
    private MChartOfAccountRepository mChartOfAccountRepository;

    @Override
    public List<JournalReportDto> getJournalByReference(String referenceNumber) {
        List<JournalLedger> ledgers = tJournalLedgerRepository.findAllByReferenceNumber(referenceNumber);
        if (ledgers.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND);
        }

        return ledgers.stream()
                .flatMap(ledger -> {
                    List<JournalLedgerDetail> details = tJournalLedgerDetailRepository.findByJournalLedgerId(ledger.getId());
                    return details.stream().map(detail -> {
                        MChartOfAccount coa = mChartOfAccountRepository.findById(detail.getCoaId())
                                .orElseThrow();
                        return JournalReportDto.builder()
                                .journalCode(ledger.getJournalCode())
                                .referenceNumber(ledger.getReferenceNumber())
                                .systemDate(ledger.getSystemDate())
                                .coaCode(coa.getCode())
                                .coaName(coa.getName())
                                .debit(detail.getDebit())
                                .credit(detail.getCredit())
                                .description(detail.getDescription())
                                .transactionDate(detail.getCreatedAt())
                                .build();
                    });
                }).collect(Collectors.toList());
    }
}
