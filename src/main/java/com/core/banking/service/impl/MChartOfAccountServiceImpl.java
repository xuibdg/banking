package com.core.banking.service.impl;

import com.core.banking.dto.MChartOfAccountRequest;
import com.core.banking.dto.MChartOfAccountResponse;
import com.core.banking.entity.MChartOfAccount;
import com.core.banking.enums.AccountType;
import com.core.banking.repository.MChartOfAccountRepository;
import com.core.banking.service.MChartOfAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MChartOfAccountServiceImpl implements MChartOfAccountService {

    @Autowired
    private MChartOfAccountRepository mChartOfAccountRepository;


    @Override
    @Transactional
    public MChartOfAccountResponse create(MChartOfAccountRequest request) {
        // 1. Validasi Kode Unik
        if (mChartOfAccountRepository.existsByCode(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CoA code '" + request.getCode() + "' already exists");
        }

        // 2. Validasi penomoran berdasarkan Tipe Akun (dengan aturan baru)
        validateCodeByType(request.getCode(), request.getType());

        // 3. Validasi parentCode jika ada
        if (request.getParentCode() != null && !request.getParentCode().isEmpty()) {
            mChartOfAccountRepository.findByCode(request.getParentCode())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent CoA with code '" + request.getParentCode() + "' not found"));
        }

        MChartOfAccount coa = MChartOfAccount.builder()
                .code(request.getCode())
                .name(request.getName())
                .type(request.getType())
                .parentCode(request.getParentCode())
                .isActive(true)
                .build();

        mChartOfAccountRepository.save(coa);
        return toCoaResponse(coa);
    }

    @Override
    public MChartOfAccountResponse getById(String id) {
        MChartOfAccount mChartOfAccount = mChartOfAccountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CoA with id '" + id + "' not found"));
        return toCoaResponse(mChartOfAccount);
    }

    @Override
    public List<MChartOfAccountResponse> getAll() {
        return mChartOfAccountRepository.findAll().stream()
                .map(this::toCoaResponse)
                .collect(Collectors.toList());
    }

    private void validateCodeByType(String code, AccountType type) {
        if (code == null || code.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code cannot be empty");
        }

        String firstChar = code.substring(0, 1);
        List<String> aktivaPrefixes = List.of("1", "3", "7");
        List<String> pasivaPrefixes = List.of("2", "4", "5", "6", "8");

        switch (type) {
            case AKTIVA:
                if (!aktivaPrefixes.contains(firstChar)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid code for AKTIVA. Code must start with one of " + aktivaPrefixes);
                }
                break;
            case PASIVA:
                if (!pasivaPrefixes.contains(firstChar)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Invalid code for PASIVA. Code must start with one of " + pasivaPrefixes);
                }
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown AccountType");
        }
    }

    private MChartOfAccountResponse toCoaResponse(MChartOfAccount mChartOfAccount) {
        return MChartOfAccountResponse.builder()
                .id(mChartOfAccount.getId())
                .code(mChartOfAccount.getCode())
                .name(mChartOfAccount.getName())
                .type(mChartOfAccount.getType())
                .parentCode(mChartOfAccount.getParentCode())
                .isActive(mChartOfAccount.getIsActive())
                .build();
    }
}
