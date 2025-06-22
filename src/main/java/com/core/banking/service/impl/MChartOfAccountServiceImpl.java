package com.core.banking.service.impl;

import com.core.banking.dto.MChartOfAccountRequest;
import com.core.banking.dto.MChartOfAccountResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.MChartOfAccount;
import com.core.banking.enums.AccountType;
import com.core.banking.enums.Category;
import com.core.banking.repository.MChartOfAccountRepository;
import com.core.banking.service.MChartOfAccountService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
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
    public MChartOfAccountResponse create(MChartOfAccountRequest request, UserMetaData userMetaData) {
        validateCoaRequest(request);

        MChartOfAccount mChartOfAccount = MChartOfAccount.builder()
                .code(request.getCode())
                .name(request.getName())
                .type(request.getType())
                .category(request.getCategory())
                .parentCode(request.getParentCode())
                .isActive(true)
                .build();

        mChartOfAccountRepository.save(mChartOfAccount);
        return toCoaResponse(mChartOfAccount);
    }

    private void validateCoaRequest(MChartOfAccountRequest request) {
        if (mChartOfAccountRepository.existsByCode(request.getCode())) {
            throw new BusinessException(HttpStatus.CONFLICT, GlobalErrorMapping.CODE_ALREADY_EXISTS);
        }

        if (request.getParentCode() != null && !request.getParentCode().isEmpty()) {
            mChartOfAccountRepository.findByCode(request.getParentCode())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.PARENT_CODE_NOT_FOUND));
        }

        String code = request.getCode();
        Category category = request.getCategory();
        AccountType type = request.getType();
        String firstChar = code.substring(0, 1);

        switch (category) {
            case ASET:
                if (type != AccountType.AKTIVA) throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_COMBINATION_CATEGORY_TYPE);
                if (!List.of("1", "3", "7").contains(firstChar)) throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_CODE_FOR_CATEGORY);
                break;
            case LIABILITY:
                if (type != AccountType.PASIVA) throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_COMBINATION_CATEGORY_TYPE);
                if (!List.of("2", "4").contains(firstChar)) throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_CODE_FOR_CATEGORY);
                break;
            case EQUITY:
                if (type != AccountType.PASIVA) throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_COMBINATION_CATEGORY_TYPE);
                if (!"5".equals(firstChar)) throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_CODE_FOR_CATEGORY);
                break;
            case REVENUE:
                if (type != AccountType.LABA_RUGI) throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_COMBINATION_CATEGORY_TYPE);
                if (!"6".equals(firstChar)) throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_CODE_FOR_CATEGORY);
                break;
            case EXPENSE:
                if (type != AccountType.LABA_RUGI) throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_COMBINATION_CATEGORY_TYPE);
                if (!"8".equals(firstChar)) throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INVALID_CODE_FOR_CATEGORY);
                break;
            default:
                throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
        }

    }

    @Override
    public MChartOfAccountResponse getById(String id, UserMetaData userMetaData) {
        MChartOfAccount mChartOfAccount = mChartOfAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.ID_NOT_FOUND));
        return toCoaResponse(mChartOfAccount);
    }

    @Override
    public List<MChartOfAccountResponse> getAll() {
        return mChartOfAccountRepository.findAll().stream()
                .map(this::toCoaResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MChartOfAccountResponse update(MChartOfAccountRequest request, String id, UserMetaData userMetaData) {
        MChartOfAccount mChartOfAccount = mChartOfAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, GlobalErrorMapping.ID_NOT_FOUND));
        validateCoaRequest(request);
                mChartOfAccount.setCode(request.getCode());
                mChartOfAccount.setName(request.getName());
                mChartOfAccount.setType(request.getType());
                mChartOfAccount.setCategory(request.getCategory());
                mChartOfAccount.setParentCode(request.getParentCode());
                mChartOfAccount.setIsActive(true);
        mChartOfAccountRepository.save(mChartOfAccount);
        return toCoaResponse(mChartOfAccount);
    }

    private MChartOfAccountResponse toCoaResponse(MChartOfAccount mChartOfAccount) {
        return MChartOfAccountResponse.builder()
                .id(mChartOfAccount.getId())
                .code(mChartOfAccount.getCode())
                .name(mChartOfAccount.getName())
                .type(mChartOfAccount.getType())
                .category(mChartOfAccount.getCategory())
                .parentCode(mChartOfAccount.getParentCode())
                .isActive(mChartOfAccount.getIsActive())
                .build();
    }
}
