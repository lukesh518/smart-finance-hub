package com.finance.exceptions;

/**
 * Exception thrown when asset or transaction parameters are invalid.
 */
public class InvalidAssetException extends Exception {
    public InvalidAssetException(String message) {
        super(message);
    }
}
