package com.pinelabs.chargeslip.pdf.util;

import com.pinelabs.chargeslip.pdf.exception.InvalidHexDumpException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HexUtils {

    public static byte[] hexToBytes(String hex) {

        if (hex == null || hex.isBlank()) {
            throw new InvalidHexDumpException("Hex input empty");
        }

        log.debug("Converting hex to bytes. Length={}", hex.length());

        hex = hex.replaceAll("\\s+", "");

        if (hex.length() % 2 != 0) {
            throw new InvalidHexDumpException("Hex length must be even");
        }

        byte[] data = new byte[hex.length() / 2]; //Memory created to store converted bytes

        for (int i=0;i<hex.length();i+=2) { //Loop increment is 2
            int hi = Character.digit(hex.charAt(i),16); //Convert first hex digit - numeric value base-16 - e.g. 'A' to 16
            int lo = Character.digit(hex.charAt(i+1),16); //Convert second hex digit

            if (hi==-1 || lo==-1) {
                throw new InvalidHexDumpException("Invalid hex char at index "+i);
            }

            data[i/2] = (byte)((hi<<4)+lo); //Combine into single byte - Assume hex pair = "4F" - hi = 4, lo = 15 - (4<<4*16) + 15 = 79 - decimal = 0x4F

        }

        log.debug("Hex conversion complete. Bytes={}", data.length);
        return data;
    }
}
