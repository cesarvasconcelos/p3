package com.example.books.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented // Includes the annotation in Javadoc
@Target( { FIELD } ) // Specifies where the annotation can be applied
@Retention( RUNTIME ) // Retains the annotation at runtime
@Constraint( validatedBy = { PriceLimitValidationLogic.class} ) // Links the annotation to its validator class that has business logic
public @interface PriceLimit {
    // Default error message (can be a key to a resource bundle)
    // String message() default "Price must be a value lower than {limit}";

    // Uses annotation attribute inside the message using {limit}
    String message() default "{com.example.books.constraints.priceLimit}";

    // Allows grouping of constraints (e.g., for conditional validation)
    Class<?>[] groups() default {};

    // Allows attaching metadata to the constraint
    Class<? extends Payload>[] payload() default {};

    // Custom attribute for the annotation (e.g., specifies the expected case)
    double limit() default 500D;
}
