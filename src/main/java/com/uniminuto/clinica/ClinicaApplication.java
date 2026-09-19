package com.uniminuto.clinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicacion.
 *
 * <p>Esta es la puerta de entrada del programa: cuando se ejecuta el
 * metodo {@link #main(String[])}, Spring Boot arranca todo el servidor web,
 * lee la configuracion de {@code application.properties}, crea la base de
 * datos en memoria y deja todos los servicios REST disponibles.</p>
 *
 * <p>La anotacion {@code @SpringBootApplication} es en realidad la union de
 * tres anotaciones:</p>
 * <ul>
 *     <li>{@code @Configuration}: indica que esta clase puede definir configuracion.</li>
 *     <li>{@code @EnableAutoConfiguration}: le dice a Spring que configure
 *     automaticamente lo necesario (servidor web, JPA, etc) segun las
 *     dependencias que agregamos en el pom.xml.</li>
 *     <li>{@code @ComponentScan}: le dice a Spring que busque, dentro de este
 *     paquete y sus subpaquetes, todas las clases anotadas como
 *     {@code @RestController}, {@code @Service}, {@code @Repository}, etc,
 *     para poder inyectarlas donde se necesiten.</li>
 * </ul>
 */
@SpringBootApplication
public class ClinicaApplication {

    /**
     * Metodo de arranque de la aplicacion.
     *
     * @param args argumentos que se le pueden pasar al programa por
     *             linea de comandos (no se usan en este ejercicio).
     */
    public static void main(String[] args) {
        SpringApplication.run(ClinicaApplication.class, args);
    }
}
