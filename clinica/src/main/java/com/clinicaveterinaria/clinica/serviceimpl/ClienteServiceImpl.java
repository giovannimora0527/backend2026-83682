package com.clinicaveterinaria.clinica.serviceimpl;

import com.clinicaveterinaria.clinica.entity.Cliente;
import com.clinicaveterinaria.clinica.models.ClienteDTO;
import com.clinicaveterinaria.clinica.repository.ClienteRepository;
import com.clinicaveterinaria.clinica.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    // --- CONVERSIÓN: DTO → Entity ---
    private Cliente toEntity(ClienteDTO dto) {
        Cliente entity = new Cliente();
        entity.setTipoDocumento(dto.getTipoDocumento());
        entity.setNumeroDocumento(dto.getNumeroDocumento());
        entity.setNombres(dto.getNombres());
        entity.setApellidos(dto.getApellidos());
        entity.setFechaNacimiento(dto.getFechaNacimiento());
        entity.setGenero(dto.getGenero());
        entity.setTelefono(dto.getTelefono());
        entity.setDireccion(dto.getDireccion());
        entity.setActivo(true); // Por defecto activo
        return entity;
    }

    // --- CONVERSIÓN: Entity → DTO ---
    private ClienteDTO toDTO(Cliente entity) {
        return new ClienteDTO(
                entity.getTipoDocumento(),
                entity.getNumeroDocumento(),
                entity.getNombres(),
                entity.getApellidos(),
                entity.getFechaNacimiento(),
                entity.getGenero(),
                entity.getTelefono(),
                entity.getDireccion()
        );
    }

    // --- CREAR (POST) ---
    @Override
    public ClienteDTO crear(ClienteDTO dto) {
        // Validaciones
        if (dto.getNumeroDocumento() == null || dto.getNumeroDocumento().isEmpty()) {
            throw new RuntimeException("El número de documento es obligatorio.");
        }
        if (dto.getNombres() == null || dto.getNombres().isEmpty()) {
            throw new RuntimeException("El nombre es obligatorio.");
        }
        if (dto.getApellidos() == null || dto.getApellidos().isEmpty()) {
            throw new RuntimeException("El apellido es obligatorio.");
        }

        // Verificar si ya existe el documento
        if (clienteRepository.findByNumeroDocumento(dto.getNumeroDocumento()).isPresent()) {
            throw new RuntimeException("Ya existe un cliente con el documento: " + dto.getNumeroDocumento());
        }

        // Convertir y guardar
        Cliente entity = toEntity(dto);
        Cliente guardado = clienteRepository.save(entity);
        return toDTO(guardado);
    }

    // --- LISTAR TODOS (GET) ---
    @Override
    public List<ClienteDTO> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // --- BUSCAR POR ID (GET) ---
    @Override
    public ClienteDTO buscarPorId(Long id) {
        Cliente entity = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        return toDTO(entity);
    }

    // --- BUSCAR POR DOCUMENTO (GET) ---
    @Override
    public ClienteDTO buscarPorDocumento(String numeroDocumento) {
        Cliente entity = clienteRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con documento: " + numeroDocumento));
        return toDTO(entity);
    }



    // --- ACTUALIZAR (PUT) ---
    @Override
    public ClienteDTO actualizarporid(Long id, ClienteDTO dto) {
        Cliente entity = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        entity.setTipoDocumento(dto.getTipoDocumento());
        entity.setNumeroDocumento(dto.getNumeroDocumento());
        entity.setNombres(dto.getNombres());
        entity.setApellidos(dto.getApellidos());
        entity.setFechaNacimiento(dto.getFechaNacimiento());
        entity.setGenero(dto.getGenero());
        entity.setTelefono(dto.getTelefono());
        entity.setDireccion(dto.getDireccion());

        Cliente actualizado = clienteRepository.save(entity);
        return toDTO(actualizado);
    }

    // --- ACTUALIZAR (PUT) ---
    @Override
    public ClienteDTO actualizarPorDocumento(String numeroDocumento, ClienteDTO dto) {
        Cliente entity = clienteRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con Numero de documento: " + numeroDocumento));

        entity.setTipoDocumento(dto.getTipoDocumento());
        entity.setNumeroDocumento(dto.getNumeroDocumento());
        entity.setNombres(dto.getNombres());
        entity.setApellidos(dto.getApellidos());
        entity.setFechaNacimiento(dto.getFechaNacimiento());
        entity.setGenero(dto.getGenero());
        entity.setTelefono(dto.getTelefono());
        entity.setDireccion(dto.getDireccion());

        Cliente actualizado = clienteRepository.save(entity);
        return toDTO(actualizado);
    }




    // --- ELIMINAR (DELETE)  ---
    @Override
    public void eliminar(Long id) {
        Cliente entity = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        entity.setActivo(false); // No se borra, solo se marca como inactivo
        clienteRepository.save(entity);
    }

    @Override
    public ClienteDTO actualizarPorDocumento(String numeroDocumento) {
        return null;
    }
}