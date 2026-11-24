package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.FacturaRs;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.FacturaRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.service.FacturaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;

    public FacturaServiceImpl(FacturaRepository facturaRepository, ClienteRepository clienteRepository,
                              MascotaRepository mascotaRepository) {
        this.facturaRepository = facturaRepository;
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
    }

    @Override
    public List<FacturaRs> listarPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new NotFoundException("El cliente no existe.");
        }
        return facturaRepository.findByCliente_IdOrderByFechaGeneracionDesc(clienteId).stream()
                .map(FacturaRs::de).toList();
    }

    @Override
    public List<FacturaRs> listarPorMascota(Integer mascotaId) {
        if (!mascotaRepository.existsById(mascotaId)) {
            throw new NotFoundException("La mascota no existe.");
        }
        return facturaRepository.findByMascota_MascotaIdOrderByFechaGeneracionDesc(mascotaId).stream()
                .map(FacturaRs::de).toList();
    }
}
