package com.core.banking.utils;

import com.core.banking.repository.DepositAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class DepositAccountNumberGenerator {

    @Autowired
    private DepositAccountRepository depositAccountRepository;

    private static final String PREFIX = "4519";
    private static final Random RANDOM = new Random();

    /**
     * Menghasilkan nomor rekening deposito yang unik dengan format:
     * 4519 + YYYYMM + 6 digit random
     *
     * @return nomor rekening yang unik
     */
    public String generateDepositAccountNumber() {
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomDigits = String.format("%06d", RANDOM.nextInt(1000000));
        String accountNumber = PREFIX + yearMonth + randomDigits;

        // Cek apakah nomor rekening sudah ada di database
        if (depositAccountRepository.existsByAccountNumber(accountNumber)) {
            // Jika sudah ada, generate ulang
            return generateDepositAccountNumber();
        }

        return accountNumber;
    }
}