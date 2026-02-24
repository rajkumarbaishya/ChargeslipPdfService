package com.pinelabs.chargeslip.pdf.exception;

public class OrchestratorClientException extends ChargeSlipException {

    public OrchestratorClientException(String message) {
        super(message);
    }

    public OrchestratorClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
