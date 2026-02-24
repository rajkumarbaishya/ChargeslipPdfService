package com.pinelabs.chargeslip.pdf.model;

import com.pinelabs.chargeslip.pdf.exception.LayoutBuildException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ChargeSlipLayout {

    private final List<LayoutElement> elements = new ArrayList<>();

    public void add(LayoutElement element) {

        if (element == null) {

            log.warn("Attempted to add null LayoutElement — ignored");

            return;
        }

        if (element.getType() == null) {

            log.error("LayoutElement type is null");

            throw new LayoutBuildException("LayoutElement type must not be null");
        }

        elements.add(element);

        log.debug("LayoutElement added. type={} totalElements={}",
                element.getType(),
                elements.size());
    }

    public List<LayoutElement> getElements() {

        return Collections.unmodifiableList(elements);
    }

    public int size() {

        return elements.size();
    }

    public boolean isEmpty() {

        return elements.isEmpty();
    }
}