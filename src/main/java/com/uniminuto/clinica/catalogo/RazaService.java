package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Tiempo;
import com.uniminuto.clinica.comun.Validar;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Reglas del catálogo de razas: nombre + especie únicos y solo con letras; no se cambia la especie de una raza
 * con mascotas ni se elimina una raza en uso (integridad de las mascotas).
 */
@Service
public class RazaService {

    /** Solo letras (con tildes), espacios, apóstrofos y guiones; entre 2 y 100 caracteres. */
    static final Pattern NOMBRE = Pattern.compile("^[\\p{L}][\\p{L} '\\-]{1,99}$");

    /** Acceso a datos de razas. */
    private final RazaRepository repositorio;

    /**
     * @param repositorio repositorio de razas.
     */
    public RazaService(RazaRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** @return razas con su número de mascotas. */
    public List<RazaDto> listar() {
        return repositorio.listar();
    }

    /**
     * @param d datos.
     * @return raza creada.
     */
    @Transactional
    public RazaDto crear(RazaApi.RazaRq d) {
        String[] v = validar(d);
        if (repositorio.existe(v[0], v[1], null)) {
            throw new ConflictException("La raza " + v[0] + " ya está registrada para la especie " + v[1] + ".");
        }
        return repositorio.buscar(repositorio.insertar(v[0], v[1], Tiempo.ahora()), false);
    }

    /**
     * @param id raza.
     * @param d nuevos datos.
     * @return raza actualizada.
     */
    @Transactional
    public RazaDto actualizar(int id, RazaApi.RazaRq d) {
        RazaDto actual = repositorio.buscar(id, true);
        if (actual == null) {
            throw new NotFoundException("La raza no existe.");
        }
        String[] v = validar(d);
        if (repositorio.existe(v[0], v[1], (long) id)) {
            throw new ConflictException("Ya existe otra raza " + v[0] + " para la especie " + v[1] + ".");
        }
        if (actual.mascotas() > 0 && !v[1].equalsIgnoreCase(actual.especie())) {
            throw new ConflictException("No se puede cambiar la especie: la raza tiene " + actual.mascotas() + " mascota(s) registrada(s).");
        }
        repositorio.actualizar(id, v[0], v[1], Tiempo.ahora());
        return repositorio.buscar(id, false);
    }

    /**
     * @param id raza sin mascotas.
     */
    @Transactional
    public void eliminar(int id) {
        RazaDto actual = repositorio.buscar(id, true);
        if (actual == null) {
            throw new NotFoundException("La raza no existe.");
        }
        if (actual.mascotas() > 0) {
            throw new ConflictException("No se puede eliminar: la raza tiene " + actual.mascotas() + " mascota(s) registrada(s).");
        }
        repositorio.eliminar(id);
    }

    /**
     * Valida y normaliza nombre y especie (primera letra en mayúscula).
     *
     * @param d datos recibidos.
     * @return arreglo {nombre, especie}.
     */
    static String[] validar(RazaApi.RazaRq d) {
        if (d == null) {
            throw new BadRequestException("Los datos de la raza son obligatorios.");
        }
        String nombre = capital(Validar.texto(d.nombre(), "nombre", 100));
        String especie = capital(Validar.texto(d.especie(), "especie", 100));
        if (!NOMBRE.matcher(nombre).matches()) {
            throw new BadRequestException("El nombre de la raza solo puede contener letras y espacios (mínimo 2).");
        }
        if (!NOMBRE.matcher(especie).matches()) {
            throw new BadRequestException("La especie solo puede contener letras y espacios (mínimo 2).");
        }
        return new String[]{nombre, especie};
    }

    /**
     * @param texto texto sin espacios extremos.
     * @return texto con espacios simples y la primera letra en mayúscula.
     */
    static String capital(String texto) {
        String limpio = texto.replaceAll("\\s+", " ");
        return limpio.substring(0, 1).toUpperCase(Locale.ROOT) + limpio.substring(1);
    }
}
