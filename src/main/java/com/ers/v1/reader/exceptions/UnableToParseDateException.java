/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ers.v1.reader.exceptions;

/**
 *
 * @author Plamen
 */
public class UnableToParseDateException extends RuntimeException {
    
    private final static String MESSAGE = "Unable to parse given value! Sheet may contain invalid format of cells.";
    
    public UnableToParseDateException() {
        super();
    }
    
    @Override
    public String getMessage() {
        return MESSAGE;
    }
    
}
