package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.ConfiguracionClinica;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.ConfiguracionRq;
import com.uniminuto.clinica.models.ConfiguracionRs;
import com.uniminuto.clinica.repository.ConfiguracionClinicaRepository;
import com.uniminuto.clinica.service.ConfiguracionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private final ConfiguracionClinicaRepository repository;

    public ConfiguracionServiceImpl(ConfiguracionClinicaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ConfiguracionRs obtener() {
        return ConfiguracionRs.de(repository.findFirstByOrderById().orElseThrow(this::sinConfiguracion));
    }

    @Override
    @Transactional
    public ConfiguracionRs actualizar(ConfiguracionRq rq) {
        ConfiguracionClinica c = bloquear();
        c.setNombre(rq.nombre().trim());
        c.setNit(rq.nit().trim());
        c.setDireccion(rq.direccion());
        c.setTelefono(rq.telefono());
        c.setCorreo(rq.correo());
        c.setPrefijoFactura(rq.prefijoFactura().toUpperCase(Locale.ROOT));
        if (rq.prefijoFormula() != null) {
            c.setPrefijoFormula(rq.prefijoFormula().toUpperCase(Locale.ROOT));
        }
        if (rq.prefijoDespacho() != null) {
            c.setPrefijoDespacho(rq.prefijoDespacho().toUpperCase(Locale.ROOT));
        }
        if (rq.topeDescuentoSecretaria() != null) {
            c.setTopeDescuentoSecretaria(rq.topeDescuentoSecretaria());
        }
        return ConfiguracionRs.de(repository.saveAndFlush(c));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public String siguienteFormula() {
        ConfiguracionClinica c = bloquear();
        c.setConsecutivoFormula(c.getConsecutivoFormula() + 1);
        repository.saveAndFlush(c);
        return numero(c.getPrefijoFormula(), c.getConsecutivoFormula());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public String siguienteDespacho() {
        ConfiguracionClinica c = bloquear();
        c.setConsecutivoDespacho(c.getConsecutivoDespacho() + 1);
        repository.saveAndFlush(c);
        return numero(c.getPrefijoDespacho(), c.getConsecutivoDespacho());
    }

    private ConfiguracionClinica bloquear() {
        return repository.bloquear().stream().findFirst().orElseThrow(this::sinConfiguracion);
    }

    private static String numero(String prefijo, long consecutivo) {
        return String.format("%s-%06d", prefijo, consecutivo);
    }

    private NotFoundException sinConfiguracion() {
        return new NotFoundException("No se ha registrado la configuración de la clínica.");
    }
}
