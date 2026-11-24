package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.entity.Servicio;
import com.uniminuto.clinica.entity.Tarifa;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.ServicioRq;
import com.uniminuto.clinica.models.ServicioRs;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.ServicioRepository;
import com.uniminuto.clinica.repository.TarifaRepository;
import com.uniminuto.clinica.service.ServicioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository servicioRepository;
    private final TarifaRepository tarifaRepository;
    private final EspecializacionRepository especializacionRepository;

    public ServicioServiceImpl(ServicioRepository servicioRepository, TarifaRepository tarifaRepository,
                               EspecializacionRepository especializacionRepository) {
        this.servicioRepository = servicioRepository;
        this.tarifaRepository = tarifaRepository;
        this.especializacionRepository = especializacionRepository;
    }

    /** soloActivos excluye tambien los servicios de especialidades inactivas. */
    @Override
    @Transactional(readOnly = true)
    public List<ServicioRs> listar(boolean soloActivos, Long especializacionId) {
        Map<Long, Tarifa> tarifas = tarifaRepository.findByActivaTrue().stream()
                .collect(Collectors.toMap(t -> t.getServicio().getId(), Function.identity()));
        return servicioRepository.findAllByOrderByNombre().stream()
                .filter(s -> !soloActivos || Boolean.TRUE.equals(s.getActivo())
                        && (s.getEspecializacion() == null
                        || Boolean.TRUE.equals(s.getEspecializacion().getActivo())))
                .filter(s -> especializacionId == null || s.getEspecializacion() != null
                        && especializacionId.equals(s.getEspecializacion().getId()))
                .map(s -> ServicioRs.de(s, tarifas.get(s.getId()))).toList();
    }

    @Override
    @Transactional
    public ServicioRs crear(ServicioRq rq) {
        return guardar(new Servicio(), rq);
    }

    @Override
    @Transactional
    public ServicioRs actualizar(Long id, ServicioRq rq) {
        return guardar(buscar(id), rq);
    }

    @Override
    @Transactional
    public ServicioRs cambiarEstado(Long id, boolean activo) {
        Servicio servicio = buscar(id);
        servicio.setActivo(activo);
        return convertir(servicioRepository.saveAndFlush(servicio));
    }

    private ServicioRs guardar(Servicio servicio, ServicioRq rq) {
        String codigo = rq.codigo().trim().toUpperCase(Locale.ROOT);
        servicioRepository.findByCodigo(codigo).filter(s -> !s.getId().equals(servicio.getId()))
                .ifPresent(s -> {
                    throw new ConflictException("codigo", "Ya existe un servicio con ese código.");
                });
        Especializacion especializacion = null;
        if (rq.especializacionId() != null) {
            especializacion = especializacionRepository.findById(rq.especializacionId())
                    .orElseThrow(() -> new NotFoundException("La especialización no existe."));
            if (!Boolean.TRUE.equals(especializacion.getActivo())) {
                throw new ConflictException("especializacionId", "La especialización está inactiva.");
            }
        }
        servicio.setCodigo(codigo);
        servicio.setNombre(rq.nombre().trim());
        servicio.setDescripcion(rq.descripcion());
        servicio.setTipoServicio(rq.tipoServicio());
        servicio.setEspecializacion(especializacion);
        servicio.setDuracionMinutos(rq.duracionMinutos() == null ? 30 : rq.duracionMinutos());
        return convertir(servicioRepository.saveAndFlush(servicio));
    }

    private Servicio buscar(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("El servicio no existe."));
    }

    private ServicioRs convertir(Servicio servicio) {
        return ServicioRs.de(servicio, tarifaRepository.findByServicio_IdAndActivaTrue(servicio.getId()).orElse(null));
    }
}
