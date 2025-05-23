package com.core.banking.service.impl;

import com.core.banking.dto.CustomerRequest;
import com.core.banking.dto.CustomerResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.Customer;
import com.core.banking.enums.CustomerStatus;
import com.core.banking.enums.LoanAccountStatus;
import com.core.banking.enums.SavingAccountStatus;
import com.core.banking.repository.CustomerRepository;
import com.core.banking.repository.LoanAccountRepository;
import com.core.banking.repository.SavingAccountRepository;
import com.core.banking.service.CustomerService;
import com.core.banking.utils.exception.BusinessException;
import com.core.banking.utils.exception.GlobalErrorMapping;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SavingAccountRepository savingAccountRepository;

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    public String registerNewCustomer (CustomerRequest request, UserMetaData userMetaData){
        String nik = validateNik(request.getNik());
        if(!validateAge(request.getDateOfBirth())){
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.INSUFFICIENT_AGE);}

        String email = validateEmail(request.getEmail());

        String phoneNumber = validatePhoneNumber(request.getPhoneNumber());

        Customer customer = Customer.builder()
                .nik(nik)
                .fullName(request.getFullName())
                .address(request.getAddress())
                .phoneNumber(phoneNumber)
                .email(email)
                .dateOfBirth(request.getDateOfBirth())
                .customerStatus(CustomerStatus.ACTIVE)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .isSpecialAdministrative(false)
                .build();

        customerRepository.save(customer);
        return "REGISTER NEW CUSTOMER SUCCESS";
    }

    public CustomerResponse viewCustomerDetails(String id, String nik) {

        Optional<Customer> optionalCustomer;
        if (id != null) {
            optionalCustomer = customerRepository.findById(id);
        } else if (nik != null) {
            optionalCustomer = customerRepository.findByNik(nik);
        } else {

            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
        }

        Customer customer = optionalCustomer.orElseThrow(()-> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_USER_NOT_FOUND));

        return CustomerResponse.builder()
                .customerId(customer.getId())
                .nik(customer.getNik())
                .fullName(customer.getFullName())
                .address(customer.getAddress())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .customerStatus(customer.getCustomerStatus())
                .createdAt(Timestamp.valueOf(customer.getCreatedAt()
                        .toLocalDateTime()))
                .build();

    }

    public String updateCustomerInformation(String id, String nik, CustomerRequest request) {

        Optional<Customer> customerOpt;

        if (id != null && !id.trim().isEmpty()) {
            customerOpt = customerRepository.findById(id);
        } else if (nik != null && !nik.trim().isEmpty()) {
            customerOpt = customerRepository.findByNik(nik);
        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM);
        }
        Customer customer = customerOpt
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_USER_NOT_FOUND));

        if(customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_INACTIVE);
        }

        if(customerRepository.existsByEmailAndIdNot(request.getEmail(),id)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_ALREADY_EXIST);
        }
        if(customerRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(),id)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_ALREADY_EXIST);
        }

        String email = validateEmail(request.getEmail());

        String phoneNumber = validatePhoneNumber(request.getPhoneNumber());

            customer.setNik(request.getNik());
            customer.setAddress(request.getAddress());
            customer.setPhoneNumber(phoneNumber);
            customer.setEmail(email);
            customer.setUpdatedAt(Timestamp.valueOf(customer.getCreatedAt()
                    .toLocalDateTime()));
            customerRepository.save(customer);

        return "CUSTOMER INFORMATION UPDATED";
    }
    public String changeCustomerStatus(String id, CustomerStatus newStatus){

        customerRepository.findById(id).map(customer ->{
            if (customer.getIsSpecialAdministrative().equals(false)) {
                if(newStatus == CustomerStatus.INACTIVE || newStatus == CustomerStatus.CLOSED) {
                    boolean hasSavingAccount = savingAccountRepository.existsByCustomer_IdAndAccountStatus(customer.getId(), SavingAccountStatus.ACTIVE);
                    if (hasSavingAccount) {
                        throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ACCOUNT_CLOSE_RESTRICTED);
                    }

                    boolean hasloanAccount = loanAccountRepository.existsByCustomer_IdAndAccountStatus(customer.getId(), LoanAccountStatus.ACTIVE);
                    if (hasloanAccount) {
                        throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ACCOUNT_CLOSE_RESTRICTED);
                    }
                }
            } else {
                customer.setCustomerStatus(newStatus);
            }
                return customerRepository.save(customer);
        }).orElseThrow(()-> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_USER_NOT_FOUND));
        return "CUSTOMER STATUS CHANGED";
    }


    public String validateNik(String nik) {

        if (nik == null || !nik.matches("^\\d{16}$")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_NOT_FOUND_CUSTOM);
        }

        Customer nikNew = customerRepository.findByNik(nik).orElse(null);

        if (Objects.nonNull(nikNew)){
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DATA_ALREADY_EXIST);
        } return nik;
    }

    public Boolean validateAge (LocalDate dateOfBirth) {
        return calculateAge(dateOfBirth) >= 17;

    }

    private int calculateAge(LocalDate dateOfBirth) {
        LocalDate today = LocalDate.now();
        return Period.between(dateOfBirth,today).getYears();
    }

    public String validateEmail(String email) {
        if (email == null || (!email.endsWith(".com") && !email.endsWith(".co.id"))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
        } return email;
    }

    public String validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("^\\+628\\d{8,11}$")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.RULE_NOT_FOUND);
        } return phoneNumber;
    }
}
