package com.core.banking.service.impl;

import com.core.banking.dto.CustomerRequest;
import com.core.banking.dto.CustomerResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.entity.Customer;
import com.core.banking.enums.CustomerStatus;
import com.core.banking.repository.CustomerRepository;
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

    public String registerNewCustomer (CustomerRequest request, UserMetaData userMetaData){
        String nik = validateNik(request.getNik());
        if(!validateAge(request.getDateOfBirth())){
            throw new IllegalArgumentException("MINIMUM AGE MUST BE 17 YEARS OLD");}

        Customer customer = Customer.builder()
                .nik(nik)
                .fullName(request.getFullName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .dateOfBirth(request.getDateOfBirth())
                .customerStatus(CustomerStatus.ACTIVE)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        customerRepository.save(customer);
        return "REGISTER NEW CUSTOMER SUCCESS";
    }

    public CustomerResponse viewCustomerDetails(String id, String nik) {

        Optional<Customer> optionalCustomer;
        if (id != null) {
            optionalCustomer = customerRepository.findById(String.valueOf(id));
        } else if (nik != null) {
            optionalCustomer = customerRepository.findByNik(nik);
        } else {
            throw new IllegalArgumentException("ID atau NIK harus diisi");
        }

        Customer customer = optionalCustomer.orElseThrow(()-> new RuntimeException("CUSTOMER NOT FOUND"));

        return CustomerResponse.builder()
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
        Customer customer = customerRepository.findByIdOrNik(id,nik)
                        .orElseThrow(() -> new RuntimeException("Customer tidak ditemukan"));

        if(customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
            throw new RuntimeException("Customer tidak dalam status aktif, tidak bisa diperbarui");
        }

        if(customerRepository.existsByEmailAndIdNot(request.getEmail(),id)) {
            throw new RuntimeException("Email sudah digunakan oleh customer lain");
        }
        if(customerRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(),id)) {
            throw new RuntimeException("Nomor HP sudah digunakan oleh customer lain");
        }

            customer.setAddress(request.getAddress());
            customer.setPhoneNumber(request.getPhoneNumber());
            customer.setEmail(request.getEmail());
            customer.setUpdatedAt(Timestamp.valueOf(customer.getCreatedAt()
                    .toLocalDateTime()));
            customerRepository.save(customer);

        return "CUSTOMER INFORMATION UPDATED";
    }
//    public String changeCustomerStatus(String id, CustomerStatus newStatus){
//        customerRepository.findById(String.valueOf(id)).map(customer ->{
//            customer.setCustomerStatus(newStatus);
//            return customerRepository.save(customer);
//        }).orElseThrow(()-> new  RuntimeException("CUSTOMER NOT FOUND"));
//        return "CUSTOMER STATUS CHANGED";
//    }






    public String validateNik(String nik) {
        Customer nikNew = customerRepository.findByNik(nik).orElse(null);

        if (Objects.nonNull(nikNew)){
            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_ALREADY_EXIST);
        }return String.valueOf(nik);
    }

    public Boolean validateAge (LocalDate dateOfBirth) {
        return calculateAge(dateOfBirth) >= 17;

    }

    private int calculateAge(LocalDate dateOfBirth) {
        LocalDate today = LocalDate.now();
        return Period.between(dateOfBirth,today).getYears();

    }
}
