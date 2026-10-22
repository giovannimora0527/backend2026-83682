package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.FormulaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FormulaServiceImpl implements FormulaService {

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Override
    public List<FormulaMedica> findAllFormulas() {
        return formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();
    }

    @Override
    public MiRespuestaRS guardarFormula(FormulaRq formulaRq) throws BadRequestException {
        this.validarFormula(formulaRq);
        Cita cita = this.existeCita(formulaRq);
        Medicamento medicamento = this.existeMedicamento(formulaRq);
        FormulaMedica formulaNueva = new FormulaMedica();
        formulaNueva.setCita(cita);
        formulaNueva.setMedicamento(medicamento);
        formulaNueva.setDosis(formulaRq.getDosis());
        formulaNueva.setIndicaciones(formulaRq.getIndicaciones());
        formulaNueva.setDuracion(formulaRq.getDuracion());
        formulaNueva.setFrecuencia(formulaRq.getFrecuencia());
        formulaNueva.setFechaCreacionRegistro(LocalDateTime.now());
        formulaMedicaRepository.save(formulaNueva);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Fórmula médica guardada correctamente.");
        return rta;
    }

    private Cita existeCita(FormulaRq formulaRq) throws BadRequestException {
        Optional<Cita> citaOptional = citaRepository.findById(formulaRq.getCitaId());
        if (citaOptional.isEmpty()) {
            throw new BadRequestException("La cita con el id proporcionado no existe.");
        }
        return citaOptional.get();
    }

    private Medicamento existeMedicamento(FormulaRq formulaRq) throws BadRequestException {
        Optional<Medicamento> medicamentoOptional = medicamentoRepository.findById(formulaRq.getMedicamentoId());
        if (medicamentoOptional.isEmpty()) {
            throw new BadRequestException("El medicamento con el id proporcionado no existe.");
        }
        return medicamentoOptional.get();
    }

    @Override
    public MiRespuestaRS actualizarFormula(FormulaRq formulaRq) throws BadRequestException {
        this.validarFormula(formulaRq);
        Cita cita = this.existeCita(formulaRq);
        Medicamento medicamento = this.existeMedicamento(formulaRq);
        Optional<FormulaMedica> optionalFormulaMedica = this.formulaMedicaRepository.findById(formulaRq.getId());
        if (optionalFormulaMedica.isEmpty()) {
            throw new BadRequestException("La fórmula médica con el id proporcionado no existe.");
        }
        FormulaMedica formulaExistente = optionalFormulaMedica.get();
        formulaExistente.setCita(cita);
        formulaExistente.setMedicamento(medicamento);
        formulaExistente.setDosis(formulaRq.getDosis());
        formulaExistente.setIndicaciones(formulaRq.getIndicaciones());
        formulaExistente.setDuracion(formulaRq.getDuracion());
        formulaExistente.setFrecuencia(formulaRq.getFrecuencia());
        formulaExistente.setFechaActualizacionRegistro(LocalDateTime.now());
        this.formulaMedicaRepository.save(formulaExistente);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(200);
        rta.setMessage("Fórmula médica actualizada correctamente.");
        return rta;
    }


    private void validarFormula(FormulaRq formulaRq) throws BadRequestException {
        if (formulaRq.getCitaId() == null || formulaRq.getCitaId() <= 0) {
            throw new BadRequestException("El id de la cita es obligatorio y debe ser mayor a cero.");
        }
        if (formulaRq.getCitaId() == null || formulaRq.getCitaId() <= 0) {
            throw new BadRequestException("El id de la cita es obligatorio y debe ser mayor a cero.");
        }
        if (formulaRq.getDosis() == null || formulaRq.getDosis().isEmpty()) {
            throw new BadRequestException("La dosis de la fórmula es obligatoria.");
        }
        if (formulaRq.getIndicaciones() == null || formulaRq.getIndicaciones().isEmpty()) {
            throw new BadRequestException("Las indicaciones de la fórmula son obligatorias.");
        }
        if(formulaRq.getDuracion() == null || formulaRq.getDuracion().isEmpty()) {
            throw new BadRequestException("La duración de la fórmula es obligatoria.");
        }
        if(formulaRq.getFrecuencia() == null || formulaRq.getFrecuencia().isEmpty()) {
            throw new BadRequestException("La frecuencia de la fórmula es obligatoria.");
        }

    }
}
