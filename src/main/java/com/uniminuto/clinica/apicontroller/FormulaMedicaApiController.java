package com.uniminuto.clinica.apicontroller;

import com.uniminuto.clinica.api.FormulaMedicaApi;
import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.models.FormulaMedicaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class FormulaMedicaApiController implements FormulaMedicaApi {

    @Autowired
    private FormulaMedicaService formulaMedicaService;

    @Override
    public ResponseEntity<List<FormulaMedica>> listarFormulas() {
        return ResponseEntity.ok(this.formulaMedicaService.listarFormulasOrdenadas(false));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> crearFormula(FormulaMedicaRq formulaRq) throws BadRequestException {
        return ResponseEntity.ok(this.formulaMedicaService.crearFormula(formulaRq));
    }

    @Override
    public ResponseEntity<MiRespuestaRS> actualizarFormula(FormulaMedicaRq formulaRq) throws BadRequestException {
        // Aquí llamamos a tu servicio. Asegúrate de tener el método 'actualizarFormula' en tu FormulaMedicaService
        return ResponseEntity.ok(this.formulaMedicaService.actualizarFormula(formulaRq));


    }

    // Inyecta el repositorio si no lo tienes en el controlador
    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Override
    public ResponseEntity<List<Medicamento>> listarMedicamentosParaFormula() {
        // Retorna todos los medicamentos de la base de datos
        return ResponseEntity.ok(this.medicamentoRepository.findAll());
    }
}