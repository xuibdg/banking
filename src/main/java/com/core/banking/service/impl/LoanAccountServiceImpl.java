package com.core.banking.service.impl;

import com.core.banking.dto.LoanAccountRequest;
import com.core.banking.entity.*;
import com.core.banking.enums.LoanAccountStatus;
import com.core.banking.enums.LoanRepaymentStatus;
import com.core.banking.enums.LoanTransactionType;
import com.core.banking.repository.*;
import com.core.banking.service.LoanAccountService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
@RequiredArgsConstructor
public class LoanAccountServiceImpl implements LoanAccountService {

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanTypeConfigRepository loanTypeConfigRepository;

    @Autowired
    private LoanTransactionRepository loanTransactionRepository;

    @Autowired
    private LoanRepaymentScheduleRepository loanRepaymentScheduleRepository;

    @Override
    public List<LoanAccount> findAll() {
        return loanAccountRepository.findAll();
    }

    @Override
    public String createLoanAccount(LoanAccountRequest request) {
        String customerId = request.getCustomerId();
        String loanTypeConfigId = request.getLoanTypeConfigId();
        BigDecimal nominal = request.getPrincipalAmount();
        Integer duration = request.getDurationMonths();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        if (!"ACTIVE".equalsIgnoreCase(customer.getCustomerStatus().toString())) {
            throw new RuntimeException("Customer tidak active");
        }

        LoanTypeConfig config = loanTypeConfigRepository.findById(loanTypeConfigId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        if (nominal == null || nominal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Nominal harus lebih dari 0");
        }

        if (duration == null || duration <= 0) {
            throw new RuntimeException("Durasi harus lebih dari 0");
        }

        if (nominal.compareTo(config.getMinLoanAmount()) < 0 || nominal.compareTo(config.getMaxLoanAmount()) > 0) {
            throw new RuntimeException("Nominal tidak sesuai batas yang diizinkan");
        }

        if (duration < config.getMinDurationMonths() || duration > config.getMaxDurationMonths()) {
            throw new RuntimeException("Durasi tidak sesuai batas yang diizinkan");
        }

        LoanAccount loanAccount = new LoanAccount();

        String prefix = "1291";
        int lengthRandomDigits = 6;
        StringBuilder sb = new StringBuilder(prefix);
        Random random = new Random();

        for (int i = 0; i < lengthRandomDigits; i++) {
            sb.append(random.nextInt(10));
        }
        String accountNumber = sb.toString();
        loanAccount.setAccountNumber(accountNumber);

        loanAccount.setLoanAccountId(UUID.randomUUID().toString());
        loanAccount.setCustomerId(customer);
        loanAccount.setLoanTypeConfig(config);
        loanAccount.setPrincipalAmount(nominal);
        loanAccount.setDurationMonths(duration);

        loanAccount.setInterestRateApplied(config.getInterestRatePa());

        loanAccount.setOutstandingPrincipal(nominal);
        loanAccount.setAccountStatus(LoanAccountStatus.PENDING_APPROVAL);
        loanAccount.setCreatedAt(OffsetDateTime.now());

        loanAccountRepository.save(loanAccount);

        return "Succes membuat loan account dengan ID : " + loanAccount.getLoanAccountId();
    }

    @Override
    @Transactional
    public String updateLoanAccount(String loanAccountId, LoanAccountRequest request) {
        LoanAccount loanAccount = loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new EntityNotFoundException("LoanAccount not found"));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        LoanTypeConfig config = loanTypeConfigRepository.findById(request.getLoanTypeConfigId())
                .orElseThrow(() -> new EntityNotFoundException("LoanTypeConfig not found"));

        loanAccount.setCustomerId(customer);
        loanAccount.setLoanTypeConfig(config);
        loanAccount.setAccountNumber(request.getAccountNumber());
        loanAccount.setPrincipalAmount(request.getPrincipalAmount());
        loanAccount.setInterestRateApplied(request.getInterestRateApplied());
        loanAccount.setDurationMonths(request.getDurationMonths());
        loanAccount.setOutstandingPrincipal(request.getOutstandingPrincipal());
        loanAccount.setInstallmentAmount(request.getInstallmentAmount());
        loanAccount.setDisbursementDate(request.getDisbursementDate());
        loanAccount.setFirstRepaymentDate(request.getFirstRepaymentDate());
        loanAccount.setLastRepaymentDate(request.getLastRepaymentDate());

        loanAccountRepository.save(loanAccount);

        return "Berhasil update Loan Account dengan ID: " + loanAccountId;
    }

    @Override
    public String deleteLoanAccount(String loanAccountId) {
        LoanAccount loanAccount = loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        loanAccount.setIsDeleted(true);
        return "SUCCES DELETE ACCOUNT";
    }
}
