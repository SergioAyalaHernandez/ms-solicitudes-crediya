package co.com.pragma.usecase.exceptions;

import lombok.Data;

@Data
public class ConstraintViolation {
    private final String message;

    public ConstraintViolation(String message) {
        this.message = message;
    }

}
