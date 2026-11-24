package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.UsuarioApi;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.models.UsuarioRq;
import com.uniminuto.clinica.models.UsuarioRs;
import com.uniminuto.clinica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsuarioApiController implements UsuarioApi {

    @Autowired
    private UsuarioService usuarioService;


    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioRs>> getUsuarios() throws BadRequestException {
        return ResponseEntity.ok(this.usuarioService.obtenerUsuariosOrdenados());
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MiRespuestaRS> guardar(UsuarioRq usuarioRq) throws BadRequestException {
        return ResponseEntity.ok(this.usuarioService.crearUsuario(usuarioRq));
    }

    @Override
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MiRespuestaRS> actualizar(UsuarioRq usuarioRq) throws BadRequestException {
        return ResponseEntity.ok(this.usuarioService.actualizarUsuario(usuarioRq));
    }
}
