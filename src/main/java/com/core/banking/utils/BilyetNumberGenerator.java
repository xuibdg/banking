package com.core.banking.utils;

import com.core.banking.entity.DepositAccount;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class BilyetNumberGenerator {

    public String generateBilyetNumber(DepositAccount depositAccount) {
        String accountNumber = depositAccount.getAccountNumber();
        String dateStr = depositAccount.getOpenedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return String.format("BLY-%s-%s", accountNumber, dateStr);
    }
}