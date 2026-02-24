package com.pinelabs.chargeslip.pdf.service;

import com.pinelabs.chargeslip.pdf.exception.PdfRenderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class LogoFileService {

    /**
     * Loads logo bytes from classpath (/logos folder).
     *
     * Fail-safe behavior:
     * - If logo not found → returns null (no exception)
     * - If read fails → returns null
     * - Interpreter continues processing remaining elements
     */
    public byte[] loadLogo(String filename) {

        log.debug("Attempting to load logo: {}", filename);

        try (InputStream is = getClass().getResourceAsStream("/logos/" + filename)) {

            if (is == null) {
                log.warn("Logo missing: {}", filename);
                return null;
            }

            return is.readAllBytes();

        }  catch(IOException e) {
            log.error("Logo read failed {}", filename, e);
            throw new PdfRenderException("Failed to read logo: " + filename, e);
        }
    }
}
