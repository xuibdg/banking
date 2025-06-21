package com.core.banking.service.impl;

import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanTransactionRequest;
import com.core.banking.dto.LoanTransactionResponse;
import com.core.banking.entity.EscrowAccount;
import com.core.banking.entity.EscrowAccountDetail;
import com.core.banking.entity.LoanAccount;
import com.core.banking.entity.LoanRepaymentSchedule;
import com.core.banking.entity.LoanTransaction;
import com.core.banking.entity.SavingAccount;
import com.core.banking.entity.SavingAccountDetail;
import com.core.banking.enums.EscrowAccountStatus;
import com.core.banking.enums.EscrowTransactionType;
import com.core.banking.enums.LoanAccountStatus;
import com.core.banking.enums.LoanRepaymentStatus;
import com.core.banking.enums.LoanTransactionType;
import com.core.banking.enums.MutationType;
import com.core.banking.enums.SavingTransactionType;
import com.core.banking.repository.EscrowAccountDetailRepository;
import com.core.banking.repository.EscrowAccountRepository;
import com.core.banking.repository.LoanAccountRepository;
import com.core.banking.repository.LoanRepaymentScheduleRepository;
import com.core.banking.repository.LoanTransactionRepository;
import com.core.banking.repository.SavingAccountDetailRepository;
import com.core.banking.repository.SavingAccountRepository;
import com.core.banking.service.LoanTransactionService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.core.banking.dto.JournalDetailRequest;
import com.core.banking.dto.JournalRequest;
import com.core.banking.entity.MChartOfAccount;
import com.core.banking.repository.MChartOfAccountRepository;
import com.core.banking.service.JournalLedgerService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    private JournalLedgerService journalLedgerService;

    @Autowired
    private MChartOfAccountRepository mChartOfAccountRepository;

    @Autowired
    private JournalLedgerServiceImpl journalLedgerServiceImpl;

    private static final String COA_PIUTANG_PEMBIAYAAN = "1201";
    private static final String COA_TABUNGAN_NASABAH = "2001";
    private static final String COA_PENDAPATAN_BUNGA_LOAN = "4001";
    private static final String COA_PENDAPATAN_BIAYA_ADMIN = "4101";

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
        transaction.setSystemDate(journalLedgerServiceImpl.getSystemAt());
        loanTransactionRepository.save(transaction);
        BigDecimal disbursementAmount = request.getAmount();
        String journalId = createDisbursementJournal(loanAccount, disbursementAmount, userMetaData);

        return "Succes membuat transaksi loan dengan Journal ID: " + journalId;
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
        loanAccount.setSystemDate(journalLedgerServiceImpl.getSystemAt());
        loanAccountRepository.save(loanAccount);

        BigDecimal disbursementAmount = loanAccount.getPrincipalAmount();
        BigDecimal fixedFee = new BigDecimal("10000");

        List<SavingAccount> savingAccounts = savingAccountRepository.findByCustomer_Id(loanAccount.getCustomer().getId());
        if (savingAccounts.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_ALREADY_EXIST);
        }
        SavingAccount savingAccount = savingAccounts.get(0);


//        EscrowAccount escrowAccount = escrowAccountRepository.findByPayerCustomer_Id(loanAccount.getCustomer().getId())
//                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ESCROW_ACCOUNT_NOT_FOUND));

        EscrowAccount escrowAccount = escrowAccountRepository.findByLoanAccount_LoanAccountId(loanAccountId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ESCROW_ACCOUNT_NOT_FOUND));

        // Validasi status harus PENDING_FUNDING
        if (escrowAccount.getAccountStatus() != EscrowAccountStatus.PENDING_FUNDING) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Escrow account status must be PENDING_FUNDING");
        }

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
//                .releaseAccountNumber(savingAccount.getAccountNumber())
                .transactionAt(Timestamp.valueOf(LocalDateTime.now()))
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
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
//                .releaseAccountNumber(savingAccount.getAccountNumber())
                .transactionAt(Timestamp.valueOf(LocalDateTime.now()))
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
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
        transaction.setSystemDate(journalLedgerServiceImpl.getSystemAt());
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
        String journalId = createDisbursementJournal(loanAccount, disbursementAmount, userMetaData);
        System.out.println("Journal entry created with ID: " + journalId);

        return LoanTransactionResponse.builder()
                .loanAccountId(loanAccount.getLoanAccountId())
                .status(loanAccount.getAccountStatus())
                .installmentAmount(loanAccount.getInstallmentAmount())
                .firstRepaymentDate(loanAccount.getFirstRepaymentDate())
                .message("loan sukses aprove dan pencairan ke saving account. Journal ID: " + journalId)
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

    private String createDisbursementJournal(LoanAccount loanAccount, BigDecimal amount, UserMetaData userMetaData) {
        List<JournalDetailRequest> details = new ArrayList<>();

        details.add(new JournalDetailRequest());
        details.get(0).setCoaId(getCoaId(COA_PIUTANG_PEMBIAYAAN));
        details.get(0).setMutationType("DEBIT");
        details.get(0).setAmount(amount);
        details.get(0).setDescription("Pencairan pinjaman - " + loanAccount.getAccountNumber());

        details.add(new JournalDetailRequest());
        details.get(1).setCoaId(getCoaId(COA_TABUNGAN_NASABAH));
        details.get(1).setMutationType("CREDIT");
        details.get(1).setAmount(amount);
        details.get(1).setDescription("Pencairan pinjaman ke tabungan - " + loanAccount.getAccountNumber());

        JournalRequest journalRequest = new JournalRequest();
        journalRequest.setDescription("Pencairan Pinjaman - " + loanAccount.getAccountNumber());
        journalRequest.setReferenceType("LOAN_DISBURSEMENT");
        journalRequest.setDetails(details);

        return journalLedgerService.createJournal(journalRequest, userMetaData).getJournalId();
    }

    private String getCoaId(String coaCode) {
        return mChartOfAccountRepository.findByCode(coaCode)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.COA_NOT_FOUND))
                .getId();
    }

}
