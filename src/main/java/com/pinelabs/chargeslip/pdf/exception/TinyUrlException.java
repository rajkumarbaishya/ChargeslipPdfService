package com.pinelabs.chargeslip.pdf.exception;

public class TinyUrlException extends ChargeSlipException {

    public TinyUrlException(String message) {
        super(message);
    }

    public TinyUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
