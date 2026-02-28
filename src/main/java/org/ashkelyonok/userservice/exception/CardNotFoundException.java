package org.ashkelyonok.userservice.exception;

import java.io.Serial;

public class CardNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4073785611887084870L;

    public CardNotFoundException() {
        super("Card not found");
    }

    public CardNotFoundException(Long id) {
        super("Card not found with id: " + id);
    }

    public CardNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
