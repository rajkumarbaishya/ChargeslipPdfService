package com.pinelabs.chargeslip.pdf.exception;

public class InvalidHexDumpException extends ChargeSlipException {

    public InvalidHexDumpException(String message) {
        super(message);
    }

    public InvalidHexDumpException(String message, Throwable cause) {
        super(message, cause);
    }
}
