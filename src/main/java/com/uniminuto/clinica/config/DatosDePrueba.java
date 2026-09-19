package com.uniminuto.clinica.config;

import com.uniminuto.clinica.models.AnotacionHistoria;
import com.uniminuto.clinica.models.Cita;
import com.uniminuto.clinica.models.EstadoCita;
import com.uniminuto.clinica.models.FormulaMedica;
import com.uniminuto.clinica.models.HistoriaMedica;
import com.uniminuto.clinica.models.Paciente;
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
    private final CitaRepository citaRepository;
    private final FormulaMedicaRepository formulaMedicaRepository;
    private final HistoriaMedicaRepository historiaMedicaRepository;
    private final AnotacionHistoriaRepository anotacionHistoriaRepository;

    /**
     * Constructor con inyeccion de dependencias de todos los repositorios
     * necesarios para crear los datos de ejemplo.
     */
    public DatosDePrueba(PacienteRepository pacienteRepository,
                          CitaRepository citaRepository,
                          FormulaMedicaRepository formulaMedicaRepository,
                          HistoriaMedicaRepository historiaMedicaRepository,
                          AnotacionHistoriaRepository anotacionHistoriaRepository) {
        this.pacienteRepository = pacienteRepository;
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
        // --- Pacientes ---
        Paciente firulais = pacienteRepository.save(new Paciente("Firulais", "Perro", "Labrador", "Juan Perez"));
        Paciente michi = pacienteRepository.save(new Paciente("Michi", "Gato", "Persa", "Maria Gomez"));

        // --- Citas (para probar el filtro por rango de fechas) ---
        crearCita(firulais, LocalDateTime.now().minusDays(1), "Vacunacion", EstadoCita.COMPLETADA);
        crearCita(michi, LocalDateTime.now().minusDays(3), "Control general", EstadoCita.PENDIENTE);
        crearCita(firulais, LocalDateTime.now().plusDays(2), "Desparasitacion", EstadoCita.CONFIRMADA);

        // --- Formulas medicas (inventario) ---
        crearFormula(firulais, "Amoxicilina", "500mg cada 12 horas", "Administrar con alimento", 10);
        crearFormula(michi, "Meloxicam", "0.1mg/kg cada 24 horas", "No administrar en ayunas", 5);

        // --- Historia medica con una anotacion ---
        HistoriaMedica historia = new HistoriaMedica();
        historia.setDiagnostico("Dermatitis leve");
        historia.setObservaciones("Se recomienda seguimiento en 2 semanas");
        historia.setPaciente(firulais);
        historia.setFechaCreacion(LocalDateTime.now().minusDays(5));
        historia = historiaMedicaRepository.save(historia);

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
    private void crearCita(Paciente paciente, LocalDateTime fecha, String motivo, EstadoCita estado) {
        Cita cita = new Cita();
        cita.setPaciente(paciente);
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
