package com.pinelabs.chargeslip.pdf.exception;

public class PdfRenderException extends ChargeSlipException {

    public PdfRenderException(String message) {
        super(message);
    }

    public PdfRenderException(String message, Throwable cause) {
        super(message, cause);
    }
}
