package com.pinelabs.chargeslip.pdf.controller;

import com.pinelabs.chargeslip.pdf.service.HexToPdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class HexToPdfController {

    private final HexToPdfService service;

    @GetMapping(value = "/chargeslip", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPdf(
            @RequestParam long transactionId,
            @RequestHeader("x-client-id") String clientId,
            @RequestHeader("x-tenant-id") String tenantId) {

        log.info("PDF request received. transactionId={}, clientId={}, tenantId={}", transactionId, clientId, tenantId);

        if (transactionId <= 0) {

            log.warn("Invalid transactionId received: {}", transactionId);

            throw new IllegalArgumentException("transactionId must be positive");
        }

        byte[] pdf = service.fetchAndGeneratePdf(transactionId, clientId, tenantId);

        log.info("PDF generated successfully. transactionId={}, size={} bytes", transactionId, pdf.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=chargeslip_" + transactionId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    /**
     * Internal testing endpoint (hex -> PDF)
     */
    @PostMapping(value = "/from-hex", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> fromHex(@RequestBody String hex) {

        log.info("Received internal hex-to-PDF conversion request. hexLength={}", hex != null ? hex.length() : 0);

        if (hex == null || hex.isBlank()) {

            log.warn("Empty hex dump received");

            throw new IllegalArgumentException("hex dump must not be empty");
        }

        byte[] pdf = service.convertHexToPdf(hex);

        log.info("PDF generated successfully from hex. size={} bytes", pdf.length);

        String filename = "chargeslip_" + java.util.UUID.randomUUID() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
