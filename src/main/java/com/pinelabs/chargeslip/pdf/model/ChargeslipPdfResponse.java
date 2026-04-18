package com.pinelabs.chargeslip.pdf.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeslipPdfResponse {

    private long transactionId;
    private String tinyUrl;
    private String status;
}
