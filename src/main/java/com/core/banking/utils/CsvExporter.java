package com.core.banking.utils;

import com.core.banking.dto.JournalReportDto;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvExporter {

    public byte[] export(List<JournalReportDto> data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            writer.println("JOURNAL REPORT\n");

            writer.printf(
                    "%-15s %-18s %-12s %-5s %-18s %12s %12s %-25s%n",
                    "Journal Code", "Ref No", "Date", "COA", "COA Name", "Debit", "Credit", "Description"
            );

            BigDecimal totalDebit = BigDecimal.ZERO;
            BigDecimal totalCredit = BigDecimal.ZERO;

            for (JournalReportDto item : data) {
                BigDecimal debit = item.getDebit() != null ? item.getDebit() : BigDecimal.ZERO;
                BigDecimal credit = item.getCredit() != null ? item.getCredit() : BigDecimal.ZERO;

                writer.printf(
                        "%-15s %-18s %-12s %-5s %-18s %12.2f %12.2f %-25s%n",
                        trimTo(item.getJournalCode(), 15),
                        trimTo(item.getReferenceNumber(), 18),
                        item.getSystemDate(),
                        item.getCoaCode(),
                        trimTo(item.getCoaName(), 18),
                        debit,
                        credit,
                        trimTo(item.getDescription(), 25)
                );

                totalDebit = totalDebit.add(debit);
                totalCredit = totalCredit.add(credit);
            }

            writer.println();

            writer.printf(
                    "%-15s %-18s %-12s %-5s %-18s %12.2f %12.2f %-25s%n",
                    "TOTAL", "", "", "", "", totalDebit, totalCredit, ""
            );
        }

        return baos.toByteArray();
    }

    private String trimTo(String value, int max) {
        if (value == null) return "";
        value = value.replaceAll("[\\t\\n\\r]", " ");
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }
}
