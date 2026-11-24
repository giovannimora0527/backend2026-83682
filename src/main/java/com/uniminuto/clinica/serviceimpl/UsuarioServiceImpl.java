package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Perfil;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import com.uniminuto.clinica.repository.PerfilRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.UsuarioService;
import com.uniminuto.clinica.util.PasswordUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PerfilRepository perfilRepository,
                              PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
    }

    @Override
    public List<UsuarioRs> obtenerUsuariosOrdenados() {
        List<Usuario> usuarios = usuarioRepository.findAllByOrderByUsernameAsc();
        return convertirUsuarioToUsuarioRs(usuarios);
    }

    private List<UsuarioRs> convertirUsuarioToUsuarioRs(List<Usuario> usuarios) {
        return usuarios.stream().map(usuario -> {
            UsuarioRs usuarioRs = new UsuarioRs();
            usuarioRs.setId(usuario.getId());
            usuarioRs.setUsername(usuario.getUsername());
            usuarioRs.setRol(usuario.getPerfil().getNombre());
            usuarioRs.setFechaCreacion(usuario.getFechaCreacion());
            usuarioRs.setActivo(usuario.getActivo());
            usuarioRs.setEmail(usuario.getEmail());
            return usuarioRs;
        }).toList();
    }

    @Override
    public MiRespuestaRS crearUsuario(UsuarioRq usuarioRq) {
        this.validarUsuario(usuarioRq);
        if (!tienePassword(usuarioRq)) {
            throw new BadRequestException("La contraseña no puede estar vacía");
        }

        if (this.usuarioRepository.existsByUsername(usuarioRq.getUsername())) {
            throw new ConflictException("El nombre de usuario ya existe");
        }
        if (this.usuarioRepository.existsByEmail(usuarioRq.getEmail())) {
            throw new ConflictException("El correo electrónico ya existe");
        }

        Optional<Perfil> optPerfil = this.perfilRepository
                .findById(usuarioRq.getPerfilId());
        if (optPerfil.isEmpty()) {
            throw new NotFoundException("El perfil no existe");
        }

        Usuario usuarioNuevo = new Usuario();
        usuarioNuevo.setUsername(usuarioRq.getUsername());
        usuarioNuevo.setPasswordHash(passwordEncoder.encode(usuarioRq.getPassword()));
        usuarioNuevo.setPerfil(optPerfil.get());
        usuarioNuevo.setEmail(usuarioRq.getEmail());
        usuarioNuevo.setActivo(true);
        usuarioNuevo.setFechaCreacion(LocalDateTime.now());

        usuarioRepository.save(usuarioNuevo);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Usuario creado exitosamente");
        rta.setStatus(200);

        return rta;
    }

    @Override
    public MiRespuestaRS actualizarUsuario(UsuarioRq usuarioRq) {
        this.validarUsuario(usuarioRq);
        if (usuarioRq.getId() == null) {
            throw new BadRequestException("El id del usuario es obligatorio");
        }

        Optional<Usuario> optUser = this.usuarioRepository.findById(usuarioRq.getId());
        if (optUser.isEmpty()) {
            throw new NotFoundException("El usuario no existe");
        }

        Usuario usuarioExistente = optUser.get();
        if (!usuarioExistente.getUsername().equals(usuarioRq.getUsername())) {
            if (this.usuarioRepository.existsByUsername(usuarioRq.getUsername())) {
                throw new ConflictException("El nombre de usuario ya existe");
            }
        }
        usuarioExistente.setUsername(usuarioRq.getUsername());
        if (!usuarioExistente.getEmail().equals(usuarioRq.getEmail())) {
            if (this.usuarioRepository.existsByEmail(usuarioRq.getEmail())) {
                throw new ConflictException("El correo electrónico ya existe");
            }
        }
        Optional<Perfil> optPerfil = this.perfilRepository
                .findById(usuarioRq.getPerfilId());
        if (optPerfil.isEmpty()) {
            throw new NotFoundException("El perfil no existe");
        }

        usuarioExistente.setEmail(usuarioRq.getEmail());
        if (usuarioRq.getActivo() != null) {
            usuarioExistente.setActivo(usuarioRq.getActivo());
        }
        if (tienePassword(usuarioRq)) {
            usuarioExistente.setPasswordHash(passwordEncoder.encode(usuarioRq.getPassword()));
        }
        usuarioExistente.setPerfil(optPerfil.get());
        usuarioRepository.save(usuarioExistente);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Usuario actualizado exitosamente");
        rta.setStatus(200);

        return rta;
    }

    private boolean tienePassword(UsuarioRq usuarioRq) {
        return usuarioRq.getPassword() != null && !usuarioRq.getPassword().isEmpty();
    }

    private void validarUsuario(UsuarioRq usuarioRq) throws BadRequestException {
        if (usuarioRq == null) {
            throw new BadRequestException("El usuario no puede estar vacío");
        }
        if (usuarioRq.getUsername() == null || usuarioRq.getUsername().isEmpty()) {
            throw new BadRequestException("El nombre de usuario no puede estar vacío");
        }
        if (tienePassword(usuarioRq) && !PasswordUtil.cabeEnBCrypt(usuarioRq.getPassword())) {
            throw new BadRequestException("La contraseña admite hasta 72 bytes");
        }
        if (usuarioRq.getPerfilId() == null) {
            throw new BadRequestException("El perfil no puede estar vacío");
        }
        if (usuarioRq.getEmail() == null || usuarioRq.getEmail().isEmpty()) {
            throw new BadRequestException("El correo electrónico no puede estar vacío");
        }
    }
}
