package com.pinelabs.chargeslip.pdf.service;

import com.pinelabs.chargeslip.pdf.constants.PdfLayoutConstants;
import com.pinelabs.chargeslip.pdf.model.ChargeSlipLayout;
import com.pinelabs.chargeslip.pdf.model.LayoutElement;
import com.pinelabs.chargeslip.pdf.renderer.ElementRenderer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates the required page height based on layout elements
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PageHeightCalculator {

    private final List<ElementRenderer> elementRenderers;

    private final Map<LayoutElement.Type, ElementRenderer> rendererMap =
            new EnumMap<>(LayoutElement.Type.class);

    @PostConstruct
    void init() {
        for (ElementRenderer renderer : elementRenderers) {
            for (LayoutElement.Type type : LayoutElement.Type.values()) {
                if (renderer.supports(type)) {
                    rendererMap.put(type, renderer);
                }
            }
        }
        log.info("PageHeightCalculator renderer map initialized. entries={}", rendererMap.size());
    }

    public float calculate(ChargeSlipLayout layout) {
        float totalHeight = PdfLayoutConstants.TOP_PADDING + PdfLayoutConstants.BOTTOM_PADDING;

        for (var element : layout.getElements()) {
            if (element == null || element.getType() == null) {
                continue;
            }

            ElementRenderer renderer = rendererMap.get(element.getType());
            float elementHeight = (renderer != null) ? renderer.calculateHeight(element) : 0f;

            totalHeight += elementHeight;
            log.trace("Element type={} height={}", element.getType(), elementHeight);
        }

        float finalHeight = Math.max(totalHeight, PdfLayoutConstants.MIN_PAGE_HEIGHT);
        log.debug("Calculated page height: {}", finalHeight);
        return finalHeight;
    }
}