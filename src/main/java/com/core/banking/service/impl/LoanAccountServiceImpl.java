package com.core.banking.service.impl;

import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanAccountRequest;
import com.core.banking.dto.LoanAccountResponse;
import com.core.banking.entity.Customer;
import com.core.banking.entity.LoanAccount;
import com.core.banking.entity.LoanTypeConfig;
import com.core.banking.enums.LoanAccountStatus;
import com.core.banking.repository.CustomerRepository;
import com.core.banking.repository.LoanAccountRepository;
import com.core.banking.repository.LoanRepaymentScheduleRepository;
import com.core.banking.repository.LoanTransactionRepository;
import com.core.banking.repository.LoanTypeConfigRepository;
import com.core.banking.service.LoanAccountService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public List<LoanAccountResponse> findAll() {
        List<LoanAccount> accounts = loanAccountRepository.findAll();
        return accounts.stream()
                .map(account -> new LoanAccountResponse(
                        account.getLoanAccountId(),
                        account.getAccountNumber(),
                        account.getAccountStatus(),
                        account.getApplicationDate(),
                        account.getClosedAt(),
                        account.getCreatedAt(),
                        account.getDisbursementDate(),
                        account.getDurationMonths(),
                        account.getFirstRepaymentDate(),
                        account.getInstallmentAmount(),
                        account.getInterestRateApplied(),
                        account.getLastRepaymentDate(),
                        account.getOutstandingPrincipal(),
                        account.getPrincipalAmount(),
                        account.getUpdatedAt(),
                        account.getCustomerId().getCustomerId(),
                        account.getLoanTypeConfig().getLoanTypeConfigId(),
                        account.getIsDeleted()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public String createLoanAccount(LoanAccountRequest request, UserMetaData userMetaData) {
        String customerId = request.getCustomerId();
        String loanTypeConfigId = request.getLoanTypeConfigId();
        BigDecimal nominal = request.getPrincipalAmount();
        Integer duration = request.getDurationMonths();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        if (!"ACTIVE".equalsIgnoreCase(customer.getCustomerStatus().toString())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_NOT_ACTIVE);
        }

        List<LoanAccountStatus> activeStatuses = List.of(
                LoanAccountStatus.PENDING_APPROVAL,
                LoanAccountStatus.ACTIVE,
                LoanAccountStatus.REJECTED
        );

        boolean existsActiveLoan = loanAccountRepository.existsByCustomerIdAndAccountStatusIn(customer, activeStatuses);
        if (existsActiveLoan) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_BORROW);
        }

        LoanTypeConfig config = loanTypeConfigRepository.findById(loanTypeConfigId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        if (nominal == null || nominal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.NOMINAL_NOT_ENOUGHT);
        }

        if (duration == null || duration <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DURATION_NOT_ENOUGHT);
        }

        if (nominal.compareTo(config.getMinLoanAmount()) < 0 || nominal.compareTo(config.getMaxLoanAmount()) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.NOMINAL_NOT_ENOUGHT);
        }

        if (duration < config.getMinDurationMonths() || duration > config.getMaxDurationMonths()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DURATION_NOT_ENOUGHT);
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
        loanAccount.setApplicationDate(Timestamp.valueOf(LocalDateTime.now()));

        BigDecimal annualInterestRate = config.getInterestRatePa();
        BigDecimal monthlyInterestRate = annualInterestRate
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal monthlyPrincipal = nominal.divide(BigDecimal.valueOf(duration), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyInterest = nominal.multiply(monthlyInterestRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyInstallment = monthlyPrincipal.add(monthlyInterest);

        BigDecimal totalOutstanding = monthlyInstallment.multiply(BigDecimal.valueOf(duration)).setScale(2, RoundingMode.HALF_UP);

        loanAccount.setInterestRateApplied(annualInterestRate);
        loanAccount.setOutstandingPrincipal(totalOutstanding);
        loanAccount.setInstallmentAmount(monthlyInstallment);



        loanAccount.setAccountStatus(LoanAccountStatus.PENDING_APPROVAL);
        loanAccount.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        loanAccountRepository.save(loanAccount);

        return "Succes membuat loan account dengan ID : " + loanAccount.getLoanAccountId();
    }

    @Override
    @Transactional
    public String updateLoanAccount(String loanAccountId, LoanAccountRequest request, UserMetaData userMetaData) {
        LoanAccount loanAccount = loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        LoanTypeConfig config = loanTypeConfigRepository.findById(request.getLoanTypeConfigId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

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
    public String deleteLoanAccount(String loanAccountId, UserMetaData userMetaData) {
        LoanAccount loanAccount = loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        loanAccount.setIsDeleted(true);
        return "SUCCES DELETE ACCOUNT";
    }
}
