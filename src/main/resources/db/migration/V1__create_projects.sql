CREATE TABLE projects (
    id UUID PRIMARY KEY,
    display_name VARCHAR(120) NOT NULL CHECK (length(trim(display_name)) > 0),
    github_repository_id BIGINT NOT NULL UNIQUE CHECK (github_repository_id > 0),
    repository_full_name VARCHAR(200) NOT NULL CHECK (length(trim(repository_full_name)) > 0),
    allowed_branch VARCHAR(255) NOT NULL CHECK (length(trim(allowed_branch)) > 0),
    github_workflow_id BIGINT NOT NULL CHECK (github_workflow_id > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
