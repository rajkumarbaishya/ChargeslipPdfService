package com.pinelabs.chargeslip.pdf.interpreter.mode;

import com.pinelabs.chargeslip.pdf.exception.PrintDumpParseException;
import com.pinelabs.chargeslip.pdf.model.ChargeSlipLayout;

/**
 * Strategy interface for handling different printer dump modes.
 *
 * Each implementation parses a specific mode block from the printer dump
 * and adds corresponding elements to the ChargeSlipLayout.
 *
 * Implementations must:
 * - Read data starting at the given offset
 * - Parse the mode-specific structure
 * - Add elements to layout
 * - Return the new offset after parsing
 */
public interface ModeHandler {

    /**
     * Checks if this handler supports the given mode byte.
     *
     * @param mode printer dump mode byte
     * @return true if supported
     */
    boolean supports(byte mode);

    /**
     * Parses the mode block starting at offset.
     *
     * @param data printer dump byte array
     * @param offset starting offset (after mode byte)
     * @param layout layout to populate
     * @return new offset after parsing
     * @throws PrintDumpParseException if parsing fails
     */
    int handle(byte[] data, int offset, ChargeSlipLayout layout)
            throws PrintDumpParseException;
}