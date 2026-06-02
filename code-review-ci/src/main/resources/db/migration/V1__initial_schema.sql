CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    github_id VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255),
    email VARCHAR(255),
    avatar_url VARCHAR(255),
    github_access_token VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE repositories (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    url VARCHAR(255),
    webhook_secret VARCHAR(255),
    installation_id VARCHAR(255),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pull_requests (
    id BIGSERIAL PRIMARY KEY,
    repo_id BIGINT NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
    pr_number INTEGER NOT NULL,
    title VARCHAR(255),
    status VARCHAR(50),
    diff_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    pr_id BIGINT NOT NULL REFERENCES pull_requests(id) ON DELETE CASCADE,
    file TEXT,
    line_number INTEGER,
    severity VARCHAR(20),
    category VARCHAR(50),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
