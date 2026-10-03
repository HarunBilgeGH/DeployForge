package io.deployforge.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "github_repository_id", nullable = false, unique = true)
    private Long githubRepositoryId;

    @Column(name = "repository_full_name", nullable = false, length = 200)
    private String repositoryFullName;

    @Column(name = "allowed_branch", nullable = false, length = 255)
    private String allowedBranch;

    @Column(name = "github_workflow_id", nullable = false)
    private Long githubWorkflowId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Project() {
    }

    public UUID getId() {
        return id;
    }
}
