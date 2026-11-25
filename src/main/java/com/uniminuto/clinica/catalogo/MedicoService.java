package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.auditoria.AuditoriaService;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.seguridad.Perfiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Reglas de negocio de médicos veterinarios: documento (CC de 6 a 10 dígitos) y registro profesional únicos,
 * especialidad existente y activa, usuario asociado con rol Veterinario y sin otro médico, y no se inactiva un
 * médico con citas futuras reservadas.
 */
@Service
public class MedicoService {

    /** Tipos de documento permitidos para un profesional. */
    static final Set<String> TIPOS = Set.of("CC", "CE", "PA", "PPT");
    /** Cédula de ciudadanía: solo dígitos, entre 6 y 10. */
    static final Pattern CEDULA = Pattern.compile("^\\d{6,10}$");
    /** Otros documentos: letras y dígitos, entre 5 y 15. */
    static final Pattern DOCUMENTO = Pattern.compile("^[A-Za-z0-9]{5,15}$");
    /** Nombres y apellidos: letras (con tildes), espacios, apóstrofos y guiones. */
    static final Pattern NOMBRE = Pattern.compile("^[\\p{L}][\\p{L} '\\-]{1,99}$");
    /** Teléfono: entre 7 y 10 dígitos. */
    static final Pattern TELEFONO = Pattern.compile("^\\d{7,10}$");
    /** Registro profesional: letras, dígitos y guiones, entre 3 y 50. */
    static final Pattern REGISTRO = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9\\-]{2,49}$");

    /**
     * Datos de un médico validados y normalizados.
     *
     * @param tipo tipo de documento.
     * @param documento número de documento.
     * @param nombres nombres.
     * @param apellidos apellidos.
     * @param telefono teléfono o null.
     * @param registro registro profesional.
     * @param especializacionId especialidad.
     * @param usuarioId usuario asociado o null.
     * @param activo estado.
     */
    record Datos(String tipo, String documento, String nombres, String apellidos, String telefono, String registro,
                 long especializacionId, Long usuarioId, boolean activo) {
    }

    /** Acceso a datos de médicos. */
    private final MedicoRepository repositorio;
    /** Log de operaciones sensibles. */
    private final AuditoriaService auditoria;

    /**
     * @param repositorio repositorio de médicos.
     * @param auditoria servicio de auditoría.
     */
    public MedicoService(MedicoRepository repositorio, AuditoriaService auditoria) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
    }

    /**
     * @param soloActivos true para los asignables a citas.
     * @return médicos.
     */
    public List<MedicoDto> listar(boolean soloActivos) {
        return repositorio.listar(soloActivos);
    }

    /**
     * @param d datos.
     * @return médico creado.
     */
    @Transactional
    public MedicoDto crear(MedicoApi.MedicoRq d) {
        return consultar(repositorio.insertar(validar(d, null)));
    }

    /**
     * @param id médico.
     * @param d nuevos datos.
     * @return médico actualizado.
     */
    @Transactional
    public MedicoDto actualizar(long id, MedicoApi.MedicoRq d) {
        if (repositorio.bloquearYEspecialidadActiva(id) == null) {
            throw new NotFoundException("El médico no existe.");
        }
        Datos datos = validar(d, id);
        if (!datos.activo()) {
            verificarSinCitasFuturas(id);
        }
        repositorio.actualizar(id, datos);
        return consultar(id);
    }

    /**
     * Activa o inactiva un médico.
     *
     * @param id médico.
     * @param datos nuevo estado.
     * @return médico actualizado.
     */
    @Transactional
    public MedicoDto cambiarEstado(long id, MedicoApi.EstadoRq datos) {
        Validar.requerido(datos == null ? null : datos.activo(), "activo");
        Boolean especialidadActiva = repositorio.bloquearYEspecialidadActiva(id);
        if (especialidadActiva == null) {
            throw new NotFoundException("El médico no existe.");
        }
        if (datos.activo()) {
            if (!especialidadActiva) {
                throw new BadRequestException("No se puede activar: la especialidad del médico está inactiva.");
            }
        } else {
            verificarSinCitasFuturas(id);
        }
        repositorio.cambiarEstado(id, datos.activo());
        auditoria.registrar(datos.activo() ? "VETERINARIO_ACTIVADO" : "VETERINARIO_INACTIVADO", "medico", id,
                "Veterinario " + id + (datos.activo() ? " activado" : " inactivado"));
        return consultar(id);
    }

    /**
     * @param id médico.
     * @return médico; 404 si no existe.
     */
    private MedicoDto consultar(long id) {
        MedicoDto m = repositorio.buscar(id);
        if (m == null) {
            throw new NotFoundException("El médico no existe.");
        }
        return m;
    }

    /**
     * Impide inactivar a un médico que todavía tiene citas futuras reservadas.
     *
     * @param id médico.
     */
    private void verificarSinCitasFuturas(long id) {
        long futuras = repositorio.citasFuturas(id, Tiempo.ahora());
        if (futuras > 0) {
            throw new ConflictException("No se puede inactivar: el médico tiene " + futuras
                    + " cita(s) futura(s) reservada(s). Reasígnelas o cancélelas primero.");
        }
    }

    /**
     * Valida formato y reglas de negocio de los datos del médico.
     *
     * @param d datos recibidos.
     * @param id médico que se actualiza o null.
     * @return datos validados.
     */
    private Datos validar(MedicoApi.MedicoRq d, Long id) {
        Datos v = validarFormato(d);
        Boolean especialidad = repositorio.especialidadActiva(v.especializacionId());
        if (especialidad == null) {
            throw new NotFoundException("La especialidad no existe.");
        }
        if (!especialidad && v.activo()) {
            throw new BadRequestException("La especialidad está inactiva.");
        }
        if (repositorio.existeDocumento(v.documento(), id)) {
            throw new ConflictException("Ya existe un médico con el documento " + v.documento() + ".");
        }
        if (repositorio.existeRegistro(v.registro(), id)) {
            throw new ConflictException("Ya existe un médico con el registro profesional " + v.registro() + ".");
        }
        if (v.usuarioId() != null) {
            String rol = repositorio.rolDeUsuario(v.usuarioId());
            if (rol == null) {
                throw new NotFoundException("El usuario asociado no existe.");
            }
            if (!Perfiles.VETERINARIO.equals(Perfiles.codigo(rol))) {
                throw new BadRequestException("El usuario asociado debe tener rol Veterinario.");
            }
            if (repositorio.usuarioAsociado(v.usuarioId(), id)) {
                throw new ConflictException("El usuario ya está asociado a otro médico.");
            }
        }
        return v;
    }

    /**
     * Valida el formato de los campos (sin consultar la base de datos).
     *
     * @param d datos recibidos.
     * @return datos normalizados.
     */
    static Datos validarFormato(MedicoApi.MedicoRq d) {
        if (d == null) {
            throw new BadRequestException("Los datos del médico son obligatorios.");
        }
        String tipo = Validar.texto(d.tipoDocumento(), "tipo de documento", 10).toUpperCase(Locale.ROOT);
        if (!TIPOS.contains(tipo)) {
            throw new BadRequestException("Tipo de documento no válido. Use CC, CE, PA o PPT.");
        }
        String doc = Validar.texto(d.numeroDocumento(), "número de documento", 20).replace(" ", "");
        if ("CC".equals(tipo) ? !CEDULA.matcher(doc).matches() : !DOCUMENTO.matcher(doc).matches()) {
            throw new BadRequestException("CC".equals(tipo) ? "La cédula debe tener entre 6 y 10 dígitos."
                    : "El documento debe tener entre 5 y 15 letras o dígitos.");
        }
        String nombres = Validar.texto(d.nombres(), "nombres", 100).replaceAll("\\s+", " ");
        String apellidos = Validar.texto(d.apellidos(), "apellidos", 100).replaceAll("\\s+", " ");
        if (!NOMBRE.matcher(nombres).matches() || !NOMBRE.matcher(apellidos).matches()) {
            throw new BadRequestException("Nombres y apellidos solo pueden contener letras y espacios.");
        }
        String tel = Validar.opcional(d.telefono(), "teléfono", 20);
        if (tel != null && !TELEFONO.matcher(tel).matches()) {
            throw new BadRequestException("El teléfono debe tener entre 7 y 10 dígitos.");
        }
        String registro = Validar.texto(d.registroProfesional(), "registro profesional", 50).toUpperCase(Locale.ROOT);
        if (!REGISTRO.matcher(registro).matches()) {
            throw new BadRequestException("El registro profesional solo admite letras, dígitos y guiones (3 a 50).");
        }
        Validar.requerido(d.especializacionId(), "especialidad");
        return new Datos(tipo, doc, nombres, apellidos, tel, registro, d.especializacionId(), d.usuarioId(),
                d.activo() == null || d.activo());
    }
}
