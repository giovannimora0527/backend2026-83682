package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Especializacion;
import com.uniminuto.clinica.entity.Veterinario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.VeterinarioRequest;
import com.uniminuto.clinica.repository.EspecializacionRepository;
import com.uniminuto.clinica.repository.VeterinarioRepository;
import com.uniminuto.clinica.service.VeterinarioService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.regex.Pattern;

/** Implementa la consulta, creación y actualización de médicos (tabla medico). */
@Service
@Transactional(readOnly = true)
public class VeterinarioServiceImpl implements VeterinarioService {
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\- ]{7,20}$");
    private final VeterinarioRepository repository;
    private final EspecializacionRepository especializaciones;
    /** Construye el servicio con los repositorios de médicos y especializaciones. */
    public VeterinarioServiceImpl(VeterinarioRepository repository, EspecializacionRepository especializaciones) {
        this.repository = repository; this.especializaciones = especializaciones;
    }

    /** {@inheritDoc} */
    @Transactional
    public Veterinario crear(VeterinarioRequest request) {
        Datos d = validar(request);
        if (repository.existsByNumeroDocumento(d.numeroDocumento()))
            throw new BadRequestException(HttpStatus.CONFLICT, "Ya existe un médico con el número de documento " + d.numeroDocumento());
        if (repository.existsByRegistroProfesional(d.registroProfesional()))
            throw new BadRequestException(HttpStatus.CONFLICT, "Ya existe un médico con el registro profesional " + d.registroProfesional());
        Veterinario medico = new Veterinario();
        aplicar(medico, d);
        return repository.save(medico);
    }

    /** {@inheritDoc} */
    @Transactional
    public Veterinario actualizar(Long id, VeterinarioRequest request) {
        Veterinario medico = consultar(id);
        Datos d = validar(request);
        if (repository.existsByNumeroDocumentoAndVeterinarioIdNot(d.numeroDocumento(), id))
            throw new BadRequestException(HttpStatus.CONFLICT, "Ya existe otro médico con el número de documento " + d.numeroDocumento());
        if (repository.existsByRegistroProfesionalAndVeterinarioIdNot(d.registroProfesional(), id))
            throw new BadRequestException(HttpStatus.CONFLICT, "Ya existe otro médico con el registro profesional " + d.registroProfesional());
        aplicar(medico, d);
        return repository.save(medico);
    }

    private void aplicar(Veterinario medico, Datos d) {
        medico.setTipoDocumento(d.tipoDocumento());
        medico.setNumeroDocumento(d.numeroDocumento());
        medico.setNombres(d.nombres());
        medico.setApellidos(d.apellidos());
        medico.setTelefono(d.telefono());
        medico.setRegistroProfesional(d.registroProfesional());
        medico.setEspecializacion(d.especializacion());
    }

    /** Valida contra las longitudes y obligatoriedad reales de la tabla medico. */
    private Datos validar(VeterinarioRequest r) {
        if (r == null) throw new BadRequestException("Los datos del médico son obligatorios");
        String tipo = texto(r.tipoDocumento(), "tipoDocumento", 10).toUpperCase(java.util.Locale.ROOT);
        String numero = texto(r.numeroDocumento(), "numeroDocumento", 20);
        if (!numero.matches("^[A-Za-z0-9-]+$"))
            throw new BadRequestException("numeroDocumento solo admite letras, números y guiones");
        String nombres = texto(r.nombres(), "nombres", 100);
        String apellidos = texto(r.apellidos(), "apellidos", 100);
        String registro = texto(r.registroProfesional(), "registroProfesional", 50);
        String telefono = r.telefono() == null || r.telefono().isBlank() ? null : r.telefono().trim();
        if (telefono != null && !TELEFONO.matcher(telefono).matches())
            throw new BadRequestException("telefono debe tener entre 7 y 20 caracteres (números, espacios, +, -, paréntesis)");
        if (r.especializacionId() == null)
            throw new BadRequestException("especializacionId es obligatorio");
        Especializacion especializacion = especializaciones.findById(r.especializacionId())
                .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND, "Especialización no encontrada"));
        return new Datos(tipo, numero, nombres, apellidos, telefono, registro, especializacion);
    }

    private String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) throw new BadRequestException(campo + " es obligatorio");
        String limpio = valor.trim();
        if (limpio.length() > maximo) throw new BadRequestException(campo + " no puede superar " + maximo + " caracteres");
        return limpio;
    }

    private record Datos(String tipoDocumento, String numeroDocumento, String nombres, String apellidos,
                         String telefono, String registroProfesional, Especializacion especializacion) { }
    /** {@inheritDoc} */
    public List<Veterinario> listar() { return repository.findAll(); }
    /** {@inheritDoc} */
    public Veterinario consultar(Long id) { return repository.findById(id).orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND, "Veterinario no encontrado")); }

    public Veterinario buscarPorDocumento(String tipoDocumento, String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.isBlank())
            throw new BadRequestException("El número de documento es obligatorio");
        if (tipoDocumento == null || tipoDocumento.isBlank())
            throw new BadRequestException("El tipo de documento es obligatorio");
        return repository.findFirstByTipoDocumentoAndNumeroDocumento(tipoDocumento.trim(), numeroDocumento.trim())
                .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND, "Medico no encontrado"));
    }

    public Veterinario buscarPorRegistroProfesional(String registroProfesional) {
        if (registroProfesional == null || registroProfesional.isBlank())
            throw new BadRequestException("El registro profesional es obligatorio");
        return repository.findFirstByRegistroProfesional(registroProfesional.trim())
                .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND, "Medico no encontrado"));
    }
}
