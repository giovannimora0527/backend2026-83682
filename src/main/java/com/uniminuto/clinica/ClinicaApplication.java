
package com.uniminuto.clinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación de la clínica veterinaria.
 * Configura e inicia la aplicación utilizando Spring Boot.
 */
@SpringBootApplication
public class ClinicaApplication {

    /**
     * Punto de entrada de la aplicación.
     *
     * @param args argumentos recibidos al iniciar la aplicación
     */
    public static void main(String[] args) {
        SpringApplication.run(ClinicaApplication.class, args);
    }

}
