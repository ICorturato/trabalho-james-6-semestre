package br.com.socialconnect.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EstoqueNaoNegativoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EstoqueNaoNegativo {
    String message() default "O valor do estoque não pode ser negativo";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}