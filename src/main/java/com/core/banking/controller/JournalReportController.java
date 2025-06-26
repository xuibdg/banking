package com.core.banking.controller;

import com.core.banking.dto.JournalReportDto;
import com.core.banking.service.JournalReportService;
import com.core.banking.utils.CsvExporter;
import com.core.banking.utils.PdfExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/report/journal")
@RequiredArgsConstructor
public class JournalReportController {

    @Autowired
    private JournalReportService journalReportService;

    private final PdfExporter pdfExporter;

    private final CsvExporter csvExporter;

    @GetMapping("/{referenceNumber}")
    public List<JournalReportDto> getReportByReference(@PathVariable String referenceNumber) {
        return journalReportService.getJournalByReference(referenceNumber);
    }

    @GetMapping(value = "/{referenceNumber}/csv", produces = "text/csv")
    public Mono<ResponseEntity<byte[]>> generateCsv(@PathVariable String referenceNumber) {
        List<JournalReportDto> data = journalReportService.getJournalByReference(referenceNumber);
        byte[] csvBytes;
        try {
            csvBytes = csvExporter.export(data);
        } catch (IOException e) {
            return Mono.error(new RuntimeException("Gagal membuat CSV", e));
        }

        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + referenceNumber + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes));
    }

    @GetMapping(value = "/{referenceNumber}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public Mono<ResponseEntity<byte[]>> generatePdf(@PathVariable String referenceNumber) {
        List<JournalReportDto> data = journalReportService.getJournalByReference(referenceNumber);
        byte[] pdfBytes;
        try {
            pdfBytes = pdfExporter.export(data);
        } catch (IOException e) {
            return Mono.error(new RuntimeException("Gagal membuat PDF", e));
        }

        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + referenceNumber + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes));
    }
}
