package com.uniminuto.clinica.catalogo;

import com.uniminuto.clinica.comun.Db;
import com.uniminuto.clinica.comun.Validar;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Acceso a datos de clientes (tabla cliente) y de sus mascotas activas. */
@Repository
public class ClienteRepository {

    /** Consulta base de clientes. */
    private static final String CONSULTA = """
            SELECT c.cliente_id, c.usuario_id, c.tipo_documento, c.numero_documento, c.nombres, c.apellidos,
                   c.fecha_nacimiento, c.genero, c.telefono, c.direccion, c.activo
            FROM cliente c
            """;

    /** Acceso JDBC con SQL parametrizado. */
    private final Db db;

    /**
     * @param db acceso a datos.
     */
    public ClienteRepository(Db db) {
        this.db = db;
    }

    /** @return clientes, activos primero. */
    public List<ClienteDto> listar() {
        return db.lista(CONSULTA + " ORDER BY c.activo DESC, c.apellidos, c.nombres").stream().map(ClienteRepository::aDto).toList();
    }

    /**
     * @param id identificador.
     * @return cliente o null.
     */
    public ClienteDto buscar(long id) {
        Map<String, Object> f = db.uno(CONSULTA + " WHERE c.cliente_id = :id", Db.p("id", id));
        return f == null ? null : aDto(f);
    }

    /**
     * @param documento número de documento.
     * @return cliente o null.
     */
    public ClienteDto buscarPorDocumento(String documento) {
        Map<String, Object> f = db.uno(CONSULTA + " WHERE c.numero_documento = :d", Db.p("d", documento));
        return f == null ? null : aDto(f);
    }

    /**
     * @param documento número de documento.
     * @param excluir cliente a excluir (edición) o null.
     * @return true si otro cliente ya usa el documento.
     */
    public boolean existeDocumento(String documento, Long excluir) {
        return db.existe("SELECT 1 FROM cliente WHERE numero_documento = :doc AND (:id IS NULL OR cliente_id <> :id)",
                Db.p("doc", documento, "id", excluir));
    }

    /**
     * @param id cliente.
     * @return mascotas activas del cliente.
     */
    public List<MascotaDeClienteDto> mascotasActivas(long id) {
        return db.lista("""
                SELECT m.mascota_id, m.nombre_mascota, m.edad, r.nombre AS raza, r.especie, m.cliente_id
                FROM mascota m JOIN raza r ON r.raza_id = m.raza_id
                WHERE m.cliente_id = :id AND m.activo = 1 ORDER BY m.nombre_mascota
                """, Db.p("id", id)).stream().map(f -> new MascotaDeClienteDto(Validar.largo(f, "mascota_id"),
                Validar.cadena(f, "nombre_mascota"), Validar.numero(f, "edad"), Validar.cadena(f, "raza"),
                Validar.cadena(f, "especie"), Validar.largo(f, "cliente_id"))).toList();
    }

    /**
     * Inserta un cliente activo.
     *
     * @param c datos validados.
     * @return id generado.
     */
    public long insertar(ClienteService.Datos c) {
        return db.insertar("""
                INSERT INTO cliente (tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento,
                                     genero, telefono, direccion, activo)
                VALUES (:tipo, :doc, :nombres, :apellidos, :nacimiento, :genero, :telefono, :direccion, 1)
                """, parametros(c));
    }

    /**
     * Actualiza los datos de un cliente.
     *
     * @param id cliente.
     * @param c datos validados.
     */
    public void actualizar(long id, ClienteService.Datos c) {
        Map<String, Object> p = parametros(c);
        p.put("id", id);
        db.ejecutar("""
                UPDATE cliente SET tipo_documento = :tipo, numero_documento = :doc, nombres = :nombres,
                       apellidos = :apellidos, fecha_nacimiento = :nacimiento, genero = :genero,
                       telefono = :telefono, direccion = :direccion
                WHERE cliente_id = :id
                """, p);
    }

    /**
     * @param id cliente.
     * @param ahora fecha y hora actual.
     * @return true si tiene citas futuras reservadas.
     */
    public boolean tieneCitasFuturas(long id, LocalDateTime ahora) {
        return db.existe("""
                SELECT 1 FROM cita WHERE cliente_id = :id AND estado IN ('pendiente', 'programada', 'confirmada')
                  AND fecha_hora >= :ahora
                """, Db.p("id", id, "ahora", ahora));
    }

    /**
     * Archiva el cliente e inactiva sus mascotas.
     *
     * @param id cliente.
     * @param ahora fecha de modificación de las mascotas.
     */
    public void archivarConMascotas(long id, LocalDateTime ahora) {
        db.ejecutar("UPDATE cliente SET activo = 0 WHERE cliente_id = :id", Db.p("id", id));
        db.ejecutar("UPDATE mascota SET activo = 0, fecha_modificacion = :f WHERE cliente_id = :id", Db.p("id", id, "f", ahora));
    }

    /**
     * Bloquea la fila del cliente para actualizarla (SELECT … FOR UPDATE).
     *
     * @param id cliente.
     * @return estado activo o null si no existe.
     */
    public Boolean bloquearEstado(long id) {
        Map<String, Object> f = db.uno("SELECT activo FROM cliente WHERE cliente_id = :id FOR UPDATE", Db.p("id", id));
        return f == null ? null : Validar.bool(f, "activo");
    }

    /**
     * @param id cliente.
     */
    public void reactivar(long id) {
        db.ejecutar("UPDATE cliente SET activo = 1 WHERE cliente_id = :id", Db.p("id", id));
    }

    /**
     * @param c datos validados.
     * @return parámetros SQL.
     */
    private static Map<String, Object> parametros(ClienteService.Datos c) {
        return Db.p("tipo", c.tipo(), "doc", c.documento(), "nombres", c.nombres(), "apellidos", c.apellidos(),
                "nacimiento", c.nacimiento(), "genero", c.genero(), "telefono", c.telefono(), "direccion", c.direccion());
    }

    /**
     * @param f fila.
     * @return DTO del cliente.
     */
    private static ClienteDto aDto(Map<String, Object> f) {
        return new ClienteDto(Validar.largo(f, "cliente_id"), Validar.largoNulo(f, "usuario_id"), Validar.cadena(f, "tipo_documento"),
                Validar.cadena(f, "numero_documento"), Validar.cadena(f, "nombres"), Validar.cadena(f, "apellidos"),
                (LocalDate) f.get("fecha_nacimiento"), Validar.cadena(f, "genero"), Validar.cadena(f, "telefono"),
                Validar.cadena(f, "direccion"), Validar.bool(f, "activo"));
    }
}
