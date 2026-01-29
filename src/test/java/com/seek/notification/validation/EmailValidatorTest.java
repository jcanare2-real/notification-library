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
    @DisplayName("Debe fallar al intentar instanciar un email con formato inválido")
    void shouldFailOnInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EmailNotification("formato-incorrecto", "Sub", "Body");
        });
    }
}