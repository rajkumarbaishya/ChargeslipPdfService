package com.pinelabs.chargeslip.pdf.renderer;

import com.pinelabs.chargeslip.pdf.exception.PdfRenderException;
import com.pinelabs.chargeslip.pdf.model.LayoutElement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

/**
 * Strategy interface for rendering different types of layout elements
 */
public interface ElementRenderer {

    /**
     * Check if this renderer supports the given element type
     */
    boolean supports(LayoutElement.Type type);

    /**
     * Render the element and return the new Y position
     *
     * @throws PdfRenderException if rendering fails
     */
    float render(
            PDDocument document,
            PDPageContentStream contentStream,
            float currentY,
            LayoutElement element
    ) throws PdfRenderException;

    /**
     * Calculate vertical space required for element
     */
    float calculateHeight(LayoutElement element);

    /**
     * Renderer name (for logging/debugging)
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}