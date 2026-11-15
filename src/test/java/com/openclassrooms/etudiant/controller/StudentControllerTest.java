package com.openclassrooms.etudiant.controller;

import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.StudentRepository;
import com.openclassrooms.etudiant.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class StudentControllerTest {

    private static final String URL = "/api/students";
    // Clés jetables de test (jamais la vraie clé du .env)
    private static final String TEST_SECRET = Base64.getEncoder().encodeToString(new byte[32]);
    private static final String OTHER_SECRET = Base64.getEncoder().encodeToString(new byte[]{
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2});

    @Container
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.4");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private JwtService jwtService;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mySQLContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mySQLContainer.getUsername());
        registry.add("spring.datasource.password", () -> mySQLContainer.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add("jwt.secret", () -> TEST_SECRET);
        registry.add("jwt.expiration", () -> "3600000");
    }

    @AfterEach
    public void afterEach() {
        studentRepository.deleteAll();
    }

    private String validBearer() {
        User user = new User();
        user.setLogin("student-api-test-user");
        return "Bearer " + jwtService.generateToken(user);
    }

    private String bearerSignedWithOtherKey() {
        User user = new User();
        user.setLogin("student-api-test-user");
        return "Bearer " + new JwtService(OTHER_SECRET, 3_600_000L).generateToken(user);
    }

    private Student saveStudent(String firstName, String lastName, String email) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        return studentRepository.saveAndFlush(student);
    }

    // ---------- GET /api/students ----------

    @Test
    public void findAllWithValidTokenReturnsEmptyListWhenNoStudent() throws Exception {
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, validBearer()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void findAllWithValidTokenReturnsStudents() throws Exception {
        saveStudent("Alice", "Martin", "alice@example.com");
        saveStudent("Bob", "Dupont", "bob@example.com");

        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, validBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].firstName").value("Alice"))
                .andExpect(jsonPath("$[0].lastName").value("Martin"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists())
                .andExpect(jsonPath("$[1].firstName").value("Bob"));
    }

    @Test
    public void findAllWithoutTokenReturnsUnauthorized() throws Exception {
        saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void findAllWithMalformedTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void findAllWithTokenSignedByAnotherKeyReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearerSignedWithOtherKey()))
                .andExpect(status().isUnauthorized());
    }

    // ---------- GET /api/students/{id} ----------

    @Test
    public void findByIdWithValidTokenReturnsStudent() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(get(URL + "/" + saved.getId()).header(HttpHeaders.AUTHORIZATION, validBearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Martin"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    public void findByIdUnknownReturnsNotFound() throws Exception {
        mockMvc.perform(get(URL + "/999999").header(HttpHeaders.AUTHORIZATION, validBearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student with id 999999 not found"));
    }

    @Test
    public void findByIdWithNonNumericIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get(URL + "/abc").header(HttpHeaders.AUTHORIZATION, validBearer()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void findByIdWithoutTokenReturnsUnauthorized() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(get(URL + "/" + saved.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void findByIdWithMalformedTokenReturnsUnauthorized() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(get(URL + "/" + saved.getId()).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void findByIdWithTokenSignedByAnotherKeyReturnsUnauthorized() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(get(URL + "/" + saved.getId()).header(HttpHeaders.AUTHORIZATION, bearerSignedWithOtherKey()))
                .andExpect(status().isUnauthorized());
    }
}
