package com.pinelabs.chargeslip.pdf.interpreter.mode;

import com.pinelabs.chargeslip.pdf.constants.PrinterProtocolConstants;
import com.pinelabs.chargeslip.pdf.exception.PrintDumpParseException;
import com.pinelabs.chargeslip.pdf.model.ChargeSlipLayout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DisplayModeHandler implements ModeHandler {

    @Override
    public boolean supports(byte mode) {
        return mode == PrinterProtocolConstants.PRINTDUMP_DISPLAYMODE
                || mode == PrinterProtocolConstants.PRINTDUMP_DISPLAYMODE_PROMPT;
    }

    @Override
    public int handle(byte[] data, int offset, ChargeSlipLayout layout) {

        try {

            log.debug("DISPLAY_MODE detected at offset={}", offset - 1);

            if (offset + 2 > data.length) {

                throw new PrintDumpParseException("DISPLAY length bytes missing at offset=" + offset);
            }

            int displayLen = ((data[offset++] & 0xFF) << 8) | (data[offset++] & 0xFF);

            log.debug("Display length={}", displayLen);

            if (displayLen < 0) {

                throw new PrintDumpParseException("Invalid DISPLAY length=" + displayLen);
            }

            if (offset + displayLen > data.length) {

                throw new PrintDumpParseException("DISPLAY payload exceeds buffer");
            }

            // Skip display payload (not rendered in PDF)
            offset += displayLen;

            if (offset + 2 > data.length) {

                throw new PrintDumpParseException("DISPLAY PrintDataLen missing");
            }

            int printDataLen = ((data[offset++] & 0xFF) << 8) | (data[offset++] & 0xFF);

            log.debug("PrintDataLen after DISPLAY={}", printDataLen);

            log.debug("DISPLAY_MODE skipped successfully. NextOffset={}", offset);

            return offset;

        }
        catch (PrintDumpParseException e) {

            throw e;
        }
        catch (Exception e) {

            log.error("DISPLAY parsing failed at offset={}", offset, e);

            throw new PrintDumpParseException("DISPLAY mode parsing failed", e);
        }
    }
}