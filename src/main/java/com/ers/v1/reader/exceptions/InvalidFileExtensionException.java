/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.reader.exceptions;

/**
 *
 * @author Plamen
 */
public class InvalidFileExtensionException extends RuntimeException {

    private final static String MESSAGE = "The uploaded file has an incompatible format. Valid formats are: 'xlsx' and 'xls'.";

    public InvalidFileExtensionException() {
        super();
    }

    @Override
    public String getMessage() {
        return MESSAGE;
    }

}
