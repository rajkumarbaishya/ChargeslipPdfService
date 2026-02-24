package com.pinelabs.chargeslip.pdf.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder(access = lombok.AccessLevel.PRIVATE)
public class LayoutElement {

    public enum Type {
        TEXT,
        IMAGE,
        QR,
        BARCODE
    }

    private final Type type;

    private final String text;
    private final boolean bold;
    private final int fontSize;

    private final long imageId;

    private final String qrData;
    private final String barcodeData;

    // Factory methods
    public static LayoutElement text(String text, boolean bold, int fontSize) {

        if (text == null)
            throw new IllegalArgumentException("Text cannot be null");

        return LayoutElement.builder()
                .type(Type.TEXT)
                .text(text)
                .bold(bold)
                .fontSize(fontSize)
                .build();
    }

    public static LayoutElement image(long imageId) {

        if (imageId <= 0)
            throw new IllegalArgumentException("Invalid imageId");

        return LayoutElement.builder()
                .type(Type.IMAGE)
                .imageId(imageId)
                .build();
    }

    public static LayoutElement qr(String data) {

        if (data == null)
            throw new IllegalArgumentException("QR data cannot be null");

        return LayoutElement.builder()
                .type(Type.QR)
                .qrData(data)
                .build();
    }

    public static LayoutElement barcode(String data) {

        if (data == null)
            throw new IllegalArgumentException("Barcode data cannot be null");

        return LayoutElement.builder()
                .type(Type.BARCODE)
                .barcodeData(data)
                .build();
    }
}