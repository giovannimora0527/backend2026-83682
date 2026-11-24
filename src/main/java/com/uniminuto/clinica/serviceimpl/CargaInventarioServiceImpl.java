package com.uniminuto.clinica.serviceimpl;

import com.uniminuto.clinica.carga.ColumnasCarga;
import com.uniminuto.clinica.carga.ConvertidorFila;
import com.uniminuto.clinica.carga.FilaArchivo;
import com.uniminuto.clinica.carga.ImportadorFila;
import com.uniminuto.clinica.carga.LectorArchivo;
import com.uniminuto.clinica.carga.ValidadorCarga;
import com.uniminuto.clinica.entity.CargaInventario;
import com.uniminuto.clinica.entity.CargaInventarioFila;
import com.uniminuto.clinica.entity.ErrorFila;
import com.uniminuto.clinica.entity.Impuesto;
import com.uniminuto.clinica.entity.Usuario;
import com.uniminuto.clinica.entity.enums.EstadoCarga;
import com.uniminuto.clinica.exception.BadRequestException;
import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.exception.NotFoundException;
import com.uniminuto.clinica.models.CargaRs;
import com.uniminuto.clinica.models.Descarga;
import com.uniminuto.clinica.repository.CargaInventarioFilaRepository;
import com.uniminuto.clinica.repository.CargaInventarioRepository;
import com.uniminuto.clinica.repository.ImpuestoRepository;
import com.uniminuto.clinica.repository.UsuarioRepository;
import com.uniminuto.clinica.security.UsuarioActual;
import com.uniminuto.clinica.service.CargaInventarioService;
import com.uniminuto.clinica.util.ArchivoTabular;
import com.uniminuto.clinica.util.RangoFechas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CargaInventarioServiceImpl implements CargaInventarioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CargaInventarioServiceImpl.class);
    /** Una confirmacion interrumpida (por ejemplo, por un reinicio) se puede reanudar despues de este tiempo. */
    private static final int MINUTOS_REANUDAR = 10;

    private final CargaInventarioRepository cargaRepository;
    private final CargaInventarioFilaRepository filaRepository;
    private final ImpuestoRepository impuestoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LectorArchivo lector;
    private final ValidadorCarga validador;
    private final ImportadorFila importador;
    private final UsuarioActual usuarioActual;
    private final TransactionTemplate transaccion;

    public CargaInventarioServiceImpl(CargaInventarioRepository cargaRepository,
                                      CargaInventarioFilaRepository filaRepository,
                                      ImpuestoRepository impuestoRepository, UsuarioRepository usuarioRepository,
                                      LectorArchivo lector, ValidadorCarga validador, ImportadorFila importador,
                                      UsuarioActual usuarioActual, PlatformTransactionManager transacciones) {
        this.cargaRepository = cargaRepository;
        this.filaRepository = filaRepository;
        this.impuestoRepository = impuestoRepository;
        this.usuarioRepository = usuarioRepository;
        this.lector = lector;
        this.validador = validador;
        this.importador = importador;
        this.usuarioActual = usuarioActual;
        this.transaccion = new TransactionTemplate(transacciones);
    }

    @Override
    public Descarga plantilla(String formato) {
        return ArchivoTabular.generar("plantilla_inventario", formato, ColumnasCarga.TODAS, List.of(
                List.of("AMX00250", "Amoxicilina 250 mg", "MEDICAMENTO", "Antibiótico", "Caja x 10 tabletas",
                        "tableta", "SI", "INVIMA 2020M-0000000", "900", "1500", "0", "20", "L2309", "100",
                        LocalDate.now().toString(), LocalDate.now().plusYears(2).toString(), ""),
                List.of("JER00010", "Jeringa 10 ml", "INSUMO", "", "Unidad", "unidad", "NO", "", "600", "1000",
                        "19", "50", "J100", "200", LocalDate.now().toString(), "", "")));
    }

    @Override
    @Transactional
    public CargaRs previsualizar(MultipartFile archivo) {
        String nombre = nombreSeguro(archivo.getOriginalFilename());
        String formato = lector.formato(nombre);
        List<FilaArchivo> filas = lector.leer(formato, contenido(archivo));
        List<ValidadorCarga.Resultado> resultados = validador.validar(
                filas.stream().map(FilaArchivo::datos).toList());
        int validas = (int) resultados.stream().filter(ValidadorCarga.Resultado::valida).count();

        CargaInventario carga = new CargaInventario();
        carga.setNombreArchivo(nombre);
        carga.setFormato(formato);
        carga.setUsuario(usuarioRepository.getReferenceById(usuarioActual.obtener().id()));
        carga.setFechaCarga(LocalDateTime.now());
        carga.setEstado(EstadoCarga.PREVISUALIZADA);
        carga.setTotalFilas(filas.size());
        carga.setFilasValidas(validas);
        carga.setFilasConError(filas.size() - validas);
        carga = cargaRepository.save(carga);
        List<CargaInventarioFila> entidades = new ArrayList<>();
        for (int i = 0; i < filas.size(); i++) {
            ValidadorCarga.Resultado resultado = resultados.get(i);
            CargaInventarioFila fila = new CargaInventarioFila();
            fila.setCarga(carga);
            fila.setNumeroFila(filas.get(i).numero());
            fila.setDatosJson(filas.get(i).datos());
            fila.setValida(resultado.valida());
            fila.setAccion(resultado.accion());
            fila.setErroresJson(resultado.errores());
            entidades.add(fila);
        }
        filaRepository.saveAll(entidades);
        return convertir(carga, entidades);
    }

    @Override
    @Transactional(readOnly = true)
    public CargaRs obtener(Long cargaId) {
        return convertir(buscar(cargaId), filaRepository.findByCarga_IdOrderByNumeroFila(cargaId));
    }

    /** No es una sola transaccion a proposito: cada fila se confirma por separado. */
    @Override
    public CargaRs confirmar(Long cargaId) {
        Long usuarioId = usuarioActual.obtener().id();
        transaccion.executeWithoutResult(estado -> reservar(cargaId, usuarioId));
        for (Long filaId : filaRepository.pendientes(cargaId)) {
            importar(filaId, usuarioId);
        }
        transaccion.executeWithoutResult(estado -> cerrar(cargaId));
        return transaccion.execute(estado -> obtener(cargaId));
    }

    @Override
    @Transactional
    public CargaRs descartar(Long cargaId) {
        CargaInventario carga = bloquearPrevisualizada(cargaId);
        if (carga.getFechaConfirmacion() != null) {
            throw new ConflictException("La carga se está confirmando.");
        }
        carga.setEstado(EstadoCarga.DESCARTADA);
        cargaRepository.save(carga);
        return convertir(carga, filaRepository.findByCarga_IdOrderByNumeroFila(cargaId));
    }

    @Override
    @Transactional(readOnly = true)
    public Descarga errores(Long cargaId, String formato) {
        buscar(cargaId);
        List<String> encabezados = new ArrayList<>(List.of("fila", "campo", "valor", "motivo"));
        encabezados.addAll(ColumnasCarga.TODAS);
        List<List<String>> lineas = new ArrayList<>();
        for (CargaInventarioFila fila : filaRepository.findByCarga_IdOrderByNumeroFila(cargaId)) {
            for (ErrorFila error : fila.getErroresJson()) {
                lineas.add(linea(fila, error.campo(), error.mensaje()));
            }
            if (fila.getErrorImportacion() != null) {
                lineas.add(linea(fila, "", fila.getErrorImportacion()));
            }
        }
        return ArchivoTabular.generar("errores_carga_" + cargaId, formato, encabezados, lineas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargaRs> historial(LocalDate desde, LocalDate hasta) {
        RangoFechas rango = RangoFechas.de(desde, hasta);
        return cargaRepository.findByFechaCargaGreaterThanEqualAndFechaCargaLessThanOrderByFechaCargaDesc(
                rango.inicio(), rango.finExclusivo()).stream().map(c -> convertir(c, null)).toList();
    }

    private void reservar(Long cargaId, Long usuarioId) {
        CargaInventario carga = bloquearPrevisualizada(cargaId);
        LocalDateTime ahora = LocalDateTime.now();
        if (carga.getFechaConfirmacion() != null
                && carga.getFechaConfirmacion().isAfter(ahora.minusMinutes(MINUTOS_REANUDAR))) {
            throw new ConflictException("La carga ya se está confirmando.");
        }
        if (carga.getFilasValidas() == 0) {
            throw new ConflictException("La carga no tiene filas válidas para importar.");
        }
        carga.setFechaConfirmacion(ahora);
        carga.setUsuarioConfirmacion(usuarioRepository.getReferenceById(usuarioId));
        cargaRepository.save(carga);
    }

    private void importar(Long filaId, Long usuarioId) {
        try {
            importador.importar(filaId, usuarioId);
        } catch (ConflictException | BadRequestException | NotFoundException ex) {
            importador.marcarError(filaId, ex.getMessage());
        } catch (RuntimeException ex) {
            LOGGER.error("No se pudo importar la fila {}", filaId, ex);
            importador.marcarError(filaId, "No fue posible importar la fila.");
        }
    }

    private void cerrar(Long cargaId) {
        CargaInventario carga = cargaRepository.bloquear(cargaId).orElseThrow();
        int importadas = (int) filaRepository.countByCarga_IdAndImportadaTrue(cargaId);
        carga.setFilasImportadas(importadas);
        carga.setFechaConfirmacion(LocalDateTime.now());
        carga.setEstado(importadas == carga.getTotalFilas()
                ? EstadoCarga.CONFIRMADA : EstadoCarga.CONFIRMADA_CON_ERRORES);
        cargaRepository.save(carga);
    }

    private CargaInventario bloquearPrevisualizada(Long cargaId) {
        CargaInventario carga = cargaRepository.bloquear(cargaId)
                .orElseThrow(() -> new NotFoundException("La carga no existe."));
        if (carga.getEstado() != EstadoCarga.PREVISUALIZADA) {
            throw new ConflictException("La carga ya está en estado " + carga.getEstado() + ".");
        }
        return carga;
    }

    private CargaInventario buscar(Long cargaId) {
        return cargaRepository.findById(cargaId).orElseThrow(() -> new NotFoundException("La carga no existe."));
    }

    private CargaRs convertir(CargaInventario c, List<CargaInventarioFila> filas) {
        return new CargaRs(c.getId(), c.getNombreArchivo(), c.getFormato(), c.getEstado(), c.getTotalFilas(),
                c.getFilasValidas(), c.getFilasConError(), c.getFilasImportadas(),
                nombreUsuario(c.getUsuario()), c.getFechaCarga(),
                c.getUsuarioConfirmacion() == null ? null : nombreUsuario(c.getUsuarioConfirmacion()),
                c.getFechaConfirmacion(), filas == null ? null : convertir(filas));
    }

    private String nombreUsuario(Usuario usuario) {
        return usuarioRepository.findById(usuario.getId()).map(Usuario::getUsername).orElse(null);
    }

    private List<CargaRs.Fila> convertir(List<CargaInventarioFila> filas) {
        List<BigDecimal> iva = impuestoRepository.findByActivoTrueOrderByPorcentaje().stream()
                .map(Impuesto::getPorcentaje).toList();
        LocalDate hoy = LocalDate.now();
        return filas.stream().map(f -> new CargaRs.Fila(f.getNumeroFila(), f.getValida(), f.getAccion(),
                f.getDatosJson(), f.getErroresJson(),
                new ConvertidorFila(f.getDatosJson(), iva, hoy).advertencias(), f.getImportada(),
                f.getErrorImportacion())).toList();
    }

    private static List<String> linea(CargaInventarioFila fila, String campo, String motivo) {
        List<String> linea = new ArrayList<>(List.of(String.valueOf(fila.getNumeroFila()), campo,
                fila.getDatosJson().getOrDefault(campo, ""), motivo));
        ColumnasCarga.TODAS.forEach(c -> linea.add(fila.getDatosJson().getOrDefault(c, "")));
        return linea;
    }

    private static String nombreSeguro(String original) {
        String nombre = original == null ? "" : original.replaceAll("^.*[/\\\\]", "").trim();
        return nombre.length() > 255 ? nombre.substring(nombre.length() - 255) : nombre;
    }

    private static byte[] contenido(MultipartFile archivo) {
        try {
            return archivo.getBytes();
        } catch (IOException ex) {
            throw new BadRequestException("No fue posible leer el archivo.");
        }
    }
}
