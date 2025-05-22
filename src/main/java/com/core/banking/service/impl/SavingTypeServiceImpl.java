package com.core.banking.service.impl;

import com.core.banking.entity.SavingType;
import com.core.banking.repository.SavingTypeRepository;
import com.core.banking.service.SavingTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SavingTypeServiceImpl implements SavingTypeService {
    @Autowired
    private SavingTypeRepository savingTypeRepository;

    @Override
    public List<SavingType> findAll() {
        return savingTypeRepository.findAll();
    }
}
