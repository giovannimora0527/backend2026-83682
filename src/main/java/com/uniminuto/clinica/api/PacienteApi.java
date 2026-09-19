package com.uniminuto.clinica.api;

import com.uniminuto.clinica.dto.PacienteDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Interfaz que define el "contrato" del servicio REST de pacientes.
 *
 * <p>Siguiendo la arquitectura vista en clase, primero se define aqui QUE
 * operaciones ofrece la API (sin implementarlas), y luego, en el paquete
 * {@code apicontroller}, una clase implementa esta interfaz agregando las
 * anotaciones de Spring ({@code @RestController}, {@code @GetMapping}, etc)
 * y el codigo real de cada metodo.</p>
 */
public interface PacienteApi {

    /**
     * Crea un nuevo paciente.
     *
     * @param pacienteDTO datos del paciente a crear
     * @return respuesta HTTP con el paciente creado
     */
    ResponseEntity<PacienteDTO> crearPaciente(PacienteDTO pacienteDTO);

    /**
     * Lista todos los pacientes registrados.
     *
     * @return respuesta HTTP con la lista de pacientes
     */
    ResponseEntity<List<PacienteDTO>> listarPacientes();
}
