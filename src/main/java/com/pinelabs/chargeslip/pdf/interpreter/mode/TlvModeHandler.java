package com.pinelabs.chargeslip.pdf.interpreter.mode;

import com.pinelabs.chargeslip.pdf.constants.PrinterProtocolConstants;
import com.pinelabs.chargeslip.pdf.exception.PrintDumpParseException;
import com.pinelabs.chargeslip.pdf.model.ChargeSlipLayout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TlvModeHandler implements ModeHandler {

    @Override
    public boolean supports(byte mode) {
        return mode == PrinterProtocolConstants.PRINTDUMP_CHARGESLIPMODE;
    }

    @Override
    public int handle(byte[] data, int offset, ChargeSlipLayout layout) {

        try {

            log.debug("TLV mode detected at offset={}", offset - 1);

            if (offset + 2 > data.length) {

                throw new PrintDumpParseException("Invalid TLV block — insufficient length bytes");
            }

            int len =
                    ((data[offset++] & 0xFF) << 8) |
                            (data[offset++] & 0xFF);

            log.debug("TLV block length={}", len);

            if (len == 0) {

                log.trace("TLV block length is zero");

                return offset;
            }

            if (offset + len > data.length) {

                throw new PrintDumpParseException("Invalid TLV block — payload exceeds dump length");
            }

            log.trace("Skipping TLV block from offset={} to {}", offset, offset + len);

            offset += len;

            log.debug("TLV block skipped successfully. NextOffset={}", offset);

            return offset;

        }
        catch (PrintDumpParseException e) {

            throw e;
        }
        catch (Exception e) {

            log.error("TLV parsing failed at offset={}", offset, e);

            throw new PrintDumpParseException("TLV mode parsing failed", e);
        }
    }
}