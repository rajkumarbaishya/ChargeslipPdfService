package com.pinelabs.chargeslip.pdf.renderer;

import com.pinelabs.chargeslip.pdf.constants.PdfLayoutConstants;
import com.pinelabs.chargeslip.pdf.exception.PdfRenderException;
import com.pinelabs.chargeslip.pdf.model.ChargeSlipLayout;
import com.pinelabs.chargeslip.pdf.model.LayoutElement;
import com.pinelabs.chargeslip.pdf.service.PageHeightCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfRenderer {

    private final List<ElementRenderer> elementRenderers;
    private final PageHeightCalculator pageHeightCalculator;

    /**
     * Fast lookup registry for renderers
     * O(1) performance instead of stream scanning
     */
    private final Map<LayoutElement.Type, ElementRenderer> rendererMap =
            new EnumMap<>(LayoutElement.Type.class);

    /**
     * Initialize renderer registry
     */
    @PostConstruct
    private void initRendererMap() {

        log.info("Initializing ElementRenderer registry");

        for (ElementRenderer renderer : elementRenderers) {

            for (LayoutElement.Type type : LayoutElement.Type.values()) {

                if (renderer.supports(type)) {

                    rendererMap.put(type, renderer);

                    log.debug("Registered renderer={} for type={}",
                            renderer.getClass().getSimpleName(),
                            type);
                }
            }
        }

        log.info("Renderer registry initialized. totalRenderers={}", rendererMap.size());
    }

    /**
     * Main entrypoint: ChargeSlipLayout → PDF bytes
     */
    public byte[] render(ChargeSlipLayout layout) {

        validateLayout(layout);

        float pageHeight = pageHeightCalculator.calculate(layout);

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {

            PDPage page = createPage(pageHeight);

            document.addPage(page);

            renderElements(document, page, layout);

            document.save(outputStream);

            byte[] pdfBytes = outputStream.toByteArray();

            log.info("PDF rendered successfully. size={} bytes elements={}",
                    pdfBytes.length,
                    layout.size());

            return pdfBytes;
        }
        catch (Exception e) {

            log.error("PDF rendering failed", e);

            throw new PdfRenderException("Failed to render PDF", e);
        }
    }

    /**
     * Validate layout before rendering
     */
    private void validateLayout(ChargeSlipLayout layout) {

        if (layout == null) {

            throw new PdfRenderException("ChargeSlipLayout cannot be null");
        }

        if (layout.getElements().isEmpty()) {

            throw new PdfRenderException("ChargeSlipLayout contains no elements");
        }
    }

    /**
     * Create PDF page with dynamic height
     */
    private PDPage createPage(float pageHeight) {

        log.debug("Creating PDF page. width={} height={}", PdfLayoutConstants.PAGE_WIDTH, pageHeight);

        return new PDPage(new PDRectangle(PdfLayoutConstants.PAGE_WIDTH, pageHeight));
    }

    /**
     * Render layout elements sequentially
     */
    private void renderElements(
            PDDocument document,
            PDPage page,
            ChargeSlipLayout layout
    ) {

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            float y = page.getMediaBox().getHeight() - PdfLayoutConstants.TOP_PADDING;

            for (LayoutElement element : layout.getElements()) {

                if (element == null || element.getType() == null) {

                    log.warn("Skipping invalid layout element");

                    continue;
                }

                ElementRenderer renderer = rendererMap.get(element.getType());

                if (renderer == null) {

                    log.warn("No renderer registered for element type={}", element.getType());

                    continue;
                }

                try {

                    y = renderer.render(
                            document,
                            contentStream,
                            y,
                            element
                    );

                }
                catch (Exception e) {

                    // Fail-safe: continue rendering remaining elements

                    log.error("Failed to render element type={} — continuing", element.getType(), e);
                }
            }
        }
        catch (Exception e) {

            throw new PdfRenderException("Content stream rendering failed", e);
        }
    }
}