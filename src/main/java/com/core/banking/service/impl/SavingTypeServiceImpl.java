package com.core.banking.service.impl;

import com.core.banking.dto.SavingTypeRequest;
import com.core.banking.entity.SavingType;
import com.core.banking.repository.SavingTypeRepository;
import com.core.banking.service.SavingTypeService;
import com.core.banking.utils.exception.GlobalErrorMapping;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class SavingTypeServiceImpl implements SavingTypeService {
    @Autowired
    private SavingTypeRepository savingTypeRepository;

    @Override
    public List<SavingType> findAll() {
        return savingTypeRepository.findAll();
    }

    @Override
    public SavingType createSavingType(SavingTypeRequest savingType) {
        if (savingTypeRepository.existsByTypeName(savingType.getTypeName())) {
            throw new IllegalArgumentException(GlobalErrorMapping.DATA_ALREADY_EXIST+" ( "+savingType.getTypeName()+" ) ");
        }

        savingType.setCreatedAt(Timestamp.from(Instant.now()));
        savingType.setUpdatedAt(Timestamp.from(Instant.now()));
        return savingTypeRepository.save(SavingType.builder()
                .typeName(savingType.getTypeName())
                .description(savingType.getDescription())
                .build());
    }

    @Override
    public List<SavingType> getAllSavingTypes() {
        return savingTypeRepository.findAll();
    }

    @Override
    public Optional<SavingType> getSavingTypeById(String id) {
        return savingTypeRepository.findById(id);
    }

    @Override
    @Transactional
    public SavingType updateSavingType(String id, SavingTypeRequest updatedSavingType) {
        return savingTypeRepository.findById(id)
                .map(existingSavingType -> {
                    if (!existingSavingType.getTypeName().equals(updatedSavingType.getTypeName()) &&
                            savingTypeRepository.existsByTypeName(updatedSavingType.getTypeName())) {
                        throw new IllegalArgumentException(GlobalErrorMapping.DATA_ALREADY_EXIST +""+ updatedSavingType.getTypeName());
                    }

                    existingSavingType.setTypeName(updatedSavingType.getTypeName());
                    existingSavingType.setDescription(updatedSavingType.getDescription());
                    existingSavingType.setUpdatedAt(Timestamp.from(Instant.now()));
                    return savingTypeRepository.save(existingSavingType);
                })
                .orElseThrow(() -> new IllegalArgumentException(GlobalErrorMapping.NOT_FOUND_ID+""+id));
    }

    @Override
    public String deleteSavingType(String id) {
        savingTypeRepository.findById(id).map(data -> {
            data.setIsDeleted(Boolean.TRUE);
            savingTypeRepository.save(data);
            return data;
        });

        return "SUCCESS DELETED";
    }
}
