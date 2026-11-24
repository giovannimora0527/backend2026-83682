package com.uniminuto.clinica.carga;

import com.uniminuto.clinica.entity.ErrorFila;
import com.uniminuto.clinica.entity.Impuesto;
import com.uniminuto.clinica.entity.Medicamento;
import com.uniminuto.clinica.entity.enums.AccionCarga;
import com.uniminuto.clinica.entity.enums.TipoProducto;
import com.uniminuto.clinica.repository.ImpuestoRepository;
import com.uniminuto.clinica.repository.LoteMedicamentoRepository;
import com.uniminuto.clinica.repository.MedicamentoRepository;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Valida todas las filas: cada una por si sola, contra el resto del archivo y contra la base de datos. */
@Component
public class ValidadorCarga {

    private final MedicamentoRepository medicamentoRepository;
    private final LoteMedicamentoRepository loteRepository;
    private final ImpuestoRepository impuestoRepository;

    public ValidadorCarga(MedicamentoRepository medicamentoRepository, LoteMedicamentoRepository loteRepository,
                          ImpuestoRepository impuestoRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.impuestoRepository = impuestoRepository;
    }

    public record Resultado(DatosFila datos, AccionCarga accion, List<ErrorFila> errores,
                            List<ErrorFila> advertencias) {

        public boolean valida() {
            return errores.isEmpty();
        }
    }

    public List<Resultado> validar(List<Map<String, String>> filas) {
        List<BigDecimal> iva = impuestoRepository.findByActivoTrueOrderByPorcentaje().stream()
                .map(Impuesto::getPorcentaje).toList();
        LocalDate hoy = LocalDate.now();
        List<ConvertidorFila> convertidas = filas.stream().map(f -> new ConvertidorFila(f, iva, hoy)).toList();

        Set<String> codigos = new HashSet<>(Set.of(""));
        Set<String> nombres = new HashSet<>(Set.of(""));
        convertidas.forEach(c -> {
            if (c.resultado().codigo() != null) {
                codigos.add(c.resultado().codigo());
            }
            if (c.resultado().nombre() != null) {
                nombres.add(clave(c.resultado().nombre()));
            }
        });
        Map<String, Medicamento> porCodigo = new HashMap<>();
        Map<String, Medicamento> porNombre = new HashMap<>();
        medicamentoRepository.buscarPorCodigosONombres(codigos, nombres).forEach(m -> {
            porCodigo.put(m.getCodigo(), m);
            porNombre.put(clave(m.getNombre()), m);
        });
        Set<String> lotesBase = new HashSet<>();
        loteRepository.buscarPorCodigos(codigos).forEach(l ->
                lotesBase.add(l.getMedicamento().getCodigo() + "|" + clave(l.getNumeroLote())));

        Set<String> lotesArchivo = new HashSet<>();
        Map<String, DatosFila> productosArchivo = new HashMap<>();
        Map<String, String> codigoPorNombre = new HashMap<>();
        List<Resultado> resultados = new ArrayList<>();
        for (ConvertidorFila fila : convertidas) {
            DatosFila d = fila.resultado();
            AccionCarga accion = null;
            if (d.codigo() != null && d.numeroLote() != null) {
                String lote = d.codigo() + "|" + clave(d.numeroLote());
                if (!lotesArchivo.add(lote)) {
                    fila.error("numero_lote", "El código y el lote están repetidos en el archivo");
                } else if (lotesBase.contains(lote)) {
                    fila.error("numero_lote", "El lote ya existe para ese código");
                }
            }
            if (d.codigo() != null && d.nombre() != null) {
                Medicamento existente = porCodigo.get(d.codigo());
                Medicamento mismoNombre = porNombre.get(clave(d.nombre()));
                DatosFila previa = productosArchivo.get(d.codigo());
                String otroCodigo = codigoPorNombre.get(clave(d.nombre()));
                if (existente != null) {
                    comparar(fila, existente.getNombre(), existente.getTipoProducto(), "registrado");
                    if (!Boolean.TRUE.equals(existente.getActivo())) {
                        fila.error("codigo", "El producto está inactivo");
                    }
                } else if (mismoNombre != null) {
                    fila.error("nombre", "Ya existe con el código " + mismoNombre.getCodigo());
                } else if (previa != null) {
                    comparar(fila, previa.nombre(), previa.tipo(), "usado en otra fila");
                } else if (otroCodigo != null && !otroCodigo.equals(d.codigo())) {
                    fila.error("nombre", "Está en otra fila con el código " + otroCodigo);
                }
                accion = existente != null || previa != null ? AccionCarga.NUEVO_LOTE : AccionCarga.NUEVO_PRODUCTO;
                if (fila.errores().isEmpty()) {
                    productosArchivo.putIfAbsent(d.codigo(), d);
                    codigoPorNombre.putIfAbsent(clave(d.nombre()), d.codigo());
                }
            }
            resultados.add(new Resultado(d, fila.errores().isEmpty() ? accion : null, fila.errores(),
                    fila.advertencias()));
        }
        return resultados;
    }

    private void comparar(ConvertidorFila fila, String nombre, TipoProducto tipo, String origen) {
        if (!clave(nombre).equals(clave(fila.resultado().nombre()))) {
            fila.error("codigo", "Está " + origen + " con el nombre " + nombre);
        }
        if (fila.resultado().tipo() != null && fila.resultado().tipo() != tipo) {
            fila.error("tipo_producto", "El producto " + origen + " es " + tipo);
        }
    }

    private static String clave(String valor) {
        return valor.trim().toLowerCase(Locale.ROOT);
    }
}
