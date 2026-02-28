package org.ashkelyonok.userservice.exception;

import java.io.Serial;

public class UserAlreadyExistsException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 7169810410427442888L;

    public UserAlreadyExistsException() {
        super("User already exists");
    }

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public static UserAlreadyExistsException withEmail(String email) {
        return new UserAlreadyExistsException("User with email " + email + " already exists");
    }
}
