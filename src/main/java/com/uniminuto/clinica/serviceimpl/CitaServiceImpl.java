package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.service.CitaService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio encargado de gestionar
 * las operaciones relacionadas con las citas de la clínica.
 */
@Service
public class CitaServiceImpl implements CitaService {

    /**
     * Repositorio utilizado para consultar y guardar
     * las citas en la base de datos.
     */
    @Autowired
    private CitaRepository citaRepository;

    /**
     * Busca las citas entre una fecha inicial y una fecha final.
     *
     * @param fechaInicial fecha y hora inicial de búsqueda.
     * @param fechaFinal fecha y hora final de búsqueda.
     * @return lista de citas encontradas.
     */
    @Override
    public List<Cita> buscarCitasPorFechas(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal) {

        return citaRepository
                .findByFechaHoraBetweenOrderByFechaHoraDesc(
                        fechaInicial,
                        fechaFinal
                );
    }

    /**
     * Guarda una nueva cita en la base de datos.
     *
     * @param cita información de la nueva cita.
     * @return cita almacenada.
     */
    @Override
    public Cita crearCita(Cita cita) {

        cita.setId(null);

        return citaRepository.save(cita);
    }

    /**
     * Actualiza una cita que ya existe en la base de datos.
     *
     * @param id identificador de la cita.
     * @param cita información actualizada.
     * @return cita actualizada.
     * @throws BadRequestException si la cita no existe.
     */
    @Override
    public Cita actualizarCita(Long id, Cita cita)
            throws BadRequestException {

        Cita citaExistente = citaRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException(
                                "No existe una cita con el id: " + id
                        )
                );

        citaExistente.setClienteId(cita.getClienteId());
        citaExistente.setMascotaId(cita.getMascotaId());
        citaExistente.setMedicoId(cita.getMedicoId());
        citaExistente.setFechaHora(cita.getFechaHora());
        citaExistente.setEstado(cita.getEstado());
        citaExistente.setMotivo(cita.getMotivo());

        return citaRepository.save(citaExistente);
    }
}