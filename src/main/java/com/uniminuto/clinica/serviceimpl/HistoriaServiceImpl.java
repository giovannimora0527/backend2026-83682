package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRs;
import com.uniminuto.clinica.models.HistoriaMedicaRq;
import com.uniminuto.clinica.models.HistoriaMedicaRs;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.service.HistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HistoriaServiceImpl implements HistoriaService {

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    @Override
    public List<HistoriaMedicaRs> getAllHistorias() {
        List<HistoriaMedica> historias = historiaMedicaRepository.findAll();
        List<HistoriaMedicaRs> resultados = new ArrayList<>();
        if (!historias.isEmpty()) {
            resultados.addAll(historias.stream().map(historia -> {
                HistoriaMedicaRs historiaMedicaRs = new HistoriaMedicaRs();
                historiaMedicaRs.setHistoria(historia);
                historiaMedicaRs.setAnotaciones(anotacionHistoriaRepository
                        .findByHistoria(historia).stream()
                        .map(AnotacionHistoriaRs::new).toList());
                return historiaMedicaRs;
            }).toList());
        }
        List<AnotacionHistoriaRs> anotaciones = resultados.stream()
                .flatMap(historia -> historia.getAnotaciones().stream()).toList();
        List<Long> citaIds = anotaciones.stream()
                .map(AnotacionHistoriaRs::getCita)
                .filter(Objects::nonNull)
                .map(cita -> cita.getId())
                .distinct().toList();
        if (!citaIds.isEmpty()) {
            Map<Long, List<FormulaMedica>> formulasPorCita = formulaMedicaRepository
                    .findByCita_IdInOrderByFechaCreacionRegistroDesc(citaIds).stream()
                    .collect(Collectors.groupingBy(formula -> formula.getCita().getId()));
            anotaciones.stream().filter(anotacion -> anotacion.getCita() != null)
                    .forEach(anotacion -> anotacion.setFormulasMedicas(
                            formulasPorCita.getOrDefault(anotacion.getCita().getId(), List.of())));
        }
        return resultados;
    }

    @Override
    public MiRespuestaRS guardarHistoria(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        this.validarHistoria(historiaMedicaRq);
        Mascota mascota = this.existeMascota(historiaMedicaRq);
        if (this.historiaMedicaRepository.existsByMascota(mascota)) {
            throw new BadRequestException("La mascota ya tiene una historia clínica registrada.");
        }

        HistoriaMedica historiaNueva = new HistoriaMedica();
        historiaNueva.setMascota(mascota);
        historiaNueva.setFechaCreacion(LocalDateTime.now());
        this.historiaMedicaRepository.save(historiaNueva);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Historia clínica guardada correctamente.");
        return rta;
    }

    @Override
    public MiRespuestaRS actualizarHistoria(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        if (historiaMedicaRq == null || historiaMedicaRq.getId() == null || historiaMedicaRq.getId() <= 0) {
            throw new BadRequestException("El id de la historia clínica es obligatorio y debe ser mayor a cero.");
        }
        this.validarHistoria(historiaMedicaRq);
        Optional<HistoriaMedica> optionalHistoria = this.historiaMedicaRepository
                .findById(historiaMedicaRq.getId());
        if (optionalHistoria.isEmpty()) {
            throw new BadRequestException("La historia clínica con el id proporcionado no existe.");
        }
        Mascota mascota = this.existeMascota(historiaMedicaRq);
        if (this.historiaMedicaRepository.existsByMascotaAndIdNot(mascota, historiaMedicaRq.getId())) {
            throw new BadRequestException("La mascota ya tiene otra historia clínica registrada.");
        }

        HistoriaMedica historiaExistente = optionalHistoria.get();
        historiaExistente.setMascota(mascota);
        this.historiaMedicaRepository.save(historiaExistente);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Historia clínica actualizada correctamente.");
        return rta;
    }

    private Mascota existeMascota(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        Optional<Mascota> optionalMascota = this.mascotaRepository.findById(historiaMedicaRq.getMascotaId());
        if (optionalMascota.isEmpty()) {
            throw new BadRequestException("La mascota con el id proporcionado no existe.");
        }
        return optionalMascota.get();
    }

    private void validarHistoria(HistoriaMedicaRq historiaMedicaRq) throws BadRequestException {
        if (historiaMedicaRq == null) {
            throw new BadRequestException("La información de la historia clínica es obligatoria.");
        }
        if (historiaMedicaRq.getMascotaId() == null || historiaMedicaRq.getMascotaId() <= 0) {
            throw new BadRequestException("El id de la mascota es obligatorio y debe ser mayor a cero.");
        }
    }
}
