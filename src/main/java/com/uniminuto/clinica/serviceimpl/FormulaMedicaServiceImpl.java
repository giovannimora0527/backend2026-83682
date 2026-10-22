
package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio de Fórmulas Médicas.
 *
 * Permite:
 * 1. Listar fórmulas médicas.
 * 2. Consultar una fórmula por ID.
 * 3. Crear fórmulas.
 * 4. Actualizar fórmulas.
 * 5. Validar los datos antes de guardarlos.
 */
@Service
public class FormulaMedicaServiceImpl
        implements FormulaMedicaService {

    // Repositorio de fórmulas médicas.
    private final FormulaMedicaRepository formulaMedicaRepository;

    // Permite verificar los registros relacionados en MySQL.
    @PersistenceContext
    private EntityManager entityManager;

    public FormulaMedicaServiceImpl(
            FormulaMedicaRepository formulaMedicaRepository
    ) {
        this.formulaMedicaRepository = formulaMedicaRepository;
    }

    // ================================================
    // 1. LISTAR TODAS LAS FÓRMULAS
    // ================================================

    @Override
    @Transactional(readOnly = true)
    public List<FormulaMedica> obtenerFormulasMedicas() {

        // Mantiene el orden que ya teníamos funcionando.
        return formulaMedicaRepository
                .findAllByOrderByFechaCreacionRegistroDesc();
    }

    // ================================================
    // 2. CONSULTAR UNA FÓRMULA POR ID
    // ================================================

    @Override
    @Transactional(readOnly = true)
    public FormulaMedica obtenerFormulaPorId(Long id) {

        return formulaMedicaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe una fórmula médica con ID: " + id
                ));
    }

    // ================================================
    // 3. CREAR UNA NUEVA FÓRMULA
    // ================================================

    @Override
    @Transactional
    public FormulaMedica crearFormula(FormulaMedica formula) {

        // Revisar que la información sea válida.
        validarFormula(formula);

        // El identificador lo genera MySQL.
        formula.setId(null);

        // Registrar automáticamente la fecha de creación.
        formula.setFechaCreacionRegistro(LocalDateTime.now());

        // Todavía no existe ninguna modificación.
        formula.setFechaActualizacionRegistro(null);

        // Si no enviaron una fecha de emisión,
        // se toma el momento de creación.
        if (formula.getFechaEmision() == null) {
            formula.setFechaEmision(LocalDateTime.now());
        }

        // Guardar la fórmula en la base de datos.
        return formulaMedicaRepository.save(formula);
    }

    // ================================================
    // 4. ACTUALIZAR UNA FÓRMULA EXISTENTE
    // ================================================

    @Override
    @Transactional
    public FormulaMedica actualizarFormula(
            Long id,
            FormulaMedica formula
    ) {

        // Buscar el registro que vamos a modificar.
        FormulaMedica existente = obtenerFormulaPorId(id);

        // Validar la nueva información.
        validarFormula(formula);

        // Actualizar únicamente los campos permitidos.
        existente.setCitaId(formula.getCitaId());

        existente.setMedicamentoId(formula.getMedicamentoId());

        existente.setDosis(formula.getDosis());

        existente.setFrecuencia(formula.getFrecuencia());

        existente.setDuracionTratamiento(
                formula.getDuracionTratamiento()
        );

        existente.setIndicaciones(formula.getIndicaciones());

        existente.setFechaEmision(formula.getFechaEmision());

        // No modificar la fecha original de creación.
        // Registrar cuándo se actualizó.
        existente.setFechaActualizacionRegistro(
                LocalDateTime.now()
        );

        return formulaMedicaRepository.save(existente);
    }

    // ================================================
    // 5. VALIDAR INFORMACIÓN DE LA FÓRMULA
    // ================================================

    private void validarFormula(FormulaMedica formula) {

        if (formula == null) {
            errorValidacion("Debes enviar los datos de la fórmula.");
        }

        // Validar identificador de la cita.
        if (formula.getCitaId() == null
                || formula.getCitaId() <= 0) {
            errorValidacion("Selecciona una cita médica válida.");
        }

        // Validar identificador del medicamento.
        if (formula.getMedicamentoId() == null
                || formula.getMedicamentoId() <= 0) {
            errorValidacion("Selecciona un medicamento válido.");
        }

        // Validar la dosis.
        if (formula.getDosis() == null
                || formula.getDosis().isBlank()) {
            errorValidacion("La dosis es obligatoria.");
        }

        // Validar la frecuencia de administración.
        if (formula.getFrecuencia() == null
                || formula.getFrecuencia().isBlank()) {
            errorValidacion("La frecuencia es obligatoria.");
        }

        // Validar la duración del tratamiento.
        if (formula.getDuracionTratamiento() == null
                || formula.getDuracionTratamiento().isBlank()) {
            errorValidacion("La duración del tratamiento es obligatoria.");
        }

        // Verificar los tamaños de los nuevos campos.
        if (formula.getFrecuencia().length() > 100) {
            errorValidacion("La frecuencia no puede superar 100 caracteres.");
        }

        if (formula.getDuracionTratamiento().length() > 100) {
            errorValidacion("La duración no puede superar 100 caracteres.");
        }

        // ============================================
        // VERIFICAR QUE LA CITA EXISTA
        // ============================================

        Number citasEncontradas = (Number) entityManager
                .createNativeQuery(
                        "SELECT COUNT(*) FROM cita WHERE id = :id"
                )
                .setParameter("id", formula.getCitaId())
                .getSingleResult();

        if (citasEncontradas.longValue() == 0) {
            errorValidacion("La cita médica seleccionada no existe.");
        }

        // ============================================
        // VERIFICAR QUE EL MEDICAMENTO EXISTA
        // ============================================

        Number medicamentosEncontrados = (Number) entityManager
                .createNativeQuery(
                        "SELECT COUNT(*) FROM medicamento WHERE id = :id"
                )
                .setParameter("id", formula.getMedicamentoId())
                .getSingleResult();

        if (medicamentosEncontrados.longValue() == 0) {
            errorValidacion("El medicamento seleccionado no existe.");
        }

        // ============================================
        // VALIDAR EL PROPIETARIO DE LA MASCOTA
        // ============================================

        // El cliente de la cita debe corresponder
        // al propietario de la mascota.
        // Esta es la validación que acabamos de
        // revisar y corregir en el caso de Thor.

        Number relacionesCorrectas = (Number) entityManager
                .createNativeQuery(
                        """
                        SELECT COUNT(*)
                        FROM cita c
                        INNER JOIN mascota m
                            ON c.mascota_id = m.mascota_id
                        WHERE c.id = :citaId
                          AND c.cliente_id = m.cliente_id
                        """
                )
                .setParameter("citaId", formula.getCitaId())
                .getSingleResult();

        if (relacionesCorrectas.longValue() == 0) {
            errorValidacion(
                    "El cliente de la cita no corresponde "
                            + "al propietario de la mascota."
            );
        }
    }

    // ================================================
    // 6. RESPONDER CUANDO EXISTE UN ERROR
    // ================================================

    private void errorValidacion(String mensaje) {

        // Devolver error HTTP 400 al frontend.
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                mensaje
        );
    }
}
