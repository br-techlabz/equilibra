package br.com.equilibra.auth.api.validation;

import br.com.equilibra.auth.domain.validator.PasswordValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Adapter Jakarta Bean Validation para a política de senha centralizada.
 */
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, CharSequence> {

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        PasswordValidator.ValidationResult result = PasswordValidator.validate(value);
        if (result.isValid()) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        result.violations().forEach(violation ->
            context.buildConstraintViolationWithTemplate(violation).addConstraintViolation()
        );
        return false;
    }
}
