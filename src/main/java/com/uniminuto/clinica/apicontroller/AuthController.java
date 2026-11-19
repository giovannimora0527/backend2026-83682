package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.dto.LoginRequest;
import com.uniminuto.clinica.dto.LoginResponse;
import com.uniminuto.clinica.service.AutenticacionService;
import com.uniminuto.clinica.service.IdentidadService;
import com.uniminuto.clinica.service.IdentidadService.Identidad;
import com.uniminuto.clinica.service.JwtService;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AutenticacionService autenticacion;
    private final IdentidadService identidades;
    private final JwtService jwt;

    public AuthController(AutenticacionService autenticacion, IdentidadService identidades, JwtService jwt) {
        this.autenticacion = autenticacion;
        this.identidades = identidades;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(@RequestBody LoginRequest solicitud) {
        var usuario = autenticacion.validarCredenciales(solicitud.username(), solicitud.password());
        var identidad = identidades.perfilActivo(usuario);
        var respuesta = new LoginResponse(jwt.generarToken(usuario), "Bearer", identidad.usuarioId(),
                identidad.username(), identidad.perfilId(), identidad.perfil());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(respuesta);
    }

    @GetMapping("/validar")
    public Map<String, Object> validarToken(@RequestAttribute(IdentidadService.ATRIBUTO) Identidad identidad) {
        return Map.of("valido", true, "username", identidad.username(), "mensaje", "Token JWT válido.");
    }
}
