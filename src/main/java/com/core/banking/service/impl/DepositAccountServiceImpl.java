package com.core.banking.service.impl;

import com.core.banking.dto.DepositAccountRequest;
import com.core.banking.dto.DepositAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.Customer;
import com.core.banking.entity.DepositAccount;
import com.core.banking.entity.DepositAccountDetail;
import com.core.banking.entity.DepositTypeConfig;
import com.core.banking.enums.*;
import com.core.banking.repository.CustomerRepository;
import com.core.banking.repository.DepositAccountDetailRepository;
import com.core.banking.repository.DepositAccountRepository;
import com.core.banking.repository.DepositTypeConfigRepository;
import com.core.banking.service.DepositAccountService;
import com.core.banking.utils.DepositAccountNumberGenerator;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepositAccountServiceImpl implements DepositAccountService {
    @Autowired
    DepositAccountRepository depositAccountRepository;

    @Autowired
    DepositAccountDetailRepository depositAccountDetailRepository;

    @Autowired
    DepositTypeConfigRepository depositTypeConfigRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    DepositAccountNumberGenerator depositAccountNumberGenerator;

    @Override
    public List<DepositAccount> findAll() {
        return depositAccountRepository.findAll();
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DepositAccountResponse openDepositAccount(DepositAccountRequest request, UserMetaData userMetaData) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_NOT_FOUND));

        if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_NOT_ACTIVE);
        }

        DepositTypeConfig depositTypeConfig = depositTypeConfigRepository.findById(request.getDepositTypeConfigId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_TYPE_CONFIG_NOT_FOUND));

        if (!depositTypeConfig.getIsActive()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_TYPE_CONFIG_NOT_ACTIVE);
        }

        if (request.getNominalDeposit().compareTo(depositTypeConfig.getMinDepositAmount()) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_AMOUNT_BELOW_MINIMUM);
        }

        LocalDate maturityDate = LocalDate.now().plusMonths(depositTypeConfig.getTermInMonths());

        String accountNumber = depositAccountNumberGenerator.generateDepositAccountNumber();

        DepositAccount depositAccount = DepositAccount.builder()
                .accountNumber(accountNumber)
                .customer(customer)
                .depositTypeConfig(depositTypeConfig)
                .principalAmount(request.getNominalDeposit())
                .maturityDate(maturityDate)
                .accountStatus(DepositAccountStatus.ACTIVE)
                .rolloverOption(request.getRolloverOption())
                .createdBy(userMetaData.getUserId())
                .openedAt(LocalDateTime.now())
                .build();

        DepositAccount savedAccount = depositAccountRepository.save(depositAccount);

        DepositAccountDetail depositAccountDetail = DepositAccountDetail.builder()
                .depositAccount(savedAccount)
                .transactionType(DepositoTransactionType.INITIAL_DEPOSIT)
                .mutationType(MutationType.CREDIT)
                .nominalTransaction(request.getNominalDeposit())
                .beginBalance(BigDecimal.ZERO)
                .endBalance(request.getNominalDeposit())
                .createdBy(userMetaData.getUserId())
                .description("Setoran awal deposito")
                .transactionAt(LocalDateTime.now())
                .build();

        depositAccountDetailRepository.save(depositAccountDetail);

        DepositAccountResponse depositAccountResponse = new DepositAccountResponse(savedAccount);
        depositAccountResponse.setCustomerName(customer.getFullName());

        if (depositTypeConfig.getDepositType() != null) {
            depositAccountResponse.setDepositTypeName(depositTypeConfig.getDepositType().getTypeName());
        }
        depositAccountResponse.setProfitSharePercentage(depositTypeConfig.getProfitSharePercentagePa());
        depositAccountResponse.setTermInMonths(depositTypeConfig.getTermInMonths());

        return depositAccountResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public DepositAccountResponse getDepositAccountById(Long depositAccountId) {
        DepositAccount depositAccount = depositAccountRepository.findById(depositAccountId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));
            return new DepositAccountResponse(depositAccount);
    }

    @Override
    public List<DepositAccountResponse> getDepositAccountsByCustomerId(String customerId) {
        customerRepository.findById(customerId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_NOT_FOUND));
        List<DepositAccountResponse> list = depositAccountRepository.findAll().stream()
                .filter(data -> data.getCustomer().getId().equals(customerId))
                .map(DepositAccountResponse::new)
                .collect(Collectors.toList());
        return list;
    }

    @Override
    public List<DepositAccountResponse> getDepositAccountsByStatus(DepositAccountStatus status) {
        List <DepositAccountResponse> list = depositAccountRepository.findByAccountStatus(status).stream()
                .map(DepositAccountResponse::new)
                .collect(Collectors.toList());
        return list;
    }

    @Override
    public String deleteDepositAccount(Long depositoAccountId) {
        DepositAccount depositAccount = depositAccountRepository.findById(depositoAccountId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ERROR));

        if (depositAccount.getAccountStatus() == DepositAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ERROR);
        }

        depositAccountRepository.findByDepositoAccountId(depositoAccountId).map(data -> {
            data.setDeleted(true);
            depositAccountRepository.save(data);
            return data;
        });
        return "SUCCESS DELETE A DEPOSITO ACCOUNT";
    }
}

