package com.uniminuto.clinica.serviceimpl;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.service.HistoriaMedicaService;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;


    // =========================================================
    // LISTAR HISTORIAS MÉDICAS POR RANGO DE FECHAS
    // =========================================================
    @Override
    public List<HistoriaMedica> listarHistorias(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    ) throws BadRequestException {

        return historiaMedicaRepository
                .findAllByFechaCreacionBetweenOrderByFechaCreacionDesc(
                        fechaInicial,
                        fechaFinal
                );
    }


    // =========================================================
    // GUARDAR UNA NUEVA HISTORIA MÉDICA
    // =========================================================
    @Override
    public MiRespuestaRS guardarHistoriaMedica(
            HistoriaMedicaRq historiaMedicaRq
    ) throws BadRequestException {

        // Validar que el objeto recibido no sea nulo
        if (historiaMedicaRq == null) {
            throw new BadRequestException(
                    "El objeto HistoriaMedicaRq no puede ser nulo"
            );
        }

        // Validar que el paciente tenga un id válido
        if (historiaMedicaRq.getPacienteId() == null ||
                historiaMedicaRq.getPacienteId() <= 0) {

            throw new BadRequestException(
                    "El id del paciente no puede ser nulo o negativo"
            );
        }

        // Crear una nueva historia médica
        HistoriaMedica historiaMedica = new HistoriaMedica();

        // Asignar el paciente recibido
        historiaMedica.setPacienteId(
                historiaMedicaRq.getPacienteId()
        );

        // Asignar la fecha actual de creación
        historiaMedica.setFechaCreacion(
                LocalDateTime.now()
        );

        // Guardar la historia en la base de datos
        historiaMedicaRepository.save(historiaMedica);

        // Crear la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setMessage(
                "Historia medica guardada exitosamente"
        );
        respuesta.setStatus(200);

        // Devolver la respuesta
        return respuesta;
    }


    // =========================================================
    // ACTUALIZAR UNA HISTORIA MÉDICA EXISTENTE
    // =========================================================
    @Override
    public MiRespuestaRS actualizarHistoriaMedica(
            HistoriaMedicaRq historiaMedicaRq
    ) throws BadRequestException {

        // Validar que el objeto recibido no sea nulo
        if (historiaMedicaRq == null) {
            throw new BadRequestException(
                    "El objeto HistoriaMedicaRq no puede ser nulo"
            );
        }

        // Validar que el id de la historia sea válido
        if (historiaMedicaRq.getHistoriaId() == null ||
                historiaMedicaRq.getHistoriaId() <= 0) {

            throw new BadRequestException(
                    "El id de la historia no puede ser nulo o negativo"
            );
        }

        // Validar que el paciente tenga un id válido
        if (historiaMedicaRq.getPacienteId() == null ||
                historiaMedicaRq.getPacienteId() <= 0) {

            throw new BadRequestException(
                    "El id del paciente no puede ser nulo o negativo"
            );
        }

        // Buscar la historia médica por su id
        Optional<HistoriaMedica> optHistoria =
                historiaMedicaRepository.findById(
                        historiaMedicaRq.getHistoriaId()
                );

        // Validar que la historia exista en la base de datos
        if (optHistoria.isEmpty()) {
            throw new BadRequestException(
                    "La historia medica seleccionada no es valida"
            );
        }

        // Obtener la historia encontrada
        HistoriaMedica historiaExistente =
                optHistoria.get();

        // Actualizar el paciente
        historiaExistente.setPacienteId(
                historiaMedicaRq.getPacienteId()
        );

        // Guardar los cambios
        historiaMedicaRepository.save(historiaExistente);

        // Crear la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setMessage(
                "Historia medica actualizada exitosamente"
        );
        respuesta.setStatus(200);

        // Devolver la respuesta
        return respuesta;
    }
}
