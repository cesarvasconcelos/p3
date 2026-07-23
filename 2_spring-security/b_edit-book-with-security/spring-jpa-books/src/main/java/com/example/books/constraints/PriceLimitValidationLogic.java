package com.example.books.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class PriceLimitValidationLogic implements ConstraintValidator<PriceLimit, BigDecimal> {
    private BigDecimal upperLimit;

    @Override public void initialize( PriceLimit annotation )
    {
        // this.upperLimit = BigDecimal.valueOf( annotation.limit() );
        this.upperLimit = new BigDecimal( String.valueOf( annotation.limit() ) );
    }

    @Override public boolean isValid( BigDecimal valueOfBookPriceField, ConstraintValidatorContext context )
    {
        if ( valueOfBookPriceField == null )
        {
            return true; // Consider null values as valid
        }

        return valueOfBookPriceField.compareTo( upperLimit ) < 0;
    }
}
