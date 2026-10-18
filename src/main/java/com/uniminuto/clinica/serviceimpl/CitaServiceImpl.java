
package com.uniminuto.clinica.serviceimpl;

// ==========================================================
// CLÍNICA VETERINARIA - UNIMINUTO
// IMPLEMENTACIÓN DEL SERVICIO DE CITAS MÉDICAS
// ==========================================================

// Entidad de citas.
import com.uniminuto.clinica.entity.Cita;

// Repositorio de citas.
import com.uniminuto.clinica.repository.CitaRepository;

// Interfaz del servicio.
import com.uniminuto.clinica.service.CitaService;

// Excepción utilizada en el proyecto.
import org.apache.coyote.BadRequestException;

// Herramientas de Spring Boot.
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// Herramientas de Java.
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * ==========================================================
 * SERVICIO: CitaServiceImpl
 * ==========================================================
 *
 * Implementa las operaciones de Gestión de Citas:
 *
 * 1. Buscar citas por fechas.
 * 2. Crear citas.
 * 3. Actualizar citas.
 * 4. Listar todas las citas.
 * 5. Consultar una cita por ID.
 *
 * También valida:
 *
 * - Existencia del cliente.
 * - Existencia de la mascota.
 * - Relación entre cliente y mascota.
 * - Existencia del veterinario.
 * - Fechas de programación.
 * - Conflictos de horarios.
 * - Estados permitidos.
 *
 * Las validaciones se realizan en el backend
 * para mantener la integridad de la información.
 */
@Service
public class CitaServiceImpl implements CitaService {

    // ======================================================
    // 1. DEPENDENCIAS
    // ======================================================

    /**
     * Repositorio para consultar y guardar citas.
     */
    private final CitaRepository citaRepository;

    /**
     * Permite ejecutar consultas SQL para comprobar
     * las relaciones existentes en la base de datos.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor que recibe las dependencias.
     *
     * @param citaRepository Repositorio de citas.
     * @param jdbcTemplate Herramienta de consultas SQL.
     */
    public CitaServiceImpl(
            CitaRepository citaRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.citaRepository = citaRepository;
        this.jdbcTemplate = jdbcTemplate;
    }


    // ======================================================
    // 2. BUSCAR CITAS POR FECHAS
    // ======================================================

    /**
     * Busca las citas dentro de un rango de fechas.
     *
     * Verifica que:
     *
     * - Ambas fechas estén presentes.
     * - La fecha inicial no supere la fecha final.
     *
     * @param fechaInicial Inicio del rango.
     * @param fechaFinal Final del rango.
     * @return Citas ordenadas por fecha descendente.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Cita> buscarCitasPorFechas(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    ) {

        // Validar que las fechas no sean nulas.
        if (fechaInicial == null || fechaFinal == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes indicar las dos fechas."
            );
        }

        // Validar el orden de las fechas.
        if (fechaInicial.isAfter(fechaFinal)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha inicial no puede superar la fecha final."
            );
        }

        // Consultar la base de datos.
        return citaRepository
                .findByFechaHoraBetweenOrderByFechaHoraDesc(
                        fechaInicial,
                        fechaFinal
                );
    }


    // ======================================================
    // 3. CREAR UNA NUEVA CITA
    // ======================================================

    /**
     * Registra una cita veterinaria en MySQL.
     *
     * Antes de guardar valida los datos,
     * las relaciones y la disponibilidad.
     *
     * @param cita Información de la nueva cita.
     * @return Cita registrada en MySQL.
     */
    @Override
    @Transactional
    public Cita crearCita(Cita cita) {

        // Paso 1: Validar los campos obligatorios.
        validarCampos(cita);

        // Paso 2: Normalizar el estado recibido.
        String estado = normalizarEstado(
                cita.getEstado()
        );

        // Una cita nueva no puede comenzar
        // directamente como atendida o cancelada.
        if (estado.equals("atendida")
                || estado.equals("cancelada")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Una cita nueva debe iniciar en un estado activo."
            );
        }

        cita.setEstado(estado);

        // Paso 3: Comprobar que la fecha sea futura.
        validarFechaFutura(cita.getFechaHora());

        // Paso 4: Comprobar cliente, mascota y médico.
        validarRelaciones(cita);

        // Paso 5: Comprobar que el horario esté libre.
        validarDisponibilidad(cita, 0L);

        // Paso 6: El ID se genera automáticamente.
        cita.setId(null);

        // Paso 7: Guardar en MySQL.
        return citaRepository.save(cita);
    }


    // ======================================================
    // 4. ACTUALIZAR UNA CITA EXISTENTE
    // ======================================================

    /**
     * Modifica una cita existente.
     *
     * No permite modificar citas que ya estén
     * canceladas o atendidas.
     *
     * Al cerrar una cita, únicamente permite
     * cambiar su estado, conservando sus datos.
     *
     * @param id Identificador de la cita.
     * @param cita Información actualizada.
     * @return Cita modificada.
     * @throws BadRequestException Si la cita no existe.
     */
    @Override
    @Transactional
    public Cita actualizarCita(
            Long id,
            Cita cita
    ) throws BadRequestException {

        // Paso 1: Comprobar que la cita exista.
        Cita citaExistente = citaRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException(
                                "No existe una cita con el ID: " + id
                        )
                );

        // Paso 2: Revisar el estado anterior.
        String estadoAnterior = normalizarEstado(
                citaExistente.getEstado()
        );

        // Las citas finalizadas no pueden modificarse.
        if (estadoAnterior.equals("cancelada")
                || estadoAnterior.equals("atendida")) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puedes modificar una cita cancelada o atendida."
            );
        }

        // Paso 3: Validar los datos recibidos.
        validarCampos(cita);

        // Paso 4: Obtener el estado solicitado.
        String nuevoEstado = normalizarEstado(
                cita.getEstado()
        );

        cita.setEstado(nuevoEstado);

        // Paso 5: Comprobar cambios de horario.
        boolean cambioHorario = !Objects.equals(
                citaExistente.getFechaHora(),
                cita.getFechaHora()
        );

        // Paso 6: Si se cierra una cita, no permitir
        // modificaciones simultáneas de otros datos.
        if (nuevoEstado.equals("atendida")
                || nuevoEstado.equals("cancelada")) {

            boolean cambioCliente = !Objects.equals(
                    citaExistente.getClienteId(),
                    cita.getClienteId()
            );

            boolean cambioMascota = !Objects.equals(
                    citaExistente.getMascotaId(),
                    cita.getMascotaId()
            );

            boolean cambioMedico = !Objects.equals(
                    citaExistente.getMedicoId(),
                    cita.getMedicoId()
            );

            boolean cambioMotivo = !Objects.equals(
                    citaExistente.getMotivo(),
                    cita.getMotivo()
            );

            if (cambioHorario
                    || cambioCliente
                    || cambioMascota
                    || cambioMedico
                    || cambioMotivo) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Para cerrar la cita, conserva sus datos originales."
                );
            }
        }

        // Paso 7: Validar fechas de citas activas.
        if (!nuevoEstado.equals("atendida")
                && !nuevoEstado.equals("cancelada")) {

            validarFechaFutura(cita.getFechaHora());
        }

        // Paso 8: Validar relaciones.
        validarRelaciones(cita);

        // Paso 9: Verificar disponibilidad solo
        // cuando la cita continúa activa.
        if (!nuevoEstado.equals("cancelada")
                && !nuevoEstado.equals("atendida")) {

            validarDisponibilidad(cita, id);
        }

        // Paso 10: Actualizar los campos permitidos.
        citaExistente.setClienteId(
                cita.getClienteId()
        );

        citaExistente.setMascotaId(
                cita.getMascotaId()
        );

        citaExistente.setMedicoId(
                cita.getMedicoId()
        );

        citaExistente.setFechaHora(
                cita.getFechaHora()
        );

        citaExistente.setEstado(
                nuevoEstado
        );

        citaExistente.setMotivo(
                cita.getMotivo()
        );

        // Paso 11: Guardar los cambios.
        return citaRepository.save(citaExistente);
    }


    // ======================================================
    // 5. LISTAR TODAS LAS CITAS - NUEVO
    // ======================================================

    /**
     * Obtiene todas las citas médicas.
     *
     * Las devuelve ordenadas por fecha
     * y hora de forma descendente.
     *
     * Este método permitirá alimentar
     * el DataTable de Angular.
     *
     * @return Lista completa de citas.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitas() {

        // Consultar todas las citas ordenadas
        // desde la más reciente.
        return citaRepository
                .findAllByOrderByFechaHoraDesc();
    }


    // ======================================================
    // 6. CONSULTAR UNA CITA POR ID - NUEVO
    // ======================================================

    /**
     * Obtiene una cita mediante su identificador.
     *
     * Si la cita no existe, devuelve
     * el código HTTP 404 Not Found.
     *
     * @param id Identificador de la cita.
     * @return Cita encontrada.
     */
    @Override
    @Transactional(readOnly = true)
    public Cita consultarCitaPorId(Long id) {

        // Validar identificador.
        if (id == null || id <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El identificador de la cita no es válido."
            );
        }

        // Buscar la cita o devolver 404.
        return citaRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No existe una cita con el ID: " + id
                        )
                );
    }


    // ======================================================
    // 7. VALIDAR CAMPOS OBLIGATORIOS
    // ======================================================

    /**
     * Verifica los campos necesarios para
     * registrar o modificar una cita.
     *
     * @param cita Información recibida.
     */
    private void validarCampos(Cita cita) {

        // Comprobar que exista el objeto.
        if (cita == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes enviar los datos de la cita."
            );
        }

        // Validar cliente.
        if (cita.getClienteId() == null
                || cita.getClienteId() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecciona un cliente válido."
            );
        }

        // Validar mascota.
        if (cita.getMascotaId() == null
                || cita.getMascotaId() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecciona una mascota válida."
            );
        }

        // Validar médico.
        if (cita.getMedicoId() == null
                || cita.getMedicoId() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecciona un veterinario válido."
            );
        }

        // Validar fecha y hora.
        if (cita.getFechaHora() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes indicar la fecha y hora de la cita."
            );
        }

        // Validar estado.
        normalizarEstado(cita.getEstado());
    }


    // ======================================================
    // 8. VALIDAR FECHA FUTURA
    // ======================================================

    /**
     * Impide programar citas en el pasado.
     *
     * @param fechaHora Fecha solicitada.
     */
    private void validarFechaFutura(
            LocalDateTime fechaHora
    ) {

        if (fechaHora == null
                || fechaHora.isBefore(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No puedes programar una cita en el pasado."
            );
        }
    }


    // ======================================================
    // 9. VALIDAR ESTADOS DE LA CITA
    // ======================================================

    /**
     * Comprueba que el estado sea válido.
     *
     * Estados permitidos:
     *
     * - pendiente
     * - programada
     * - confirmada
     * - atendida
     * - cancelada
     *
     * @param estado Estado recibido.
     * @return Estado normalizado.
     */
    private String normalizarEstado(String estado) {

        if (estado == null || estado.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El estado de la cita es obligatorio."
            );
        }

        // Convertir a minúsculas.
        String valor = estado.trim()
                .toLowerCase(Locale.ROOT);

        // Verificar los valores permitidos.
        if (!valor.equals("pendiente")
                && !valor.equals("programada")
                && !valor.equals("confirmada")
                && !valor.equals("atendida")
                && !valor.equals("cancelada")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El estado de la cita no es válido."
            );
        }

        return valor;
    }


    // ======================================================
    // 10. VALIDAR RELACIONES
    // ======================================================

    /**
     * Verifica que:
     *
     * - El cliente exista.
     * - La mascota exista.
     * - La mascota pertenezca al cliente.
     * - El médico exista.
     *
     * @param cita Datos de la cita.
     */
    private void validarRelaciones(Cita cita) {

        // --------------------------------------------------
        // 10.1. COMPROBAR EXISTENCIA DEL CLIENTE
        // --------------------------------------------------

        Integer totalClientes = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM cliente
                WHERE cliente_id = ?
                """,
                Integer.class,
                cita.getClienteId()
        );

        if (totalClientes == null || totalClientes == 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El cliente seleccionado no existe."
            );
        }

        // --------------------------------------------------
        // 10.2. COMPROBAR EXISTENCIA DE LA MASCOTA
        // --------------------------------------------------

        Integer totalMascotas = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM mascota
                WHERE mascota_id = ?
                """,
                Integer.class,
                cita.getMascotaId()
        );

        if (totalMascotas == null || totalMascotas == 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La mascota seleccionada no existe."
            );
        }

        // --------------------------------------------------
        // 10.3. VERIFICAR QUE LA MASCOTA SEA DEL CLIENTE
        // --------------------------------------------------

        Integer relaciones = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM mascota
                WHERE mascota_id = ?
                  AND cliente_id = ?
                """,
                Integer.class,
                cita.getMascotaId(),
                cita.getClienteId()
        );

        if (relaciones == null || relaciones == 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La mascota no pertenece al cliente seleccionado."
            );
        }

        // --------------------------------------------------
        // 10.4. VALIDAR EXISTENCIA DEL MÉDICO
        // --------------------------------------------------

        validarMedico(cita.getMedicoId());
    }


    // ======================================================
    // 11. VALIDAR EXISTENCIA DEL VETERINARIO
    // ======================================================

    /**
     * Comprueba que el médico esté registrado.
     *
     * Identifica la columna de su llave primaria
     * a partir de la estructura real de MySQL.
     *
     * @param medicoId Identificador del médico.
     */
    private void validarMedico(Integer medicoId) {

        // Consultar el nombre de la llave primaria.
        String columnaId = jdbcTemplate.query(
                """
                SELECT COLUMN_NAME
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'medico'
                  AND COLUMN_KEY = 'PRI'
                ORDER BY ORDINAL_POSITION
                LIMIT 1
                """,
                rs -> rs.next() ? rs.getString(1) : null
        );

        // Verificar que la columna sea válida.
        if (columnaId == null
                || !columnaId.matches("[A-Za-z_][A-Za-z0-9_]*")) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo identificar la llave primaria de medico."
            );
        }

        // Consultar el médico en MySQL.
        Integer totalMedicos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM medico WHERE `"
                        + columnaId + "` = ?",
                Integer.class,
                medicoId
        );

        if (totalMedicos == null || totalMedicos == 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El veterinario seleccionado no existe."
            );
        }
    }


    // ======================================================
    // 12. VALIDAR DISPONIBILIDAD DE CITAS
    // ======================================================

    /**
     * Comprueba los conflictos de horarios.
     *
     * Reglas:
     *
     * 1. Un veterinario no podrá tener
     *    otra cita dentro de los 15 minutos
     *    anteriores o posteriores.
     *
     * 2. Una mascota no podrá tener
     *    dos citas simultáneas.
     *
     * 3. Las citas canceladas no bloquean
     *    horarios disponibles.
     *
     * @param cita Cita solicitada.
     * @param idExcluir Cita que se debe excluir
     *                  durante una actualización.
     */
    private void validarDisponibilidad(
            Cita cita,
            Long idExcluir
    ) {

        // Obtener fecha y hora solicitadas.
        LocalDateTime fecha = cita.getFechaHora();

        // Calcular margen del veterinario.
        LocalDateTime inicio = fecha.minusMinutes(15);

        LocalDateTime fin = fecha.plusMinutes(15);

        // --------------------------------------------------
        // 12.1. BUSCAR CONFLICTOS DEL VETERINARIO
        // --------------------------------------------------

        long conflictosMedico =
                citaRepository.contarConflictosMedico(
                        cita.getMedicoId(),
                        inicio,
                        fin,
                        idExcluir
                );

        // Rechazar si el veterinario está ocupado.
        if (conflictosMedico > 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El veterinario ya tiene una cita en ese horario."
            );
        }

        // --------------------------------------------------
        // 12.2. BUSCAR CONFLICTOS DE LA MASCOTA
        // --------------------------------------------------

        long conflictosMascota =
                citaRepository.contarConflictosMascota(
                        cita.getMascotaId(),
                        fecha,
                        idExcluir
                );

        // Rechazar si la mascota tiene otra cita
        // exactamente a la misma hora.
        if (conflictosMascota > 0) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La mascota ya tiene una cita en ese horario."
            );
        }
    }

}
