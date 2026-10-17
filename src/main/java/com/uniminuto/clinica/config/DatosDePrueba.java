package com.uniminuto.clinica.config;

import com.uniminuto.clinica.models.AnotacionHistoria;
import com.uniminuto.clinica.models.Cita;
import com.uniminuto.clinica.models.EstadoCita;
import com.uniminuto.clinica.models.FormulaMedica;
import com.uniminuto.clinica.models.HistoriaMedica;
import com.uniminuto.clinica.models.Paciente;
import com.uniminuto.clinica.models.Cliente;
import com.uniminuto.clinica.models.Veterinario;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.VeterinarioRepository;
import com.uniminuto.clinica.repository.AnotacionHistoriaRepository;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.repository.HistoriaMedicaRepository;
import com.uniminuto.clinica.repository.PacienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Componente que carga datos de ejemplo en la base de datos cada vez que la
 * aplicacion arranca.
 *
 * <p>Esto NO es un requerimiento del taller, pero es muy util para poder
 * probar y sustentar los servicios inmediatamente (usando Postman, el
 * navegador o la consola de H2) sin tener que crear manualmente pacientes,
 * citas, formulas e historias antes de poder probar los filtros por fecha.</p>
 *
 * <p>{@code CommandLineRunner} es una interfaz de Spring Boot: cualquier
 * bean que la implemente ejecuta su metodo {@code run} automaticamente una
 * sola vez, justo despues de que la aplicacion termina de arrancar.</p>
 */
@Component
public class DatosDePrueba implements CommandLineRunner {

    private final PacienteRepository pacienteRepository;
    private final ClienteRepository clienteRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final CitaRepository citaRepository;
    private final FormulaMedicaRepository formulaMedicaRepository;
    private final HistoriaMedicaRepository historiaMedicaRepository;
    private final AnotacionHistoriaRepository anotacionHistoriaRepository;

    /**
     * Constructor con inyeccion de dependencias de todos los repositorios
     * necesarios para crear los datos de ejemplo.
     */
    public DatosDePrueba(PacienteRepository pacienteRepository, ClienteRepository clienteRepository, VeterinarioRepository veterinarioRepository,
                          CitaRepository citaRepository,
                          FormulaMedicaRepository formulaMedicaRepository,
                          HistoriaMedicaRepository historiaMedicaRepository,
                          AnotacionHistoriaRepository anotacionHistoriaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.clienteRepository = clienteRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.citaRepository = citaRepository;
        this.formulaMedicaRepository = formulaMedicaRepository;
        this.historiaMedicaRepository = historiaMedicaRepository;
        this.anotacionHistoriaRepository = anotacionHistoriaRepository;
    }

    /**
     * Metodo ejecutado automaticamente al iniciar la aplicacion. Crea
     * pacientes, citas, formulas medicas, historias medicas y anotaciones
     * de ejemplo.
     *
     * @param args argumentos de linea de comandos (no se usan aqui)
     */
    @Override
    public void run(String... args) {
        if (clienteRepository.count() > 0) return;
        // --- Clientes y veterinario de ejemplo ---
        Cliente juan = clienteRepository.save(new Cliente("Juan Perez", "3001234567", "juan@example.com", "Bogotá"));
        Cliente maria = clienteRepository.save(new Cliente("Maria Gomez", "3101234567", "maria@example.com", "Bogotá"));
        Veterinario vet = veterinarioRepository.save(new Veterinario("Dra. Laura Torres", "VET-12345", "laura@example.com", "6011234567"));
        // --- Pacientes asociados a un cliente registrado ---
        Paciente firulais = new Paciente("Firulais", "Perro", "Labrador", juan.getNombre()); firulais.setCliente(juan); firulais = pacienteRepository.save(firulais);
        Paciente michi = new Paciente("Michi", "Gato", "Persa", maria.getNombre()); michi.setCliente(maria); michi = pacienteRepository.save(michi);

        // --- Citas (para probar el filtro por rango de fechas) ---
        crearCita(firulais, vet, LocalDateTime.now().minusDays(1), "Vacunacion", EstadoCita.COMPLETADA);
        crearCita(michi, vet, LocalDateTime.now().minusDays(3), "Control general", EstadoCita.PENDIENTE);
        crearCita(firulais, vet, LocalDateTime.now().plusDays(2), "Desparasitacion", EstadoCita.CONFIRMADA);

        // --- Formulas medicas (inventario) ---
        crearFormula(firulais, "Amoxicilina", "500mg cada 12 horas", "Administrar con alimento", 10);
        crearFormula(michi, "Meloxicam", "0.1mg/kg cada 24 horas", "No administrar en ayunas", 5);

        // --- Historia medica con una anotacion ---
        HistoriaMedica historia = new HistoriaMedica();
        historia.setDiagnostico("Dermatitis leve");
        historia.setObservaciones("Se recomienda seguimiento en 2 semanas");
        historia.setPaciente(firulais);
        historia.setFechaCreacion(LocalDateTime.now().minusDays(5));
        Long firulaisId = firulais.getId();
        Cita citaAtendida = citaRepository.findAll().stream()
                .filter(c -> c.getPaciente() != null && c.getPaciente().getId().equals(firulaisId))
                .filter(c -> c.getEstado() == EstadoCita.COMPLETADA)
                .findFirst().orElse(null);
        historia.setCita(citaAtendida);
        historia.setVeterinario(vet);
        historia = historiaMedicaRepository.save(historia);

        // Se relaciona la fórmula de Firulais con su cita e historia clínica de ejemplo.
        HistoriaMedica historiaGuardada = historia;
        formulaMedicaRepository.findAll().stream()
                .filter(f -> f.getPaciente() != null && f.getPaciente().getId().equals(firulaisId))
                .findFirst().ifPresent(f -> {
                    f.setCita(citaAtendida);
                    f.setHistoriaMedica(historiaGuardada);
                    f.setVeterinario(vet);
                    f.setFrecuencia("Cada 12 horas");
                    formulaMedicaRepository.save(f);
                });

        AnotacionHistoria anotacion = new AnotacionHistoria();
        anotacion.setDescripcion("Primera revision: se observa mejoria en la piel");
        anotacion.setFecha(LocalDateTime.now().minusDays(2));
        anotacion.setHistoriaMedica(historia);
        anotacionHistoriaRepository.save(anotacion);
    }

    /**
     * Metodo de apoyo para crear y guardar una cita de ejemplo.
     *
     * @param paciente paciente asociado a la cita
     * @param fecha    fecha y hora de la cita
     * @param motivo   motivo de la consulta
     * @param estado   estado inicial de la cita
     */
    private void crearCita(Paciente paciente, Veterinario veterinario, LocalDateTime fecha, String motivo, EstadoCita estado) {
        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setVeterinario(veterinario);
        cita.setFecha(fecha);
        cita.setMotivo(motivo);
        cita.setEstado(estado);
        citaRepository.save(cita);
    }

    /**
     * Metodo de apoyo para crear y guardar una formula medica de ejemplo.
     *
     * @param paciente     paciente al que se le receta la formula
     * @param medicamento  nombre del medicamento
     * @param dosis        dosis indicada
     * @param indicaciones indicaciones adicionales
     * @param cantidad     cantidad de unidades tomadas del inventario
     */
    private void crearFormula(Paciente paciente, String medicamento, String dosis, String indicaciones, int cantidad) {
        FormulaMedica formula = new FormulaMedica();
        formula.setPaciente(paciente);
        formula.setMedicamento(medicamento);
        formula.setDosis(dosis);
        formula.setIndicaciones(indicaciones);
        formula.setCantidad(cantidad);
        formula.setFechaCreacion(LocalDateTime.now());
        formulaMedicaRepository.save(formula);
    }
}
