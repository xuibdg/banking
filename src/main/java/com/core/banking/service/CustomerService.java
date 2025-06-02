package com.core.banking.service;

import com.core.banking.dto.CustomerRequest;
import com.core.banking.dto.CustomerResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.enums.CustomerStatus;

import java.util.List;

public interface CustomerService {
    String registerNewCustomer(CustomerRequest request, UserMetaData userMetaData);
    CustomerResponse viewCustomerDetails(String id, String nik);
    String updateCustomerInformation(String id, String nik, CustomerRequest request, UserMetaData userMetaData);
    String changeCustomerStatus(String id, CustomerStatus newStatus, UserMetaData userMetaData);
}
