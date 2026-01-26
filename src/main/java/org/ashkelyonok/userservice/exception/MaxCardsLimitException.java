package org.ashkelyonok.userservice.exception;

public class MaxCardsLimitException extends RuntimeException {

    public MaxCardsLimitException() {
        super("User already has maximum (5) active cards");
    }
}
