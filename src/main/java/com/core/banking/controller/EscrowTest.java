//package com.core.banking.controller;
//
//import com.core.banking.dto.PGTransactionFromEscrowRequest;
//import com.core.banking.service.impl.PgTransactionToEscrowClient;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("api/test-escrow")
//@RequiredArgsConstructor
//public class EscrowTest {
//
//    private final PgTransactionToEscrowClient pgTransactionToEscrowClient;
//
//    @GetMapping("/ping-pg")
//    public String testEscrow() {
//        return pgTransactionToEscrowClient.pingPgTransaction();
//    }
//
//    @PostMapping("/send-to-pg")
//    public String sendToPgTransaction(@RequestBody PGTransactionFromEscrowRequest request) {
//        String response = pgTransactionToEscrowClient.escrowToPGTransaction(request);
//        return "SUCCESS TO SEND REQUEST PAYMENT GATEAWAY: " + response;
//    }
//}
