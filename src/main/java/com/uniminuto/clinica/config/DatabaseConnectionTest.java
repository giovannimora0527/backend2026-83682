package com.uniminuto.clinica.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/** Al iniciar, muestra en el log los datos de la conexión a la base de datos (verificación de arranque). */
@Component
public class DatabaseConnectionTest implements CommandLineRunner {

    /** Fuente de datos configurada por Spring Boot. */
    private final DataSource dataSource;

    /**
     * @param dataSource fuente de datos.
     */
    public DatabaseConnectionTest(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Abre una conexión y escribe en el log la URL, el usuario y el driver.
     *
     * @param args argumentos de arranque.
     * @throws Exception si no es posible conectarse.
     */
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
