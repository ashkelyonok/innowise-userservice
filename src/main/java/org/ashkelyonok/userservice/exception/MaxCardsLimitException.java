package org.ashkelyonok.userservice.exception;

import java.io.Serial;

public class MaxCardsLimitException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1120072459229959037L;

    public MaxCardsLimitException() {
        super("User already has maximum (5) active cards");
    }

    public MaxCardsLimitException(String message) {
        super(message);
    }

    public MaxCardsLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
