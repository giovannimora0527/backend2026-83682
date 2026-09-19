package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.dto.AnotacionHistoriaDTO;
import com.uniminuto.clinica.exception.RecursoNoEncontradoException;
import com.uniminuto.clinica.models.AnotacionHistoria;
import com.uniminuto.clinica.models.HistoriaMedica;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.service.AnotacionHistoriaService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementacion de {@link AnotacionHistoriaService}.
 *
 * <p>Resuelve la parte del Requerimiento 5 del taller relacionada con
 * anotacion_historia: crear, listar y actualizar (no se pide eliminar).</p>
 */
@Service
public class AnotacionHistoriaServiceImpl implements AnotacionHistoriaService {

    /** Repositorio para acceder a los datos de AnotacionHistoria en la base de datos. */
    private final AnotacionHistoriaRepository anotacionHistoriaRepository;

    /** Repositorio de HistoriaMedica, usado para validar que la historia asociada exista. */
    private final HistoriaMedicaRepository historiaMedicaRepository;

    /**
     * Constructor con inyeccion de dependencias.
     *
     * @param anotacionHistoriaRepository repositorio de anotaciones
     * @param historiaMedicaRepository    repositorio de historias medicas
     */
    public AnotacionHistoriaServiceImpl(AnotacionHistoriaRepository anotacionHistoriaRepository,
                                         HistoriaMedicaRepository historiaMedicaRepository) {
        this.anotacionHistoriaRepository = anotacionHistoriaRepository;
        this.historiaMedicaRepository = historiaMedicaRepository;
    }

    /** {@inheritDoc} */
    @Override
    public AnotacionHistoriaDTO crearAnotacion(AnotacionHistoriaDTO anotacionDTO) {
        // Verificamos que la historia medica indicada realmente exista.
        HistoriaMedica historia = historiaMedicaRepository.findById(anotacionDTO.getHistoriaMedicaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontro la historia medica con id " + anotacionDTO.getHistoriaMedicaId()));

        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setDescripcion(anotacionDTO.getDescripcion());
        anotacion.setHistoriaMedica(historia);

        // Si no se envia la fecha, se usa el momento actual.
        anotacion.setFecha(anotacionDTO.getFecha() != null ? anotacionDTO.getFecha() : LocalDateTime.now());

        AnotacionHistoria anotacionGuardada = anotacionHistoriaRepository.save(anotacion);
        return convertirADTO(anotacionGuardada);
    }

    /** {@inheritDoc} */
    @Override
    public List<AnotacionHistoriaDTO> listarAnotacionesPorHistoria(Long historiaMedicaId) {
        // Primero validamos que la historia medica exista, para dar un
        // mensaje de error claro si el id no es valido.
        if (!historiaMedicaRepository.existsById(historiaMedicaId)) {
            throw new RecursoNoEncontradoException("No se encontro la historia medica con id " + historiaMedicaId);
        }

        return anotacionHistoriaRepository.findByHistoriaMedicaIdOrderByFechaDesc(historiaMedicaId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public AnotacionHistoriaDTO actualizarAnotacion(Long id, AnotacionHistoriaDTO anotacionDTO) {
        AnotacionHistoria anotacionExistente = anotacionHistoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro la anotacion con id " + id));

        // Solo se actualizan los campos que llegaron en la peticion.
        if (anotacionDTO.getDescripcion() != null) {
            anotacionExistente.setDescripcion(anotacionDTO.getDescripcion());
        }
        if (anotacionDTO.getFecha() != null) {
            anotacionExistente.setFecha(anotacionDTO.getFecha());
        }

        AnotacionHistoria anotacionActualizada = anotacionHistoriaRepository.save(anotacionExistente);
        return convertirADTO(anotacionActualizada);
    }

    /**
     * Convierte una entidad {@link AnotacionHistoria} en su correspondiente
     * {@link AnotacionHistoriaDTO}.
     *
     * @param anotacion entidad a convertir
     * @return DTO con los datos de la anotacion
     */
    private AnotacionHistoriaDTO convertirADTO(AnotacionHistoria anotacion) {
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
