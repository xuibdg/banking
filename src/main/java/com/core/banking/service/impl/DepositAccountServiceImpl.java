package com.core.banking.service.impl;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.DepositAccountRequest;
import com.core.banking.dto.DepositAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.*;
import com.core.banking.enums.*;
import com.core.banking.repository.*;
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

    @Autowired
    SavingAccountRepository savingAccountRepository;

    @Override
    public List<DepositAccount> findAll() {
        return depositAccountRepository.findAll();
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DepositAccountResponse openDepositAccount(DepositAccountRequest DepositAccountRequest, String savingAccountId, @CurrentUser UserMetaData userMetaData) {
        Customer customer = customerRepository.findById(DepositAccountRequest.getCustomerId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_NOT_FOUND));

        if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_NOT_ACTIVE);
        }

        SavingAccount savingAccount = savingAccountRepository.findById(savingAccountId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));

        if (savingAccount.getAccountStatus() != SavingAccountStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_ACTIVE);
        }

        DepositTypeConfig depositTypeConfig = depositTypeConfigRepository.findById(DepositAccountRequest.getDepositTypeConfigId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_TYPE_CONFIG_NOT_FOUND));

        if (!depositTypeConfig.getIsActive()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_TYPE_CONFIG_NOT_ACTIVE);
        }

        if (DepositAccountRequest.getNominalDeposit().compareTo(depositTypeConfig.getMinDepositAmount()) < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_AMOUNT_BELOW_MINIMUM);
        }

        LocalDate maturityDate = LocalDate.now().plusMonths(depositTypeConfig.getTermInMonths());

        String accountNumber = depositAccountNumberGenerator.generateDepositAccountNumber();

        DepositAccount depositAccount = DepositAccount.builder()
                .accountNumber(accountNumber)
                .customer(customer)
                .depositTypeConfig(depositTypeConfig)
                .principalAmount(DepositAccountRequest.getNominalDeposit())
                .maturityDate(maturityDate)
                .createdBy(userMetaData.getUserId())
                .accountStatus(DepositAccountStatus.ACTIVE)
                .rolloverOption(RolloverOption.valueOf(DepositAccountRequest.getRolloverOption()))
                .openedAt(LocalDateTime.now())
                .createdBy(userMetaData.getUserId())
                .build();

        DepositAccount savedAccount = depositAccountRepository.save(depositAccount);

        DepositAccountDetail depositAccountDetail = DepositAccountDetail.builder()
                .depositAccount(savedAccount)
                .transactionType(DepositoTransactionType.INITIAL_DEPOSIT)
                .mutationType(MutationType.CREDIT)
                .nominalTransaction(DepositAccountRequest.getNominalDeposit())
                .beginBalance(BigDecimal.ZERO)
                .endBalance(DepositAccountRequest.getNominalDeposit())
                .createdBy(userMetaData.getUserId())
                .description("Setoran awal deposito")
                .transactionAt(LocalDateTime.now())
                .createdBy(userMetaData.getUserId())
                .build();

        depositAccountDetailRepository.save(depositAccountDetail);

        DepositAccountResponse depositAccountResponse = new DepositAccountResponse();
        depositAccountResponse.setCustomerName(customer.getFullName());

        if (depositTypeConfig.getDepositType() != null) {
            depositAccountResponse.setDepositTypeName(depositTypeConfig.getDepositType().getTypeName());
        }
        depositAccountResponse.setProfitSharePercentage(depositTypeConfig.getProfitSharePercentagePa());
        depositAccountResponse.setTermInMonths(depositTypeConfig.getTermInMonths());
        depositAccountResponse.setDepositoAccountId(savedAccount.getDepositoAccountId());
        depositAccountResponse.setAccountNumber(savedAccount.getAccountNumber());
        depositAccountResponse.setCustomerId(savedAccount.getCustomer().getId());
        depositAccountResponse.setPrincipalAmount(savedAccount.getPrincipalAmount());
        depositAccountResponse.setMaturityDate(savedAccount.getMaturityDate());
        depositAccountResponse.setAccountStatus(savedAccount.getAccountStatus().name());
        depositAccountResponse.setRolloverOption(savedAccount.getRolloverOption().name());
        depositAccountResponse.setOpenedAt(savedAccount.getOpenedAt());
        depositAccountResponse.setCreatedAt(savedAccount.getCreatedAt());

        return depositAccountResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public DepositAccountResponse getDepositAccountById(Long depositAccountId) {
        DepositAccount depositAccount = depositAccountRepository.findById(depositAccountId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));
        DepositAccountResponse depositAccountResponse = new DepositAccountResponse();
        depositAccountResponse.setDepositoAccountId(depositAccount.getDepositoAccountId());
        depositAccountResponse.setAccountNumber(depositAccount.getAccountNumber());
        depositAccountResponse.setCustomerId(depositAccount.getCustomer().getId());
        depositAccountResponse.setPrincipalAmount(depositAccount.getPrincipalAmount());
        depositAccountResponse.setMaturityDate(depositAccount.getMaturityDate());
        depositAccountResponse.setAccountStatus(depositAccount.getAccountStatus().name());
        depositAccountResponse.setRolloverOption(depositAccount.getRolloverOption().name());
        depositAccountResponse.setOpenedAt(depositAccount.getOpenedAt());
        depositAccountResponse.setCreatedAt(depositAccount.getCreatedAt());
        depositAccountResponse.setCustomerId(depositAccount.getCustomer().getId());
        depositAccountResponse.setCustomerName(depositAccount.getCustomer().getFullName());
        depositAccountResponse.setDepositTypeName(depositAccount.getDepositTypeConfig().getDepositType().getTypeName());
        depositAccountResponse.setProfitSharePercentage(depositAccount.getDepositTypeConfig().getProfitSharePercentagePa());
        depositAccountResponse.setTermInMonths(depositAccount.getDepositTypeConfig().getTermInMonths());
        return depositAccountResponse;
    }

    @Override
    public List<DepositAccountResponse> getDepositAccountsByCustomerId(String customerId) {
        customerRepository.findById(customerId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_NOT_FOUND));
        List<DepositAccountResponse> list = depositAccountRepository.findAll().stream()
                .filter(data -> data.getCustomer().getId().equals(customerId))
                .map(depositAccount -> {
                    DepositAccountResponse depositAccountResponse = new DepositAccountResponse();
                    depositAccountResponse.setDepositoAccountId(depositAccount.getDepositoAccountId());
                    depositAccountResponse.setAccountNumber(depositAccount.getAccountNumber());
                    depositAccountResponse.setCustomerId(depositAccount.getCustomer().getId());
                    depositAccountResponse.setPrincipalAmount(depositAccount.getPrincipalAmount());
                    depositAccountResponse.setMaturityDate(depositAccount.getMaturityDate());
                    depositAccountResponse.setAccountStatus(String.valueOf(depositAccount.getAccountStatus()));
                    depositAccountResponse.setRolloverOption(depositAccount.getRolloverOption().name());
                    depositAccountResponse.setOpenedAt(depositAccount.getOpenedAt());
                    depositAccountResponse.setCreatedAt(depositAccount.getCreatedAt());
                    depositAccountResponse.setCustomerName(depositAccount.getCustomer().getFullName());
                    depositAccountResponse.setDepositTypeName(depositAccount.getDepositTypeConfig().getDepositType().getTypeName());
                    depositAccountResponse.setProfitSharePercentage(depositAccount.getDepositTypeConfig().getProfitSharePercentagePa());
                    depositAccountResponse.setTermInMonths(depositAccount.getDepositTypeConfig().getTermInMonths());
                    return depositAccountResponse;
                })
                .collect(Collectors.toList());
        return list;
    }

    @Override
    public List<DepositAccountResponse> getDepositAccountsByStatus(DepositAccountStatus status) {
        List <DepositAccountResponse> list = depositAccountRepository.findByAccountStatus(status).stream()
                .map(depositAccount -> {
                    DepositAccountResponse depositAccountResponse = new DepositAccountResponse();
                    depositAccountResponse.setDepositoAccountId(depositAccount.getDepositoAccountId());
                    depositAccountResponse.setAccountNumber(depositAccount.getAccountNumber());
                    depositAccountResponse.setCustomerId(depositAccount.getCustomer().getId());
                    depositAccountResponse.setPrincipalAmount(depositAccount.getPrincipalAmount());
                    depositAccountResponse.setMaturityDate(depositAccount.getMaturityDate());
                    depositAccountResponse.setAccountStatus(String.valueOf(depositAccount.getAccountStatus()));
                    depositAccountResponse.setRolloverOption(depositAccount.getRolloverOption().name());
                    depositAccountResponse.setOpenedAt(depositAccount.getOpenedAt());
                    depositAccountResponse.setCreatedAt(depositAccount.getCreatedAt());
                    depositAccountResponse.setCustomerName(depositAccount.getCustomer().getFullName());
                    depositAccountResponse.setDepositTypeName(depositAccount.getDepositTypeConfig().getDepositType().getTypeName());
                    depositAccountResponse.setProfitSharePercentage(depositAccount.getDepositTypeConfig().getProfitSharePercentagePa());
                    depositAccountResponse.setTermInMonths(depositAccount.getDepositTypeConfig().getTermInMonths());
                    return depositAccountResponse;
                })
                .collect(Collectors.toList());
        return list;
    }}

//    @Override
//    public String deleteDepositAccount(Long depositoAccountId) {
//        DepositAccount depositAccount = depositAccountRepository.findById(depositoAccountId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ERROR));
//
//        if (depositAccount.getAccountStatus() == DepositAccountStatus.ACTIVE) {
//            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ERROR);
//        }
//
//        depositAccountRepository.findByDepositoAccountId(depositoAccountId).map(data -> {
//            data.setDeleted(true);
//            depositAccountRepository.save(data);
//            return data;
//        });
//        return "SUCCESS DELETE A DEPOSITO ACCOUNT";
//    }

