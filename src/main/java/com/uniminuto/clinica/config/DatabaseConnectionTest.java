package com.uniminuto.clinica.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;

/** Verificación no destructiva de conectividad al iniciar la aplicación. */
@Component
public class DatabaseConnectionTest implements CommandLineRunner {
    private final DataSource dataSource;
    public DatabaseConnectionTest(DataSource dataSource) { this.dataSource = dataSource; }
    @Override
    public void run(String... args) throws Exception {
        try (var connection = dataSource.getConnection()) {
            System.out.println("Conexión a la base de datos: OK");
            System.out.println("Motor: " + connection.getMetaData().getDatabaseProductName());
        }
    }
}
