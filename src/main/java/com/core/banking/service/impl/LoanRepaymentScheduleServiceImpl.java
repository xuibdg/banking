package com.core.banking.service.impl;

import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanRepaymentScheduleRequest;
import com.core.banking.dto.LoanRepaymentScheduleResponse;
import com.core.banking.entity.EscrowAccount;
import com.core.banking.entity.EscrowAccountDetail;
import com.core.banking.entity.LoanAccount;
import com.core.banking.entity.LoanRepaymentSchedule;
import com.core.banking.entity.LoanTransaction;
import com.core.banking.entity.LoanTypeConfig;
import com.core.banking.entity.SavingAccount;
import com.core.banking.entity.SavingAccountDetail;
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
import com.core.banking.repository.LoanTypeConfigRepository;
import com.core.banking.repository.SavingAccountDetailRepository;
import com.core.banking.repository.SavingAccountRepository;
import com.core.banking.service.LoanRepaymentScheduleService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static java.lang.String.valueOf;

@Service
public class LoanRepaymentScheduleServiceImpl implements LoanRepaymentScheduleService {

    @Autowired
    private LoanRepaymentScheduleRepository loanRepaymentScheduleRepository;

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Autowired
    private LoanTransactionRepository loanTransactionRepository;

    @Autowired
    private SavingAccountRepository savingAccountRepository;

    @Autowired
    private SavingAccountDetailRepository savingAccountDetailRepository;

    @Autowired
    private EscrowAccountRepository escrowAccountRepository;

    @Autowired
    private EscrowAccountDetailRepository escrowAccountDetailRepository;

    @Autowired
    private EscrowAccountDetailServiceImpl escrowAccountDetailServiceImpl;

    @Override
    public List<LoanRepaymentScheduleResponse> findAll() {
        List<LoanRepaymentSchedule> schedules = loanRepaymentScheduleRepository.findAll();
        return schedules.stream()
                .map(schedule -> LoanRepaymentScheduleResponse.builder()
                        .loanAccountId(schedule.getLoanAccount().getLoanAccountId())
                        .installmentNumber(schedule.getInstallmentNumber())
                        .dueDate(schedule.getDueDate())
                        .principalDue(schedule.getPrincipalDue())
                        .interestDue(schedule.getInterestDue())
                        .amountPaid(schedule.getAmountPaid())
                        .principalPaid(schedule.getPrincipalPaid())
                        .interestPaid(schedule.getInterestPaid())
                        .paymentDate(schedule.getPaymentDate())
                        .paymentStatus(schedule.getPaymentStatus())
                        .paymentAmount(schedule.getAmountPaid())
                        .build())
                .toList();
    }

    @Override
    public List<LoanRepaymentScheduleResponse> findById(String customerId) {
        List<LoanAccount> accounts = loanAccountRepository.findByCustomerId(customerId);

        if (accounts.isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.ID_CUSTOMER_NOT_FOUND);
        }

        List<LoanRepaymentSchedule> schedules = loanRepaymentScheduleRepository.findByLoanAccountIn(accounts);

        return schedules.stream()
                .map(schedule -> LoanRepaymentScheduleResponse.builder()
                        .loanAccountId(schedule.getLoanAccount().getLoanAccountId())
                        .installmentNumber(schedule.getInstallmentNumber())
                        .dueDate(schedule.getDueDate())
                        .principalDue(schedule.getPrincipalDue())
                        .interestDue(schedule.getInterestDue())
                        .amountPaid(schedule.getAmountPaid())
                        .principalPaid(schedule.getPrincipalPaid())
                        .interestPaid(schedule.getInterestPaid())
                        .paymentDate(schedule.getPaymentDate())
                        .paymentStatus(schedule.getPaymentStatus())
                        .paymentAmount(schedule.getAmountPaid())
                        .build())
                .toList();
    }


    @Override
    public String createLoanRepaymentSchedule(LoanRepaymentScheduleRequest request, UserMetaData userMetaData) {
        LoanAccount loanAccount = loanAccountRepository.findById(valueOf(request.getLoanAccountId()))
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_ACCOUNT_NOT_FOUND));

        LoanRepaymentSchedule schedule = new LoanRepaymentSchedule();
        schedule.setLoanAccount(loanAccount);
        schedule.setInstallmentNumber(request.getInstallmentNumber());
        schedule.setDueDate(request.getDueDate());
        schedule.setPrincipalDue(request.getPrincipalDue());
        schedule.setInterestDue(request.getInterestDue());

        BigDecimal totalDue = request.getPrincipalDue().add(request.getInterestDue());
        schedule.setTotalDue(totalDue);

        schedule.setPrincipalPaid(request.getPrincipalPaid() != null ? request.getPrincipalPaid() : BigDecimal.ZERO);
        schedule.setInterestPaid(request.getInterestPaid() != null ? request.getInterestPaid() : BigDecimal.ZERO);
        schedule.setAmountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : BigDecimal.ZERO);
        schedule.setPaymentDate(request.getPaymentDate());
        schedule.setPaymentStatus(LoanRepaymentStatus.PENDING);
        schedule.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        loanRepaymentScheduleRepository.save(schedule);
        return schedule.getLoanRepaymentScheduleId();
    }

    @Override
    @Transactional
    public LoanRepaymentScheduleResponse loanRepayment(LoanRepaymentScheduleRequest request, UserMetaData userMetaData) {
        LoanAccount loanAccount = loanAccountRepository.findById(request.getLoanAccountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_ACCOUNT_NOT_FOUND));

        LoanRepaymentSchedule repaymentSchedule = loanRepaymentScheduleRepository
                .findByLoanAccount_LoanAccountIdAndInstallmentNumber(request.getLoanAccountId(), request.getInstallmentNumber())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_REPAYMENT_NOT_FOUND));

        if (repaymentSchedule.getPaymentStatus() != LoanRepaymentStatus.PENDING){
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.NOT_PENDING);
        }

        if (loanAccount.getAccountStatus() != LoanAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.NOT_ACTIVE);
        }

        BigDecimal fixedFee = new BigDecimal("2000");

        Timestamp paymentDate = request.getPaymentDate() != null
                ? request.getPaymentDate()
                : Timestamp.valueOf(LocalDateTime.now());

        BigDecimal lateFee = BigDecimal.ZERO;

        if (repaymentSchedule.getDueDate() != null) {
            LocalDate dueDate = repaymentSchedule.getDueDate();
            LocalDate paidDate = paymentDate.toLocalDateTime().toLocalDate();

            if (paidDate.isAfter(dueDate)) {
                long daysLate = ChronoUnit.DAYS.between(dueDate, paidDate);

                LoanTypeConfig loanTypeConfig = repaymentSchedule.getLoanAccount().getLoanTypeConfig();
                if (loanTypeConfig == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_CONFIG_NOT_FOUND); // Buat error mapping ini jika belum ada
                }

                BigDecimal dailyLateFee = loanTypeConfig.getLatePaymentFee();
                if (dailyLateFee == null) {
                    dailyLateFee = BigDecimal.ZERO;
                }

                lateFee = dailyLateFee.multiply(BigDecimal.valueOf(daysLate));
            }
        }

        BigDecimal expectedInterest = repaymentSchedule.getInterestDue();
        BigDecimal expectedPrincipal = repaymentSchedule.getPrincipalDue();
        BigDecimal totalDue = expectedPrincipal.add(expectedInterest).add(fixedFee).add(lateFee);
        BigDecimal paymentAmount = totalDue;

        SavingAccount savingAccount = savingAccountRepository
                .findByCustomerId(loanAccount.getCustomer().getId());

        if (savingAccount == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND);
        }

        if (savingAccount.getCurrentBalance().compareTo(paymentAmount) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_ENOUGH);
        }

        BigDecimal beginBalance = savingAccount.getCurrentBalance();
        BigDecimal endBalance = beginBalance.subtract(paymentAmount);

        savingAccount.setCurrentBalance(endBalance);
        savingAccount.setLastTransactionAt(Timestamp.valueOf(LocalDateTime.now()));
        savingAccountRepository.save(savingAccount);

        SavingAccountDetail savingAccountDetail = SavingAccountDetail.builder()
                .savingAccount(savingAccount)
                .transactionType(SavingTransactionType.FEE_DEBIT)
                .mutationType(MutationType.DEBIT)
                .nominalTransaction(paymentAmount)
                .beginBalance(beginBalance)
                .endBalance(endBalance)
                .description("Pembayaran pinjaman dipotong dari rekening tabungan")
                .transactionReference(escrowAccountDetailServiceImpl.generateTrxCode())
                .transactionAt(Timestamp.valueOf(LocalDateTime.now()))
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .channel("SYSTEM")
                .build();

        savingAccountDetailRepository.save(savingAccountDetail);

        EscrowAccount escrowAccount = escrowAccountRepository
                .findByLoanAccount_LoanAccountId(loanAccount.getLoanAccountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ESCROW_ACCOUNT_NOT_FOUND));

        BigDecimal escrowBeginBalance = escrowAccount.getCurrentBalance();
        BigDecimal escrowEndBalance = escrowBeginBalance.subtract(expectedPrincipal);

        escrowAccount.setCurrentBalance(escrowEndBalance);
        escrowAccount.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        escrowAccountRepository.save(escrowAccount);

        EscrowAccountDetail escrowDetail = EscrowAccountDetail.builder()
                .escrowAccount(escrowAccount)
                .transactionType(EscrowTransactionType.FEE_DEBIT)
                .mutationType(MutationType.CREDIT)
                .nominalTransaction(expectedPrincipal)
                .beginBalance(escrowBeginBalance)
                .endBalance(escrowEndBalance)
                .description("masuk pembayaran pinjaman ke escrow")
                .transactionReference(escrowAccountDetailServiceImpl.generateTrxCode())
                .releaseAccountNumber(savingAccount.getAccountNumber())
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .transactionAt(Timestamp.valueOf(LocalDateTime.now()))
                .createBy(userMetaData.getUserId())
                .isDeleted(false)
                .build();

        escrowAccountDetailRepository.save(escrowDetail);

        LoanTransaction payment = new LoanTransaction();
        payment.setLoanTransactionId(UUID.randomUUID().toString());
        payment.setLoanAccount(loanAccount);
        payment.setTransactionType(LoanTransactionType.REPAYMENT);
        payment.setAmount(paymentAmount);
        payment.setInterestComponent(expectedInterest);
        payment.setPrincipalComponent(expectedPrincipal);
        payment.setFeeComponent(fixedFee);
        payment.setLatePaymentFeeComponent(lateFee);
        payment.setReferenceNumber(escrowDetail.getTransactionReference());
        payment.setTransactionDate(paymentDate);
        payment.setDescription("Loan repayment");
        payment.setLoanRepaymentSchedule(repaymentSchedule);
        payment.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        loanTransactionRepository.save(payment);

        repaymentSchedule.setPaymentStatus(LoanRepaymentStatus.PAID);
        repaymentSchedule.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
        repaymentSchedule.setAmountPaid(paymentAmount);
        repaymentSchedule.setPrincipalPaid(expectedPrincipal);
        repaymentSchedule.setInterestPaid(expectedInterest);
        loanRepaymentScheduleRepository.save(repaymentSchedule);

        BigDecimal newOutstanding = loanAccount.getOutstandingPrincipal().subtract(expectedPrincipal);
        loanAccount.setOutstandingPrincipal(newOutstanding.max(BigDecimal.ZERO));

        if (newOutstanding.compareTo(BigDecimal.ZERO) <= 0) {
            loanAccount.setAccountStatus(LoanAccountStatus.PAID_OFF);
            loanAccount.setClosedAt(Timestamp.valueOf(LocalDateTime.now()));
        }

        loanAccountRepository.save(loanAccount);

        return LoanRepaymentScheduleResponse.builder()
                .loanAccountId(loanAccount.getLoanAccountId())
                .paymentAmount(paymentAmount)
                .principalPaid(expectedPrincipal)
                .interestPaid(expectedInterest)
                .feePaid(fixedFee)
                .status(loanAccount.getAccountStatus().name())
                .message("Pembayaran per bulan sukses sebanyak :" + paymentAmount)
                .build();
    }

    @Override
    public String updateLoanRepaymentSchedule(String loanRepaymentScheduleId, LoanRepaymentScheduleRequest request, UserMetaData userMetaData) {
        LoanRepaymentSchedule schedule = loanRepaymentScheduleRepository.findById(loanRepaymentScheduleId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_REPAYMENT_NOT_FOUND));

        if (request.getLoanAccountId() != null) {
            LoanAccount loanAccount = loanAccountRepository.findById(valueOf(request.getLoanAccountId()))
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_ACCOUNT_NOT_FOUND));
            schedule.setLoanAccount(loanAccount);
        }

        schedule.setInstallmentNumber(request.getInstallmentNumber());
        schedule.setDueDate(request.getDueDate());
        schedule.setPrincipalDue(request.getPrincipalDue());
        schedule.setInterestDue(request.getInterestDue());
        schedule.setTotalDue(request.getTotalDue());
        schedule.setPrincipalPaid(request.getPrincipalPaid());
        schedule.setInterestPaid(request.getInterestPaid());
        schedule.setAmountPaid(request.getAmountPaid());
        schedule.setPaymentDate(request.getPaymentDate());

        if (request.getPaymentStatus() != null) {
            schedule.setPaymentStatus(request.getPaymentStatus());
        }

        schedule.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        loanRepaymentScheduleRepository.save(schedule);

        return schedule.getLoanRepaymentScheduleId();
    }

    @Override
    public String deleteLoanRepaymentSchedule(String loanRepaymentScheduleId, UserMetaData userMetaData) {
        LoanRepaymentSchedule schedule = loanRepaymentScheduleRepository.findById(loanRepaymentScheduleId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_REPAYMENT_NOT_FOUND));

        schedule.setIsDeleted(true);
        loanRepaymentScheduleRepository.save(schedule);
        return "Deleted loan repayment schedule with id: " + loanRepaymentScheduleId;
    }
}
