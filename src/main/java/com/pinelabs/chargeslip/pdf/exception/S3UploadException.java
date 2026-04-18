package com.pinelabs.chargeslip.pdf.exception;

public class S3UploadException extends ChargeSlipException {

    public S3UploadException(String message) {
        super(message);
    }

    public S3UploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
