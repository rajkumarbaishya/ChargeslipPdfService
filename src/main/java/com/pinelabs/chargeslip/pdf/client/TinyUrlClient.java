package com.pinelabs.chargeslip.pdf.client;

import com.pinelabs.chargeslip.pdf.exception.TinyUrlException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TinyUrlClient {

    private final RestTemplate restTemplate;

    @Value("${tiny-url.base-url}")
    private String baseUrl;

    @PostConstruct
    void init() {
        baseUrl = baseUrl.trim();
        log.info("TinyUrlClient initialized. baseUrl={}", baseUrl);
    }

    /**
     * Calls the tiny URL service to shorten a long URL.
     *
     * @param longUrl the original presigned S3 URL
     * @return the shortened tiny URL
     */
    public String shorten(String longUrl) {

        log.info("Calling tiny URL service to shorten URL");

        String url = baseUrl + "/api/v1/shorten";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of("url", longUrl);

        ResponseEntity<Map<String, Object>> resp;

        try {

            resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    new ParameterizedTypeReference<>() {}
            );

        } catch (RestClientException ex) {

            log.error("Tiny URL service call failed. error={}", ex.getMessage(), ex);

            throw new TinyUrlException("Failed to generate tiny URL", ex);
        }

        Map<String, Object> response = resp.getBody();

        if (response == null || response.get("tinyUrl") == null) {
            throw new TinyUrlException("tinyUrl missing in tiny URL service response");
        }

        String tinyUrl = response.get("tinyUrl").toString();

        log.info("Tiny URL generated successfully. tinyUrl={}", tinyUrl);

        return tinyUrl;
    }
}
