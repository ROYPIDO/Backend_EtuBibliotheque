package com.openclassrooms.etudiant.mapper;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.entities.Student;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StudentDtoMapperTest {

    private final StudentDtoMapper mapper = new StudentDtoMapperImpl();

    private StudentRequestDTO request() {
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setFirstName("Alice");
        dto.setLastName("Martin");
        dto.setEmail("alice@example.com");
        return dto;
    }

    @Test
    public void test_toEntity_copies_fields_and_leaves_id_and_dates_empty() {
        Student student = mapper.toEntity(request());

        assertThat(student.getFirstName()).isEqualTo("Alice");
        assertThat(student.getLastName()).isEqualTo("Martin");
        assertThat(student.getEmail()).isEqualTo("alice@example.com");
        assertThat(student.getId()).isNull();
        assertThat(student.getCreatedAt()).isNull();
        assertThat(student.getUpdatedAt()).isNull();
    }

    @Test
    public void test_toDto_copies_all_fields() {
        LocalDateTime now = LocalDateTime.now();
        Student student = new Student(1L, "Alice", "Martin", "alice@example.com", now, now);

        StudentResponseDTO dto = mapper.toDto(student);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFirstName()).isEqualTo("Alice");
        assertThat(dto.getLastName()).isEqualTo("Martin");
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    public void test_toDtoList_maps_every_element() {
        Student student = new Student(1L, "Alice", "Martin", "alice@example.com", null, null);

        assertThat(mapper.toDtoList(List.of(student, student))).hasSize(2);
        assertThat(mapper.toDtoList(List.of())).isEmpty();
    }

    @Test
    public void test_updateEntity_changes_fields_but_keeps_id_and_dates() {
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime updated = LocalDateTime.now().minusHours(1);
        Student student = new Student(7L, "Old", "Name", "old@example.com", created, updated);

        StudentRequestDTO dto = request();
        mapper.updateEntity(dto, student);

        assertThat(student.getFirstName()).isEqualTo("Alice");
        assertThat(student.getLastName()).isEqualTo("Martin");
        assertThat(student.getEmail()).isEqualTo("alice@example.com");
        assertThat(student.getId()).isEqualTo(7L);
        assertThat(student.getCreatedAt()).isEqualTo(created);
        assertThat(student.getUpdatedAt()).isEqualTo(updated);
    }
}
