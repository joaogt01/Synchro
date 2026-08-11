package com.projetos.agendamento;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
public class FlywayMigrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    static void configureProperties(DynamicPropertyRegistry registrar) {
        registrar.add("spring.datasource.url", postgres::getJdbcUrl);
        registrar.add("spring.datasource.username", postgres::getUsername);
        registrar.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void contextLoads() {

    }
}
