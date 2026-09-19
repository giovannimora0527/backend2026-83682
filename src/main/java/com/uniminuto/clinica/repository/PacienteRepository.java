package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.models.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de acceso a datos para la entidad {@link Paciente}.
 *
 * <p>Al extender de {@link JpaRepository}, Spring Data JPA genera
 * automaticamente (sin que tengamos que escribir SQL) metodos como
 * {@code save}, {@code findById}, {@code findAll} y {@code deleteById}.</p>
 *
 * <p>Los dos parametros genericos son: el tipo de la entidad
 * ({@code Paciente}) y el tipo de su llave primaria ({@code Long}).</p>
 */
@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}
