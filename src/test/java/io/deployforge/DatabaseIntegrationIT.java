package io.deployforge;

import io.deployforge.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(properties = {
        "deployforge.bootstrap.username=test-admin",
        "deployforge.bootstrap.password=test-password-for-integration"
})
class DatabaseIntegrationIT {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17.11-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    ProjectRepository projects;

    @Test
    void migrationCreatesUsableSchemaAndEnforcesRepositoryUniqueness() {
        UUID id = UUID.randomUUID();
        String sql = """
                INSERT INTO projects (id, display_name, github_repository_id,
                  repository_full_name, allowed_branch, github_workflow_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbc.update(sql, id, "Demo", 123L, "example/demo", "main", 456L);

        assertTrue(projects.findById(id).isPresent());
        assertThrows(DuplicateKeyException.class, () -> jdbc.update(sql,
                UUID.randomUUID(), "Duplicate", 123L, "example/demo", "main", 456L));
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success = true", Integer.class));
    }
}
