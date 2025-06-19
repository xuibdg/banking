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
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            writer.println("JOURNAL REPORT");
            writer.println();

            writer.println("Journal Code,Reference No,System Date,COA Code,COA Name,Debit,Credit,Description");

            BigDecimal totalDebit = BigDecimal.ZERO;
            BigDecimal totalCredit = BigDecimal.ZERO;

            for (JournalReportDto item : data) {
                BigDecimal debit = item.getDebit() != null ? item.getDebit() : BigDecimal.ZERO;
                BigDecimal credit = item.getCredit() != null ? item.getCredit() : BigDecimal.ZERO;

                writer.printf("%s,%s,%s,%s,%s,%.2f,%.2f,%s%n",
                        item.getJournalCode(),
                        item.getReferenceNumber(),
                        item.getSystemDate(),
                        item.getCoaCode(),
                        item.getCoaName(),
                        debit,
                        credit,
                        escapeCsv(item.getDescription())
                );

                totalDebit = totalDebit.add(debit);
                totalCredit = totalCredit.add(credit);
            }

            writer.println();

            writer.printf("TOTAL,,,,,%.2f,%.2f,%n", totalDebit, totalCredit);
        }

        return baos.toByteArray();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
