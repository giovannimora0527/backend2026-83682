package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    @Autowired
    private HistoriaMedicaRepository historiaMedicaRepository;

    @Autowired
    private AnotacionHistoriaRepository anotacionHistoriaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Override
    public MiRespuestaRS crearAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq) {
        this.validarAnotacionHistoria(anotacionHistoriaRq);
        Optional<Cita> optCita = this.citaRepository.findById(anotacionHistoriaRq.getCitaId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita con ID " + anotacionHistoriaRq.getCitaId()  +
                    " no existe. Por favor consulte al administrador del sistema o registre la cita antes de " +
                    "crear la anotación de historia clínica.");
        }

        HistoriaMedica historia = new HistoriaMedica();
        AnotacionHistoria nuevaAnotacion = new AnotacionHistoria();

        Cita cita = optCita.get();

        Optional<HistoriaMedica> historiaOpt = this.historiaMedicaRepository
                .findByMascota(cita.getMascota());
        boolean existeHistoria = historiaOpt.isPresent();
        if (historiaOpt.isEmpty()) {
           HistoriaMedica nueva = new HistoriaMedica();
           nueva.setMascota(cita.getMascota());
           nueva.setFechaCreacion(LocalDateTime.now());
           historia = this.historiaMedicaRepository.save(nueva);
        }

        Optional<Medico> medicoOptional = this.medicoRepository.findById(anotacionHistoriaRq.getMedicoId());
        if (medicoOptional.isEmpty()) {
            throw new BadRequestException("El médico con ID " + anotacionHistoriaRq.getMedicoId() + " no existe");
        }
        nuevaAnotacion.setDescripcion(anotacionHistoriaRq.getDescripcion());
        nuevaAnotacion.setHistoria(existeHistoria? historiaOpt.get() : historia);
        nuevaAnotacion.setDiagnostico(anotacionHistoriaRq.getDiagnostico());
        nuevaAnotacion.setObservaciones(anotacionHistoriaRq.getObservaciones());
        nuevaAnotacion.setMedico(medicoOptional.get());
        nuevaAnotacion.setFecha(LocalDateTime.now());
        nuevaAnotacion.setCita(cita);
        this.anotacionHistoriaRepository.save(nuevaAnotacion);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Anotación de historia clínica creada exitosamente");
        return rta;
    }

    private void validarAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq) throws BadRequestException {
        if (anotacionHistoriaRq == null) {
            throw new BadRequestException("La anotación de historia clínica es obligatoria");
        }
        if (anotacionHistoriaRq.getCitaId() == null) {
            throw new BadRequestException("El ID de la cita es obligatorio");
        }
        if (anotacionHistoriaRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio");
        }
        if (anotacionHistoriaRq.getDescripcion() == null || anotacionHistoriaRq.getDescripcion().isEmpty()) {
            throw new BadRequestException("La descripción de la anotación es obligatoria");
        }
        if (anotacionHistoriaRq.getDiagnostico() == null || anotacionHistoriaRq.getDiagnostico().isEmpty()) {
            throw new BadRequestException("El diagnóstico de la anotación es obligatorio");
        }
        if (anotacionHistoriaRq.getObservaciones() == null || anotacionHistoriaRq.getObservaciones().isEmpty()) {
            throw new BadRequestException("Las observaciones de la anotación son obligatorias");
        }
    }
}
