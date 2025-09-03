package co.com.pragma.usecase.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ConstraintViolationException extends RuntimeException {
    private final List<ConstraintViolation> constraintViolations;

    public ConstraintViolationException(List<ConstraintViolation> constraintViolations) {
        this.constraintViolations = constraintViolations;
    }

}
