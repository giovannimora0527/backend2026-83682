package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.AnotacionHistoriaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
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



    @Override
    public MiRespuestaRS crearAnotacionHistoria(AnotacionHistoriaRq anotacionHistoriaRq) {
        this.validarAnotacionHistoria(anotacionHistoriaRq);
        Optional<Mascota> optMascota = this.mascotaRepository.findById(anotacionHistoriaRq.getMascotaId());
        if (optMascota.isEmpty()) {
            throw new BadRequestException("La mascota con ID " + anotacionHistoriaRq.getMascotaId()  +
                    " no existe. Por favor consulte al administrador del sistema o registre a la mascota antes de " +
                    "crear la anotación de historia clínica.");
        }

        HistoriaMedica historia = new HistoriaMedica();
        AnotacionHistoria nuevaAnotacion = new AnotacionHistoria();

        Optional<HistoriaMedica> historiaOpt = this.historiaMedicaRepository
                .findByMascota(optMascota.get());
        boolean existeHistoria = historiaOpt.isPresent();
        if (historiaOpt.isEmpty()) {
           HistoriaMedica nueva = new HistoriaMedica();
           nueva.setMascota(optMascota.get());
           nueva.setFechaCreacion(LocalDateTime.now());
           historia = this.historiaMedicaRepository.save(nueva);
        }

        Optional<Medico> medicoOptional = this.medicoRepository.findById(anotacionHistoriaRq.getMedicoId());
        if (medicoOptional.isEmpty()) {
            throw new BadRequestException("El médico con ID " + anotacionHistoriaRq.getMedicoId() + " no existe");
        }
        nuevaAnotacion.setDescripcion(anotacionHistoriaRq.getDescripcion());
        nuevaAnotacion.setHistoria(existeHistoria? historiaOpt.get() : historia);
        nuevaAnotacion.setDescripcion(anotacionHistoriaRq.getDescripcion());
        nuevaAnotacion.setMedico(medicoOptional.get());
        nuevaAnotacion.setFecha(LocalDateTime.now());
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
        if (anotacionHistoriaRq.getMascotaId() == null) {
            throw new BadRequestException("El ID de la mascota es obligatorio");
        }
        if (anotacionHistoriaRq.getMedicoId() == null) {
            throw new BadRequestException("El ID del médico es obligatorio");
        }
        if (anotacionHistoriaRq.getDescripcion() == null || anotacionHistoriaRq.getDescripcion().isEmpty()) {
            throw new BadRequestException("La descripción de la anotación es obligatoria");
        }
    }
}
