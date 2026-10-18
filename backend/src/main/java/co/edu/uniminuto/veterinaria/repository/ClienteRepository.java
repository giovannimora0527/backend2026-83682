package co.edu.uniminuto.veterinaria.repository;

import co.edu.uniminuto.veterinaria.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Acceso a datos de clientes.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {
}
