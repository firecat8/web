/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.servlet.exceptions;

/**
 *
 * @author Plamen
 */
public class InvalidUploadRequestException extends RuntimeException {
    
    private final static String MESSAGE = "Invalid upload request! Please fill all input fields.";
    
    public InvalidUploadRequestException() {
        super();
    }
    
    @Override
    public String getMessage() {
        return MESSAGE;
    }
    
}
