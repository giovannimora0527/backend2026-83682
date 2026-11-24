package com.uniminuto.clinica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseConnectionTest implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionTest.class);

    private final DataSource dataSource;

    public DatabaseConnectionTest(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {

        try (var connection = dataSource.getConnection()) {

            LOGGER.info("Conexion a la base de datos establecida: {}", connection.getCatalog());
        }
    }
}
