package com.core.banking.service.impl;

import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.EscrowAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.Customer;
import com.core.banking.entity.DepositAccount;
import com.core.banking.entity.EscrowAccount;
import com.core.banking.entity.LoanAccount;
import com.core.banking.entity.SavingAccount;
import com.core.banking.enums.EscrowAccountStatus;
import com.core.banking.enums.TransactionTypeStatus;
import com.core.banking.repository.CustomerRepository;
import com.core.banking.repository.DepositAccountRepository;
import com.core.banking.repository.EscrowAccountRepository;
import com.core.banking.repository.LoanAccountRepository;
import com.core.banking.repository.SavingAccountRepository;
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
import java.time.LocalDate;
import java.time.LocalTime;
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
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.PAYER_CUSTOMER_NOT_FOUND));
        Customer beneficiaryId = customerRepository.findById(request.getBeneficiaryCustomer())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.BENEFICIARY_CUSTOMER_NOT_FOUND));

        SavingAccount savingAccount = null;
        LoanAccount loanAccount = null;
        DepositAccount depositAccount = null;

        TransactionTypeStatus transactionType = request.getTransactionTypeStatus();
        if (transactionType == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.TRANSACTION_TYPE_INVALID_OR_NULL);
        }
        if (transactionType == TransactionTypeStatus.SAVING_PAYMENT) {
            if (request.getSavingAccount() == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND);
            }
            savingAccount = savingAccountRepository.findById(request.getSavingAccount())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));
        } else if (transactionType == TransactionTypeStatus.LOAN_PAYMENT) {
            if (request.getLoanAccount() == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.LOAN_ACCOUNT_NOT_FOUND);
            }
            loanAccount = loanAccountRepository.findById(request.getLoanAccount())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.LOAN_ACCOUNT_NOT_FOUND));
        } else if (transactionType == TransactionTypeStatus.DEPOSIT_PAYMENT) {
            if (request.getDepositAccount() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND);
                }
                depositAccount = depositAccountRepository.findById(request.getDepositAccount())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));
        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.TRANSACTION_TYPE_INVALID_OR_NULL);
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
                .createdBy(userMetaData.getUserId())
                .build();
        escrowAccountRepository.save(escrowAccount);
        return "SUCCESS CREATE NEW ESCROW ACCOUNT " +
                "| ID : "  + escrowAccount.getId() + " |";
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
                            .map(DepositAccount::getDepositoAccountId)
                            .map(Object::toString)
                            .orElse("NOT USED"))
                    .transactionType(data.getTransactionTypeStatus())
                    .build();
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<EscrowAccountResponse> filterData(String id, LocalDate start, LocalDate end, EscrowAccountStatus accountStatus) {
        Timestamp startTime = (start != null) ? Timestamp.valueOf(start.atStartOfDay()) : null;
        Timestamp endTime = (end != null) ? Timestamp.valueOf(end.atTime(LocalTime.MAX)) : null;

        List<EscrowAccount> filter = escrowAccountRepository.findByNeedData(id, startTime, endTime, accountStatus);
        return filter.stream().map(data -> EscrowAccountResponse.builder()
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
                        .map(DepositAccount::getDepositoAccountId)
                        .map(Object::toString)
                        .orElse("NOT USED"))
                .transactionType(data.getTransactionTypeStatus())
                .build()
        ).collect(Collectors.toList());
    }



    @Override
    public String updateEscrowAccount(String id, EscrowAccountRequest request, UserMetaData userMetaData) {
        EscrowAccount updateEscrowAccount = escrowAccountRepository.findById(id).map(data -> {
            Customer payerId = customerRepository.findById(request.getPayerCustomer())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.PAYER_CUSTOMER_NOT_FOUND));
            Customer beneficiaryId = customerRepository.findById(request.getBeneficiaryCustomer())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.BENEFICIARY_CUSTOMER_NOT_FOUND));
            SavingAccount savingAccount = null;
            LoanAccount loanAccount = null;
            DepositAccount depositAccount = null;

            TransactionTypeStatus transactionType = request.getTransactionTypeStatus();
            if (transactionType == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.TRANSACTION_TYPE_INVALID_OR_NULL);
            }
            if (transactionType == TransactionTypeStatus.SAVING_PAYMENT) {
                if (request.getSavingAccount() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND);
                }
                savingAccount = savingAccountRepository.findById(request.getSavingAccount())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));
            } else if (transactionType == TransactionTypeStatus.LOAN_PAYMENT) {
                if (request.getLoanAccount() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.LOAN_ACCOUNT_NOT_FOUND);
                }
                loanAccount = loanAccountRepository.findById(request.getLoanAccount())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.LOAN_ACCOUNT_NOT_FOUND));
            } else if (transactionType == TransactionTypeStatus.DEPOSIT_PAYMENT) {
                if (request.getDepositAccount() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND);
                }
                depositAccount = depositAccountRepository.findById(request.getDepositAccount())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));
            } else {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.TRANSACTION_TYPE_INVALID_OR_NULL);
            }
            data.setPurpose(request.getPurpose());
            data.setPayerCustomer(payerId);
            data.setBeneficiaryCustomer(beneficiaryId);
            data.setTransactionTypeStatus(transactionType);
            data.setSavingAccount(savingAccount);
            data.setLoanAccount(loanAccount);
            data.setDepositAccount(depositAccount);
            data.setCreatedBy(userMetaData.getUserId());
            data.setUpdatedAt(Timestamp.from(Instant.now()));
            return escrowAccountRepository.save(data);
        }).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ESCROW_ACCOUNT_NOT_FOUND));
        return "SUCCESS UPDATE ESCROW ACCOUNT " +
                "| ID : "  + updateEscrowAccount.getId() + " |";
    }

    @Override
    public String deleteEscrowAccount(String id, UserMetaData userMetaData) {
        EscrowAccount deleteEscrowAccount = escrowAccountRepository.findById(id).map(data -> {
            data.setDeleted(true);
            data.setUpdatedAt(Timestamp.from(Instant.now()));
            data.setCreatedBy(userMetaData.getUserId());
            return escrowAccountRepository.save(data);
        }).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ESCROW_ACCOUNT_NOT_FOUND));
        return "SUCCESS DELETED ESCROW ACCOUNT " +
                "| ID : " + deleteEscrowAccount.getId() + " |";
    }

    private String generateAccountNumber() {
        String prefix = "2358";
        long count = escrowAccountRepository.countByAccountNumberStartingWith(prefix);
        String suffix = String.format("%06d", count + 1);
        return prefix + suffix;
    }
}
