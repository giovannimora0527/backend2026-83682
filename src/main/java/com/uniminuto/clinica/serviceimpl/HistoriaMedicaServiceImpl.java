package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.dto.AnotacionHistoriaDTO;
import com.uniminuto.clinica.dto.HistoriaMedicaDTO;
import com.uniminuto.clinica.exception.FechaInvalidaException;
import com.uniminuto.clinica.exception.RecursoNoEncontradoException;
import com.uniminuto.clinica.models.AnotacionHistoria;
import com.uniminuto.clinica.models.HistoriaMedica;
import com.uniminuto.clinica.models.Paciente;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.service.HistoriaMedicaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementacion de {@link HistoriaMedicaService}.
 *
 * <p>Resuelve el Requerimiento 5 del taller: CRUD completo de historia
 * medica, incluyendo el listado filtrado por rango de fechas.</p>
 *
 * <p>La anotacion {@code @Transactional} a nivel de clase mantiene abierta
 * la conexion con la base de datos mientras dura cada metodo. Esto es
 * necesario porque la lista de anotaciones de {@link HistoriaMedica} se
 * carga de forma "perezosa" (LAZY): si no mantuvieramos la transaccion
 * abierta, intentar leer esa lista lanzaria un error.</p>
 */
@Service
@Transactional
public class HistoriaMedicaServiceImpl implements HistoriaMedicaService {

    /** Repositorio para acceder a los datos de HistoriaMedica en la base de datos. */
    private final HistoriaMedicaRepository historiaMedicaRepository;

    /** Servicio de pacientes, usado para validar y obtener el paciente dueño de la historia. */
    private final PacienteServiceImpl pacienteServiceImpl;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param historiaMedicaRepository repositorio de historias medicas
     * @param pacienteServiceImpl      servicio para obtener los pacientes asociados
     */
    public HistoriaMedicaServiceImpl(HistoriaMedicaRepository historiaMedicaRepository,
                                      PacienteServiceImpl pacienteServiceImpl) {
        this.historiaMedicaRepository = historiaMedicaRepository;
        this.pacienteServiceImpl = pacienteServiceImpl;
    }

    /** {@inheritDoc} */
    @Override
    public HistoriaMedicaDTO crearHistoriaMedica(HistoriaMedicaDTO historiaMedicaDTO) {
        // Verificamos que el paciente indicado realmente exista.
        Paciente paciente = pacienteServiceImpl.buscarPacientePorId(historiaMedicaDTO.getPacienteId());

        HistoriaMedica historia = new HistoriaMedica();
        historia.setDiagnostico(historiaMedicaDTO.getDiagnostico());
        historia.setObservaciones(historiaMedicaDTO.getObservaciones());
        historia.setPaciente(paciente);

        // Si no se envia la fecha de creacion, se usa el momento actual.
        historia.setFechaCreacion(
                historiaMedicaDTO.getFechaCreacion() != null ? historiaMedicaDTO.getFechaCreacion() : LocalDateTime.now()
        );

        HistoriaMedica historiaGuardada = historiaMedicaRepository.save(historia);
        return convertirADTO(historiaGuardada);
    }

    /** {@inheritDoc} */
    @Override
    public List<HistoriaMedicaDTO> listarHistoriasMedicas() {
        return historiaMedicaRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public List<HistoriaMedicaDTO> listarHistoriasMedicasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        validarRangoDeFechas(fechaInicio, fechaFin);

        return historiaMedicaRepository.findByFechaCreacionBetweenOrderByFechaCreacionDesc(fechaInicio, fechaFin)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public HistoriaMedicaDTO obtenerHistoriaMedicaPorId(Long id) {
        return convertirADTO(buscarHistoriaPorId(id));
    }

    /** {@inheritDoc} */
    @Override
    public HistoriaMedicaDTO actualizarHistoriaMedica(Long id, HistoriaMedicaDTO historiaMedicaDTO) {
        HistoriaMedica historiaExistente = buscarHistoriaPorId(id);

        // Solo se actualizan los campos que llegaron en la peticion.
        if (historiaMedicaDTO.getDiagnostico() != null) {
            historiaExistente.setDiagnostico(historiaMedicaDTO.getDiagnostico());
        }
        if (historiaMedicaDTO.getObservaciones() != null) {
            historiaExistente.setObservaciones(historiaMedicaDTO.getObservaciones());
        }
        if (historiaMedicaDTO.getPacienteId() != null) {
            Paciente paciente = pacienteServiceImpl.buscarPacientePorId(historiaMedicaDTO.getPacienteId());
            historiaExistente.setPaciente(paciente);
        }

        HistoriaMedica historiaActualizada = historiaMedicaRepository.save(historiaExistente);
        return convertirADTO(historiaActualizada);
    }

    /** {@inheritDoc} */
    @Override
    public void eliminarHistoriaMedica(Long id) {
        // Nos aseguramos de que exista antes de intentar borrarla, para
        // poder devolver un error 404 claro en vez de dejar que falle
        // silenciosamente o lance un error generico.
        HistoriaMedica historia = buscarHistoriaPorId(id);
        historiaMedicaRepository.delete(historia);
    }

    /**
     * Busca una historia medica por id como entidad. Es un metodo de apoyo
     * interno reutilizado por varios metodos de esta clase.
     *
     * @param id identificador de la historia medica
     * @return la entidad HistoriaMedica encontrada
     * @throws RecursoNoEncontradoException si no existe una historia medica con ese id
     */
    private HistoriaMedica buscarHistoriaPorId(Long id) {
        return historiaMedicaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro la historia medica con id " + id));
    }

    /**
     * Valida que el rango de fechas recibido sea correcto.
     *
     * @param fechaInicio fecha inicial del rango
     * @param fechaFin    fecha final del rango
     * @throws FechaInvalidaException si el rango de fechas no es valido
     */
    private void validarRangoDeFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new FechaInvalidaException("Debe indicar la fecha inicial y la fecha final para filtrar las historias medicas");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new FechaInvalidaException("La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    /**
     * Convierte una entidad {@link HistoriaMedica} en su correspondiente
     * {@link HistoriaMedicaDTO}, incluyendo un resumen de sus anotaciones.
     *
     * @param historia entidad a convertir
     * @return DTO con los datos de la historia medica
     */
    private HistoriaMedicaDTO convertirADTO(HistoriaMedica historia) {
        HistoriaMedicaDTO dto = new HistoriaMedicaDTO();
        dto.setId(historia.getId());
        dto.setFechaCreacion(historia.getFechaCreacion());
        dto.setDiagnostico(historia.getDiagnostico());
        dto.setObservaciones(historia.getObservaciones());
        if (historia.getPaciente() != null) {
            dto.setPacienteId(historia.getPaciente().getId());
            dto.setNombrePaciente(historia.getPaciente().getNombre());
        }

        if (historia.getAnotaciones() != null) {
            List<AnotacionHistoriaDTO> anotacionesDTO = historia.getAnotaciones().stream()
                    .map(this::convertirAnotacionADTO)
                    .collect(Collectors.toList());
            dto.setAnotaciones(anotacionesDTO);
        }

        return dto;
    }

    /**
     * Convierte una entidad {@link AnotacionHistoria} en su correspondiente
     * {@link AnotacionHistoriaDTO}. Se usa solo para mostrar el resumen de
     * anotaciones dentro de una historia medica.
     *
     * @param anotacion entidad a convertir
     * @return DTO con los datos de la anotacion
     */
    private AnotacionHistoriaDTO convertirAnotacionADTO(AnotacionHistoria anotacion) {
        AnotacionHistoriaDTO dto = new AnotacionHistoriaDTO();
        dto.setId(anotacion.getId());
        dto.setFecha(anotacion.getFecha());
        dto.setDescripcion(anotacion.getDescripcion());
        if (anotacion.getHistoriaMedica() != null) {
            dto.setHistoriaMedicaId(anotacion.getHistoriaMedica().getId());
        }
        return dto;
    }
}
