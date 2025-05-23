package com.core.banking.service.impl;

import com.core.banking.dto.EscrowAccountRequest;
import com.core.banking.dto.EscrowAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.Customer;
import com.core.banking.entity.EscrowAccount;
import com.core.banking.enums.EscrowAccountStatus;
import com.core.banking.repository.CustomerRepository;
import com.core.banking.repository.EscrowAccountRepository;
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
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class EscrowAccountServiceImpl implements EscrowAccountService {

    @Autowired
    private EscrowAccountRepository escrowAccountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public String createEscrowAccount(EscrowAccountRequest request, UserMetaData userMetaData) {
        Customer payerId = customerRepository.findById(request.getPayerCustomerId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
        Customer beneficiaryId = customerRepository.findById(request.getBeneficiaryCustomerId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
        EscrowAccount escrowAccount = EscrowAccount.builder()
                .accountNumber(generateAccountNumber())
                .purpose(request.getPurpose())
                .payerCustomerId(payerId)
                .beneficiaryCustomerId(beneficiaryId)
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
                    .payerCustomerId(data.getPayerCustomerId().getId())
                    .payerCustomerName(data.getPayerCustomerId().getFullName())
                    .beneficiaryCustomerId(data.getBeneficiaryCustomerId().getId())
                    .beneficiaryCustomerName(data.getBeneficiaryCustomerId().getFullName())
                    .build();
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public String updateEscrowAccount(String id, EscrowAccountRequest request) {
        escrowAccountRepository.findById(id).map(data -> {
            Customer payerId = customerRepository.findById(request.getPayerCustomerId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
            Customer beneficiaryId = customerRepository.findById(request.getBeneficiaryCustomerId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM));
            data.setPurpose(request.getPurpose());
            data.setPayerCustomerId(payerId);
            data.setBeneficiaryCustomerId(beneficiaryId);
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
