package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.service.UsuarioService;
import com.uniminuto.clinica.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;


    @Override
    public List<UsuarioRs> obtenerUsuariosOrdenados() {
        List<Usuario> usuarios = usuarioRepository.findAllByOrderByUsernameAsc(true);
        return convertirUsuarioToUsuarioRs(usuarios);
    }

    private List<UsuarioRs> convertirUsuarioToUsuarioRs(List<Usuario> usuarios) {
        return usuarios.stream().map(usuario -> {
            UsuarioRs usuarioRs = new UsuarioRs();
            usuarioRs.setId(usuario.getId());
            usuarioRs.setUsername(usuario.getUsername());
            usuarioRs.setRol(usuario.getRol());
            usuarioRs.setFechaCreacion(usuario.getFechaCreacion());
            usuarioRs.setActivo(usuario.getActivo());
            usuarioRs.setEmail(usuario.getEmail());
            return usuarioRs;
        }).toList();
    }

    @Override
    public MiRespuestaRS crearUsuario(UsuarioRq usuarioRq) {
        this.validarUsuario(usuarioRq);

        if (this.usuarioRepository.existsByUsername(usuarioRq.getUsername())) {
            throw new BadRequestException("El nombre de usuario ya existe");
        }
        if (this.usuarioRepository.existsByEmail(usuarioRq.getEmail())) {
            throw new BadRequestException("El correo electrónico ya existe");
        }

        String claveMD5 = PasswordUtil.generarMD5(usuarioRq.getPassword());
        Usuario usuarioNuevo = new Usuario();
        usuarioNuevo.setUsername(usuarioRq.getUsername());
        usuarioNuevo.setPasswordHash(claveMD5);
        usuarioNuevo.setRol(usuarioRq.getRol());
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

        Optional<Usuario> optUser = this.usuarioRepository.findById(usuarioRq.getId());
        if (optUser.isEmpty()) {
            throw new BadRequestException("El usuario no existe");
        }

        Usuario usuarioExistente = optUser.get();
        if (!usuarioExistente.getUsername().equals(usuarioRq.getUsername())) {
            if (this.usuarioRepository.existsByUsername(usuarioRq.getUsername())) {
                throw new BadRequestException("El nombre de usuario ya existe");
            }
        }
        usuarioExistente.setUsername(usuarioRq.getUsername());
        if (!usuarioExistente.getEmail().equals(usuarioRq.getEmail())) {
            if (this.usuarioRepository.existsByEmail(usuarioRq.getEmail())) {
                throw new BadRequestException("El correo electrónico ya existe");
            }
        }
        usuarioExistente.setEmail(usuarioRq.getEmail());
        usuarioExistente.setRol(usuarioRq.getRol());
        usuarioExistente.setActivo(usuarioRq.getActivo());
        usuarioRepository.save(usuarioExistente);

        MiRespuestaRS rta = new MiRespuestaRS();
        rta.setMessage("Usuario actualizado exitosamente");
        rta.setStatus(200);

        return rta;
    }

    private void validarUsuario(UsuarioRq usuarioRq) throws BadRequestException {
        if (usuarioRq == null) {
            throw new BadRequestException("El usuario no puede estar vacío");
        }
        if (usuarioRq.getUsername() == null || usuarioRq.getUsername().isEmpty()) {
            throw new BadRequestException("El nombre de usuario no puede estar vacío");
        }
        if (usuarioRq.getPassword() == null || usuarioRq.getPassword().isEmpty()) {
            throw new BadRequestException("La contraseña no puede estar vacía");
        }
        if (usuarioRq.getRol() == null || usuarioRq.getRol().isEmpty()) {
            throw new BadRequestException("El rol no puede estar vacío");
        }
        if (usuarioRq.getEmail() == null || usuarioRq.getEmail().isEmpty()) {
            throw new BadRequestException("El correo electrónico no puede estar vacío");
        }
    }
}
