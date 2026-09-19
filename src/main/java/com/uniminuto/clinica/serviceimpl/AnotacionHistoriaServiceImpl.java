package com.uniminuto.clinica.serviceimpl;
import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class AnotacionHistoriaServiceImpl
        implements AnotacionHistoriaService {

    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;


    // =========================================================
    // LISTAR TODAS LAS ANOTACIONES
    // =========================================================
    @Override
    public List<AnotacionHistoria> listarAnotaciones()
            throws BadRequestException {

        return anotacionHistoriaRepository.findAll();
    }


    // =========================================================
    // GUARDAR UNA NUEVA ANOTACIÓN
    // =========================================================
    @Override
    public MiRespuestaRS guardarAnotacion(
            AnotacionHistoriaRq anotacionHistoriaRq
    ) throws BadRequestException {

        // Validar que el objeto recibido no sea nulo
        if (anotacionHistoriaRq == null) {
            throw new BadRequestException(
                    "El objeto AnotacionHistoriaRq no puede ser nulo"
            );
        }

        // Validar el id de la historia médica
        if (anotacionHistoriaRq.getHistoriaId() == null ||
                anotacionHistoriaRq.getHistoriaId() <= 0) {

            throw new BadRequestException(
                    "El id de la historia no puede ser nulo o negativo"
            );
        }

        // Validar el id del médico
        if (anotacionHistoriaRq.getMedicoId() == null ||
                anotacionHistoriaRq.getMedicoId() <= 0) {

            throw new BadRequestException(
                    "El id del medico no puede ser nulo o negativo"
            );
        }

        // Validar la descripción
        if (anotacionHistoriaRq.getDescripcion() == null ||
                anotacionHistoriaRq.getDescripcion().isEmpty()) {

            throw new BadRequestException(
                    "La descripcion no puede ser nula o vacia"
            );
        }

        // Crear una nueva anotación
        AnotacionHistoria anotacionHistoria =
                new AnotacionHistoria();

        // Asignar el id de la historia
        anotacionHistoria.setHistoriaId(
                anotacionHistoriaRq.getHistoriaId()
        );

        // Asignar el id del médico
        anotacionHistoria.setMedicoId(
                anotacionHistoriaRq.getMedicoId()
        );

        // Asignar la descripción
        anotacionHistoria.setDescripcion(
                anotacionHistoriaRq.getDescripcion()
        );

        // Asignar automáticamente la fecha actual
        anotacionHistoria.setFecha(
                LocalDateTime.now()
        );

        // Guardar la anotación en la base de datos
        anotacionHistoriaRepository.save(anotacionHistoria);

        // Crear la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setMessage(
                "Anotacion guardada exitosamente"
        );
        respuesta.setStatus(200);

        // Devolver la respuesta
        return respuesta;
    }


    // =========================================================
    // ACTUALIZAR UNA ANOTACIÓN
    // =========================================================
    @Override
    public MiRespuestaRS actualizarAnotacion(
            AnotacionHistoriaRq anotacionHistoriaRq
    ) throws BadRequestException {

        // Validar que el objeto recibido no sea nulo
        if (anotacionHistoriaRq == null) {
            throw new BadRequestException(
                    "El objeto AnotacionHistoriaRq no puede ser nulo"
            );
        }

        // Validar el id de la anotación
        if (anotacionHistoriaRq.getAnotacionId() == null ||
                anotacionHistoriaRq.getAnotacionId() <= 0) {

            throw new BadRequestException(
                    "El id de la anotacion no puede ser nulo o negativo"
            );
        }

        // Validar el id de la historia
        if (anotacionHistoriaRq.getHistoriaId() == null ||
                anotacionHistoriaRq.getHistoriaId() <= 0) {

            throw new BadRequestException(
                    "El id de la historia no puede ser nulo o negativo"
            );
        }

        // Validar el id del médico
        if (anotacionHistoriaRq.getMedicoId() == null ||
                anotacionHistoriaRq.getMedicoId() <= 0) {

            throw new BadRequestException(
                    "El id del medico no puede ser nulo o negativo"
            );
        }

        // Validar la descripción
        if (anotacionHistoriaRq.getDescripcion() == null ||
                anotacionHistoriaRq.getDescripcion().isEmpty()) {

            throw new BadRequestException(
                    "La descripcion no puede ser nula o vacia"
            );
        }

        // Buscar la anotación en la base de datos
        AnotacionHistoria anotacionExistente =
                anotacionHistoriaRepository.findById(
                        anotacionHistoriaRq.getAnotacionId()
                ).orElseThrow(() ->
                        new BadRequestException(
                                "La anotacion seleccionada no es valida"
                        )
                );

        // Actualizar los datos
        anotacionExistente.setHistoriaId(
                anotacionHistoriaRq.getHistoriaId()
        );

        anotacionExistente.setMedicoId(
                anotacionHistoriaRq.getMedicoId()
        );

        anotacionExistente.setDescripcion(
                anotacionHistoriaRq.getDescripcion()
        );

        // Guardar los cambios
        anotacionHistoriaRepository.save(anotacionExistente);

        // Crear la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setMessage(
                "Anotacion actualizada exitosamente"
        );
        respuesta.setStatus(200);

        // Devolver la respuesta
        return respuesta;
    }
    }

