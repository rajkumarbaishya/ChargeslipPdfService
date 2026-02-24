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
public class TextElementRenderer implements ElementRenderer {

    private static final float LINE_SPACING = 2f;
    private static final float PRINTABLE_WIDTH =
            PdfLayoutConstants.PAGE_WIDTH - (PdfLayoutConstants.LEFT_MARGIN * 2);

    @Override
    public boolean supports(LayoutElement.Type type) {
        return type == LayoutElement.Type.TEXT;
    }

    @Override
    public float render(
            PDDocument doc,
            PDPageContentStream cs,
            float currentY,
            LayoutElement element
    ) throws PdfRenderException {

        String text = TextUtils.safe(element.getText());

        if (text.isBlank()) {
            return currentY;
        }

        float y = currentY;

        for (String line : text.split("\\r?\\n")) {

            y = drawTextLine(
                    cs,
                    y,
                    line,
                    element.isBold(),
                    element.getFontSize()
            );
        }

        return y;
    }

    @Override
    public float calculateHeight(LayoutElement element) {

        if (element.getText() == null || element.getText().isBlank()) {
            return 0f;
        }

        int lineCount = element.getText().split("\\r?\\n").length;

        return lineCount * lineHeight(element.isBold(), element.getFontSize());
    }

    private float drawTextLine(
            PDPageContentStream cs,
            float currentY,
            String text,
            boolean bold,
            int columns
    ) throws PdfRenderException {

        try {

            var font = fontFor(bold);

            float fontSize = fontSize(columns);

            float ascent = ascent(font, fontSize);

            float totalHeight = lineHeight(bold, columns);

            cs.beginText();

            cs.setFont(font, fontSize);

            cs.newLineAtOffset(
                    PdfLayoutConstants.LEFT_MARGIN,
                    currentY - ascent
            );

            cs.showText(TextUtils.normalize(text));

            cs.endText();

            return currentY - totalHeight;

        }
        catch (IOException e) {

            log.error("TEXT rendering failed: {}", text, e);

            throw new PdfRenderException("TEXT rendering failed", e);
        }
    }

    private static PDType1Font fontFor(boolean bold) {
        return bold ? PDType1Font.COURIER_BOLD : PDType1Font.COURIER;
    }

    private static float fontSize(int columns) {
        return PRINTABLE_WIDTH / columns / 0.6f;
    }

    private static float ascent(PDType1Font font, float fontSize) {
        return font.getFontDescriptor().getAscent() / 1000f * fontSize;
    }

    private static float descent(PDType1Font font, float fontSize) {
        return Math.abs(font.getFontDescriptor().getDescent()) / 1000f * fontSize;
    }

    private static float lineHeight(boolean bold, int columns) {
        var font = fontFor(bold);
        float fontSize = fontSize(columns);
        return ascent(font, fontSize) + descent(font, fontSize) + LINE_SPACING;
    }
}