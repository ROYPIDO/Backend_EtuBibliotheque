package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.dto.StudentRequestDTO;
import com.openclassrooms.etudiant.dto.StudentResponseDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.handler.EmailAlreadyUsedException;
import com.openclassrooms.etudiant.handler.ResourceNotFoundException;
import com.openclassrooms.etudiant.mapper.StudentDtoMapper;
import com.openclassrooms.etudiant.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {

    private static final String EMAIL_ALREADY_USED = "Email already used by another student";

    private final StudentRepository studentRepository;
    private final StudentDtoMapper studentDtoMapper;

    public StudentResponseDTO create(StudentRequestDTO request) {
        Assert.notNull(request, "Student must not be null");
        log.info("Creating a new student");

        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException(EMAIL_ALREADY_USED);
        }
        Student student = studentDtoMapper.toEntity(request);
        return studentDtoMapper.toDto(saveAndFlushOrConflict(student));
    }

    public List<StudentResponseDTO> findAll() {
        return studentDtoMapper.toDtoList(studentRepository.findAll());
    }

    public StudentResponseDTO findById(Long id) {
        return studentDtoMapper.toDto(getStudentOrThrow(id));
    }

    public StudentResponseDTO update(Long id, StudentRequestDTO request) {
        Assert.notNull(request, "Student must not be null");
        log.info("Updating student {}", id);

        Student student = getStudentOrThrow(id);
        // L'étudiant peut conserver son propre email, mais pas prendre celui d'un autre
        if (studentRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new EmailAlreadyUsedException(EMAIL_ALREADY_USED);
        }
        // L'id et les dates ne sont pas touchés par le mapper
        studentDtoMapper.updateEntity(request, student);
        return studentDtoMapper.toDto(saveAndFlushOrConflict(student));
    }

    public void delete(Long id) {
        log.info("Deleting student {}", id);
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException(notFoundMessage(id));
        }
        studentRepository.deleteById(id);
    }

    private Student getStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(notFoundMessage(id)));
    }

    /**
     * Le flush force l'écriture immédiate : si deux requêtes concurrentes passent la vérification
     * d'unicité en même temps, la contrainte UNIQUE de la base lève l'erreur ici, traduite en 409.
     */
    private Student saveAndFlushOrConflict(Student student) {
        try {
            return studentRepository.saveAndFlush(student);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyUsedException(EMAIL_ALREADY_USED, e);
        }
    }

    private String notFoundMessage(Long id) {
        return "Student with id " + id + " not found";
    }
}
