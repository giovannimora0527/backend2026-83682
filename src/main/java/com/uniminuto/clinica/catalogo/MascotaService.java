package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Reglas de negocio de mascotas: propietario existente y activo, raza existente, edad entre 0 y 40, el propietario no
 * repite el nombre de una mascota activa, no se inactiva con citas futuras reservadas y no se activa si el
 * propietario está archivado.
 */
@Service
public class MascotaService {

    /**
     * Datos de una mascota validados.
     *
     * @param nombre nombre.
     * @param edad edad en años.
     * @param razaId raza.
     * @param clienteId propietario.
     */
    record Datos(String nombre, int edad, int razaId, long clienteId) {
    }

    /** Acceso a datos de mascotas. */
    private final MascotaRepository repositorio;

    /**
     * @param repositorio repositorio de mascotas.
     */
    public MascotaService(MascotaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** @return mascotas con raza y propietario. */
    public List<MascotaDto> listar() {
        return repositorio.listar();
    }

    /**
     * @param id mascota.
     * @return mascota; 404 si no existe.
     */
    public MascotaDto consultar(int id) {
        MascotaDto m = repositorio.buscar(id);
        if (m == null) {
            throw new NotFoundException("La mascota no existe.");
        }
        return m;
    }

    /**
     * @param d datos.
     * @return mascota creada.
     */
    @Transactional
    public MascotaDto crear(MascotaApi.MascotaRq d) {
        Datos v = validar(d);
        verificarNombreUnico(v, null);
        return consultar((int) repositorio.insertar(v, Tiempo.ahora()));
    }

    /**
     * @param id mascota.
     * @param d nuevos datos.
     * @return mascota actualizada.
     */
    @Transactional
    public MascotaDto actualizar(int id, MascotaApi.MascotaRq d) {
        Map<String, Object> actual = repositorio.bloquearEstados(id);
        if (actual == null) {
            throw new NotFoundException("La mascota no existe.");
        }
        Datos v = validar(d);
        verificarNombreUnico(v, id);
        boolean activo = d.activo() == null || d.activo();
        if (Validar.bool(actual, "activo") && !activo) {
            verificarSinCitasFuturas(id);
        }
        repositorio.actualizar(id, v, activo, Tiempo.ahora());
        return consultar(id);
    }

    /**
     * Activa o inactiva una mascota.
     *
     * @param id mascota.
     * @param datos nuevo estado.
     * @return mascota actualizada.
     */
    @Transactional
    public MascotaDto cambiarEstado(int id, MascotaApi.EstadoRq datos) {
        Validar.requerido(datos == null ? null : datos.activo(), "activo");
        Map<String, Object> m = repositorio.bloquearEstados(id);
        if (m == null) {
            throw new NotFoundException("La mascota no existe.");
        }
        if (datos.activo() && !Validar.bool(m, "cliente_activo")) {
            throw new BadRequestException("No se puede activar: el propietario está archivado.");
        }
        if (!datos.activo()) {
            verificarSinCitasFuturas(id);
        }
        repositorio.cambiarEstado(id, datos.activo(), Tiempo.ahora());
        return consultar(id);
    }

    /**
     * @param id mascota.
     */
    private void verificarSinCitasFuturas(int id) {
        long futuras = repositorio.citasFuturas(id, Tiempo.ahora());
        if (futuras > 0) {
            throw new ConflictException("No se puede inactivar: la mascota tiene " + futuras
                    + " cita(s) futura(s) reservada(s). Cancélelas primero.");
        }
    }

    /**
     * @param v datos validados.
     * @param id mascota que se actualiza o null.
     */
    private void verificarNombreUnico(Datos v, Integer id) {
        if (repositorio.existeNombreActivo(v.clienteId(), v.nombre(), id)) {
            throw new ConflictException("El propietario ya tiene una mascota activa llamada " + v.nombre() + ".");
        }
    }

    /**
     * @param d datos recibidos.
     * @return datos validados.
     */
    private Datos validar(MascotaApi.MascotaRq d) {
        if (d == null) {
            throw new BadRequestException("Los datos de la mascota son obligatorios.");
        }
        String nombre = Validar.texto(d.nombreMascota(), "nombre de la mascota", 100);
        int edad = Validar.entero(d.edad(), "edad", 0);
        if (edad > 40) {
            throw new BadRequestException("La edad de la mascota no es válida.");
        }
        Validar.requerido(d.razaId(), "raza");
        Validar.requerido(d.clienteId(), "propietario");
        if (!repositorio.existeRaza(d.razaId())) {
            throw new NotFoundException("La raza no existe.");
        }
        Boolean cliente = repositorio.clienteActivo(d.clienteId());
        if (cliente == null) {
            throw new NotFoundException("El propietario no existe.");
        }
        if (!cliente) {
            throw new BadRequestException("El propietario está archivado.");
        }
        return new Datos(nombre, edad, d.razaId(), d.clienteId());
    }
}
