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
public class QrModeHandler implements ModeHandler {

    @Override
    public boolean supports(byte mode) {
        return mode == PrinterProtocolConstants.PRINTDUMP_QRCODEPD;
    }

    @Override
    public int handle(byte[] data, int offset, ChargeSlipLayout layout) {

        try {

            log.debug("QR mode detected at offset={}", offset - 1);

            // Validate QR length header
            if (offset + 2 > data.length) {

                throw new PrintDumpParseException("Invalid QR block — insufficient length bytes");
            }

            int len = ((data[offset++] & 0xFF) << 8) | (data[offset++] & 0xFF);

            log.debug("QR payload length={}", len);

            if (len == 0) {

                log.warn("QR payload length is zero at offset={}", offset);

                return offset;
            }

            if (offset + len > data.length) {

                throw new PrintDumpParseException("Invalid QR block — payload exceeds dump length");
            }

            String qrText =
                    new String(
                            data,
                            offset,
                            len,
                            StandardCharsets.US_ASCII
                    ).trim();

            //Correct immutable construction
            layout.add(LayoutElement.qr(qrText));

            offset += len;

            log.debug(
                    "QR element added successfully. length={} nextOffset={}",
                    qrText.length(),
                    offset
            );

            return offset;
        }
        catch (PrintDumpParseException e) {

            throw e;
        }
        catch (Exception e) {

            log.error("QR parsing failed at offset={}", offset, e);

            throw new PrintDumpParseException("QR mode parsing failed", e);
        }
    }
}