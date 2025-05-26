package com.core.banking.service.impl;

import com.core.banking.dto.EscrowAccountDetailRequest;
import com.core.banking.dto.EscrowAccountDetailResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.EscrowAccount;
import com.core.banking.entity.EscrowAccountDetail;
import com.core.banking.enums.EscrowAccountStatus;
import com.core.banking.enums.EscrowTransactionType;
import com.core.banking.enums.MutationType;
import com.core.banking.repository.EscrowAccountDetailRepository;
import com.core.banking.repository.EscrowAccountRepository;
import com.core.banking.service.EscrowAccountDetailService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class EscrowAccountDetailServiceImpl implements EscrowAccountDetailService{

    @Autowired
    private EscrowAccountDetailRepository escrowAccountDetailRepository;

    @Autowired
    private EscrowAccountRepository escrowAccountRepository;

    @Override
    @Transactional
    public String createEscrowAccountDetail (EscrowAccountDetailRequest request, UserMetaData userMetaData) {
        EscrowAccount escrowAccount = escrowAccountRepository.findById(request.getEscrowAccount())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
        if (request.getNominalTransaction() == null || request.getNominalTransaction().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.NOMINAL_NOT_ENOUGH);
        }
        BigDecimal beginBalance = escrowAccount.getCurrentBalance();
        BigDecimal endBalance = beginBalance;
        MutationType mutationType;

        EscrowTransactionType transactionType = request.getTransactionType();

        if (transactionType == EscrowTransactionType.FUNDING) {
            if (!escrowAccount.getAccountStatus().equals(EscrowAccountStatus.PENDING_FUNDING)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
            }
            if (request.getReleaseAccountNumber() != null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
            }

            mutationType = MutationType.CREDIT;
            endBalance = beginBalance.add(request.getNominalTransaction());
            escrowAccount.setAccountStatus(EscrowAccountStatus.FUNDED);

        } else if (transactionType == EscrowTransactionType.RELEASE_TO_BENEFICIARY
                || transactionType == EscrowTransactionType.RETURN_TO_PAYER
                || transactionType == EscrowTransactionType.FEE_DEBIT) {

            if (request.getReleaseAccountNumber() == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
            }

            if (!escrowAccount.getAccountStatus().equals(EscrowAccountStatus.FUNDED)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
            }

            if (beginBalance.compareTo(request.getNominalTransaction()) < 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.NOMINAL_NOT_ENOUGH);
            }

            mutationType = MutationType.DEBIT;
            endBalance = beginBalance.subtract(request.getNominalTransaction());

            if (transactionType == EscrowTransactionType.RELEASE_TO_BENEFICIARY
                    || transactionType ==  EscrowTransactionType.RETURN_TO_PAYER) {
                escrowAccount.setAccountStatus(EscrowAccountStatus.RELEASED);
            }

        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
        }

        EscrowAccountDetail escrowAccountDetail = EscrowAccountDetail.builder()
                .escrowAccount(escrowAccount)
                .transactionType(transactionType)
                .mutationType(mutationType)
                .nominalTransaction(request.getNominalTransaction())
                .beginBalance(beginBalance)
                .endBalance(endBalance)
                .description(request.getDescription())
                .transactionReference(request.getTransactionReference())
                .releaseAccountNumber(request.getReleaseAccountNumber())
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .transactionAt(new Timestamp(System.currentTimeMillis()))
                .isDeleted(false)
                .build();
        escrowAccountDetailRepository.save(escrowAccountDetail);
        escrowAccount.setCurrentBalance(endBalance);
        escrowAccountRepository.save(escrowAccount);
        return "SUCCESS CREATE NEW ESCROW ACCOUNT DETAIL : TRANSACTION SUCCESS";

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

    public String updateEscrowAccountDetail (String id, EscrowAccountDetailRequest request) {
        EscrowAccountDetail escrowAccountDetail = escrowAccountDetailRepository.findById(id)
                .orElseThrow(() -> new  BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
        escrowAccountDetail.setDescription(request.getDescription());
        escrowAccountDetail.setTransactionReference(request.getTransactionReference());
        escrowAccountDetail.setReleaseAccountNumber(request.getReleaseAccountNumber());
        return "SUCCESS UPDATE ESCROW ACCOUNT DETAIL";

    }

    @Override
    public String deleteEscrowAccountDetail(String id) {
        escrowAccountDetailRepository.findById(id).map(data -> {
            data.setIsDeleted(true);
            escrowAccountDetailRepository.save(data);
            return data;
        });
        return "SUCCESS DELETE ESCROW ACCOUNT DETAIL";
    }



}
