package com.uniminuto.clinica.service;
import com.uniminuto.clinica.models.Veterinario;
import java.util.List;
/** Contrato de negocio para veterinarios. */
public interface VeterinarioService {
    /** Lista veterinarios. */ List<Veterinario> listar();
    /** Consulta un veterinario por id. */ Veterinario obtener(Long id);
    /** Registra un veterinario. */ Veterinario crear(Veterinario veterinario);
    /** Actualiza un veterinario. */ Veterinario actualizar(Long id, Veterinario datos);
}
