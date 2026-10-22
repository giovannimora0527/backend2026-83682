package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.entity.FormulaMedica;
import com.uniminuto.clinica.repository.FormulaMedicaRepository;
import com.uniminuto.clinica.service.FormulaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FormulaMedicaServiceImpl implements FormulaMedicaService {

    @Autowired
    private FormulaMedicaRepository formulaMedicaRepository;


    @Override
    public List<FormulaMedica> listarFormulasOrdenadas(boolean asc) {
        return asc? formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroAsc()
                : formulaMedicaRepository.findAllByOrderByFechaCreacionRegistroDesc();
    }
}
