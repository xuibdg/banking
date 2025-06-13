package com.core.banking.service.impl;

import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanTransactionRequest;
import com.core.banking.dto.LoanTransactionResponse;
import com.core.banking.entity.*;
import com.core.banking.enums.*;
import com.core.banking.repository.*;
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
import java.time.Instant;
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

    @Autowired
    private SavingAccountRepository savingAccountRepository;

    @Autowired
    private SavingAccountDetailRepository savingAccountDetailRepository;

    @Autowired
    private EscrowAccountDetailRepository escrowAccountDetailRepository;

    @Autowired
    private EscrowAccountRepository escrowAccountRepository;

    @Autowired
    private EscrowAccountDetailServiceImpl escrowAccountDetailServiceImpl;

    @Override
    public String createLoanTransaction(LoanTransactionRequest request, UserMetaData userMetaData) {
        LoanAccount loanAccount = loanAccountRepository.findById(request.getLoanAccountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_ACCOUNT_NOT_FOUND));

        LoanRepaymentSchedule repaymentSchedule = null;
        if (request.getLoanRepaymentScheduleId() != null) {
            repaymentSchedule = loanRepaymentScheduleRepository.findById(request.getLoanRepaymentScheduleId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_REPAYMENT_NOT_FOUND));
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
    public LoanTransactionResponse approveAndDisburseLoan(String loanAccountId, UserMetaData userMetaData) {
        LoanAccount loanAccount = loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_ACCOUNT_NOT_FOUND));

        if (loanAccount.getAccountStatus() != LoanAccountStatus.PENDING_APPROVAL) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.NOT_PENDING_APPROVAL);
        }

        loanAccount.setAccountStatus(LoanAccountStatus.ACTIVE);
        loanAccount.setDisbursementDate(LocalDate.now());
        loanAccountRepository.save(loanAccount);

        BigDecimal disbursementAmount = loanAccount.getPrincipalAmount();
        BigDecimal fixedFee = new BigDecimal("10000");

        List<SavingAccount> savingAccounts = savingAccountRepository.findByCustomer_Id(loanAccount.getCustomer().getId());
        if (savingAccounts.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_ALREADY_EXIST);
        }
        SavingAccount savingAccount = savingAccounts.get(0);


        EscrowAccount escrowAccount = escrowAccountRepository.findByPayerCustomer_Id(loanAccount.getCustomer().getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ESCROW_ACCOUNT_NOT_FOUND));

        escrowAccount.setAccountStatus(EscrowAccountStatus.RELEASED);

        BigDecimal escrowBegin = escrowAccount.getCurrentBalance();
        BigDecimal escrowEnd = escrowBegin.subtract(disbursementAmount);

        EscrowAccountDetail escrowDetail = EscrowAccountDetail.builder()
                .escrowAccount(escrowAccount)
                .transactionType(EscrowTransactionType.RELEASE_TO_BENEFICIARY)
                .mutationType(MutationType.CREDIT)
                .nominalTransaction(disbursementAmount)
                .beginBalance(escrowBegin)
                .endBalance(escrowEnd)
                .description("Loan disbursed ke saving account")
                .transactionReference(escrowAccountDetailServiceImpl.generateTrxCode())
                .releaseAccountNumber(savingAccount.getAccountNumber())
                .transactionAt(new Timestamp(System.currentTimeMillis()))
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .createBy(userMetaData.getUsername())
                .isDeleted(false)
                .build();

        escrowAccount.setCurrentBalance(escrowEnd);
        escrowAccountRepository.save(escrowAccount);
        escrowAccountDetailRepository.save(escrowDetail);

        EscrowAccountDetail escrowDetailRelease = EscrowAccountDetail.builder()
                .escrowAccount(escrowAccount)
                .transactionType(EscrowTransactionType.RELEASE_TO_BENEFICIARY)
                .mutationType(MutationType.DEBIT)
                .nominalTransaction(disbursementAmount)
                .beginBalance(disbursementAmount)
                .endBalance(BigDecimal.ZERO)
                .description(escrowDetail.getDescription())
                .transactionReference(escrowDetail.getTransactionReference())
                .releaseAccountNumber(savingAccount.getAccountNumber())
                .transactionAt(Timestamp.from(Instant.now()))
                .createdAt(Timestamp.from(Instant.now()))
                .createBy(userMetaData.getUserId())
                .isDeleted(false)
                .build();
        escrowAccountDetailRepository.save(escrowDetailRelease);

        LoanTransaction transaction = new LoanTransaction();
        transaction.setLoanTransactionId(UUID.randomUUID().toString());
        transaction.setLoanAccount(loanAccount);
        transaction.setTransactionType(LoanTransactionType.DISBURSEMENT);
        transaction.setAmount(disbursementAmount);
        transaction.setPrincipalComponent(disbursementAmount);
        transaction.setInterestComponent(BigDecimal.ZERO);
        transaction.setFeeComponent(fixedFee);

        transaction.setReferenceNumber(escrowDetail.getTransactionReference());

        transaction.setDescription("Loan disbursed");
        transaction.setTransactionDate(Timestamp.valueOf(LocalDateTime.now()));
        transaction.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        loanTransactionRepository.save(transaction);

        BigDecimal savingBegin = savingAccount.getCurrentBalance();
        BigDecimal savingEnd = savingBegin.add(disbursementAmount);

        SavingAccountDetail savingDetail = SavingAccountDetail.builder()
                .savingAccount(savingAccount)
                .transactionType(SavingTransactionType.DEPOSIT)
                .mutationType(MutationType.CREDIT)
                .nominalTransaction(disbursementAmount)
                .transactionReference(escrowDetail.getTransactionReference())
                .beginBalance(savingBegin)
                .endBalance(savingEnd)
                .description("Loan disbursed dari escrow")
                .channel("SYSTEM")
                .transactionAt(Timestamp.valueOf(LocalDateTime.now()))
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        savingAccount.setCurrentBalance(savingEnd);
        savingAccount.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        savingAccountRepository.save(savingAccount);
        savingAccountDetailRepository.save(savingDetail);

        BigDecimal monthlyPrincipal = disbursementAmount
                .divide(BigDecimal.valueOf(loanAccount.getDurationMonths()), 2, RoundingMode.HALF_UP);

        BigDecimal monthlyInterest = disbursementAmount
                .multiply(loanAccount.getInterestRateApplied())
                .divide(BigDecimal.valueOf(100 * 12), 2, RoundingMode.HALF_UP);

        LocalDate firstDueDate = loanAccount.getDisbursementDate().plusMonths(1);

        for (int i = 1; i <= loanAccount.getDurationMonths(); i++) {
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
            schedule.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            loanRepaymentScheduleRepository.save(schedule);
        }

        BigDecimal totalInterest = monthlyInterest.multiply(BigDecimal.valueOf(loanAccount.getDurationMonths()));
        BigDecimal totalOutstanding = disbursementAmount.add(totalInterest);

        loanAccount.setOutstandingPrincipal(totalOutstanding);
        loanAccount.setInstallmentAmount(monthlyPrincipal.add(monthlyInterest));
        loanAccount.setFirstRepaymentDate(firstDueDate);
        loanAccount.setLastRepaymentDate(firstDueDate.plusMonths(loanAccount.getDurationMonths() - 1));
        loanAccountRepository.save(loanAccount);

        return LoanTransactionResponse.builder()
                .loanAccountId(loanAccount.getLoanAccountId())
                .status(loanAccount.getAccountStatus())
                .installmentAmount(loanAccount.getInstallmentAmount())
                .firstRepaymentDate(loanAccount.getFirstRepaymentDate())
                .message("loan sukses aprove dan pencairan ke saving account.")
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
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_TRANSACITON_NOT_FOUND));

        LoanAccount loanAccount = loanAccountRepository.findById(request.getLoanAccountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_ACCOUNT_NOT_FOUND));

        LoanRepaymentSchedule repaymentSchedule = null;
        if (request.getLoanRepaymentScheduleId() != null) {
            repaymentSchedule = loanRepaymentScheduleRepository.findById(request.getLoanRepaymentScheduleId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_REPAYMENT_NOT_FOUND));
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
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_TRANSACITON_NOT_FOUND));

        transaction.setIsDeleted(true);
        return "Sukses delete loan transaction";
    }

}
