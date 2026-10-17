package com.uniminuto.clinica.apicontroller;
import com.uniminuto.clinica.entity.*;
import com.uniminuto.clinica.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
/** Catálogos sencillos para los selectores de Angular. */
@RestController @RequestMapping("/api") @CrossOrigin(origins="http://localhost:4200")
public class CatalogoParcialController {
    private final ClienteRepository clientes;
    private final MascotaRepository mascotas;
    private final MedicoRepository medicos;
    private final MedicamentoRepository medicamentos;
    public CatalogoParcialController(ClienteRepository clientes, MascotaRepository mascotas,
        MedicoRepository medicos, MedicamentoRepository medicamentos) {
        this.clientes=clientes; this.mascotas=mascotas; this.medicos=medicos; this.medicamentos=medicamentos;
    }
    /** Todos los clientes. */
    @GetMapping("/clientes") public List<Cliente> clientes() {return clientes.findAll();}
    /** Un cliente por id. */
    @GetMapping("/clientes/{id}") public Cliente cliente(@PathVariable Long id) {return clientes.findById(id)
        .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Cliente no encontrado."));}
    /** Mascotas del cliente seleccionado. */
    @GetMapping("/clientes/{id}/mascotas") public List<Mascota> mascotasCliente(@PathVariable Long id) {
        return mascotas.findAllByCliente(cliente(id));
    }
    /** Una mascota por id. */
    @GetMapping("/mascotas/{id}") public Mascota mascota(@PathVariable Integer id) {return mascotas.findById(id)
        .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Mascota no encontrada."));}
    /** Veterinarios disponibles para agendar. */
    @GetMapping("/medicos") public List<Medico> medicos() {return medicos.findAll();}
    /** Medicamentos registrados. */
    @GetMapping("/medicamentos") public List<Medicamento> medicamentos() {return medicamentos.findAll();}
}
