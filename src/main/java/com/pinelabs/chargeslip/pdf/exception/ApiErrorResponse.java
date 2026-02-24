package com.pinelabs.chargeslip.pdf.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApiErrorResponse {

    private String timestamp;
    private int status;
    private String errorCode;
    private String message;
}
