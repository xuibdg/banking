package com.core.banking.utils;

import com.core.banking.dto.JournalReportDto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Component;

import org.apache.commons.io.IOUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;

@Component
public class PdfExporter {

    public byte[] export(List<JournalReportDto> data) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            InputStream is = getClass().getClassLoader().getResourceAsStream("icon_b2camp.png");
            if (is == null) {
                System.err.println("Gambar tidak ditemukan di resource!");
            } else {
                Image logo = Image.getInstance(IOUtils.toByteArray(is));
                logo.scaleToFit(100, 50);
                logo.setAlignment(Image.ALIGN_CENTER);
                document.add(logo);
            }


            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Journal Report", fontTitle);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            JournalReportDto firstItem = data.isEmpty() ? null : data.get(0);
            if (firstItem != null) {
                Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
                Paragraph info = new Paragraph(
                        "Reference Number: " + firstItem.getReferenceNumber() + "\n" +
                                "System Date     : " + firstItem.getSystemDate(),
                        infoFont
                );
                info.setSpacingAfter(10f); // Jarak ke bawah
                document.add(info);
            }

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            float[] columnWidths = {2f, 3f, 3f, 2f, 4f, 2f, 2f, 4f};
            table.setWidths(columnWidths);

            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);

            table.addCell(new PdfPCell(new Phrase("Journal Code", headFont)));
            table.addCell(new PdfPCell(new Phrase("Reference No", headFont)));
            table.addCell(new PdfPCell(new Phrase("System Date", headFont)));
            table.addCell(new PdfPCell(new Phrase("COA Code", headFont)));
            table.addCell(new PdfPCell(new Phrase("COA Name", headFont)));
            table.addCell(new PdfPCell(new Phrase("Debit", headFont)));
            table.addCell(new PdfPCell(new Phrase("Credit", headFont)));
            table.addCell(new PdfPCell(new Phrase("Description", headFont)));

            BigDecimal totalDebit = BigDecimal.ZERO;
            BigDecimal totalCredit = BigDecimal.ZERO;

            for (JournalReportDto item : data) {
                BigDecimal debit = item.getDebit() != null ? item.getDebit() : BigDecimal.ZERO;
                BigDecimal credit = item.getCredit() != null ? item.getCredit() : BigDecimal.ZERO;

                table.addCell(item.getJournalCode());
                table.addCell(item.getReferenceNumber());
                table.addCell(String.valueOf(item.getSystemDate()));
                table.addCell(item.getCoaCode());
                table.addCell(item.getCoaName());
                table.addCell(debit.toString());
                table.addCell(credit.toString());
                table.addCell(item.getDescription());

                totalDebit = totalDebit.add(debit);
                totalCredit = totalCredit.add(credit);
            }

            PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total", headFont));
            totalLabelCell.setColspan(5);
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(totalLabelCell);

            PdfPCell totalDebitCell = new PdfPCell(new Phrase(totalDebit.toString(), headFont));
            totalDebitCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalDebitCell);

            PdfPCell totalCreditCell = new PdfPCell(new Phrase(totalCredit.toString(), headFont));
            totalCreditCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalCreditCell);

            table.addCell(new PdfPCell(new Phrase("")));

            table.addCell("");

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IOException("Gagal Membuat PDF", e);
        }
    }
}
