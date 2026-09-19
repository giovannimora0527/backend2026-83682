package com.uniminuto.clinica.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseConnectionTest implements CommandLineRunner {

    private final DataSource dataSource;

    public DatabaseConnectionTest(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {

        try (var connection = dataSource.getConnection()) {

            System.out.println("==========================================");
            System.out.println("CONEXIÓN A BASE DE DATOS");
            System.out.println("==========================================");
            System.out.println("URL      : "
                    + connection.getMetaData().getURL());
            System.out.println("USUARIO  : "
                    + connection.getMetaData().getUserName());
            System.out.println("DRIVER   : "
                    + connection.getMetaData().getDriverName());
            System.out.println("DATABASE : "
                    + connection.getCatalog());
            System.out.println("==========================================");
        }
    }
}
