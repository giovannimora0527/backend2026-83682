package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MedicoRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de Médicos.
 */
@Service
public class MedicoServiceImpl implements MedicoService {

    @Autowired
    private MedicoRepository medicoRepository;

    /**
     * Lista todos los médicos registrados.
     * @return Lista de médicos.
     */
    @Override
    public List<Medico> listarMedicos() {
        return medicoRepository.findAll();
    }

    /**
     * Busca un médico por su tipo y número de documento.
     * @param tipoDocumento Tipo de documento del médico.
     * @param numeroDocumento Número de documento.
     * @return El médico encontrado.
     * @throws BadRequestException si los parámetros son nulos o el médico no existe.
     */
    @Override
    public Medico buscarMedico(String tipoDocumento, String numeroDocumento) throws BadRequestException {
        if (numeroDocumento == null || numeroDocumento.isEmpty()) {
            throw new BadRequestException("El número de documento es obligatorio");
        }
        if (tipoDocumento == null || tipoDocumento.isEmpty()) {
            throw new BadRequestException("El tipo de documento es obligatorio");
        }

        Optional<Medico> medico = medicoRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento);
        if (medico.isEmpty()) {
            throw new BadRequestException("Medico no encontrado");
        }

        return medico.get();
    }

    /**
     * Busca un médico por su registro profesional.
     * @param registroProfesional Registro profesional del médico.
     * @return El médico encontrado.
     * @throws BadRequestException si el registro es nulo o el médico no existe.
     */
    @Override
    public Medico buscarMedicoPorRegistroProfesional(String registroProfesional) throws BadRequestException {
        if (registroProfesional == null || registroProfesional.isEmpty()) {
            throw new BadRequestException("El registro profesional es obligatorio");
        }

        Optional<Medico> medico = medicoRepository.findByRegistroProfesional(registroProfesional);
        if (medico.isEmpty()) {
            throw new BadRequestException("Medico no encontrado");
        }

        return medico.get();
    }

    /**
     * Guarda un nuevo médico en la base de datos.
     * @param medicoRq Objeto con los datos del médico a guardar.
     * @return Respuesta estandarizada con el estado de la operación.
     * @throws BadRequestException si faltan campos obligatorios.
     */
    @Override
    public MiRespuestaRS guardarMedico(MedicoRq medicoRq) throws BadRequestException {
        if (medicoRq.getNombres() == null || medicoRq.getNumeroDocumento() == null) {
            throw new BadRequestException("Nombres y número de documento son obligatorios");
        }

        Medico nuevo = new Medico();
        nuevo.setNombres(medicoRq.getNombres());
        nuevo.setApellidos(medicoRq.getApellidos());
        nuevo.setTipoDocumento(medicoRq.getTipoDocumento());
        nuevo.setNumeroDocumento(medicoRq.getNumeroDocumento());
        nuevo.setRegistroProfesional(medicoRq.getRegistroProfesional());

        medicoRepository.save(nuevo);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Médico guardado correctamente");
        return rta;
    }

    /**
     * Actualiza los datos de un médico existente.
     * @param medicoRq Objeto con los datos actualizados y el ID del médico.
     * @return Respuesta estandarizada con el estado de la operación.
     * @throws BadRequestException si el ID es nulo o el médico no existe.
     */
    @Override
    public MiRespuestaRS actualizarMedico(MedicoRq medicoRq) throws BadRequestException {
        if (medicoRq.getId() == null) throw new BadRequestException("El ID es obligatorio");

        Optional<Medico> optMedico = medicoRepository.findById(medicoRq.getId());
        if (optMedico.isEmpty()) throw new BadRequestException("El médico no existe");

        Medico existente = optMedico.get();
        existente.setNombres(medicoRq.getNombres());
        existente.setApellidos(medicoRq.getApellidos());
        existente.setTipoDocumento(medicoRq.getTipoDocumento());
        existente.setNumeroDocumento(medicoRq.getNumeroDocumento());
        existente.setRegistroProfesional(medicoRq.getRegistroProfesional());

        medicoRepository.save(existente);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Médico actualizado correctamente");
        return rta;
    }
}