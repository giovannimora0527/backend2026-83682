
package com.uniminuto.clinica.api;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// API DE FÓRMULAS MÉDICAS
// ==========================================================
//
// Este archivo define las rutas HTTP utilizadas
// para consultar las fórmulas médicas registradas
// en la base de datos MySQL.
//
// Tecnología:
// - Java 21
// - Spring Boot
// - Spring Web
// - Spring Data JPA
//
// ==========================================================


// ==========================================================
// 1. IMPORTACIONES
// ==========================================================

// Entidad relacionada con la tabla formula_medica.
import com.uniminuto.clinica.entity.FormulaMedica;

// Permite trabajar con listas.
import java.util.List;

// Excepción utilizada por el proyecto.
import org.apache.coyote.BadRequestException;

// Representa una respuesta HTTP.
import org.springframework.http.ResponseEntity;

// Permite configurar CORS.
import org.springframework.web.bind.annotation.CrossOrigin;

// Permite definir rutas HTTP GET.
import org.springframework.web.bind.annotation.GetMapping;

// Permite establecer la ruta principal de la API.
import org.springframework.web.bind.annotation.RequestMapping;


// ==========================================================
// 2. CONFIGURACIÓN DE LA API
// ==========================================================

/**
 * API encargada de definir los servicios web
 * relacionados con las fórmulas médicas.
 *
 * Esta interfaz establece las rutas HTTP.
 *
 * La implementación se realizará en:
 *
 * FormulaMedicaApiController.java
 *
 * Ruta base:
 *
 * /formula-medica
 */

// Permite solicitudes desde Angular.
// Nota: configuración abierta para desarrollo local.
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*"
)

// Ruta principal de fórmulas médicas.
@RequestMapping("/formula-medica")

public interface FormulaMedicaApi {


    // ======================================================
    // 3. LISTAR TODAS LAS FÓRMULAS MÉDICAS
    // ======================================================

    /**
     * Consulta todas las fórmulas médicas
     * registradas en MySQL.
     *
     * Método HTTP:
     *
     * GET
     *
     * Ruta:
     *
     * /formula-medica/listar-todos
     *
     * Funcionamiento:
     *
     * 1. Angular realiza una solicitud HTTP GET.
     *
     * 2. Spring Boot recibe la solicitud.
     *
     * 3. El controlador llama al servicio:
     *
     *    obtenerFormulasMedicas()
     *
     * 4. El servicio consulta el repositorio.
     *
     * 5. El repositorio obtiene los registros
     *    de la tabla formula_medica.
     *
     * 6. Spring Boot devuelve la lista
     *    de fórmulas médicas en formato JSON.
     *
     * Los resultados estarán ordenados
     * desde la fecha de creación más reciente
     * hasta la más antigua.
     *
     * @return respuesta HTTP con la lista
     *         de fórmulas médicas.
     *
     * @throws BadRequestException cuando
     *         ocurre un problema en la consulta.
     */

    @GetMapping(
            value = "/listar-todos",
            produces = "application/json"
    )

    ResponseEntity<List<FormulaMedica>> listarFormulasMedicas()
            throws BadRequestException;

}
