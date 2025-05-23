package com.core.banking.service.impl;

import com.core.banking.dto.LoanTransactionRequest;
import com.core.banking.entity.LoanAccount;
import com.core.banking.entity.LoanRepaymentSchedule;
import com.core.banking.entity.LoanTransaction;
import com.core.banking.repository.LoanAccountRepository;
import com.core.banking.repository.LoanRepaymentScheduleRepository;
import com.core.banking.repository.LoanTransactionRepository;
import com.core.banking.service.LoanTransactionService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class LoanTransactionServiceImpl implements LoanTransactionService {

    @Autowired
    private LoanTransactionRepository loanTransactionRepository;

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Autowired
    private LoanRepaymentScheduleRepository loanRepaymentScheduleRepository;

    @Override
    public String createLoanTransaction(LoanTransactionRequest request) {
        LoanAccount loanAccount = loanAccountRepository.findById(request.getLoanAccountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        LoanRepaymentSchedule repaymentSchedule = null;
        if (request.getLoanRepaymentScheduleId() != null) {
            repaymentSchedule = loanRepaymentScheduleRepository.findById(request.getLoanRepaymentScheduleId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));
        }

        LoanTransaction transaction = new LoanTransaction();
        transaction.setLoanAccount(loanAccount);
        transaction.setLoanRepaymentSchedule(repaymentSchedule);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(request.getAmount());
        transaction.setPrincipalComponent(request.getPrincipalComponent());
        transaction.setInterestComponent(request.getInterestComponent());
        transaction.setFeeComponent(request.getFeeComponent());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());
        transaction.setReferenceNumber(request.getReferenceNumber());
        transaction.setCreatedAt(OffsetDateTime.now());

        loanTransactionRepository.save(transaction);

        return "Succes membuat transaksi loan";
    }


    @Override
    public List<LoanTransaction> findAll() {
        return loanTransactionRepository.findAll();
    }

    @Override
    public String updateLoanTransaction(String loanTransactionId, LoanTransactionRequest request) {
        LoanTransaction transaction = loanTransactionRepository.findById(loanTransactionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        LoanAccount loanAccount = loanAccountRepository.findById(request.getLoanAccountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        LoanRepaymentSchedule repaymentSchedule = null;
        if (request.getLoanRepaymentScheduleId() != null) {
            repaymentSchedule = loanRepaymentScheduleRepository.findById(request.getLoanRepaymentScheduleId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));
        }

        transaction.setLoanAccount(loanAccount);
        transaction.setLoanRepaymentSchedule(repaymentSchedule);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(request.getAmount());
        transaction.setPrincipalComponent(request.getPrincipalComponent());
        transaction.setInterestComponent(request.getInterestComponent());
        transaction.setFeeComponent(request.getFeeComponent());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());
        transaction.setReferenceNumber(request.getReferenceNumber());
        transaction.setUpdatedAt(OffsetDateTime.now());

        loanTransactionRepository.save(transaction);

        return "";
    }

    @Override
    public String deleteLoanTransaction(String loanTransactionId) {
        LoanTransaction transaction = loanTransactionRepository.findById(loanTransactionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        transaction.setIsDeleted(true);
        return "";
    }
}
