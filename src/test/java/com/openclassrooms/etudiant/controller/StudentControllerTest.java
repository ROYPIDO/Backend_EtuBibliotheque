package com.openclassrooms.etudiant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.etudiant.dto.StudentRequestDTO;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    @Autowired
    private ObjectMapper objectMapper;

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

    // ---------- POST /api/students ----------

    private String body(String firstName, String lastName, String email) throws Exception {
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        return objectMapper.writeValueAsString(dto);
    }

    @Test
    public void createWithValidTokenReturnsCreatedAndPersistsStudent() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, validBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice", "Martin", "alice@example.com")))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString(URL + "/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Martin"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        assertThat(studentRepository.findByEmail("alice@example.com")).isPresent();
    }

    @Test
    public void createIgnoresIdSentByClient() throws Exception {
        String json = "{\"id\":9999,\"firstName\":\"Alice\",\"lastName\":\"Martin\",\"email\":\"alice@example.com\"}";

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, validBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(not(9999)));
    }

    @Test
    public void createWithMissingFieldsReturnsBadRequest() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, validBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        assertThat(studentRepository.count()).isZero();
    }

    @Test
    public void createWithInvalidEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, validBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice", "Martin", "not-an-email")))
                .andExpect(status().isBadRequest());

        assertThat(studentRepository.count()).isZero();
    }

    @Test
    public void createWithDuplicateEmailReturnsConflict() throws Exception {
        saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, validBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Other", "Person", "alice@example.com")))
                .andExpect(status().isConflict());

        assertThat(studentRepository.count()).isEqualTo(1);
    }

    @Test
    public void createWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice", "Martin", "alice@example.com")))
                .andExpect(status().isUnauthorized());

        assertThat(studentRepository.count()).isZero();
    }

    @Test
    public void createWithMalformedTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice", "Martin", "alice@example.com")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void createWithTokenSignedByAnotherKeyReturnsUnauthorized() throws Exception {
        mockMvc.perform(post(URL)
                        .header(HttpHeaders.AUTHORIZATION, bearerSignedWithOtherKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice", "Martin", "alice@example.com")))
                .andExpect(status().isUnauthorized());

        assertThat(studentRepository.count()).isZero();
    }

    // ---------- PUT /api/students/{id} ----------

    @Test
    public void updateWithValidTokenReturnsOkAndPreservesIdAndCreatedAt() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(put(URL + "/" + saved.getId())
                        .header(HttpHeaders.AUTHORIZATION, validBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alicia", "Durand", "alicia@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.firstName").value("Alicia"))
                .andExpect(jsonPath("$.lastName").value("Durand"))
                .andExpect(jsonPath("$.email").value("alicia@example.com"));

        Student reloaded = studentRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getFirstName()).isEqualTo("Alicia");
        assertThat(reloaded.getCreatedAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    public void updateKeepingOwnEmailReturnsOk() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(put(URL + "/" + saved.getId())
                        .header(HttpHeaders.AUTHORIZATION, validBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alicia", "Martin", "alice@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alicia"));
    }

    @Test
    public void updateWithEmailOfAnotherStudentReturnsConflict() throws Exception {
        Student alice = saveStudent("Alice", "Martin", "alice@example.com");
        saveStudent("Bob", "Dupont", "bob@example.com");

        mockMvc.perform(put(URL + "/" + alice.getId())
                        .header(HttpHeaders.AUTHORIZATION, validBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice", "Martin", "bob@example.com")))
                .andExpect(status().isConflict());

        assertThat(studentRepository.findById(alice.getId()).orElseThrow().getEmail())
                .isEqualTo("alice@example.com");
    }

    @Test
    public void updateUnknownStudentReturnsNotFound() throws Exception {
        mockMvc.perform(put(URL + "/999999")
                        .header(HttpHeaders.AUTHORIZATION, validBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice", "Martin", "alice@example.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateWithInvalidDataReturnsBadRequest() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(put(URL + "/" + saved.getId())
                        .header(HttpHeaders.AUTHORIZATION, validBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("", "Martin", "alice@example.com")))
                .andExpect(status().isBadRequest());

        assertThat(studentRepository.findById(saved.getId()).orElseThrow().getFirstName()).isEqualTo("Alice");
    }

    @Test
    public void updateWithoutTokenReturnsUnauthorized() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(put(URL + "/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alicia", "Durand", "alicia@example.com")))
                .andExpect(status().isUnauthorized());

        assertThat(studentRepository.findById(saved.getId()).orElseThrow().getFirstName()).isEqualTo("Alice");
    }

    @Test
    public void updateWithMalformedTokenReturnsUnauthorized() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(put(URL + "/" + saved.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alicia", "Durand", "alicia@example.com")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateWithTokenSignedByAnotherKeyReturnsUnauthorized() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(put(URL + "/" + saved.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerSignedWithOtherKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alicia", "Durand", "alicia@example.com")))
                .andExpect(status().isUnauthorized());
    }

    // ---------- DELETE /api/students/{id} ----------

    @Test
    public void deleteWithValidTokenReturnsNoContentAndRemovesStudent() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(delete(URL + "/" + saved.getId()).header(HttpHeaders.AUTHORIZATION, validBearer()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(studentRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    public void deleteUnknownStudentReturnsNotFound() throws Exception {
        mockMvc.perform(delete(URL + "/999999").header(HttpHeaders.AUTHORIZATION, validBearer()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteWithoutTokenReturnsUnauthorized() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(delete(URL + "/" + saved.getId()))
                .andExpect(status().isUnauthorized());

        assertThat(studentRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    public void deleteWithMalformedTokenReturnsUnauthorized() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(delete(URL + "/" + saved.getId()).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());

        assertThat(studentRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    public void deleteWithTokenSignedByAnotherKeyReturnsUnauthorized() throws Exception {
        Student saved = saveStudent("Alice", "Martin", "alice@example.com");

        mockMvc.perform(delete(URL + "/" + saved.getId()).header(HttpHeaders.AUTHORIZATION, bearerSignedWithOtherKey()))
                .andExpect(status().isUnauthorized());

        assertThat(studentRepository.existsById(saved.getId())).isTrue();
    }
}
