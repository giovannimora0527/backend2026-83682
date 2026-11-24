package com.uniminuto.clinica.service;

import com.uniminuto.clinica.models.CargaRs;
import com.uniminuto.clinica.models.Descarga;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

public interface CargaInventarioService {

    Descarga plantilla(String formato);

    CargaRs previsualizar(MultipartFile archivo);

    CargaRs obtener(Long cargaId);

    CargaRs confirmar(Long cargaId);

    CargaRs descartar(Long cargaId);

    Descarga errores(Long cargaId, String formato);

    List<CargaRs> historial(LocalDate desde, LocalDate hasta);
}
