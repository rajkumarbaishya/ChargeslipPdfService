package com.pinelabs.chargeslip.pdf.exception;

public abstract class ChargeSlipException extends RuntimeException {

    public ChargeSlipException(String message) {
        super(message);
    }

    public ChargeSlipException(String message, Throwable cause) {
        super(message, cause);
    }
}
