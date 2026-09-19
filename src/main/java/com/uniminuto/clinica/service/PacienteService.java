package com.uniminuto.clinica.service;

import com.uniminuto.clinica.dto.PacienteDTO;

import java.util.List;

/**
 * Interfaz de servicio para la entidad Paciente.
 *
 * <p>Se define como interfaz (y no directamente como clase) siguiendo la
 * arquitectura propuesta en clase: primero se declara "que" se puede hacer
 * (los metodos disponibles) y luego, en el paquete {@code serviceimpl}, se
 * implementa "como" se hace. Esto permite cambiar la implementacion en el
 * futuro sin afectar a quien use el servicio.</p>
 */
public interface PacienteService {

    /**
     * Crea un nuevo paciente (mascota) en el sistema.
     *
     * @param pacienteDTO datos del paciente a crear
     * @return el paciente creado, incluyendo el id que le asigno la base de datos
     */
    PacienteDTO crearPaciente(PacienteDTO pacienteDTO);

    /**
     * Lista todos los pacientes registrados en el sistema.
     *
     * @return lista de todos los pacientes
     */
    List<PacienteDTO> listarPacientes();

    /**
     * Busca un paciente por su id.
     *
     * @param id identificador del paciente
     * @return el paciente encontrado
     */
    PacienteDTO obtenerPacientePorId(Long id);
}
