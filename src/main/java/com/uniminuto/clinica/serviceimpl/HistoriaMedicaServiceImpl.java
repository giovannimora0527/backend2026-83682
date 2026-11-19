
package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.service.AlcanceMedico;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// IMPLEMENTACIÓN DEL SERVICIO DE HISTORIAS MÉDICAS
// ==========================================================

// Entidad correspondiente a la tabla historia_medica.
import com.uniminuto.clinica.entity.HistoriaMedica;

// Repositorio que permite consultar MySQL.
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;

// Interfaz del nuevo servicio de historias médicas.
import com.uniminuto.clinica.service.HistoriaMedicaService;

// Anotación para identificar un servicio de Spring Boot.
import org.springframework.stereotype.Service;

// Lista para devolver varios registros.
import java.util.List;

// ==========================================================
// IMPLEMENTACIÓN DEL SERVICIO
// ==========================================================

@Service
public class HistoriaMedicaServiceImpl
        implements HistoriaMedicaService {

    // ======================================================
    // 1. REPOSITORIO
    // ======================================================

    private final HistoriaMedicaRepository
            historiaMedicaRepository;

    // ======================================================
    // 2. CONSTRUCTOR
    // ======================================================

    /**
     * Spring Boot proporciona automáticamente
     * una instancia del repositorio.
     */

    private final AlcanceMedico alcance;

    public HistoriaMedicaServiceImpl(
            AlcanceMedico alcance,
            HistoriaMedicaRepository historiaMedicaRepository
    ) {
        this.alcance = alcance;

        this.historiaMedicaRepository =
                historiaMedicaRepository;

    }

    // ======================================================
    // 3. CONSULTAR TODAS LAS HISTORIAS MÉDICAS
    // ======================================================

    /**
     * Obtiene todas las historias médicas
     * ordenadas por ID de menor a mayor.
     */

    @Override
    public List<HistoriaMedica> obtenerHistoriasMedicas() {

        return filtrarHistorias(historiaMedicaRepository.findAllByOrderByIdAsc());

    }

    // ======================================================
    // 4. CONSULTAR HISTORIAS POR PACIENTE
    // ======================================================

    /**
     * Busca las historias médicas relacionadas
     * con un paciente específico.
     */

    @Override
    public List<HistoriaMedica> obtenerHistoriasPorPaciente(
            Integer pacienteId
    ) {

        return filtrarHistorias(historiaMedicaRepository.findByPacienteId(pacienteId));

    }

    private List<HistoriaMedica> filtrarHistorias(List<HistoriaMedica> historias) {
        Integer medicoId = alcance.medicoId();
        if (medicoId == null) {
            return historias;
        }
        var autorizadas = alcance.historias(medicoId);
        return historias.stream().filter(historia -> autorizadas.contains(historia.getId())).toList();
    }
}
