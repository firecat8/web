/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.reader.exceptions;

/**
 *
 * @author Plamen
 */
public class InvalidSheetFormatException extends RuntimeException {

    private final static String MESSAGE = "The Excel sheet has an invalid format!";

    public InvalidSheetFormatException() {
        super();
    }
    
    @Override
    public String getMessage() {
        return MESSAGE;
    }
    
}
