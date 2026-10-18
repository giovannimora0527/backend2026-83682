package co.edu.uniminuto.veterinaria.service.impl;

import co.edu.uniminuto.veterinaria.dto.MedicamentoResponse;
import co.edu.uniminuto.veterinaria.repository.MedicamentoRepository;
import co.edu.uniminuto.veterinaria.service.MedicamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementación de {@link MedicamentoService}.
 */
@Service
public class MedicamentoServiceImpl implements MedicamentoService {

    /** Repositorio de medicamentos. */
    private final MedicamentoRepository medicamentoRepository;

    /**
     * Crea el servicio.
     *
     * @param medicamentoRepository repositorio de medicamentos
     */
    public MedicamentoServiceImpl(MedicamentoRepository medicamentoRepository) {
        this.medicamentoRepository = medicamentoRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<MedicamentoResponse> listarVigentes() {
        return medicamentoRepository.findByFechaVenceGreaterThanEqualOrderByNombreAsc(LocalDate.now()).stream()
                .map(m -> new MedicamentoResponse(m.getId(), m.getNombre(), m.getPresentacion(), m.getFechaVence()))
                .toList();
    }
}
