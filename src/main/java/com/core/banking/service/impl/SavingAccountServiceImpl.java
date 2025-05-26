package com.core.banking.service.impl;

import com.core.banking.dto.SavingAccountRequest;
import com.core.banking.dto.SavingAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.Customer;
import com.core.banking.entity.SavingAccount;
import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.enums.SavingAccountStatus;
import com.core.banking.repository.CustomerRepository;
import com.core.banking.repository.SavingAccountDetailRepository;
import com.core.banking.repository.SavingAccountRepository;
import com.core.banking.repository.SavingTypeConfigRepository;
import com.core.banking.service.SavingAccountService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class SavingAccountServiceImpl implements SavingAccountService {

    @Autowired
    private SavingAccountRepository savingAccountRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private SavingTypeConfigRepository savingTypeConfigRepository;
    @Autowired
    private SavingAccountDetailRepository savingAccountDetailRepository;

    @Override
    public List<SavingAccount> findAll() {
        return savingAccountRepository.findAll();
    }

    @Override
    public String create(SavingAccountRequest request, UserMetaData userMetaData) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_NOT_FOUND));

        SavingTypeConfig config = savingTypeConfigRepository.findById(request.getSavingTypeConfig())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND));

        BigDecimal deposit = request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO;

        if (deposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Initial deposit cannot be negative");
        }

        if (config.getMinBalanceLimit() != null && deposit.compareTo(config.getMinBalanceLimit()) < 0) {
            throw new RuntimeException("Initial deposit less than minimum");
        }

        if (config.getMaxBalanceLimit() != null && deposit.compareTo(config.getMaxBalanceLimit()) > 0) {
            throw new RuntimeException("Initial deposit more than maximum");
        }

        String accountNumber = generateUniqueAccountNumber();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        SavingAccount account = SavingAccount.builder()
                .accountNumber(accountNumber)
                .customer(customer)
                .savingTypeConfig(config)
                .currentBalance(deposit)
                .accountStatus(SavingAccountStatus.ACTIVE)
                .createdAt(now)
                .openedAt(now)
                .build();

        savingAccountRepository.save(account);

        return "SUCCESS CREATE SAVING ACCOUNT";
    }
    private SavingAccountResponse toResponse(SavingAccount savingAccount) {
        return SavingAccountResponse.builder()
                .id(savingAccount.getSavingAccountId())
                .accountNumber(savingAccount.getAccountNumber())
                .customerId(savingAccount.getCustomer().getId())
                .customerName(savingAccount.getCustomer().getFullName())
                .savingTypeConfig(savingAccount.getSavingTypeConfig().getSavingTypeConfigId())
                .savingTypeName(savingAccount.getSavingTypeConfig().getSavingType().getTypeName())
                .balance(savingAccount.getCurrentBalance())
                .status(savingAccount.getAccountStatus())
                .build();
    }
    @Override
    public List<SavingAccountResponse> getAll() {
        List<SavingAccountResponse> list = savingAccountRepository.findAll().stream().map(data -> {
            return SavingAccountResponse.builder()
                    .id(data.getSavingAccountId())
                    .accountNumber(data.getAccountNumber())
                    .customerId(data.getCustomer().getId())
                    .customerName(data.getCustomer().getFullName())
                    .savingTypeConfig(data.getSavingTypeConfig().getSavingTypeConfigId())
                    .savingTypeName(data.getSavingTypeConfig().getSavingType().getTypeName())
                    .balance(data.getCurrentBalance())
                    .status(data.getAccountStatus())
                    .build();
        }).collect(Collectors.toList());
        return list;
    }
    @Override
    public SavingAccountResponse getByAccountNumber(String accountNumber) {
        SavingAccount savingAccount = savingAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND));

        return toResponse(savingAccount);
    }
    @Override
    public SavingAccountResponse updateStatus(String id, SavingAccountStatus status) {

        SavingAccount savingAccount = savingAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND));

        if (status == SavingAccountStatus.CLOSED && savingAccount.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0){
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Cannot close account with non-zero balance. Please withdraw remaining balance first.");
        }
        savingAccount.setAccountStatus(status);
        savingAccount.setUpdatedAt(Timestamp.from(Instant.now()));
//        savingAccount.setClosedAt(Timestamp.from(Instant.now()));
        savingAccountRepository.save(savingAccount);

        return toResponse(savingAccount);
    }

    private String generateUniqueAccountNumber() {
        String number;
        do {
            number = "3232" + String.format("%06d", new Random().nextInt(10));
        } while (savingAccountRepository.existsByAccountNumber(number));
        return number;
    }
}

