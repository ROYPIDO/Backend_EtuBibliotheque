package com.openclassrooms.etudiant.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class StudentRequestDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private StudentRequestDTO dto(String firstName, String lastName, String email) {
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        return dto;
    }

    @Test
    public void test_valid_dto_has_no_violation() {
        assertThat(validator.validate(dto("Alice", "Martin", "alice@example.com"))).isEmpty();
    }

    @Test
    public void test_blank_or_null_fields_are_rejected() {
        Set<ConstraintViolation<StudentRequestDTO>> violations = validator.validate(dto(" ", null, ""));

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("firstName", "lastName", "email");
    }

    @Test
    public void test_invalid_email_is_rejected() {
        assertThat(validator.validate(dto("Alice", "Martin", "not-an-email")))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("email");
    }

    @Test
    public void test_too_long_value_is_rejected() {
        assertThat(validator.validate(dto("a".repeat(256), "Martin", "alice@example.com")))
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactly("firstName");
    }
}
