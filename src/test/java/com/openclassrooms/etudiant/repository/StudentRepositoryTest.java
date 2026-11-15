package com.openclassrooms.etudiant.repository;

import com.openclassrooms.etudiant.entities.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class StudentRepositoryTest {

    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.4");

    @Autowired
    private StudentRepository studentRepository;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }

    private Student newStudent(String email) {
        Student student = new Student();
        student.setFirstName("Alice");
        student.setLastName("Martin");
        student.setEmail(email);
        return student;
    }

    @Test
    public void test_save_sets_id_and_both_dates() {
        Student saved = studentRepository.saveAndFlush(newStudent("alice@example.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    public void test_update_keeps_createdAt_and_changes_updatedAt() throws InterruptedException {
        Student saved = studentRepository.saveAndFlush(newStudent("bob@example.com"));
        var createdAt = saved.getCreatedAt();
        var firstUpdatedAt = saved.getUpdatedAt();

        Thread.sleep(50);
        saved.setLastName("Dupont");
        Student updated = studentRepository.saveAndFlush(saved);

        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isAfter(firstUpdatedAt);
    }

    @Test
    public void test_duplicate_email_is_rejected_by_database() {
        studentRepository.saveAndFlush(newStudent("dup@example.com"));

        assertThatThrownBy(() -> studentRepository.saveAndFlush(newStudent("dup@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void test_existsByEmail_and_findByEmail() {
        studentRepository.saveAndFlush(newStudent("find@example.com"));

        assertThat(studentRepository.existsByEmail("find@example.com")).isTrue();
        assertThat(studentRepository.existsByEmail("absent@example.com")).isFalse();
        assertThat(studentRepository.findByEmail("find@example.com")).isPresent();
        assertThat(studentRepository.findByEmail("absent@example.com")).isEmpty();
    }

    @Test
    public void test_existsByEmailAndIdNot_ignores_the_student_itself() {
        Student first = studentRepository.saveAndFlush(newStudent("first@example.com"));
        Student second = studentRepository.saveAndFlush(newStudent("second@example.com"));

        assertThat(studentRepository.existsByEmailAndIdNot("first@example.com", first.getId())).isFalse();
        assertThat(studentRepository.existsByEmailAndIdNot("first@example.com", second.getId())).isTrue();
    }
}
