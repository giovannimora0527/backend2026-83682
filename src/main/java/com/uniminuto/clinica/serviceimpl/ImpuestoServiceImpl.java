package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Impuesto;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.ImpuestoRq;
import com.uniminuto.clinica.models.ImpuestoRs;
import com.uniminuto.clinica.repository.ImpuestoRepository;
import com.uniminuto.clinica.service.ImpuestoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ImpuestoServiceImpl implements ImpuestoService {

    private final ImpuestoRepository impuestoRepository;

    public ImpuestoServiceImpl(ImpuestoRepository impuestoRepository) {
        this.impuestoRepository = impuestoRepository;
    }

    @Override
    public List<ImpuestoRs> listar() {
        return impuestoRepository.findAllByOrderByPorcentaje().stream().map(ImpuestoRs::de).toList();
    }

    @Override
    @Transactional
    public ImpuestoRs crear(ImpuestoRq rq) {
        return guardar(new Impuesto(), rq);
    }

    @Override
    @Transactional
    public ImpuestoRs actualizar(Long id, ImpuestoRq rq) {
        Impuesto impuesto = impuestoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El impuesto no existe."));
        if (impuesto.getPorcentaje().compareTo(rq.porcentaje()) != 0
                && (impuestoRepository.usadoEnTarifas(id) || impuestoRepository.usadoEnProductos(id))) {
            throw new ConflictException("porcentaje", "El impuesto está en uso: cree otro y cámbielo en cada tarifa "
                    + "para que quede en el historial.");
        }
        if (Boolean.FALSE.equals(rq.activo()) && impuestoRepository.usadoEnTarifasVigentes(id)) {
            throw new ConflictException("activo", "El impuesto está en uso en tarifas vigentes.");
        }
        return guardar(impuesto, rq);
    }

    private ImpuestoRs guardar(Impuesto impuesto, ImpuestoRq rq) {
        String nombre = rq.nombre().trim();
        impuestoRepository.findByNombreIgnoreCase(nombre).filter(i -> !i.getId().equals(impuesto.getId()))
                .ifPresent(i -> {
                    throw new ConflictException("nombre", "Ya existe un impuesto con ese nombre.");
                });
        impuesto.setNombre(nombre);
        impuesto.setPorcentaje(rq.porcentaje());
        if (rq.activo() != null) {
            impuesto.setActivo(rq.activo());
        }
        return ImpuestoRs.de(impuestoRepository.saveAndFlush(impuesto));
    }
}
