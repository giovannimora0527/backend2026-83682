package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.models.ReporteCatalogoRs;
import com.uniminuto.clinica.models.ReporteRs;
import com.uniminuto.clinica.pdf.DocumentoPdf;
import com.uniminuto.clinica.reporte.ExcelExporter;
import com.uniminuto.clinica.reporte.Filtros;
import com.uniminuto.clinica.reporte.GeneradorReporte;
import com.uniminuto.clinica.reporte.PdfExporter;
import com.uniminuto.clinica.reporte.Tabla;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.security.UsuarioAutenticado;
import com.uniminuto.clinica.service.ConfiguracionService;
import com.uniminuto.clinica.service.ReporteService;
import com.uniminuto.clinica.util.ArchivoTabular;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final Map<String, GeneradorReporte> generadores;
    private final ConfiguracionService configuracion;
    private final ExcelExporter excel;
    private final PdfExporter pdf;
    private final UsuarioActual usuarioActual;

    public ReporteServiceImpl(List<GeneradorReporte> generadores, ConfiguracionService configuracion,
                              ExcelExporter excel, PdfExporter pdf, UsuarioActual usuarioActual) {
        this.generadores = generadores.stream().collect(Collectors.toMap(GeneradorReporte::codigo,
                Function.identity(), (a, b) -> {
                    throw new IllegalStateException("Reporte repetido: " + a.codigo());
                }, LinkedHashMap::new));
        this.configuracion = configuracion;
        this.excel = excel;
        this.pdf = pdf;
        this.usuarioActual = usuarioActual;
    }

    @Override
    public List<ReporteCatalogoRs> catalogo() {
        String rol = usuarioActual.obtener().rol();
        return generadores.values().stream().filter(g -> g.rolesPermitidos().contains(rol))
                .map(g -> new ReporteCatalogoRs(g.codigo(), g.titulo(), g.grupo(), g.filtros())).toList();
    }

    @Override
    public ReporteRs generar(String codigo, Map<String, String> parametros) {
        GeneradorReporte generador = generadores.get(codigo);
        if (generador == null) {
            throw new NotFoundException("El reporte no existe.");
        }
        UsuarioAutenticado usuario = usuarioActual.obtener();
        if (!generador.rolesPermitidos().contains(usuario.rol())) {
            throw new AccessDeniedException("El rol no puede usar este reporte.");
        }
        Filtros filtros = new Filtros(generador.filtros(), parametros);
        Tabla tabla = generador.generar(filtros);
        return new ReporteRs(codigo, generador.titulo(), LocalDateTime.now(), usuario.username(), filtros.aplicados(),
                tabla.columnas(), tabla.filas(), tabla.totales());
    }

    @Override
    public Descarga exportar(String codigo, String formato, Map<String, String> parametros) {
        if (!"pdf".equals(formato) && !"xlsx".equals(formato)) {
            throw new BadRequestException("El formato debe ser pdf o xlsx.");
        }
        ReporteRs reporte = generar(codigo, parametros);
        String nombre = codigo + "_" + LocalDate.now() + "." + formato;
        return "pdf".equals(formato)
                ? new Descarga(nombre, DocumentoPdf.TIPO, pdf.exportar(configuracion.obtener(), reporte))
                : new Descarga(nombre, ArchivoTabular.TIPO_XLSX, excel.exportar(configuracion.obtener(), reporte));
    }
}
