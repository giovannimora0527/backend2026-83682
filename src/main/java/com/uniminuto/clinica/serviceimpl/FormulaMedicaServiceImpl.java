package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para la gestión de Fórmulas Médicas.
 */
@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    /**
     * Lista las fórmulas médicas ordenadas por fecha de creación.
     * @param asc Indica si el orden es ascendente (true) o descendente (false).
     * @return Lista de fórmulas médicas.
     */
    @Override
    public List<FormulaMedica> listarFormulasOrdenadas(boolean asc) {
        return asc ? formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroAsc()
                : formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();
    }

    /**
     * Crea una nueva fórmula médica en el sistema.
     * @param formulaRq Objeto con los datos de la fórmula a crear.
     * @return Respuesta estandarizada de éxito.
     * @throws BadRequestException si la cita o medicamento no existen, o si la fecha de emisión es inválida.
     */
    @Override
    public MiRespuestaRS crearFormula(FormulaMedicaRq formulaRq) throws BadRequestException {
        if (formulaRq.getCitaId() == null) {
            throw new BadRequestException("El ID de la cita es obligatorio");
        }
        if (formulaRq.getMedicamentoId() == null) {
            throw new BadRequestException("El ID del medicamento es obligatorio");
        }
        if (formulaRq.getDosis() == null || formulaRq.getDosis().isEmpty()) {
            throw new BadRequestException("La dosis es obligatoria");
        }

        Optional<Cita> optCita = citaRepository.findById(formulaRq.getCitaId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita con ID " + formulaRq.getCitaId() + " no existe");
        }
        Cita cita = optCita.get();

        Optional<Medicamento> optMedicamento = medicamentoRepository.findById(formulaRq.getMedicamentoId());
        if (optMedicamento.isEmpty()) {
            throw new BadRequestException("El medicamento con ID " + formulaRq.getMedicamentoId() + " no existe");
        }

        LocalDateTime fechaEmision = formulaRq.getFechaCreacionRegistro() != null
                ? formulaRq.getFechaCreacionRegistro()
                : LocalDateTime.now();

        if (fechaEmision.isBefore(cita.getFechaHora())) {
            throw new BadRequestException("La fecha de emisión no puede ser anterior a la fecha de la cita.");
        }

        FormulaMedica nuevaFormula = new FormulaMedica();
        nuevaFormula.setCita(cita);
        nuevaFormula.setMedicamento(optMedicamento.get());
        nuevaFormula.setDosis(formulaRq.getDosis());
        nuevaFormula.setIndicaciones(formulaRq.getIndicaciones());
        nuevaFormula.setFechaCreacionRegistro(fechaEmision);
        nuevaFormula.setFechaActualizacionRegistro(LocalDateTime.now());

        formulaMedicaRepository.save(nuevaFormula);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Fórmula médica creada correctamente");
        return respuesta;
    }

    /**
     * Actualiza los datos de una fórmula médica existente.
     * @param formulaRq Objeto con los datos actualizados y el ID de la fórmula.
     * @return Respuesta estandarizada de éxito.
     * @throws BadRequestException si la fórmula, cita o medicamento no existen.
     */
    @Override
    public MiRespuestaRS actualizarFormula(FormulaMedicaRq formulaRq) throws BadRequestException {
        if (formulaRq.getId() == null) {
            throw new BadRequestException("El ID de la fórmula es obligatorio para actualizar");
        }

        Optional<FormulaMedica> optFormula = formulaMedicaRepository.findById(formulaRq.getId());
        if (optFormula.isEmpty()) {
            throw new BadRequestException("La fórmula con ID " + formulaRq.getId() + " no existe");
        }
        FormulaMedica formulaExistente = optFormula.get();

        if (formulaRq.getCitaId() == null) {
            throw new BadRequestException("El ID de la cita es obligatorio");
        }
        Optional<Cita> optCita = citaRepository.findById(formulaRq.getCitaId());
        if (optCita.isEmpty()) {
            throw new BadRequestException("La cita con ID " + formulaRq.getCitaId() + " no existe");
        }
        Cita cita = optCita.get();

        if (formulaRq.getMedicamentoId() == null) {
            throw new BadRequestException("El ID del medicamento es obligatorio");
        }
        Optional<Medicamento> optMedicamento = medicamentoRepository.findById(formulaRq.getMedicamentoId());
        if (optMedicamento.isEmpty()) {
            throw new BadRequestException("El medicamento con ID " + formulaRq.getMedicamentoId() + " no existe");
        }

        LocalDateTime fechaEmision = LocalDateTime.now();
        if (fechaEmision.isBefore(cita.getFechaHora())) {
            throw new BadRequestException("La fecha de emisión no puede ser anterior a la fecha de la cita.");
        }

        formulaExistente.setCita(cita);
        formulaExistente.setMedicamento(optMedicamento.get());
        formulaExistente.setDosis(formulaRq.getDosis());
        formulaExistente.setIndicaciones(formulaRq.getIndicaciones());
        formulaExistente.setFechaActualizacionRegistro(LocalDateTime.now());

        formulaMedicaRepository.save(formulaExistente);

        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setStatus(200);
        respuesta.setMessage("Fórmula médica actualizada correctamente");
        return respuesta;
    }
}