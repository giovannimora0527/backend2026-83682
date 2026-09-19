package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.uniminuto.clinica.entity.FormulaMedica;
import org.apache.coyote.BadRequestException;
import java.util.List;

@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {
    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;
    // Implementa el método del Service y utiliza el Repository para consultar
// todas las fórmulas médicas ordenadas de la más reciente a la más antigua
    @Override
    public  List<FormulaMedica> listarFormulasMedicas() throws BadRequestException {
    return formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();
    }
}
