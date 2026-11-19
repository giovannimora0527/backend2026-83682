package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.repository.UsuarioRepository;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IdentidadService {
    public static final String ATRIBUTO = "identidadClinica";
    public enum Rol { ADMIN, RECEPCION, MEDICO, DESPACHO_MEDICO }
    public record Identidad(Long usuarioId, String username, Integer perfilId, String perfil, Rol rol) {}

    private final UsuarioRepository usuarios;
    private final JdbcTemplate jdbc;

    public IdentidadService(UsuarioRepository usuarios, JdbcTemplate jdbc) {
        this.usuarios = usuarios;
        this.jdbc = jdbc;
    }

    public Optional<Usuario> usuarioActivo(String username, Long id) {
        return usuarios.findById(id)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()) && username.equals(u.getUsername()));
    }

    public Identidad perfilActivo(Usuario usuario) {
        if (usuario.getPerfilId() == null) {
            throw perfilNoDisponible();
        }
        String perfil = jdbc.query("SELECT nombre FROM perfil WHERE perfil_id = ? AND activo = 1",
                (row, index) -> row.getString(1), usuario.getPerfilId())
                .stream().findFirst().orElseThrow(IdentidadService::perfilNoDisponible);
        String nombre = Normalizer.normalize(perfil, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").strip().toUpperCase(Locale.ROOT)
                .replaceAll("[\\s-]+", "_");
        Rol rol = switch (nombre) {
            case "ADMIN", "ADMINISTRADOR" -> Rol.ADMIN;
            case "RECEPCION", "SECRETARIA" -> Rol.RECEPCION;
            case "MEDICO" -> Rol.MEDICO;
            case "DESPACHO_MEDICO" -> Rol.DESPACHO_MEDICO;
            default -> throw perfilNoDisponible();
        };
        return new Identidad(usuario.getId(), usuario.getUsername(), usuario.getPerfilId(), perfil, rol);
    }

    private static ResponseStatusException perfilNoDisponible() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "El perfil del usuario no está disponible.");
    }
}
