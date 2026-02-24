package com.pinelabs.chargeslip.pdf.renderer.element;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import com.pinelabs.chargeslip.pdf.exception.PdfRenderException;
import com.pinelabs.chargeslip.pdf.model.LayoutElement;
import com.pinelabs.chargeslip.pdf.renderer.ElementRenderer;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Component
public class QrElementRenderer implements ElementRenderer {

    private static final float QR_SIZE = 120f;
    private static final float BLOCK_HEIGHT = 125f;

    @Override
    public boolean supports(LayoutElement.Type type) {
        return type == LayoutElement.Type.QR;
    }

    @Override
    public float render(
            PDDocument doc,
            PDPageContentStream cs,
            float currentY,
            LayoutElement element
    ) {

        try {

            String qrText = element.getQrData();

            if (qrText == null || qrText.isBlank()) {

                log.warn("QR data empty, skipping");

                return currentY;
            }

            // ZXing encode
            QRCodeWriter writer = new QRCodeWriter();

            BitMatrix matrix =
                    writer.encode(
                            qrText,
                            BarcodeFormat.QR_CODE,
                            300,
                            300
                    );

            // Convert to PNG byte[]
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    baos
            );

            byte[] imageBytes = baos.toByteArray();

            // PDFBox create image
            PDImageXObject image =
                    PDImageXObject.createFromByteArray(
                            doc,
                            imageBytes,
                            "QR"
                    );

            float pageWidth = doc.getPage(0).getMediaBox().getWidth();

            float x = (pageWidth - QR_SIZE) / 2;

            float y = Math.max(0, currentY - QR_SIZE);

            cs.drawImage(
                    image,
                    x,
                    y,
                    QR_SIZE,
                    QR_SIZE
            );

            log.debug("QR rendered successfully");

            return currentY - BLOCK_HEIGHT;
        }
        catch (WriterException | IOException e) {

            log.error("QR rendering failed", e);

            throw new PdfRenderException("QR rendering failed", e);
        }
    }

    @Override
    public float calculateHeight(LayoutElement element) {

        if (element.getQrData() == null || element.getQrData().isBlank()) {
            return 0f;
        }

        return BLOCK_HEIGHT;
    }
}