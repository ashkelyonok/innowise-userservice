package org.ashkelyonok.userservice.exception;

import java.io.Serial;

public class UserNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6752006888016328524L;

    public UserNotFoundException() {
        super("User not found");
    }

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }

    public UserNotFoundException(String field, String value) {
        super("User not found with " + field + ": " + value);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
