-- Esquema inicial opcional; Flyway gerencia migrations versionadas em src/main/resources/db/migration
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INTEGER NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INTEGER,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT now(),
    execution_time INTEGER NOT NULL,
    success BOOLEAN NOT NULL
);
ALTER TABLE flyway_schema_history ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);
