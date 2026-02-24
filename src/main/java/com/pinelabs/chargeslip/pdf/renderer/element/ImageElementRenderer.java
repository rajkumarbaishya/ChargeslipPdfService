package com.pinelabs.chargeslip.pdf.renderer.element;

import com.pinelabs.chargeslip.pdf.model.LayoutElement;
import com.pinelabs.chargeslip.pdf.renderer.ElementRenderer;
import com.pinelabs.chargeslip.pdf.service.LogoFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageElementRenderer implements ElementRenderer {

    private final LogoFileService logoFileService;

    private static final float IMAGE_WIDTH = 120f;
    private static final float IMAGE_HEIGHT = 40f;
    private static final float IMAGE_BLOCK_HEIGHT = 50f;

    @Override
    public boolean supports(LayoutElement.Type type) {
        return type == LayoutElement.Type.IMAGE;
    }

    @Override
    public float render(
            PDDocument doc,
            PDPageContentStream cs,
            float currentY,
            LayoutElement element
    ) {

        String filename = "im" + String.format("%08X", element.getImageId()) + ".png";

        try {

            byte[] imgBytes = logoFileService.loadLogo(filename);

            if (imgBytes == null || imgBytes.length == 0) {

                log.warn("Logo/image not found or empty: {}. Skipping rendering.", filename);

                // reserve space even if logo missing
                return currentY - IMAGE_BLOCK_HEIGHT;
            }

            PDImageXObject img =
                    PDImageXObject.createFromByteArray(
                            doc,
                            imgBytes,
                            filename
                    );

            PDPage page = doc.getPage(0);

            float pageWidth = page.getMediaBox().getWidth();

            float x = (pageWidth - IMAGE_WIDTH) / 2;

            float y = Math.max(0, currentY - IMAGE_HEIGHT);

            cs.drawImage(
                    img,
                    x,
                    y,
                    IMAGE_WIDTH,
                    IMAGE_HEIGHT
            );

            log.debug(
                    "Rendered image successfully. filename={}, x={}, y={}",
                    filename,
                    x,
                    y
            );

        }
        catch (IOException e) {

            log.error("Image rendering failed due to IO issue. filename={}", filename, e);
        }
        catch (IllegalArgumentException e) {

            log.error("Invalid image data. filename={}", filename, e);
        }

        return currentY - IMAGE_BLOCK_HEIGHT;
    }

    @Override
    public float calculateHeight(LayoutElement element) {

        return IMAGE_BLOCK_HEIGHT;
    }
}