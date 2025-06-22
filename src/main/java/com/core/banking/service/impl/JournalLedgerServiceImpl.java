package com.core.banking.service.impl;

import com.core.banking.dto.*;
import com.core.banking.entity.JournalLedger;
import com.core.banking.entity.JournalLedgerDetail;
import com.core.banking.entity.MChartOfAccount;
import com.core.banking.enums.JournalStatus;
import com.core.banking.enums.MutationType;
import com.core.banking.repository.JournalLedgerDetailRepository;
import com.core.banking.repository.JournalLedgerRepository;
import com.core.banking.repository.MChartOfAccountRepository;
import com.core.banking.repository.MSystemRepository;
import com.core.banking.service.JournalLedgerService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.core.banking.dto.JournalRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class JournalLedgerServiceImpl implements JournalLedgerService {

    @Autowired
    private JournalLedgerRepository journalLedgerRepository;
    @Autowired
    private JournalLedgerDetailRepository journalLedgerDetailRepository;
    @Autowired
    private MChartOfAccountRepository mChartOfAccountRepository;
    @Autowired
    private MSystemRepository mSystemRepository;

    @Override
    public String createJournalHeader(BigDecimal amount, String mutationType, String status,
                                      String description, String referenceNumber) { return "id"; }
    @Override
    public String createJournalDetail(String coaCode, BigDecimal amount, String mutationType,
                                      Long journalLedgerId) { return "id"; }

    public JournalResponse createJournal(JournalRequest request, UserMetaData userMetaData) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        List<JournalLedgerDetail> detailEntities = new ArrayList<>();

        long debitCount = request.getDetails().stream()
                .filter(d -> d.getMutationType().equalsIgnoreCase("DEBIT")).count();
        long creditCount = request.getDetails().stream()
                .filter(d -> d.getMutationType().equalsIgnoreCase("CREDIT")).count();
        if (debitCount == 0 || creditCount == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEBIT_CREDIT_PAIR_REQUIRED);
        }

        for (JournalDetailRequest dto : request.getDetails()) {
            MChartOfAccount coa = mChartOfAccountRepository.findById(dto.getCoaId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.COA_NOT_FOUND));

            BigDecimal amount = dto.getAmount();
            String mutationType = dto.getMutationType().toUpperCase();

            JournalLedgerDetail detail = JournalLedgerDetail.builder()
                    .coaId(coa.getId())
                    .coaCode(coa.getCode())
                    .mutationType(MutationType.valueOf(mutationType))
                    .description(dto.getDescription())
                    .createdAt(LocalDateTime.now())
                    .createdBy(userMetaData.getUserId())
                    .build();

            if (MutationType.DEBIT.name().equals(mutationType)) {
                totalDebit = totalDebit.add(amount);
                detail.setDebit(amount);
            } else if (MutationType.CREDIT.name().equals(mutationType)) {
                totalCredit = totalCredit.add(amount);
                detail.setCredit(amount);
            } else {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_MUTATION_TYPE);
            }

            detailEntities.add(detail);
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.UNBALANCED_DEBIT_CREDIT);
        }

        JournalLedger header = JournalLedger.builder()
                .journalCode(generateJournalCode())
                .referenceNumber(generateReferenceNumber())
                .referenceType(request.getReferenceType())
                .status(JournalStatus.POSTED)
                .description(request.getDescription())
                .systemDate(LocalDate.now())
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .isPosted(true)
                .createdAt(LocalDateTime.now())
                .createdBy(userMetaData.getUserId())
                .build();

        JournalLedger savedHeader = journalLedgerRepository.save(header);

        for (JournalLedgerDetail detail : detailEntities) {
            detail.setJournalLedger(savedHeader);
        }

        journalLedgerDetailRepository.saveAll(detailEntities);
        savedHeader.setDetails(detailEntities);

        return JournalResponse.builder()
                .journalId(savedHeader.getId())
                .journalCode(savedHeader.getJournalCode())
                .referenceNumber(savedHeader.getReferenceNumber())
                .status(savedHeader.getStatus().name())
                .description(savedHeader.getDescription())
                .systemDate(savedHeader.getSystemDate())
                .totalDebit(savedHeader.getTotalDebit())
                .totalCredit(savedHeader.getTotalCredit())
                .createdAt(savedHeader.getCreatedAt())
                .createdBy(savedHeader.getCreatedBy())
                .details(convertDetails(detailEntities))
                .message("Journal entry created successfully")
                .build();
    }

    private List<JournalDetailResponse> convertDetails(List<JournalLedgerDetail> details) {
        return details.stream()
                .map(detail -> JournalDetailResponse.builder()
                        .coaCode(detail.getCoaCode())
                        .mutationType(detail.getMutationType().name())
                        .debit(detail.getDebit())
                        .credit(detail.getCredit())
                        .description(detail.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    private String generateJournalCode() {
        String prefix = "JRN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.valueOf((int)(Math.random() * 10000));
        return prefix + "-" + String.format("%04d", Integer.parseInt(random));
    }
    private String generateReferenceNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "REF-" + datePart + "-TRX";
        long countToday = journalLedgerRepository.countBySystemDate(LocalDate.now());
        String sequence = String.format("%04d", countToday + 1);
        return prefix + sequence;
    }

    public LocalDate getSystemAt() {
        return mSystemRepository.findSystemAt();

    }
}
