package com.uniminuto.clinica.serviceimpl;
import com.uniminuto.clinica.exception.RecursoNoEncontradoException;
import com.uniminuto.clinica.models.Veterinario;
import com.uniminuto.clinica.repository.VeterinarioRepository;
import com.uniminuto.clinica.service.VeterinarioService;
import org.springframework.stereotype.Service;
import java.util.List;
/** Implementación de operaciones de veterinarios. */
@Service
public class VeterinarioServiceImpl implements VeterinarioService {
    private final VeterinarioRepository repository;
    public VeterinarioServiceImpl(VeterinarioRepository repository) { this.repository = repository; }
    /** {@inheritDoc} */ public List<Veterinario> listar() { return repository.findAll(); }
    /** {@inheritDoc} */ public Veterinario obtener(Long id) { return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("No existe el veterinario solicitado.")); }
    /** {@inheritDoc} */ public Veterinario crear(Veterinario veterinario) { return repository.save(veterinario); }
    /** {@inheritDoc} */ public Veterinario actualizar(Long id, Veterinario datos) { Veterinario v = obtener(id); v.setNombre(datos.getNombre()); v.setTarjetaProfesional(datos.getTarjetaProfesional()); v.setCorreo(datos.getCorreo()); v.setTelefono(datos.getTelefono()); v.setActivo(datos.isActivo()); return repository.save(v); }
}
