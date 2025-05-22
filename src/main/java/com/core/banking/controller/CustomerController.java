package com.core.banking.controller;

import com.core.banking.config.CurrentUser;
import com.core.banking.dto.CustomerRequest;
import com.core.banking.dto.UserMetaData;
import com.core.banking.service.CustomerService;
import com.core.banking.service.MUserService;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
