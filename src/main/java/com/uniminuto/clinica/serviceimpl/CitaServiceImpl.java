package com.uniminuto.clinica.serviceimpl;
import com.uniminuto.clinica.entity.Cita;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Medico;
import com.uniminuto.clinica.models.CitaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.repository.ClienteRepository;
import com.uniminuto.clinica.repository.MascotaRepository;
import com.uniminuto.clinica.repository.MedicoRepository;
import com.uniminuto.clinica.service.CitaService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private CitaRepository citaRepository;


    // Lista las citas entre dos fechas, de la más reciente a la más antigua
    @Override
    public List<Cita> listarCita(
            LocalDateTime fechaInicial,
            LocalDateTime fechaFinal
    ) throws BadRequestException {

        return citaRepository.findAllByFechaHoraBetweenOrderByFechaHoraDesc(
                fechaInicial,
                fechaFinal
        );
    }


    // Guarda una nueva cita
    @Override
    public MiRespuestaRS guardarCita(CitaRq citaRq)
            throws BadRequestException {

        // Paso 1. Validar los datos recibidos
        this.validarCitaRq(citaRq);


        // Paso 2. Consultar si el cliente existe
        Optional<Cliente> optCliente =
                this.clienteRepository.findById(citaRq.getClienteId());

        if (optCliente.isEmpty()) {
            throw new BadRequestException(
                    "El cliente seleccionado no es valido"
            );
        }


        // Paso 3. Consultar si la mascota existe
        Optional<Mascota> optMascota =
                this.mascotaRepository.findById(citaRq.getMascotaId());

        if (optMascota.isEmpty()) {
            throw new BadRequestException(
                    "La mascota seleccionada no es valida"
            );
        }


        // Paso 4. Consultar si el medico existe
        Optional<Medico> optMedico =
                this.medicoRepository.findById(citaRq.getMedicoId());

        if (optMedico.isEmpty()) {
            throw new BadRequestException(
                    "El medico seleccionado no es valido"
            );
        }


        // Paso 5. Crear el objeto Cita
        Cita cita = new Cita();

        cita.setCliente(optCliente.get());
        cita.setMascota(optMascota.get());
        cita.setMedico(optMedico.get());
        cita.setFechaHora(citaRq.getFechaHora());
        cita.setEstado(citaRq.getEstado());
        cita.setMotivo(citaRq.getMotivo());


        // Paso 6. Guardar la cita en la base de datos
        this.citaRepository.save(cita);


        // Paso 7. Crear la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setMessage("Cita guardada exitosamente");
        respuesta.setStatus(200);

        return respuesta;
    }


    // Actualiza una cita existente
    @Override
    public MiRespuestaRS actualizarCita(CitaRq citaRq)
            throws BadRequestException {

        // Paso 1. Validar los datos recibidos
        this.validarCitaRq(citaRq);


        // Paso 2. Validar que venga el id de la cita
        if (citaRq.getCitaId() == null || citaRq.getCitaId() <= 0) {
            throw new BadRequestException(
                    "El id de la cita no puede ser nulo o negativo"
            );
        }


        // Paso 3. Consultar si el cliente existe
        Optional<Cliente> optCliente =
                this.clienteRepository.findById(citaRq.getClienteId());

        if (optCliente.isEmpty()) {
            throw new BadRequestException(
                    "El cliente seleccionado no es valido"
            );
        }


        // Paso 4. Consultar si la mascota existe
        Optional<Mascota> optMascota =
                this.mascotaRepository.findById(citaRq.getMascotaId());

        if (optMascota.isEmpty()) {
            throw new BadRequestException(
                    "La mascota seleccionada no es valida"
            );
        }


        // Paso 5. Consultar si el medico existe
        Optional<Medico> optMedico =
                this.medicoRepository.findById(citaRq.getMedicoId());

        if (optMedico.isEmpty()) {
            throw new BadRequestException(
                    "El medico seleccionado no es valido"
            );
        }


        // Paso 6. Consultar si la cita que queremos actualizar existe
        Optional<Cita> optCita =
                this.citaRepository.findById(citaRq.getCitaId());

        if (optCita.isEmpty()) {
            throw new BadRequestException(
                    "La cita seleccionada no es valida"
            );
        }


        // Paso 7. Obtener la cita existente y actualizar sus datos
        Cita citaExistente = optCita.get();

        citaExistente.setCliente(optCliente.get());
        citaExistente.setMascota(optMascota.get());
        citaExistente.setMedico(optMedico.get());
        citaExistente.setFechaHora(citaRq.getFechaHora());
        citaExistente.setEstado(citaRq.getEstado());
        citaExistente.setMotivo(citaRq.getMotivo());


        // Paso 8. Guardar los cambios
        this.citaRepository.save(citaExistente);


        // Paso 9. Crear la respuesta
        MiRespuestaRS respuesta = new MiRespuestaRS();
        respuesta.setMessage("Cita actualizada exitosamente");
        respuesta.setStatus(200);

        return respuesta;
    }


    // Valida que los datos necesarios de la cita sean correctos
    private void validarCitaRq(CitaRq citaRq)
            throws BadRequestException {

        // Validar que el objeto exista
        if (citaRq == null) {
            throw new BadRequestException(
                    "El objeto CitaRq no puede ser nulo"
            );
        }

        // Validar cliente
        if (citaRq.getClienteId() == null ||
                citaRq.getClienteId() <= 0) {

            throw new BadRequestException(
                    "El id del cliente no puede ser nulo o negativo"
            );
        }

        // Validar mascota
        if (citaRq.getMascotaId() == null ||
                citaRq.getMascotaId() <= 0) {

            throw new BadRequestException(
                    "El id de la mascota no puede ser nulo o negativo"
            );
        }

        // Validar medico
        if (citaRq.getMedicoId() == null ||
                citaRq.getMedicoId() <= 0) {

            throw new BadRequestException(
                    "El id del medico no puede ser nulo o negativo"
            );
        }

        // Validar fecha y hora
        if (citaRq.getFechaHora() == null) {
            throw new BadRequestException(
                    "La fecha y hora de la cita no puede ser nula"
            );
        }

        // Validar estado
        if (citaRq.getEstado() == null ||
                citaRq.getEstado().isEmpty()) {

            throw new BadRequestException(
                    "El estado de la cita no puede ser nulo o vacio"
            );
        }
    }
}