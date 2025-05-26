package com.core.banking.service.impl;

import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.EscrowAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.*;
import com.core.banking.enums.EscrowAccountStatus;
import com.core.banking.enums.TransactionTypeStatus;
import com.core.banking.repository.*;
import com.core.banking.service.EscrowAccountService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class EscrowAccountServiceImpl implements EscrowAccountService {

    @Autowired
    private EscrowAccountRepository escrowAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SavingAccountRepository savingAccountRepository;

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Autowired
    private DepositAccountRepository depositAccountRepository;

    @Override
    public String createEscrowAccount(EscrowAccountRequest request, UserMetaData userMetaData) {
        Customer payerId = customerRepository.findById(request.getPayerCustomer())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
        Customer beneficiaryId = customerRepository.findById(request.getBeneficiaryCustomer())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));

        SavingAccount savingAccount = null;
        LoanAccount loanAccount = null;
        DepositAccount depositAccount = null;

        TransactionTypeStatus transactionType = request.getTransactionTypeStatus();
        if (transactionType == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
        }
        if (transactionType == TransactionTypeStatus.SAVING_PAYMENT) {
            if (request.getSavingAccount() == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM);
            }
            savingAccount = savingAccountRepository.findById(request.getSavingAccount())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
        } else if (transactionType == TransactionTypeStatus.LOAN_PAYMENT) {
            if (request.getLoanAccount() == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM);
            }
            loanAccount = loanAccountRepository.findById(request.getLoanAccount())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
        } else if (transactionType == TransactionTypeStatus.DEPOSIT_PAYMENT) {
            if (request.getDepositAccount() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM);
                }
                depositAccount = depositAccountRepository.findById(request.getDepositAccount())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
        }
        EscrowAccount escrowAccount = EscrowAccount.builder()
                .accountNumber(generateAccountNumber())
                .purpose(request.getPurpose())
                .payerCustomer(payerId)
                .beneficiaryCustomer(beneficiaryId)
                .savingAccount(savingAccount)
                .loanAccount(loanAccount)
                .depositAccount(depositAccount)
                .transactionTypeStatus(transactionType)
                .accountStatus(EscrowAccountStatus.PENDING_FUNDING)
                .currentBalance(BigDecimal.ZERO)
                .isDeleted(false)
                .createdAt(Timestamp.from(Instant.now()))
                .build();
        escrowAccountRepository.save(escrowAccount);
        return "SUCCESS CREATE NEW ESCROW ACCOUNT";
    }

    @Override
    public List<EscrowAccountResponse> getAll() {
        List<EscrowAccountResponse> list = escrowAccountRepository.findAll().stream().map(data -> {
            return EscrowAccountResponse.builder()
                    .id(data.getId())
                    .accountNumber(data.getAccountNumber())
                    .purpose(data.getPurpose())
                    .currentBalance(data.getCurrentBalance())
                    .accountStatus(data.getAccountStatus())
                    .payerCustomer(data.getPayerCustomer().getId())
                    .payerCustomerName(data.getPayerCustomer().getFullName())
                    .beneficiaryCustomer(data.getBeneficiaryCustomer().getId())
                    .beneficiaryCustomerName(data.getBeneficiaryCustomer().getFullName())
                    .savingAccount(Optional.ofNullable(data.getSavingAccount())
                            .map(SavingAccount::getSavingAccountId)
                            .map(Object::toString)
                            .orElse("NOT USED"))
                    .loanAccount(Optional.ofNullable(data.getLoanAccount())
                            .map(LoanAccount::getLoanAccountId)
                            .map(Object::toString)
                            .orElse("NOT USED"))
                    .depositAccount(Optional.ofNullable(data.getDepositAccount())
                            .map(DepositAccount::getId)
                            .map(Object::toString)
                            .orElse("NOT USED"))
                    .transactionType(data.getTransactionTypeStatus())
                    .build();
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public String updateEscrowAccount(String id, EscrowAccountRequest request) {
        escrowAccountRepository.findById(id).map(data -> {
            Customer payerId = customerRepository.findById(request.getPayerCustomer())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
            Customer beneficiaryId = customerRepository.findById(request.getBeneficiaryCustomer())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
            SavingAccount savingAccount = null;
            LoanAccount loanAccount = null;
            DepositAccount depositAccount = null;

            TransactionTypeStatus transactionType = request.getTransactionTypeStatus();
            if (transactionType == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
            }
            if (transactionType == TransactionTypeStatus.SAVING_PAYMENT) {
                if (request.getSavingAccount() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM);
                }
                savingAccount = savingAccountRepository.findById(request.getSavingAccount())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
            } else if (transactionType == TransactionTypeStatus.LOAN_PAYMENT) {
                if (request.getLoanAccount() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM);
                }
                loanAccount = loanAccountRepository.findById(request.getLoanAccount())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
            } else if (transactionType == TransactionTypeStatus.DEPOSIT_PAYMENT) {
                if (request.getDepositAccount() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM);
                }
                depositAccount = depositAccountRepository.findById(request.getDepositAccount())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
            } else {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
            }
            data.setPurpose(request.getPurpose());
            data.setPayerCustomer(payerId);
            data.setBeneficiaryCustomer(beneficiaryId);
            data.setTransactionTypeStatus(transactionType);
            data.setSavingAccount(savingAccount);
            data.setLoanAccount(loanAccount);
            data.setDepositAccount(depositAccount);
            data.setUpdatedAt(Timestamp.from(Instant.now()));
            escrowAccountRepository.save(data);
            return data;
        });
        return "SUCCESS UPDATE ESCROW ACCOUNT";
    }

    @Override
    public String deleteEscrowAccount(String id) {
        escrowAccountRepository.findById(id).map(data -> {
            data.setDeleted(true);
            escrowAccountRepository.save(data);
            return data;
        });
        return "SUCCESS DELETED ESCROW ACCOUNT";
    }

    private String generateAccountNumber() {
        String prefix = "2358";
        long count = escrowAccountRepository.countByAccountNumberStartingWith(prefix);
        String suffix = String.format("%06d", count + 1);
        return prefix + suffix;
    }
}
