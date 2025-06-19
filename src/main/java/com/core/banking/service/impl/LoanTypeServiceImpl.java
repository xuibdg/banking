package com.core.banking.service.impl;

import com.core.banking.dto.UserMetaData;
import com.core.banking.dto.LoanTypeRequest;
import com.core.banking.entity.LoanType;
import com.core.banking.repository.LoanTypeRepository;
import com.core.banking.service.LoanTypeService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanTypeServiceImpl implements LoanTypeService {

    @Autowired
    private LoanTypeRepository loanTypeRepository;

    @Override
    public String createLoanType(LoanTypeRequest request, UserMetaData userMetaData) {
        LoanType type = new LoanType();
        type.setLoanTypeId(UUID.randomUUID().toString());
        type.setTypeName(request.getTypeName());
        type.setDescription(request.getDescription());
        type.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        loanTypeRepository.save(type);
        return "type ID: " + type.getLoanTypeId() + ", type name : " + type.getTypeName() + ", description : " + type.getDescription();
    }

    @Override
    public List<LoanType> findAll() {
        return loanTypeRepository.findAll();
    }

    @Override
    public String updateLoanType(String loanTypeId, LoanTypeRequest request, UserMetaData userMetaData) {
        LoanType type = loanTypeRepository.findById(loanTypeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_TYPE_NOT_FOUND));

        type.setTypeName(request.getTypeName());
        type.setDescription(request.getDescription());
        type.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        loanTypeRepository.save(type);
        return "Update type name : " + type.getTypeName() + ", description : " + type.getDescription();
    }

    @Override
    public String deleteLoanType(String loanTypeId, UserMetaData userMetaData) {
        LoanType type = loanTypeRepository.findById(loanTypeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ID_LOAN_TYPE_NOT_FOUND));

        type.setIsDeleted(true);
        loanTypeRepository.save(type);
        return "SUCCES DELETE LOAN TYPE";
    }
}
