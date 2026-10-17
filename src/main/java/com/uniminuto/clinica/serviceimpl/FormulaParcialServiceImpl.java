package com.uniminuto.clinica.serviceimpl;
import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.models.FormulaParcialRq;
import com.uniminuto.clinica.repository.*;
import com.uniminuto.clinica.service.FormulaParcialService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
/** Valida el tratamiento y su correspondencia con una cita médica. */
@Service
public class FormulaParcialServiceImpl implements FormulaParcialService {
    private final FormulaMedicaRepository formulas;
    private final CitaRepository citas;
    private final org.springframework.data.jpa.repository.JpaRepository<Medicamento,Long> medicamentos;
    public FormulaParcialServiceImpl(FormulaMedicaRepository formulas, CitaRepository citas,
        com.uniminuto.clinica.repository.MedicamentoRepository medicamentos) {
        this.formulas=formulas; this.citas=citas; this.medicamentos=medicamentos;
    }
    /** Fórmulas recientes primero, excluyendo las inactivas. */
    public List<FormulaMedica> listar() { return formulas.findAllByOrderByFechaCreacionRegistroDesc().stream()
        .filter(f -> !Boolean.FALSE.equals(f.getActivo())).toList(); }
    /** Consulta una fórmula existente. */
    public FormulaMedica obtener(Long id) { return formulas.findById(id).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND,"Fórmula no encontrada.")); }
    /** Registra un tratamiento asociado con cita, mascota y veterinario. */
    public FormulaMedica guardar(FormulaParcialRq datos) { return guardarDatos(new FormulaMedica(),datos,true); }
    /** Edita una fórmula activa y mantiene su fecha de creación. */
    public FormulaMedica actualizar(Long id, FormulaParcialRq datos) {
        FormulaMedica formula = obtener(id);
        if (formula.getCita() != null && datos != null && datos.citaId() != null &&
            !formula.getCita().getId().equals(datos.citaId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"No se puede cambiar la consulta de una fórmula existente.");
        if (Boolean.FALSE.equals(formula.getActivo()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"La fórmula está inactiva.");
        return guardarDatos(formula,datos,false);
    }
    private FormulaMedica guardarDatos(FormulaMedica formula, FormulaParcialRq d, boolean nueva) {
        if (d == null || d.citaId() == null || d.medicamentoId() == null ||
            d.dosis() == null || d.dosis().isBlank() || d.frecuencia() == null || d.frecuencia().isBlank() ||
            d.duracionTratamiento() == null || d.duracionTratamiento().isBlank() ||
            d.indicaciones() == null || d.indicaciones().isBlank() || d.fechaEmision() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Completa cita, medicamento, dosis, frecuencia, duración, fecha e indicaciones.");
        if (!cantidadPositiva(d.dosis()) || !cantidadPositiva(d.duracionTratamiento()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "La dosis y la duración deben incluir una cantidad mayor que cero.");
        Cita cita = citas.findById(d.citaId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,"Cita no encontrada."));
        Medicamento medicamento = medicamentos.findById(d.medicamentoId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND,"Medicamento no encontrado."));
        if (d.fechaEmision().isBefore(cita.getFechaHora()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La fórmula no puede emitirse antes de la consulta.");
        if ("Cancelada".equals(cita.getEstado()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"No se formula sobre una cita cancelada.");
        formula.setCita(cita); formula.setMedicamento(medicamento); formula.setDosis(d.dosis().trim());
        formula.setFrecuencia(d.frecuencia().trim()); formula.setDuracionTratamiento(d.duracionTratamiento().trim());
        formula.setIndicaciones(d.indicaciones().trim()); formula.setFechaEmision(d.fechaEmision());
        if (nueva) { formula.setFechaCreacionRegistro(LocalDateTime.now()); formula.setActivo(true); }
        else formula.setFechaActualizacionRegistro(LocalDateTime.now());
        return formulas.save(formula);
    }
    /** Exige una cantidad numérica positiva, por ejemplo 5 mg o 7 días. */
    private boolean cantidadPositiva(String valor) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+(?:[.,]\\d+)?").matcher(valor);
        return m.find() && Double.parseDouble(m.group().replace(',', '.')) > 0;
    }
}
