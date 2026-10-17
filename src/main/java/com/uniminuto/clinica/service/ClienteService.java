package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.Cliente;
import com.uniminuto.clinica.models.Paciente;
import java.util.List;

/** Contrato de negocio para consultar y administrar clientes y sus mascotas. */
public interface ClienteService {
    /** Lista clientes registrados. */ List<Cliente> listar();
    /** Busca un cliente existente. */ Cliente obtener(Long id);
    /** Registra un cliente. */ Cliente crear(Cliente cliente);
    /** Actualiza datos básicos de un cliente. */ Cliente actualizar(Long id, Cliente datos);
    /** Lista las mascotas de un cliente existente. */ List<Paciente> listarMascotas(Long id);
}
