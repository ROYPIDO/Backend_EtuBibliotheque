package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.handler.EmailAlreadyUsedException;
import com.openclassrooms.etudiant.handler.ResourceNotFoundException;
import com.openclassrooms.etudiant.mapper.StudentDtoMapper;
import com.openclassrooms.etudiant.mapper.StudentDtoMapperImpl;
import com.openclassrooms.etudiant.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    private static final Long ID = 1L;
    private static final String EMAIL = "alice@example.com";

    @Mock
    private StudentRepository studentRepository;

    // Vrai mapper (pas un mock) : on vérifie aussi qu'il préserve l'id et les dates
    private final StudentDtoMapper studentDtoMapper = new StudentDtoMapperImpl();

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository, studentDtoMapper);
    }

    private StudentRequestDTO request(String email) {
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setFirstName("Alice");
        dto.setLastName("Martin");
        dto.setEmail(email);
        return dto;
    }

    private Student existingStudent() {
        return new Student(ID, "Old", "Name", "old@example.com",
                LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 2, 10, 0));
    }

    // ---------- create ----------

    @Test
    public void test_create_student() {
        when(studentRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(studentRepository.saveAndFlush(any(Student.class))).thenAnswer(invocation -> {
            Student s = invocation.getArgument(0);
            s.setId(ID);
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        StudentResponseDTO response = studentService.create(request(EMAIL));

        assertThat(response.getId()).isEqualTo(ID);
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    public void test_create_null_request_throws_IllegalArgumentException() {
        assertThatThrownBy(() -> studentService.create(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void test_create_existing_email_throws_EmailAlreadyUsedException() {
        when(studentRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(request(EMAIL)))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(studentRepository, never()).saveAndFlush(any());
    }

    @Test
    public void test_create_concurrent_duplicate_email_throws_EmailAlreadyUsedException() {
        // La vérification passe, mais la contrainte UNIQUE de la base refuse l'écriture (requête concurrente)
        when(studentRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(studentRepository.saveAndFlush(any(Student.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThatThrownBy(() -> studentService.create(request(EMAIL)))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasCauseInstanceOf(DataIntegrityViolationException.class);
    }

    // ---------- findAll / findById ----------

    @Test
    public void test_findAll_returns_all_students() {
        when(studentRepository.findAll()).thenReturn(List.of(existingStudent(), existingStudent()));

        assertThat(studentService.findAll()).hasSize(2);
    }

    @Test
    public void test_findAll_without_student_returns_empty_list() {
        when(studentRepository.findAll()).thenReturn(List.of());

        assertThat(studentService.findAll()).isEmpty();
    }

    @Test
    public void test_findById_returns_student() {
        when(studentRepository.findById(ID)).thenReturn(Optional.of(existingStudent()));

        StudentResponseDTO response = studentService.findById(ID);

        assertThat(response.getId()).isEqualTo(ID);
        assertThat(response.getEmail()).isEqualTo("old@example.com");
    }

    @Test
    public void test_findById_unknown_id_throws_ResourceNotFoundException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- update ----------

    @Test
    public void test_update_changes_fields_and_preserves_id_and_dates() {
        Student existing = existingStudent();
        LocalDateTime createdAt = existing.getCreatedAt();
        when(studentRepository.findById(ID)).thenReturn(Optional.of(existing));
        when(studentRepository.existsByEmailAndIdNot(EMAIL, ID)).thenReturn(false);
        when(studentRepository.saveAndFlush(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentResponseDTO response = studentService.update(ID, request(EMAIL));

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).saveAndFlush(captor.capture());
        Student saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(ID);
        assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
        assertThat(saved.getFirstName()).isEqualTo("Alice");
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getId()).isEqualTo(ID);
        assertThat(response.getLastName()).isEqualTo("Martin");
    }

    @Test
    public void test_update_unknown_id_throws_ResourceNotFoundException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.update(99L, request(EMAIL)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(studentRepository, never()).saveAndFlush(any());
    }

    @Test
    public void test_update_email_used_by_another_student_throws_EmailAlreadyUsedException() {
        when(studentRepository.findById(ID)).thenReturn(Optional.of(existingStudent()));
        when(studentRepository.existsByEmailAndIdNot(EMAIL, ID)).thenReturn(true);

        assertThatThrownBy(() -> studentService.update(ID, request(EMAIL)))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(studentRepository, never()).saveAndFlush(any());
    }

    @Test
    public void test_update_concurrent_duplicate_email_throws_EmailAlreadyUsedException() {
        when(studentRepository.findById(ID)).thenReturn(Optional.of(existingStudent()));
        when(studentRepository.existsByEmailAndIdNot(EMAIL, ID)).thenReturn(false);
        when(studentRepository.saveAndFlush(any(Student.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThatThrownBy(() -> studentService.update(ID, request(EMAIL)))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasCauseInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void test_update_null_request_throws_IllegalArgumentException() {
        assertThatThrownBy(() -> studentService.update(ID, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------- delete ----------

    @Test
    public void test_delete_existing_student() {
        when(studentRepository.existsById(ID)).thenReturn(true);

        studentService.delete(ID);

        verify(studentRepository).deleteById(ID);
    }

    @Test
    public void test_delete_unknown_id_throws_ResourceNotFoundException() {
        when(studentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> studentService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(studentRepository, never()).deleteById(any());
    }
}
