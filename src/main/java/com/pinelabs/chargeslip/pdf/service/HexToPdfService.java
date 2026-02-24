package com.pinelabs.chargeslip.pdf.service;

import com.pinelabs.chargeslip.pdf.client.OrchestratorChargeSlipClient;
import com.pinelabs.chargeslip.pdf.exception.ChargeSlipException;
import com.pinelabs.chargeslip.pdf.exception.InvalidHexDumpException;
import com.pinelabs.chargeslip.pdf.exception.PdfRenderException;
import com.pinelabs.chargeslip.pdf.interpreter.PrintDumpInterpreter;
import com.pinelabs.chargeslip.pdf.model.ChargeSlipLayout;
import com.pinelabs.chargeslip.pdf.renderer.PdfRenderer;
import com.pinelabs.chargeslip.pdf.util.HexUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HexToPdfService {

    private final PrintDumpInterpreter interpreter;
    private final PdfRenderer renderer;
    private final OrchestratorChargeSlipClient orchestratorClient;

    /**
     * Core conversion: HEX -> Layout -> PDF
     */
    public byte[] convertHexToPdf(String hex) {

        if (hex == null || hex.isBlank()) {

            log.warn("Empty or null hex dump received");

            throw new InvalidHexDumpException("Hex dump cannot be null or empty");
        }

        try {

            log.info("Hex->PDF conversion started. hexLength={}", hex.length());

            byte[] bytes = HexUtils.hexToBytes(hex);

            if (bytes.length == 0) {

                throw new InvalidHexDumpException("Hex dump conversion produced empty byte array");
            }

            ChargeSlipLayout layout = interpreter.interpret(bytes);

            if (layout == null || layout.size() == 0) {

                throw new PdfRenderException("Interpreter produced empty layout");
            }

            byte[] pdf = renderer.render(layout);

            log.info("Hex -> PDF conversion completed successfully. pdfSize={} bytes", pdf.length);

            return pdf;

        }
        catch (ChargeSlipException e) {

            throw e;
        }
        catch (Exception e) {

            log.error("Unexpected failure during Hex→PDF conversion", e);

            throw new PdfRenderException("Unexpected error during PDF generation", e);
        }
    }

    /**
     * Orchestrator flow:
     * transactionId -> hex -> PDF
     */
    public byte[] fetchAndGeneratePdf(Long txnId, String clientId, String tenantId) {

        log.info("Fetching hex dump from orchestrator. txnId={}, clientId={}, tenantId={}", txnId, clientId, tenantId);

        String hex = orchestratorClient.fetchHexDump(txnId, clientId, tenantId);

        return convertHexToPdf(hex);
    }
}