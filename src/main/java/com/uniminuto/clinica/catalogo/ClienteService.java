package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.auditoria.AuditoriaService;
import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Reglas de negocio de clientes (propietarios):
 * documento único con formato válido, nacimiento entre 1900 y hoy, género M/F/O y teléfono válido;
 * archivar conserva el historial, inactiva las mascotas y no se permite con citas futuras reservadas.
 */
@Service
public class ClienteService {

    /** Tipos de documento permitidos. */
    static final Set<String> TIPOS = Set.of("CC", "TI", "CE", "PA", "NIT", "PPT");

    /**
     * Datos de un cliente ya validados y normalizados.
     *
     * @param tipo tipo de documento.
     * @param documento número de documento.
     * @param nombres nombres.
     * @param apellidos apellidos.
     * @param nacimiento fecha de nacimiento.
     * @param genero género o null.
     * @param telefono teléfono o null.
     * @param direccion dirección o null.
     */
    record Datos(String tipo, String documento, String nombres, String apellidos, LocalDate nacimiento, String genero,
                 String telefono, String direccion) {
    }

    /** Acceso a datos de clientes. */
    private final ClienteRepository repositorio;
    /** Log de operaciones sensibles. */
    private final AuditoriaService auditoria;

    /**
     * @param repositorio repositorio de clientes.
     * @param auditoria servicio de auditoría.
     */
    public ClienteService(ClienteRepository repositorio, AuditoriaService auditoria) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
    }

    /** @return clientes, activos primero. */
    public List<ClienteDto> listar() {
        return repositorio.listar();
    }

    /**
     * @param id cliente.
     * @return cliente; 404 si no existe.
     */
    public ClienteDto consultar(long id) {
        ClienteDto c = repositorio.buscar(id);
        if (c == null) {
            throw new NotFoundException("El cliente no existe.");
        }
        return c;
    }

    /**
     * @param documento número de documento.
     * @return cliente; 404 si no existe.
     */
    public ClienteDto buscarPorDocumento(String documento) {
        ClienteDto c = repositorio.buscarPorDocumento(documento == null ? "" : documento.trim());
        if (c == null) {
            throw new NotFoundException("No existe un cliente con ese documento.");
        }
        return c;
    }

    /**
     * @param id cliente.
     * @return mascotas activas del cliente.
     */
    public List<MascotaDeClienteDto> mascotas(long id) {
        consultar(id);
        return repositorio.mascotasActivas(id);
    }

    /**
     * @param d datos.
     * @return cliente creado.
     */
    @Transactional
    public ClienteDto crear(ClienteApi.ClienteRq d) {
        return consultar(repositorio.insertar(validar(d, null)));
    }

    /**
     * @param id cliente.
     * @param d nuevos datos.
     * @return cliente actualizado.
     */
    @Transactional
    public ClienteDto actualizar(long id, ClienteApi.ClienteRq d) {
        consultar(id);
        repositorio.actualizar(id, validar(d, id));
        return consultar(id);
    }

    /**
     * Archiva el cliente y sus mascotas (no se permite con citas futuras reservadas).
     *
     * @param id cliente.
     */
    @Transactional
    public void archivar(long id) {
        consultar(id);
        if (repositorio.tieneCitasFuturas(id, Tiempo.ahora())) {
            throw new ConflictException("El cliente tiene citas futuras reservadas (pendientes, programadas o confirmadas). "
                    + "Cancélalas antes de archivarlo.");
        }
        repositorio.archivarConMascotas(id, Tiempo.ahora());
        auditoria.registrar("CLIENTE_ARCHIVADO", "cliente", id, "Cliente " + id + " archivado con sus mascotas");
    }

    /**
     * @param id cliente archivado.
     * @return cliente reactivado.
     */
    @Transactional
    public ClienteDto reactivar(long id) {
        Boolean activo = repositorio.bloquearEstado(id);
        if (activo == null) {
            throw new NotFoundException("El cliente no existe.");
        }
        if (activo) {
            throw new ConflictException("El cliente ya está activo.");
        }
        repositorio.reactivar(id);
        return consultar(id);
    }

    /**
     * Valida y normaliza los datos del cliente.
     *
     * @param d datos recibidos.
     * @param id cliente que se actualiza o null.
     * @return datos validados.
     */
    private Datos validar(ClienteApi.ClienteRq d, Long id) {
        if (d == null) {
            throw new BadRequestException("Los datos del cliente son obligatorios.");
        }
        String tipo = Validar.texto(d.tipoDocumento(), "tipo de documento", 10).toUpperCase();
        if (!TIPOS.contains(tipo)) {
            throw new BadRequestException("Tipo de documento no válido. Use: " + String.join(", ", TIPOS) + ".");
        }
        String documento = Validar.texto(d.numeroDocumento(), "número de documento", 20);
        if (!documento.matches("[A-Za-z0-9-]{4,20}")) {
            throw new BadRequestException("El número de documento solo admite letras, números y guiones (4 a 20).");
        }
        LocalDate nacimiento = Validar.fecha(d.fechaNacimiento(), "fecha de nacimiento", true);
        if (nacimiento.isAfter(Tiempo.hoy()) || nacimiento.isBefore(LocalDate.of(1900, 1, 1))) {
            throw new BadRequestException("La fecha de nacimiento no es válida.");
        }
        String genero = Validar.opcional(d.genero(), "género", 1);
        if (genero != null && !Set.of("M", "F", "O").contains(genero.toUpperCase())) {
            throw new BadRequestException("El género debe ser M, F u O.");
        }
        String telefono = Validar.opcional(d.telefono(), "teléfono", 20);
        if (telefono != null && !telefono.matches("[0-9+ ()-]{7,20}")) {
            throw new BadRequestException("El teléfono no es válido.");
        }
        if (repositorio.existeDocumento(documento, id)) {
            throw new ConflictException("Ya existe un cliente con el número de documento " + documento + ".");
        }
        return new Datos(tipo, documento, Validar.texto(d.nombres(), "nombres", 100), Validar.texto(d.apellidos(), "apellidos", 100),
                nacimiento, genero == null ? null : genero.toUpperCase(), telefono, Validar.opcional(d.direccion(), "dirección", 500));
    }
}
