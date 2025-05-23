package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.CustomerRequest;
import com.core.banking.dto.CustomerResponse;
import com.core.banking.dto.UserMetaData;
import com.core.banking.enums.CustomerStatus;
import com.core.banking.service.CustomerService;
import com.core.banking.service.MUserService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.core.banking.controller.BaseCRUDController.buildSuccessResponse;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MUserService mUserService;

    @PostMapping
    BaseResponse<String> registerNewCustomer(@RequestBody CustomerRequest request,
                                            @CurrentUser UserMetaData userMetaData) {
        return buildSuccessResponse(customerService.registerNewCustomer(request, userMetaData));
    }

    @GetMapping("/details")
    BaseResponse <CustomerResponse> viewCustomerDetails(@RequestParam(required = false) String id,
                                                        @RequestParam(required = false) String nik) {

        return buildSuccessResponse(customerService.viewCustomerDetails(id,nik));
    }

    @PutMapping("/update-info")
    BaseResponse <String> updateCustomerInformation(@RequestParam(required = false) String id,
                                                    @RequestParam(required = false) String nik,
                                                    @RequestBody CustomerRequest request) {
        return buildSuccessResponse(customerService.updateCustomerInformation(id,nik, request));
    }

    @PutMapping("/change-status/{id}")
    BaseResponse<String> changeCustomerStatus(@PathVariable String id, @RequestParam CustomerStatus newStatus){
        return buildSuccessResponse(customerService.changeCustomerStatus(id, newStatus));
    }
}

