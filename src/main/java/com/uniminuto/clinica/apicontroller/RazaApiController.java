
package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.entity.Raza;
import com.uniminuto.clinica.repository.RazaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST encargado de consultar las razas registradas
 * en la clínica veterinaria.
 */
@RestController
@RequestMapping("/raza")
public class RazaApiController {

    /** Repositorio que permite consultar las razas almacenadas. */
    private final RazaRepository razas;

    /**
     * Construye el controlador con el repositorio de razas.
     *
     * @param razas repositorio utilizado para acceder a las razas
     */
    public RazaApiController(RazaRepository razas) {
        this.razas = razas;
    }

    /**
     * Obtiene todas las razas registradas.
     *
     * @return respuesta HTTP con la lista de razas
     */
    @GetMapping("/listar")
    public ResponseEntity<List<Raza>> listar() {
        return ResponseEntity.ok(razas.findAll());
    }
}
