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
public class RawTextModeHandler implements ModeHandler {

    @Override
    public boolean supports(byte mode) {
        return mode == PrinterProtocolConstants.PRINTDUMP_RAWMODE;
    }

    @Override
    public int handle(byte[] data, int offset, ChargeSlipLayout layout) {

        try {

            log.debug("RAW_MODE detected at offset={}", offset - 1);

            if (offset + 2 > data.length) {
                throw new PrintDumpParseException("RAW mode length header truncated");
            }

            int totalLen = ((data[offset++] & 0xFF) << 8) | (data[offset++] & 0xFF);

            log.debug("RAW_MODE totalLen={}", totalLen);

            int blockStart = offset;

            if (offset + totalLen > data.length) {
                throw new PrintDumpParseException("RAW block exceeds buffer");
            }

            while ((offset - blockStart) < totalLen) {

                if ((offset - blockStart + 3) > totalLen) {

                    log.warn("RAW font/len header exceeds RAW block");

                    break;
                }

                byte fontType = data[offset++];

                boolean bold = fontType == 0x03 || fontType == 0x04 || fontType == 0x06;

                int fontSize =
                        (fontType == 0x01 || fontType == 0x03)
                                ? PrinterProtocolConstants.PRINT_SIZE24
                                : (fontType == 0x05 || fontType == 0x06)
                                ? PrinterProtocolConstants.PRINT_SIZE40
                                : PrinterProtocolConstants.PRINT_SIZE48;

                int len = ((data[offset++] & 0xFF) << 8) | (data[offset++] & 0xFF);

                if ((offset - blockStart + len) > totalLen) {
                    throw new PrintDumpParseException("RAW text exceeds RAW block length");
                }

                String normalized =
                        new String(data, offset, len, StandardCharsets.US_ASCII)
                                .replace("\r", "")
                                .replace("\0", "");

                String[] lines = normalized.split("\n", -1);

                for (String line : lines) {

                    layout.add(
                            LayoutElement.text(
                                    line,
                                    bold,
                                    fontSize
                            )
                    );

                    log.trace("RAW line added len={} bold={} fontSize={}", line.length(), bold, fontSize);
                }

                offset += len;
            }

            log.debug("RAW_MODE parsed successfully. NextOffset={}", offset);

            return offset;

        }
        catch (PrintDumpParseException e) {
            throw e;
        }
        catch (Exception e) {

            log.error("RAW_MODE parsing failed at offset={}", offset, e);

            throw new PrintDumpParseException("RAW mode parsing failed", e);
        }
    }
}