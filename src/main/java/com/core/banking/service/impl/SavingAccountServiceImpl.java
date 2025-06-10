package com.core.banking.service.impl;

import com.core.banking.dto.SavingAccountRequest;
import com.core.banking.dto.SavingAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.Customer;
import com.core.banking.entity.SavingAccount;
import com.core.banking.entity.SavingTypeConfig;
import com.core.banking.enums.CustomerStatus;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
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
        Customer customer = customerRepository.findByIdEligible(request.getCustomerId(), CustomerStatus.ACTIVE.name())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_NOT_FOUND));

        SavingAccount savingAccount = savingAccountRepository.findByCustomerId(request.getCustomerId());
        if (savingAccount != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_ALREADY_EXIST);
        }

        SavingTypeConfig config = savingTypeConfigRepository.findById(request.getSavingTypeConfigId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND));

        String accountNumber = generateUniqueAccountNumber();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        SavingAccount account = SavingAccount.builder()
                .accountNumber(accountNumber)
                .customer(customer)
                .savingTypeConfig(config)
                .currentBalance(BigDecimal.ZERO)
                .accruedInterest(BigDecimal.ZERO)
                .accountStatus(SavingAccountStatus.ACTIVE)
                .createdAt(now)
                .createBy(userMetaData.getUserId())
                .openedAt(now)
                .build();

        savingAccountRepository.save(account);
        return "SUCCESS CREATE SAVING ACCOUNT";
    }

    @Override
    public List<SavingAccountResponse> getAll() {
        List<SavingAccountResponse> list = savingAccountRepository.findAll().stream().map(data -> {
            return SavingAccountResponse.builder()
                    .id(data.getSavingAccountId())
                    .accountNumber(data.getAccountNumber())
                    .customerId(data.getCustomer().getId())
                    .customerName(data.getCustomer().getFullName())
                    .nik(data.getCustomer().getNik())
                    .savingTypeConfigId(data.getSavingTypeConfig().getSavingTypeConfigId())
                    .savingTypeName(String.valueOf(data.getSavingTypeConfig().getSavingType().getTypeName()))
                    .currentBalance(data.getCurrentBalance())
                    .accruedInterest(data.getAccruedInterest())
                    .status(data.getAccountStatus())
                    .isDeleted(data.getIsDeleted())
                    .build();
        }).collect(Collectors.toList());
        return list;
    }
    @Override
    public SavingAccountResponse getByAccountNumber(String accountNumber) {
        SavingAccount savingAccount = savingAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ACCOUNT_NUMBER_NOT_FOUND));

        return toResponse(savingAccount);
    }
    @Override
    public SavingAccountResponse updateStatus(String id, SavingAccountStatus status, UserMetaData userMetaData) {

        SavingAccount savingAccount = savingAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND));

        if (status == SavingAccountStatus.CLOSED && savingAccount.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CLOSED_ACCOUNT_FAILED);
        }
        if (status == SavingAccountStatus.DORMANT && savingAccount.getAccountStatus() == SavingAccountStatus.CLOSED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DORMANT_ACCOUNT_FAILED);
        }
        if (status == SavingAccountStatus.BLOCKED && savingAccount.getAccountStatus() == SavingAccountStatus.CLOSED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.BLOCK_ACCOUNT_FAILED);
        }
        savingAccount.setAccountStatus(status);
        savingAccount.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        savingAccount.setUpdateBy(userMetaData.getUserId());
        if (SavingAccountStatus.CLOSED.equals(status)){
            savingAccount.setClosedAt(Timestamp.valueOf(LocalDateTime.now()));
        } else { savingAccount.setClosedAt(null); }

        savingAccountRepository.save(savingAccount);
        return toResponse(savingAccount);
    }

    @Transactional
    public String deleted(String accountNumber, UserMetaData userMetaData) {
        SavingAccount account = savingAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.DATA_NOT_FOUND));
        account.setIsDeleted(true);
        account.setUpdatedAt(Timestamp.from(Instant.now()));
        account.setUpdateBy(userMetaData.getUserId());
        savingAccountRepository.save(account);
        return "SUCCESS IS DELETED";
    }
    private String generateUniqueAccountNumber() {
        String number;
        do {
            number = "3232" + String.format("%06d", new Random().nextInt(10000));
        } while (savingAccountRepository.existsByAccountNumber(number));
        return number;
    }
    private SavingAccountResponse toResponse(SavingAccount savingAccount) {
        return SavingAccountResponse.builder()
                .id(savingAccount.getSavingAccountId())
                .accountNumber(savingAccount.getAccountNumber())
                .customerId(savingAccount.getCustomer().getId())
                .customerName(savingAccount.getCustomer().getFullName())
                .nik(savingAccount.getCustomer().getNik())
                .savingTypeConfigId(savingAccount.getSavingTypeConfig().getSavingTypeConfigId())
                .savingTypeName(String.valueOf(savingAccount.getSavingTypeConfig().getSavingType().getTypeName()))
                .currentBalance(savingAccount.getCurrentBalance())
                .accruedInterest(savingAccount.getAccruedInterest())
                .status(savingAccount.getAccountStatus())
                .build();
    }
}

