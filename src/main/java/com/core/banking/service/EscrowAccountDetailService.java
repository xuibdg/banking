package com.core.banking.service;

import com.core.banking.entity.EscrowAccountDetail;
import java.util.List;

public interface EscrowAccountDetailService {
    List<EscrowAccountDetail> findAll();
}
