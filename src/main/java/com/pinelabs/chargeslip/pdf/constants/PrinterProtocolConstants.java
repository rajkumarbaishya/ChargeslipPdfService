package com.pinelabs.chargeslip.pdf.constants;

public final class PrinterProtocolConstants {

    private PrinterProtocolConstants() {}

    public static final byte PRINTDUMP_CHARGESLIPMODE = 0x01;
    public static final byte PRINTDUMP_RAWMODE = 0x02;
    public static final byte PRINTDUMP_IMAGEMODE = 0x03;
    public static final byte PRINTDUMP_BARCODEMODE = 0x04;
    public static final byte PRINTDUMP_QRCODEPD = 0x0A;
    public static final byte PRINTDUMP_DISPLAYMODE = 0x05;
    public static final byte PRINTDUMP_DISPLAYMODE_PROMPT = 0x06;

    public static final int PRINT_SIZE24 = 24;
    public static final int PRINT_SIZE40 = 40;
    public static final int PRINT_SIZE48 = 48;
}
