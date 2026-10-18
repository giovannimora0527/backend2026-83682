package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.UsuarioService;
import com.uniminuto.clinica.util.PasswordUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRs> obtenerUsuariosOrdenados() {
        return usuarioRepository.findAllByOrderByUsernameAsc().stream().map(this::convertir).toList();
    }

    private UsuarioRs convertir(Usuario usuario) {
        UsuarioRs usuarioRs = new UsuarioRs();
        usuarioRs.setId(usuario.getId());
        usuarioRs.setUsername(usuario.getUsername());
        usuarioRs.setRol(usuario.getRol());
        usuarioRs.setFechaCreacion(usuario.getFechaCreacion());
        usuarioRs.setActivo(usuario.getActivo());
        usuarioRs.setEmail(usuario.getEmail());
        return usuarioRs;
    }

    @Override
    @Transactional
    public MiRespuestaRS crearUsuario(UsuarioRq usuarioRq) {
        this.validarUsuario(usuarioRq);
        if (usuarioRq.getPassword() == null || usuarioRq.getPassword().isBlank()) {
            throw new BadRequestException("La contraseña no puede estar vacía");
        }
        String username = usuarioRq.getUsername().trim();
        String email = usuarioRq.getEmail().trim();
        if (this.usuarioRepository.existsByUsername(username)) {
            throw new BadRequestException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        }
        if (this.usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException(HttpStatus.CONFLICT, "El correo electrónico ya existe");
        }

        Usuario usuarioNuevo = new Usuario();
        usuarioNuevo.setUsername(username);
        usuarioNuevo.setPasswordHash(PasswordUtil.generarMD5(usuarioRq.getPassword()));
        usuarioNuevo.setRol(usuarioRq.getRol().trim());
        usuarioNuevo.setEmail(email);
        usuarioNuevo.setActivo(usuarioRq.getActivo() == null || usuarioRq.getActivo());
        usuarioNuevo.setFechaCreacion(LocalDateTime.now());
        usuarioRepository.save(usuarioNuevo);

        return respuesta(201, "Usuario creado exitosamente");
    }

    @Override
    @Transactional
    public MiRespuestaRS actualizarUsuario(UsuarioRq usuarioRq) {
        this.validarUsuario(usuarioRq);
        if (usuarioRq.getId() == null) {
            throw new BadRequestException("El id del usuario es obligatorio");
        }
        Usuario usuarioExistente = this.usuarioRepository.findById(usuarioRq.getId())
                .orElseThrow(() -> new BadRequestException(HttpStatus.NOT_FOUND, "El usuario no existe"));

        String username = usuarioRq.getUsername().trim();
        String email = usuarioRq.getEmail().trim();
        if (!username.equals(usuarioExistente.getUsername()) && this.usuarioRepository.existsByUsername(username)) {
            throw new BadRequestException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        }
        if (!email.equals(usuarioExistente.getEmail()) && this.usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException(HttpStatus.CONFLICT, "El correo electrónico ya existe");
        }
        usuarioExistente.setUsername(username);
        usuarioExistente.setEmail(email);
        usuarioExistente.setRol(usuarioRq.getRol().trim());
        if (usuarioRq.getActivo() != null) {
            usuarioExistente.setActivo(usuarioRq.getActivo());
        }
        if (usuarioRq.getPassword() != null && !usuarioRq.getPassword().isBlank()) {
            usuarioExistente.setPasswordHash(PasswordUtil.generarMD5(usuarioRq.getPassword()));
        }
        usuarioRepository.save(usuarioExistente);

        return respuesta(200, "Usuario actualizado exitosamente");
    }

    private MiRespuestaRS respuesta(int status, String mensaje) {
        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setStatus(status);
        rta.setMessage(mensaje);
        return rta;
    }

    private void validarUsuario(UsuarioRq usuarioRq) {
        if (usuarioRq == null) {
            throw new BadRequestException("El usuario no puede estar vacío");
        }
        if (usuarioRq.getUsername() == null || usuarioRq.getUsername().isBlank()) {
            throw new BadRequestException("El nombre de usuario no puede estar vacío");
        }
        if (usuarioRq.getUsername().trim().length() > 50) {
            throw new BadRequestException("El nombre de usuario no puede superar 50 caracteres");
        }
        if (usuarioRq.getRol() == null || usuarioRq.getRol().isBlank()) {
            throw new BadRequestException("El rol no puede estar vacío");
        }
        if (usuarioRq.getRol().trim().length() > 30) {
            throw new BadRequestException("El rol no puede superar 30 caracteres");
        }
        if (usuarioRq.getEmail() == null || usuarioRq.getEmail().isBlank()) {
            throw new BadRequestException("El correo electrónico no puede estar vacío");
        }
        if (!EMAIL.matcher(usuarioRq.getEmail().trim()).matches()) {
            throw new BadRequestException("El correo electrónico no tiene un formato válido");
        }
    }
}
