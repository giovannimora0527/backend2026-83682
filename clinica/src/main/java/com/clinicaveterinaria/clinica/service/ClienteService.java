package com.clinicaveterinaria.clinica.service;

import com.clinicaveterinaria.clinica.models.ClienteDTO;

import java.util.List;

public interface ClienteService {

    // Crear un cliente
    ClienteDTO crear(ClienteDTO dto);

    // Listar todos los clientes
    List<ClienteDTO> listarTodos();

    // Buscar un cliente por ID
    ClienteDTO buscarPorId(Long id);

    // Buscar un cliente por número de documento
    ClienteDTO buscarPorDocumento(String numeroDocumento);

    // Actualizar un cliente
    //ClienteDTO actualizar(Long id, ClienteDTO dto);

    // --- ACTUALIZAR (PUT) ---
    ClienteDTO actualizarporid(Long id, ClienteDTO dto);

    // --- ACTUALIZAR (PUT) ---
    ClienteDTO actualizarPorDocumento(String numeroDocumento, ClienteDTO dto);

    // Eliminar un cliente (borrado lógico)
    void eliminar(Long id);

    ClienteDTO actualizarPorDocumento(String numeroDocumento);
}