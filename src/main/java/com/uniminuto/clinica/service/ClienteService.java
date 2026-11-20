package com.uniminuto.clinica.service;

import com.uniminuto.clinica.dto.ClienteSolicitud;
import com.uniminuto.clinica.entity.Cliente;

import java.util.List;

public interface ClienteService {

    List<Cliente> obtenerClientes();
    Cliente consultar(long id);
    Cliente crear(ClienteSolicitud solicitud);
    Cliente editar(long id, ClienteSolicitud solicitud);
    void archivar(long id);
}
