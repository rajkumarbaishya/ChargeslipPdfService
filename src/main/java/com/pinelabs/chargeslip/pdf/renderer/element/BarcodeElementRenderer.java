package com.pinelabs.chargeslip.pdf.renderer.element;

import com.pinelabs.chargeslip.pdf.constants.PdfLayoutConstants;
import com.pinelabs.chargeslip.pdf.exception.PdfRenderException;
import com.pinelabs.chargeslip.pdf.model.LayoutElement;
import com.pinelabs.chargeslip.pdf.renderer.ElementRenderer;
import com.pinelabs.chargeslip.pdf.util.TextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class BarcodeElementRenderer implements ElementRenderer {

    private static final int DEFAULT_COLUMNS = 48;
    private static final float LINE_SPACING = 1f;

    @Override
    public boolean supports(LayoutElement.Type type) {
        return type == LayoutElement.Type.BARCODE;
    }

    @Override
    public float render(
            PDDocument doc,
            PDPageContentStream cs,
            float currentY,
            LayoutElement element
    ) throws PdfRenderException {

        try {

            String barcodeText = "[BARCODE] " + TextUtils.safe(element.getBarcodeData());

            var font = PDType1Font.COURIER;

            float printableWidth = PdfLayoutConstants.PAGE_WIDTH - (PdfLayoutConstants.LEFT_MARGIN * 2);

            float fontSize = printableWidth / DEFAULT_COLUMNS / 0.6f;

            float ascent = font.getFontDescriptor().getAscent() / 1000f * fontSize;

            cs.beginText();

            cs.setFont(font, fontSize);

            cs.newLineAtOffset(
                    PdfLayoutConstants.LEFT_MARGIN,
                    Math.max(0, currentY - ascent)
            );

            cs.showText(
                    TextUtils.normalize(barcodeText)
            );

            cs.endText();

            log.debug("Barcode rendered successfully: {}", barcodeText);

            return currentY - (fontSize + LINE_SPACING);
        }
        catch (IOException e) {

            log.error("Barcode rendering failed", e);

            throw new PdfRenderException("Failed to render barcode element", e);
        }
    }

    @Override
    public float calculateHeight(LayoutElement element) {
        float printableWidth =
                PdfLayoutConstants.PAGE_WIDTH - (PdfLayoutConstants.LEFT_MARGIN * 2);
        float fontSize = printableWidth / DEFAULT_COLUMNS / 0.6f;
        return fontSize + LINE_SPACING;
    }
}