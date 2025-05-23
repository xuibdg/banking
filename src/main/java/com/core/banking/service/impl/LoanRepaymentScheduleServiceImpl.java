package com.core.banking.service.impl;

import com.core.banking.dto.LoanRepaymentScheduleRequest;
import com.core.banking.entity.LoanAccount;
import com.core.banking.entity.LoanRepaymentSchedule;
import com.core.banking.enums.LoanRepaymentStatus;
import com.core.banking.repository.LoanAccountRepository;
import com.core.banking.repository.LoanRepaymentScheduleRepository;
import com.core.banking.service.LoanRepaymentScheduleService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class LoanRepaymentScheduleServiceImpl implements LoanRepaymentScheduleService {
    @Autowired
    private LoanRepaymentScheduleRepository loanRepaymentScheduleRepository;

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Override
    public List<LoanRepaymentSchedule> findAll() {

        return loanRepaymentScheduleRepository.findAll();
    }

    @Override
    public String createLoanRepaymentSchedule(LoanRepaymentScheduleRequest request) {
        LoanAccount loanAccount = loanAccountRepository.findById(request.getLoanAccountId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        LoanRepaymentSchedule schedule = new LoanRepaymentSchedule();
        schedule.setLoanAccount(loanAccount);
        schedule.setInstallmentNumber(request.getInstallmentNumber());
        schedule.setDueDate(request.getDueDate());
        schedule.setPrincipalDue(request.getPrincipalDue());
        schedule.setInterestDue(request.getInterestDue());

        BigDecimal totalDue = request.getPrincipalDue().add(request.getInterestDue());
        schedule.setTotalDue(totalDue);

        schedule.setPrincipalPaid(request.getPrincipalPaid());
        schedule.setInterestPaid(request.getInterestPaid());
        schedule.setAmountPaid(request.getAmountPaid());
        schedule.setPaymentDate(request.getPaymentDate());
        schedule.setPaymentStatus(LoanRepaymentStatus.PENDING);
        schedule.setCreatedAt(OffsetDateTime.now());

        loanRepaymentScheduleRepository.save(schedule);
        return schedule.getLoanRepaymentScheduleId();
    }

    @Override
    public String updateLoanRepaymentSchedule(String loanRepaymentScheduleId, LoanRepaymentScheduleRequest request) {
        LoanRepaymentSchedule schedule = loanRepaymentScheduleRepository.findById(loanRepaymentScheduleId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

        if (request.getLoanAccountId() != null) {
            LoanAccount loanAccount = loanAccountRepository.findById(request.getLoanAccountId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));
            schedule.setLoanAccount(loanAccount);
        }

        schedule.setInstallmentNumber(request.getInstallmentNumber());
        schedule.setDueDate(request.getDueDate());
        schedule.setPrincipalDue(request.getPrincipalDue());
        schedule.setInterestDue(request.getInterestDue());
        schedule.setTotalDue(request.getTotalDue());
        schedule.setPrincipalPaid(request.getPrincipalPaid());
        schedule.setInterestPaid(request.getInterestPaid());
        schedule.setAmountPaid(request.getAmountPaid());
        schedule.setPaymentDate(request.getPaymentDate());

        if (request.getPaymentStatus() != null) {
            schedule.setPaymentStatus(request.getPaymentStatus());
        }

        schedule.setUpdatedAt(OffsetDateTime.now());

        loanRepaymentScheduleRepository.save(schedule);

        return schedule.getLoanRepaymentScheduleId();
    }


    @Override
    public String deleteLoanRepaymentSchedule(String loanRepaymentScheduleId) {
        LoanRepaymentSchedule schedule = loanRepaymentScheduleRepository.findById(loanRepaymentScheduleId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_NOT_FOUND));

//        schedule.setIsDeleted(true);
        loanRepaymentScheduleRepository.save(schedule);
        return "";
    }
}
