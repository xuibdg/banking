package com.core.banking.client;

import com.core.banking.config.FeignClientConfig;
import com.core.banking.dto.EscrowRequestToPGRequest;
import com.core.banking.utils.exception.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "paymentGatewayClient",
        url = "${pg.transaction.url}",
        path = "/bill-payment",
        configuration = FeignClientConfig.class
)
public interface PaymentGatewayClient {
    @PostMapping(value = "/transbackV2", consumes = "application/json", produces = "application/json")
    BaseResponse<String> pgTransback(@RequestBody EscrowRequestToPGRequest request);

    @PostMapping(value = "/process-bill-payment", consumes = "application/json", produces = "application/json")
    BaseResponse<String> pgBillPayement(@RequestBody EscrowRequestToPGRequest request);



}
