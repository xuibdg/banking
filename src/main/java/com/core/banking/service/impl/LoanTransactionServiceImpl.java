package com.core.banking.service.impl;

import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanTransactionRequest;
import com.core.banking.dto.LoanTransactionResponse;
import com.core.banking.entity.LoanAccount;
import com.core.banking.entity.LoanRepaymentSchedule;
import com.core.banking.entity.LoanTransaction;
import com.core.banking.enums.LoanAccountStatus;
import com.core.banking.enums.LoanRepaymentStatus;
import com.core.banking.enums.LoanTransactionType;
import com.core.banking.repository.LoanAccountRepository;
import com.core.banking.repository.LoanRepaymentScheduleRepository;
import com.core.banking.repository.LoanTransactionRepository;
import com.core.banking.service.LoanTransactionService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LoanTransactionServiceImpl implements LoanTransactionService {

    @Autowired
    private LoanTransactionRepository loanTransactionRepository;

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Autowired
    private LoanRepaymentScheduleRepository loanRepaymentScheduleRepository;

    @Override
    public String createLoanTransaction(LoanTransactionRequest request, UserMetaData userMetaData) {
        LoanAccount loanAccount = loanAccountRepository.findById(request.getLoanAccountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        LoanRepaymentSchedule repaymentSchedule = null;
        if (request.getLoanRepaymentScheduleId() != null) {
            repaymentSchedule = loanRepaymentScheduleRepository.findById(request.getLoanRepaymentScheduleId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));
        }

        LoanTransaction transaction = new LoanTransaction();
        transaction.setLoanAccount(loanAccount);
        transaction.setLoanRepaymentSchedule(repaymentSchedule);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(request.getAmount());
        transaction.setPrincipalComponent(request.getPrincipalComponent());
        transaction.setInterestComponent(request.getInterestComponent());
        transaction.setFeeComponent(request.getFeeComponent());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());
        transaction.setReferenceNumber(request.getReferenceNumber());
        transaction.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        loanTransactionRepository.save(transaction);

        return "Succes membuat transaksi loan";
    }

    @Override
    @Transactional
    public LoanTransactionResponse approveAndDIsburseLoan(String loanAccountId, UserMetaData userMetaData) {
        LoanAccount loanAccount = loanAccountRepository.findById(loanAccountId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        if (loanAccount.getAccountStatus() != LoanAccountStatus.PENDING_APPROVAL) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.NOT_PENDING_APPROVAL);
        }

        loanAccount.setAccountStatus(LoanAccountStatus.ACTIVE);
        loanAccount.setDisbursementDate(LocalDate.now());
        loanAccountRepository.save(loanAccount);

        BigDecimal fixedFee = new BigDecimal("10000");


        LoanTransaction transaction = new LoanTransaction();
        transaction.setLoanTransactionId(UUID.randomUUID().toString());
        transaction.setLoanAccount(loanAccount);
        transaction.setTransactionType(LoanTransactionType.DISBURSEMENT);
        transaction.setAmount(loanAccount.getPrincipalAmount());
        transaction.setPrincipalComponent(loanAccount.getPrincipalAmount());
        transaction.setInterestComponent(BigDecimal.ZERO);
        transaction.setFeeComponent(fixedFee);
        transaction.setDescription("Loan disbursed");
        transaction.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        transaction.setTransactionDate(Timestamp.valueOf(LocalDateTime.now()));
        loanTransactionRepository.save(transaction);

        BigDecimal totalPrincipal = loanAccount.getPrincipalAmount();
        int months = loanAccount.getDurationMonths();

        BigDecimal monthlyPrincipal = totalPrincipal
                .divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);

        BigDecimal monthlyInterest = loanAccount.getPrincipalAmount()
                .multiply(loanAccount.getInterestRateApplied())
                .divide(BigDecimal.valueOf(100 * 12), 2, RoundingMode.HALF_UP);

        LocalDate firstDueDate = loanAccount.getDisbursementDate().plusMonths(1);

        for (int i = 1; i <= months; i++) {
            LoanRepaymentSchedule schedule = new LoanRepaymentSchedule();
            schedule.setLoanRepaymentScheduleId(UUID.randomUUID().toString());
            schedule.setLoanAccount(loanAccount);
            schedule.setInstallmentNumber(i);
            schedule.setDueDate(firstDueDate.plusMonths(i - 1));
            schedule.setPrincipalDue(monthlyPrincipal);
            schedule.setPrincipalPaid(null);
            schedule.setInterestDue(monthlyInterest);
            schedule.setInterestPaid(null);
            schedule.setTotalDue(monthlyPrincipal.add(monthlyInterest));
            schedule.setPaymentStatus(LoanRepaymentStatus.PENDING);
            schedule.setLoanRepaymentScheduleId(schedule.getLoanRepaymentScheduleId());
            schedule.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            loanRepaymentScheduleRepository.save(schedule);
        }
        loanAccount.setOutstandingPrincipal(loanAccount.getPrincipalAmount());

        BigDecimal monthlyInstallment = monthlyPrincipal.add(monthlyInterest);
        loanAccount.setInstallmentAmount(monthlyInstallment);
        loanAccount.setFirstRepaymentDate(firstDueDate);
        loanAccount.setLastRepaymentDate(firstDueDate.plusMonths(months - 1));
        loanAccountRepository.save(loanAccount);

        return LoanTransactionResponse.builder()
                .loanAccountId(loanAccount.getLoanAccountId())
                .status(loanAccount.getAccountStatus())
                .installmentAmount(loanAccount.getInstallmentAmount())
                .firstRepaymentDate(loanAccount.getFirstRepaymentDate())
                .message("SUKSES DISBURED")
                .build();
    }

    @Override
    public List<LoanTransactionResponse> findAll() {
        return loanTransactionRepository.findAll().stream()
                .map(tx -> LoanTransactionResponse.builder()
                        .loanTransactionId(tx.getLoanTransactionId())
                        .loanAccountId(tx.getLoanAccount() != null ? tx.getLoanAccount().getLoanAccountId() : null)
                        .status(tx.getLoanAccount().getAccountStatus())
                        .installmentAmount(tx.getLoanAccount().getInstallmentAmount())
                        .firstRepaymentDate(tx.getLoanAccount().getFirstRepaymentDate())
                        .message("transaction")
                        .loanRepaymentScheduleId(tx.getLoanRepaymentSchedule() != null ? tx.getLoanRepaymentSchedule().getLoanRepaymentScheduleId() : null)
                        .transactionType(tx.getTransactionType())
                        .amount(tx.getAmount())
                        .principalComponent(tx.getPrincipalComponent())
                        .interestComponent(tx.getInterestComponent())
                        .feeComponent(tx.getFeeComponent())
                        .transactionDate(tx.getTransactionDate())
                        .description(tx.getDescription())
                        .referenceNumber(tx.getReferenceNumber())
                        .createdAt(tx.getCreatedAt())
                        .updatedAt(tx.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public String updateLoanTransaction(String loanTransactionId, LoanTransactionRequest request, UserMetaData userMetaData) {
        LoanTransaction transaction = loanTransactionRepository.findById(loanTransactionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        LoanAccount loanAccount = loanAccountRepository.findById(request.getLoanAccountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        LoanRepaymentSchedule repaymentSchedule = null;
        if (request.getLoanRepaymentScheduleId() != null) {
            repaymentSchedule = loanRepaymentScheduleRepository.findById(request.getLoanRepaymentScheduleId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));
        }

        transaction.setLoanAccount(loanAccount);
        transaction.setLoanRepaymentSchedule(repaymentSchedule);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(request.getAmount());
        transaction.setPrincipalComponent(request.getPrincipalComponent());
        transaction.setInterestComponent(request.getInterestComponent());
        transaction.setFeeComponent(request.getFeeComponent());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());
        transaction.setReferenceNumber(request.getReferenceNumber());
        transaction.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        loanTransactionRepository.save(transaction);

        return "Sukses update " + transaction.getLoanTransactionId();
    }

    @Override
    public String deleteLoanTransaction(String loanTransactionId, UserMetaData userMetaData) {
        LoanTransaction transaction = loanTransactionRepository.findById(loanTransactionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        transaction.setIsDeleted(true);
        return "Sukses delete loan transaction";
    }
}
