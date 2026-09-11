package com.battleforge;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Proves the foundation holds: the context starts against a real Postgres and Flyway
 * brings the schema up. If a migration is malformed, this fails before anything else does.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresContainerSupport.class)
class ApplicationContextIT {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoadsAndFlywayCreatesTheSchema() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        Integer appliedMigrations = jdbc.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success = true", Integer.class);
        assertThat(appliedMigrations).isPositive();

        assertThat(tableExists(jdbc, "users")).isTrue();
    }

    @Test
    void usersTableCarriesThePrivacyConsentColumns() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        assertThat(columnsOf(jdbc, "users"))
                .contains("username", "email", "password_hash",
                        "accepted_privacy_version", "accepted_privacy_at");
    }

    private boolean tableExists(JdbcTemplate jdbc, String table) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.tables "
                        + "WHERE table_schema = 'public' AND table_name = ?)",
                Boolean.class, table));
    }

    private java.util.List<String> columnsOf(JdbcTemplate jdbc, String table) {
        return jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns "
                        + "WHERE table_schema = 'public' AND table_name = ?",
                String.class, table);
    }
}
