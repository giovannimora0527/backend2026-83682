package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Veterinario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.repository.VeterinarioRepository;
import com.uniminuto.clinica.service.VeterinarioService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** Implementa la consulta de veterinarios mediante su repositorio. */
/** Implementa la consulta de veterinarios mediante su repositorio. */
@Service
@Transactional(readOnly = true)
public class VeterinarioServiceImpl implements VeterinarioService {
    private final VeterinarioRepository repository;
    /** Construye el servicio con el repositorio de veterinarios. */
    public VeterinarioServiceImpl(VeterinarioRepository repository) { this.repository = repository; }
    /** {@inheritDoc} */
    public List<Veterinario> listar() { return repository.findAll(); }
    /** {@inheritDoc} */
    public Veterinario consultar(Long id) { return repository.findById(id).orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND, "Veterinario no encontrado")); }
}
