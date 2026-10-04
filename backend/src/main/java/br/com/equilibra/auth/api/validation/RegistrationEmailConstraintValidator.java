package br.com.equilibra.auth.api.validation;

import br.com.equilibra.user.domain.User;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Adapter Jakarta Bean Validation para email de cadastro.
 */
public class RegistrationEmailConstraintValidator implements ConstraintValidator<ValidRegistrationEmail, String> {

    private static final int MAX_EMAIL_LENGTH = 255;
    private static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String normalizedEmail = User.normalizeEmail(value);

        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            addViolation(context, "Email is required");
            return false;
        }

        if (normalizedEmail.length() > MAX_EMAIL_LENGTH) {
            addViolation(context, "Email must have at most 255 characters");
            return false;
        }

        if (!SIMPLE_EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            addViolation(context, "Email must be valid");
            return false;
        }

        return true;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
