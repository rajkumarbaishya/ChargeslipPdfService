package com.pinelabs.chargeslip.pdf.interpreter.mode;

import com.pinelabs.chargeslip.pdf.constants.PrinterProtocolConstants;
import com.pinelabs.chargeslip.pdf.exception.PrintDumpParseException;
import com.pinelabs.chargeslip.pdf.model.ChargeSlipLayout;
import com.pinelabs.chargeslip.pdf.model.LayoutElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class BarcodeModeHandler implements ModeHandler {

    @Override
    public boolean supports(byte mode) {
        return mode == PrinterProtocolConstants.PRINTDUMP_BARCODEMODE;
    }

    @Override
    public int handle(byte[] data, int offset, ChargeSlipLayout layout) {

        try {

            log.debug("BARCODE mode detected at offset={}", offset - 1);

            // Validate header length
            if (offset + 2 > data.length) {

                log.error("BARCODE header truncated at offset={}", offset);

                throw new PrintDumpParseException("Invalid BARCODE block — insufficient header bytes");
            }

            // Attribute byte (barcode type, width, etc.)
            int attribute = data[offset++] & 0xFF;

            // Length of barcode data
            int len = data[offset++] & 0xFF;

            log.debug("BARCODE attribute={} length={}", attribute, len);

            if (len == 0) {

                log.warn("BARCODE length is zero. Skipping element at offset={}", offset);

                return offset;
            }

            // Validate bounds
            if (offset + len > data.length) {

                log.error(
                        "BARCODE data truncated. offset={} len={} remaining={}",
                        offset,
                        len,
                        data.length - offset
                );

                throw new PrintDumpParseException("Invalid BARCODE block — declared length exceeds buffer");
            }

            // Decode ASCII barcode payload
            String barcodeText =
                    new String(
                            data,
                            offset,
                            len,
                            StandardCharsets.US_ASCII
                    ).trim();

            //Correct immutable LayoutElement construction
            layout.add(LayoutElement.barcode(barcodeText));

            log.debug(
                    "BARCODE element added successfully. attribute={} value='{}'",
                    attribute,
                    barcodeText
            );

            offset += len;

            log.debug("BARCODE block parsed successfully. NextOffset={}", offset);

            return offset;

        }
        catch (PrintDumpParseException e) {

            throw e;
        }
        catch (Exception e) {

            log.error("BARCODE parsing failed at offset={}", offset, e);

            throw new PrintDumpParseException("BARCODE mode parsing failed", e);
        }
    }
}