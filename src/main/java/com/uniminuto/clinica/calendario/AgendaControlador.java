package com.uniminuto.clinica.calendario;

import static com.uniminuto.clinica.calendario.AgendaDatos.*;
import static com.uniminuto.clinica.calendario.AgendaServicio.*;

import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.service.AlcanceMedico;
import com.uniminuto.clinica.service.CitaService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cita/agenda")
public class AgendaControlador {
    private final AgendaServicio agenda;
    private final AgendaExcel excel;
    private final AlcanceMedico alcance;
    private final CitaService citas;

    public AgendaControlador(AgendaServicio agenda,AgendaExcel excel,AlcanceMedico alcance,CitaService citas) {
        this.agenda=agenda;
        this.excel=excel;
        this.alcance=alcance;
        this.citas=citas;
    }

    @GetMapping("/mia")
    List<Evento> eventos(@RequestParam LocalDate desde,@RequestParam LocalDate hasta,
            @RequestParam(required=false) String estado) {
        return agenda.eventos(desde,hasta,estado);
    }

    @GetMapping("/mia/excel")
    ResponseEntity<byte[]> exportar(@RequestParam LocalDate desde,@RequestParam LocalDate hasta,
            @RequestParam(required=false) String estado) throws IOException {
        byte[] archivo=excel.generar(agenda.eventos(desde,hasta,estado));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"citas-"+desde+"-"+hasta+".xlsx\"")
                .cacheControl(CacheControl.noStore()).body(archivo);
    }

    @GetMapping("/mia/configuracion")
    Configuracion propia() { return agenda.configuracion(propio()); }

    @PutMapping("/mia/horarios")
    Configuracion cambiarPropios(@RequestBody List<Franja> franjas) {
        return agenda.guardarHorarios(propio(),franjas);
    }

    @PostMapping("/mia/ausencias")
    @ResponseStatus(HttpStatus.CREATED)
    Configuracion ausenciaPropia(@RequestBody Ausencia ausencia) { return agenda.registrarAusencia(propio(),ausencia); }

    @GetMapping("/medicos/{id}/configuracion")
    Configuracion configuracion(@PathVariable int id) { return agenda.configuracion(id); }

    @GetMapping("/medicos/{id}/disponibilidad")
    Disponibilidad disponibilidad(@PathVariable int id,@RequestParam LocalDate fecha) {
        return agenda.disponibilidad(id,fecha);
    }

    @PutMapping("/medicos/{id}/horarios")
    Configuracion horarios(@PathVariable int id,@RequestBody List<Franja> franjas) {
        return agenda.guardarHorarios(id,franjas);
    }

    @PostMapping("/medicos/{id}/ausencias")
    @ResponseStatus(HttpStatus.CREATED)
    Configuracion ausencia(@PathVariable int id,@RequestBody Ausencia ausencia) {
        return agenda.registrarAusencia(id,ausencia);
    }

    @PutMapping("/medicos/{id}/actividad")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void actividad(@PathVariable int id,@RequestBody Map<String,Boolean> cuerpo) {
        Boolean activo=cuerpo.get("activo");
        if (activo==null) throw invalido("Indica si el médico estará activo.");
        agenda.cambiarActividad(id,activo);
    }

    @GetMapping("/{id}/historial")
    List<Historial> historial(@PathVariable long id) {
        citas.consultarCitaPorId(id);
        return agenda.historial(id);
    }

    @PutMapping("/{id}/cambio")
    Cita cambio(@PathVariable long id,@RequestBody Cambio cambio) { return citas.cambiarAgenda(id,cambio); }

    private int propio() {
        Integer medico=alcance.medicoId();
        if (medico==null) throw prohibido("El calendario personal requiere un usuario médico vinculado.");
        return medico;
    }
}
