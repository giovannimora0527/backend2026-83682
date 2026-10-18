package co.edu.uniminuto.veterinaria.service.impl;

import co.edu.uniminuto.veterinaria.dto.VeterinarioResponse;
import co.edu.uniminuto.veterinaria.repository.VeterinarioRepository;
import co.edu.uniminuto.veterinaria.service.VeterinarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link VeterinarioService}.
 */
@Service
public class VeterinarioServiceImpl implements VeterinarioService {

    /** Repositorio de veterinarios. */
    private final VeterinarioRepository veterinarioRepository;

    /**
     * Crea el servicio.
     *
     * @param veterinarioRepository repositorio de veterinarios
     */
    public VeterinarioServiceImpl(VeterinarioRepository veterinarioRepository) {
        this.veterinarioRepository = veterinarioRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<VeterinarioResponse> listar() {
        return veterinarioRepository.findByActivoTrueOrderByNombresAscApellidosAsc().stream()
                .map(v -> new VeterinarioResponse(v.getId(), v.getNombreCompleto(), v.getRegistroProfesional(),
                        v.getEspecializacion().getNombre()))
                .toList();
    }
}
