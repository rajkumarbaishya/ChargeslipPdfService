package com.pinelabs.chargeslip.pdf.interpreter.mode;

import com.pinelabs.chargeslip.pdf.constants.PrinterProtocolConstants;
import com.pinelabs.chargeslip.pdf.exception.PrintDumpParseException;
import com.pinelabs.chargeslip.pdf.model.ChargeSlipLayout;
import com.pinelabs.chargeslip.pdf.model.LayoutElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ImageModeHandler implements ModeHandler {

    @Override
    public boolean supports(byte mode) {
        return mode == PrinterProtocolConstants.PRINTDUMP_IMAGEMODE;
    }

    @Override
    public int handle(byte[] data, int offset, ChargeSlipLayout layout) {

        log.debug("IMAGE mode detected at offset={}", offset - 1);

        if (offset + 4 > data.length) {

            throw new PrintDumpParseException("Invalid IMAGE block — insufficient bytes");
        }

        int b1 = data[offset++] & 0xFF;
        int b2 = data[offset++] & 0xFF;
        int b3 = data[offset++] & 0xFF;
        int b4 = data[offset++] & 0xFF;

        long imageId =
                ((long) b1 << 24) |
                        ((long) b2 << 16) |
                        ((long) b3 << 8)  |
                        ((long) b4);

        log.debug("Parsed imageId={} hex={}", imageId,
                String.format("%08X", imageId));

        /*
         Skip invalid imageId instead of throwing exception
         */
        if (imageId <= 0) {

            log.warn("Skipping IMAGE element due to invalid imageId={}", imageId);

            return offset;
        }

        try {

            layout.add(LayoutElement.image(imageId));

            log.debug("IMAGE element added successfully");

        } catch (Exception e) {

            /*
             Fail-safe protection:
             Never break interpretation pipeline
             */

            log.error("IMAGE element creation failed. Skipping safely.", e);
        }

        return offset;
    }
}