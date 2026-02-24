package com.pinelabs.chargeslip.pdf.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class HttpClientConfig {

    @Value("${http.client.connect-timeout}")
    private int connectTimeout;

    @Value("${http.client.read-timeout}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate() {

        log.info("Initializing RestTemplate with connectTimeout={}ms, readTimeout={}ms",
                connectTimeout, readTimeout);

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        RestTemplate restTemplate = new RestTemplate(factory);

        log.info("RestTemplate initialized successfully with configured timeouts");

        return restTemplate;
    }
}
