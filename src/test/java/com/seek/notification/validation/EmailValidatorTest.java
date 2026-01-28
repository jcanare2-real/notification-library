package com.seek.notification.validation;

import com.seek.notification.domain.EmailNotification;
import com.seek.notification.exceptions.NotificationValidationException;
import com.seek.notification.validation.impl.EmailValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailValidatorTest {

    private final EmailValidator validator = new EmailValidator();

    @Test
    @DisplayName("Debe lanzar excepción si el email no tiene formato correcto")
    void shouldFailOnInvalidEmail() {
        EmailNotification invalidEmail = new EmailNotification("formato-incorrecto", "Sub", "Body");

        assertThrows(NotificationValidationException.class, () -> {
            validator.validate(invalidEmail);
        });
    }
}