package com.pinelabs.chargeslip.pdf.interpreter;

import com.pinelabs.chargeslip.pdf.constants.PrinterProtocolConstants;
import com.pinelabs.chargeslip.pdf.exception.PrintDumpParseException;
import com.pinelabs.chargeslip.pdf.interpreter.mode.ModeHandler;
import com.pinelabs.chargeslip.pdf.model.ChargeSlipLayout;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrintDumpInterpreter {

    private final List<ModeHandler> handlers;

    private final Map<Byte, ModeHandler> handlerMap = new HashMap<>();

    private static final byte[] KNOWN_MODES = {
            PrinterProtocolConstants.PRINTDUMP_CHARGESLIPMODE,
            PrinterProtocolConstants.PRINTDUMP_RAWMODE,
            PrinterProtocolConstants.PRINTDUMP_IMAGEMODE,
            PrinterProtocolConstants.PRINTDUMP_BARCODEMODE,
            PrinterProtocolConstants.PRINTDUMP_QRCODEPD,
            PrinterProtocolConstants.PRINTDUMP_DISPLAYMODE,
            PrinterProtocolConstants.PRINTDUMP_DISPLAYMODE_PROMPT
    };

    /**
     * Initialize handler lookup map once at startup
     */
    @PostConstruct
    private void initHandlerMap() {

        log.info("Initializing ModeHandler registry");

        for (byte mode : KNOWN_MODES) {
            for (ModeHandler handler : handlers) {
                if (handler.supports(mode)) {
                    handlerMap.put(mode, handler);
                    log.debug(
                            "Registered handler {} for mode 0x{}",
                            handler.getClass().getSimpleName(),
                            String.format("%02X", mode)
                    );
                    break;
                }
            }
        }

        log.info("ModeHandler registry initialized. Total supported modes={}", handlerMap.size());
    }


    /**
     * Main interpretation entrypoint
     */
    public ChargeSlipLayout interpret(byte[] data) {

        if (data == null || data.length == 0) {

            log.warn("Empty print dump received");

            return new ChargeSlipLayout();
        }

        log.info("Starting dump interpretation. Bytes={}", data.length);

        ChargeSlipLayout layout = new ChargeSlipLayout();

        int offset = 0;

        while (offset < data.length) {

            int modePos = offset;

            byte mode = data[offset++];

            log.debug("Mode detected: 0x{} at offset {}", String.format("%02X", mode), modePos);

            ModeHandler handler = handlerMap.get(mode);

            /*
             Unsupported modes must be skipped safely
             */
            if (handler == null) {

                log.warn("Unsupported mode 0x{} at offset {}. Skipping safely.",
                        String.format("%02X", mode), modePos);

                continue;
            }

            try {

                int newOffset = handler.handle(data, offset, layout);

                /*
                 Prevent infinite loop safely
                 */
                if (newOffset <= offset) {

                    log.error("Handler {} did not advance offset. Forcing safe skip.",
                            handler.getClass().getSimpleName());

                    offset++;

                    continue;
                }

                /*
                 Prevent invalid offset crash
                 */
                if (newOffset > data.length) {

                    log.error("Handler {} returned invalid offset {}. Skipping safely.",
                            handler.getClass().getSimpleName(), newOffset);

                    offset++;

                    continue;
                }

                log.trace("Offset advanced from {} to {}", offset, newOffset);

                offset = newOffset;

            }
            catch (PrintDumpParseException e) {

                /*
                 Handler failure must not crash interpreter
                 */

                log.error("Handler {} failed at offset {}. Skipping safely.",
                        handler.getClass().getSimpleName(), offset, e);

                offset++;

            }
            catch (Exception e) {

                log.error("Unexpected handler failure at offset {}. Skipping safely.", offset, e);

                offset++;
            }
        }

        log.info("Interpretation complete. Elements parsed={}", layout.size());

        return layout;
    }
}