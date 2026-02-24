package com.pinelabs.chargeslip.pdf.client;

import com.pinelabs.chargeslip.pdf.exception.OrchestratorClientException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestratorChargeSlipClient {

    private final RestTemplate restTemplate;

    @Value("${orchestrator.base-url}")
    private String baseUrl;

    @PostConstruct
    void init() {
        baseUrl = baseUrl.trim();
        log.info("OrchestratorChargeSlipClient initialized. baseUrl={}", baseUrl);
    }

    public String fetchHexDump(Long transactionId, String clientId, String tenantId) {

        log.info("Calling orchestrator. transactionId={}, clientId={}, tenantId={}",
                transactionId, clientId, tenantId);

        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/api/v1/chargeslip")
                .queryParam("transactionId", transactionId)
                .build()
                .toUriString();

        log.debug("Orchestrator URL constructed: {}", url);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-tenant-id", tenantId);

        ResponseEntity<Map<String, Object>> resp;

        try {

            resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(null, headers),
                    new ParameterizedTypeReference<>() {}
            );

        } catch (RestClientException ex) {

            log.error("Orchestrator service call failed. transactionId={}, error={}",
                    transactionId, ex.getMessage(), ex);

            throw new OrchestratorClientException("Failed to fetch chargeslip hex dump", ex);
        }

        Map<String, Object> response = resp.getBody();

        if (response == null || response.get("chargeslipcontent") == null) {
            throw new OrchestratorClientException("chargeslipcontent missing in orchestrator response");
        }

        String hex = response.get("chargeslipcontent").toString();

        log.info("Hex dump fetched successfully. length={}", hex.length());

        return hex;

    }
}
