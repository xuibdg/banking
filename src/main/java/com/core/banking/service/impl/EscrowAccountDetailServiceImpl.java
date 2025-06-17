package com.core.banking.service.impl;

import com.core.banking.dto.EscrowAccountDetailRequest;
import com.core.banking.dto.EscrowAccountDetailResponse;
import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.*;
import com.core.banking.enums.EscrowAccountStatus;
import com.core.banking.enums.EscrowTransactionType;
import com.core.banking.enums.MutationType;
import com.core.banking.enums.TransactionTypeStatus;
import com.core.banking.repository.*;
import com.core.banking.service.EscrowAccountDetailService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class EscrowAccountDetailServiceImpl implements EscrowAccountDetailService{

    @Autowired
    private EscrowAccountDetailRepository escrowAccountDetailRepository;

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

    @Autowired
    private EscrowAccountServiceImpl escrowAccountServiceImpl;

    private static final DateTimeFormatter TRX_REF_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final Random random = new Random();

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public String createEscrowAccountDetail (EscrowAccountDetailRequest request, UserMetaData userMetaData) {
        EscrowAccount escrowAccount = escrowAccountRepository.findById(request.getEscrowAccount())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ESCROW_ACCOUNT_NOT_FOUND));
        if (request.getNominalTransaction() == null || request.getNominalTransaction().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.TRANSACTION_NOMINAL_INVALID);
        }
        BigDecimal beginBalance = escrowAccount.getCurrentBalance();
        BigDecimal endBalance = beginBalance;
        MutationType mutationType;

        EscrowTransactionType transactionType = request.getTransactionType();

        if (transactionType == EscrowTransactionType.FUNDING) {
            if (!escrowAccount.getAccountStatus().equals(EscrowAccountStatus.PENDING_FUNDING)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ESCROW_STATUS_NOT_PENDING_FUNDING);
            }
            if (request.getReleaseAccountNumber() != null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.FUNDING_SHOULD_NOT_HAVE_RELEASE_ACCOUNT);
            }

            mutationType = MutationType.CREDIT;
            endBalance = beginBalance.add(request.getNominalTransaction());
            escrowAccount.setAccountStatus(EscrowAccountStatus.FUNDED);

        } else if (transactionType == EscrowTransactionType.RELEASE_TO_BENEFICIARY
                || transactionType == EscrowTransactionType.RETURN_TO_PAYER
                || transactionType == EscrowTransactionType.FEE_DEBIT) {

            if (request.getReleaseAccountNumber() == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RELEASE_ACCOUNT_NUMBER_REQUIRED);
            }

            boolean isAllowedStatus = escrowAccount.getAccountStatus().equals(EscrowAccountStatus.FUNDED)
                    || escrowAccount.getAccountStatus().equals(EscrowAccountStatus.RELEASED);

            if (!isAllowedStatus) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Status escrow account harus FUNDED atau RELEASED untuk transaksi ini");
            }

            if (beginBalance.compareTo(request.getNominalTransaction()) < 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ESCROW_BALANCE_NOT_ENOUGH);
            }

            mutationType = MutationType.DEBIT;
            endBalance = beginBalance.subtract(request.getNominalTransaction());

            if (transactionType == EscrowTransactionType.RELEASE_TO_BENEFICIARY
                    || transactionType ==  EscrowTransactionType.RETURN_TO_PAYER) {
                escrowAccount.setAccountStatus(EscrowAccountStatus.RELEASED);
            }

        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ESCROW_TRANSACTION_TYPE_INVALID);
        }

        EscrowAccountDetail escrowAccountDetail = EscrowAccountDetail.builder()
                .escrowAccount(escrowAccount)
                .transactionType(transactionType)
                .mutationType(mutationType)
                .nominalTransaction(request.getNominalTransaction())
                .beginBalance(beginBalance)
                .endBalance(endBalance)
                .description(request.getDescription())
                .transactionReference(generateTrxCode())
                .releaseAccountNumber(request.getReleaseAccountNumber())
                .createdAt(Timestamp.from(Instant.now()))
                .createBy(userMetaData.getUserId())
                .transactionAt(Timestamp.from(Instant.now()))
                .isDeleted(false)
                .build();
        escrowAccountDetailRepository.save(escrowAccountDetail);
        escrowAccount.setCurrentBalance(endBalance);
        escrowAccountRepository.save(escrowAccount);
        return escrowAccountDetail.getTransactionReference() +
                "|" + escrowAccountDetail.getId();

    }

    @Override
    @Transactional
    public String createAndReleaseEscrowAccount(EscrowAccountRequest escrowRequest, BigDecimal nominalTransaction, String releaseAccountNumber, String description, UserMetaData userMetaData) {
        // Validasi customer
        Customer payer = customerRepository.findById(escrowRequest.getPayerCustomer())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.PAYER_CUSTOMER_NOT_FOUND));
        Customer beneficiary = customerRepository.findById(escrowRequest.getBeneficiaryCustomer())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.BENEFICIARY_CUSTOMER_NOT_FOUND));

        // Validasi akun tujuan
        SavingAccount savingAccount = null;
        LoanAccount loanAccount = null;
        DepositAccount depositAccount = null;

        TransactionTypeStatus transactionType = escrowRequest.getTransactionTypeStatus();
        if (transactionType == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.TRANSACTION_TYPE_INVALID_OR_NULL);
        }

        switch (transactionType) {
            case SAVING_PAYMENT:
                if (escrowRequest.getSavingAccount() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND);
                }
                savingAccount = savingAccountRepository.findById(escrowRequest.getSavingAccount())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));
                break;
            case LOAN_PAYMENT:
                if (escrowRequest.getLoanAccount() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.LOAN_ACCOUNT_NOT_FOUND);
                }
                loanAccount = loanAccountRepository.findById(escrowRequest.getLoanAccount())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.LOAN_ACCOUNT_NOT_FOUND));
                break;
            case DEPOSIT_PAYMENT:
                if (escrowRequest.getDepositAccount() == null) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND);
                }
                depositAccount = depositAccountRepository.findById(escrowRequest.getDepositAccount())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));
                break;
            default:
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.TRANSACTION_TYPE_INVALID_OR_NULL);
        }

        // Validasi nominal
        if (nominalTransaction == null || nominalTransaction.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.TRANSACTION_NOMINAL_INVALID);
        }

        // Buat escrow account langsung status RELEASED
        EscrowAccount escrowAccount = EscrowAccount.builder()
                .accountNumber(escrowAccountServiceImpl.generateAccountNumber())
                .purpose(escrowRequest.getPurpose())
                .payerCustomer(payer)
                .beneficiaryCustomer(beneficiary)
                .savingAccount(savingAccount)
                .loanAccount(loanAccount)
                .depositAccount(depositAccount)
                .transactionTypeStatus(transactionType)
                .accountStatus(EscrowAccountStatus.RELEASED)
                .currentBalance(nominalTransaction) // langsung diset ke nominal
                .isDeleted(false)
                .createdAt(Timestamp.from(Instant.now()))
                .createdBy(userMetaData.getUserId())
                .build();
        escrowAccountRepository.save(escrowAccount);

        // Buat detail funding
        EscrowAccountDetail escrowDetail = EscrowAccountDetail.builder()
                .escrowAccount(escrowAccount)
                .transactionType(EscrowTransactionType.FUNDING)
                .mutationType(MutationType.CREDIT)
                .nominalTransaction(nominalTransaction)
                .beginBalance(BigDecimal.ZERO)
                .endBalance(nominalTransaction)
                .description(description != null ? description : "AUTO GENERATE FUNDING ")
                .transactionReference(generateTrxCode())
                .releaseAccountNumber(releaseAccountNumber) // optional
                .transactionAt(Timestamp.from(Instant.now()))
                .createdAt(Timestamp.from(Instant.now()))
                .createBy(userMetaData.getUserId())
                .isDeleted(false)
                .build();
        escrowAccountDetailRepository.save(escrowDetail);

        EscrowAccountDetail escrowDetailRelease = EscrowAccountDetail.builder()
                .escrowAccount(escrowAccount)
                .transactionType(EscrowTransactionType.RELEASE_TO_BENEFICIARY)
                .mutationType(MutationType.DEBIT)
                .nominalTransaction(nominalTransaction)
                .beginBalance(nominalTransaction)
                .endBalance(BigDecimal.ZERO)
                .description(description != null ? description : "AUTO GENERATE RELEASE")
                .transactionReference(generateTrxCode())
                .releaseAccountNumber(releaseAccountNumber) // optional
                .transactionAt(Timestamp.from(Instant.now()))
                .createdAt(Timestamp.from(Instant.now()))
                .createBy(userMetaData.getUserId())
                .isDeleted(false)
                .build();
        escrowAccountDetailRepository.save(escrowDetailRelease);

        return escrowDetail.getTransactionReference();

    }

    @Override
    public List<EscrowAccountDetailResponse> getAll() {
        List<EscrowAccountDetailResponse> list = escrowAccountDetailRepository.findAll().stream().map(data -> {
            return EscrowAccountDetailResponse.builder()
                    .id(data.getId())
                    .escrowAccount(data.getEscrowAccount().getAccountNumber())
                    .escrowAccountStatus(data.getEscrowAccount().getAccountStatus())
                    .transactionType(data.getTransactionType())
                    .mutationType(data.getMutationType())
                    .nominalTransaction(data.getNominalTransaction())
                    .beginBalance(data.getBeginBalance())
                    .endBalance(data.getEndBalance())
                    .description(data.getDescription())
                    .transactionReference(data.getTransactionReference())
                    .releaseAccountNumber(data.getReleaseAccountNumber())
                    .build();
        }).collect(Collectors.toList());
        return list;

    }

    @Override
    public List<EscrowAccountDetailResponse> filterData(String id, LocalDate startDate, LocalDate endDate, EscrowTransactionType transactionType) {
        Timestamp startTime = (startDate != null) ? Timestamp.valueOf(startDate.atStartOfDay()) : null;
        Timestamp endTime = (endDate != null) ? Timestamp.valueOf(endDate.atTime(LocalTime.MAX)) : null;

        List<EscrowAccountDetail> filter = escrowAccountDetailRepository.findByNeedData(id, startTime, endTime, transactionType);
        return filter.stream().map(data -> EscrowAccountDetailResponse.builder()
                .id(data.getId())
                .escrowAccount(data.getEscrowAccount().getAccountNumber())
                .escrowAccountStatus(data.getEscrowAccount().getAccountStatus())
                .transactionType(data.getTransactionType())
                .mutationType(data.getMutationType())
                .nominalTransaction(data.getNominalTransaction())
                .beginBalance(data.getBeginBalance())
                .endBalance(data.getEndBalance())
                .description(data.getDescription())
                .transactionReference(data.getTransactionReference())
                .releaseAccountNumber(data.getReleaseAccountNumber())
                .build()
        ).collect(Collectors.toList());
    }



    public String updateEscrowAccountDetail (String id, EscrowAccountDetailRequest request, UserMetaData userMetaData) {
        EscrowAccountDetail escrowAccountDetail = escrowAccountDetailRepository.findById(id)
                .orElseThrow(() -> new  BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ESCROW_ACCOUNT_DETAIL_ID_NOT_FOUND));
        escrowAccountDetail.setDescription(request.getDescription());
        escrowAccountDetail.setReleaseAccountNumber(request.getReleaseAccountNumber());
        return "SUCCESS UPDATE ESCROW ACCOUNT DETAIL " +
                "| ID : " + escrowAccountDetail.getId() + " |";

    }

    @Override
    public String deleteEscrowAccountDetail(String id, UserMetaData userMetaData) {
        EscrowAccountDetail deleteEscrowAccountDetail = escrowAccountDetailRepository.findById(id).map(data -> {
            data.setIsDeleted(true);
            data.setCreateBy(userMetaData.getUserId());
            return escrowAccountDetailRepository.save(data);
        }).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ESCROW_ACCOUNT_DETAIL_ID_NOT_FOUND));
        return "SUCCESS DELETE ESCROW ACCOUNT DETAIL " +
                " ID : " + deleteEscrowAccountDetail.getId() + " |";
    }


    public String generateTrxCode() {
        LocalDateTime ldt = LocalDateTime.now();
        String dateTimeStr = ldt.format(TRX_REF_DATETIME_FORMATTER);
        String randomSuffix = String.format("%04d", random.nextInt(10000));
        return "TRX-" + dateTimeStr + "-" + randomSuffix;
    }



}
