package com.core.banking.service.impl;

import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanRepaymentScheduleRequest;
import com.core.banking.dto.LoanRepaymentScheduleResponse;
import com.core.banking.entity.LoanAccount;
import com.core.banking.entity.LoanRepaymentSchedule;
import com.core.banking.entity.LoanTransaction;
import com.core.banking.enums.LoanAccountStatus;
import com.core.banking.enums.LoanRepaymentStatus;
import com.core.banking.enums.LoanTransactionType;
import com.core.banking.repository.LoanAccountRepository;
import com.core.banking.repository.LoanRepaymentScheduleRepository;
import com.core.banking.repository.LoanTransactionRepository;
import com.core.banking.service.LoanRepaymentScheduleService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
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
    public String createLoanRepaymentSchedule(LoanRepaymentScheduleRequest request, UserMetaData userMetaData) {
        LoanAccount loanAccount = loanAccountRepository.findById(valueOf(request.getLoanAccountId()))
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

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
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        LoanRepaymentSchedule repaymentSchedule = loanRepaymentScheduleRepository
                .findByLoanAccount_LoanAccountIdAndInstallmentNumber(request.getLoanAccountId(), request.getInstallmentNumber())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        if (repaymentSchedule.getPaymentStatus() != LoanRepaymentStatus.PENDING){
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.NOT_PENDING);
        }

        if (loanAccount.getAccountStatus() != LoanAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.NOT_ACTIVE);
        }

        BigDecimal paymentAmount = request.getAmountPaid();
        if (paymentAmount.compareTo(loanAccount.getInstallmentAmount()) != 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.AMOUNT_NOT_ENOUGH);
        }

        BigDecimal expectedInterest = repaymentSchedule.getInterestDue();
        BigDecimal expectedPrincipal = repaymentSchedule.getPrincipalDue();

        BigDecimal interestPaid = expectedInterest;
        BigDecimal principalPaid = expectedPrincipal;

        LoanTransaction payment = new LoanTransaction();
        payment.setLoanTransactionId(UUID.randomUUID().toString());
        payment.setLoanAccount(loanAccount);
        payment.setTransactionType(LoanTransactionType.REPAYMENT);
        payment.setAmount(paymentAmount);
        payment.setInterestComponent(interestPaid);
        payment.setPrincipalComponent(principalPaid);
        payment.setFeeComponent(BigDecimal.ZERO);
        payment.setReferenceNumber(generateReferenceNumber());
        payment.setTransactionDate(request.getPaymentDate() != null ? request.getPaymentDate() : Timestamp.valueOf(LocalDateTime.now()));
        payment.setDescription("Loan repayment");
        payment.setLoanRepaymentSchedule(repaymentSchedule);
        payment.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        loanTransactionRepository.save(payment);

        repaymentSchedule.setPaymentStatus(LoanRepaymentStatus.PAID);
        repaymentSchedule.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
        repaymentSchedule.setAmountPaid(paymentAmount);
        repaymentSchedule.setPrincipalPaid(principalPaid);
        repaymentSchedule.setInterestPaid(interestPaid);
        loanRepaymentScheduleRepository.save(repaymentSchedule);

        BigDecimal newOutstanding = loanAccount.getOutstandingPrincipal().subtract(principalPaid);
        loanAccount.setOutstandingPrincipal(newOutstanding.max(BigDecimal.ZERO));

        if (newOutstanding.compareTo(BigDecimal.ZERO) <= 0) {
            loanAccount.setAccountStatus(LoanAccountStatus.PAID_OFF);
            loanAccount.setClosedAt(Timestamp.valueOf(LocalDateTime.now()));
        }
        loanAccountRepository.save(loanAccount);

        return LoanRepaymentScheduleResponse.builder()
                .loanAccountId(loanAccount.getLoanAccountId())
                .paymentAmount(paymentAmount)
                .principalPaid(principalPaid)
                .interestPaid(interestPaid)
                .status(loanAccount.getAccountStatus().name())
                .message("Repayment recorded successfully")
                .build();
    }

    private String generateReferenceNumber() {
        String datePart = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomPart = new Random().nextInt(9000) + 1000; // 1000 - 9999
        return "LOAN-" + datePart + "-" + randomPart;
    }


    @Override
    public String updateLoanRepaymentSchedule(String loanRepaymentScheduleId, LoanRepaymentScheduleRequest request, UserMetaData userMetaData) {
        LoanRepaymentSchedule schedule = loanRepaymentScheduleRepository.findById(loanRepaymentScheduleId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        if (request.getLoanAccountId() != null) {
            LoanAccount loanAccount = loanAccountRepository.findById(valueOf(request.getLoanAccountId()))
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));
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
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        schedule.setIsDeleted(true);
        loanRepaymentScheduleRepository.save(schedule);
        return "Deleted loan repayment schedule with id: " + loanRepaymentScheduleId;
    }
}
