package com.uniminuto.clinica.inventario;

import static com.uniminuto.clinica.inventario.InventarioDatos.*;
import static com.uniminuto.clinica.inventario.InventarioServicio.invalido;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.*;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.ZipInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
class ArchivoInventario {
    static final List<String> COLUMNAS=List.of("codigo","nombre","descripcion","presentacion","tipo","invima",
            "precioCompra","precioVenta","stockMinimo","fechaCompra","fechaIngreso","fechaProximoPedido",
            "fechaVence","activo","numeroLote","cantidad");
    private static final int LIMITE=2000;
    private static final int BYTES=2*1024*1024;

    static {
        ZipSecureFile.setMaxEntrySize(12L*1024*1024);
        ZipSecureFile.setMaxFileCount(128);
    }

    List<Fila> leer(MultipartFile archivo) {
        String nombre=archivo.getOriginalFilename();
        if (nombre==null || archivo.isEmpty() || archivo.getSize()>BYTES) {
            throw invalido("Selecciona un archivo de hasta 2 MiB.");
        }
        nombre=nombre.toLowerCase(Locale.ROOT);
        try (var entrada=archivo.getInputStream()) {
            List<Fila> filas;
            if (nombre.endsWith(".xlsx")) filas=xlsx(entrada.readAllBytes());
            else if (nombre.endsWith(".csv")) filas=csv(entrada);
            else throw invalido("Solo se admiten archivos .xlsx o .csv.");
            if (filas.isEmpty()) throw invalido("El archivo no contiene registros.");
            return filas;
        } catch (org.springframework.web.server.ResponseStatusException error) {
            throw error;
        } catch (Exception error) {
            throw invalido("El archivo no se pudo leer. Verifica el formato, la plantilla y sus límites.");
        }
    }

    private List<Fila> csv(InputStream entrada) throws IOException {
        var decoder=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT);
        try (var lector=new BufferedReader(new InputStreamReader(entrada,decoder))) {
            lector.mark(1);
            if (lector.read()!=0xFEFF) lector.reset();
            try (var csv=CSVFormat.DEFAULT.builder().setIgnoreEmptyLines(true).get().parse(lector)) {
                var registros=csv.iterator();
                if (!registros.hasNext()) throw invalido("Faltan los encabezados.");
                validarColumnas(registros.next().toList());
                var filas=new ArrayList<Fila>();
                while (registros.hasNext()) {
                    var registro=registros.next();
                    agregar(filas,Math.toIntExact(registro.getRecordNumber()),registro.toList());
                }
                return filas;
            }
        }
    }

    private List<Fila> xlsx(byte[] archivo) throws Exception {
        comprobarPaquete(archivo);
        try (var libro=new XSSFWorkbook(new ByteArrayInputStream(archivo))) {
            if (libro.getNumberOfSheets()!=1 || libro.isMacroEnabled()
                    || !libro.getExternalLinksTable().isEmpty()) {
                throw invalido("Usa una sola hoja XLSX sin macros ni vínculos externos.");
            }
            var hoja=libro.getSheetAt(0);
            if (hoja.getLastRowNum()>LIMITE) throw invalido("El máximo es de 2000 registros.");
            var formato=new DataFormatter(Locale.ROOT);
            validarColumnas(celdas(hoja.getRow(0),formato,true));
            var filas=new ArrayList<Fila>();
            for (int i=1;i<=hoja.getLastRowNum();i++) {
                var fila=hoja.getRow(i);
                if (fila==null) continue;
                boolean noPermitida=false;
                for (var celda:fila) {
                    if (celda.getCellType()==CellType.FORMULA || celda.getHyperlink()!=null) noPermitida=true;
                }
                if (noPermitida) {
                    filas.add(new Fila(i+1,null,null,List.of("La fila contiene fórmulas o vínculos.")));
                } else if (!celdas(fila,formato,false).stream().allMatch(String::isBlank)) {
                    agregar(filas,i+1,celdas(fila,formato,false));
                }
            }
            return filas;
        }
    }

    private void comprobarPaquete(byte[] archivo) throws IOException {
        try (var zip=new ZipInputStream(new ByteArrayInputStream(archivo))) {
            int entradas=0;
            long bytes=0;
            byte[] bloque=new byte[8192];
            var nombres=new HashSet<String>();
            for (var entrada=zip.getNextEntry(); entrada!=null; entrada=zip.getNextEntry()) {
                String nombre=entrada.getName().toLowerCase(Locale.ROOT);
                if (++entradas>128 || !nombres.add(nombre) || nombre.contains("vbaproject")
                        || nombre.contains("/embeddings/") || nombre.contains("/externallinks/")) {
                    throw invalido("El XLSX tiene componentes que no se admiten.");
                }
                for (int n; (n=zip.read(bloque))!=-1;) {
                    bytes+=n;
                    if (bytes>24L*1024*1024) throw invalido("El XLSX supera el tamaño descomprimido permitido.");
                }
            }
        }
    }

    private List<String> celdas(Row fila, DataFormatter formato, boolean encabezado) {
        if (fila==null) return List.of();
        int ancho=encabezado ? fila.getLastCellNum() : Math.max(COLUMNAS.size(),fila.getLastCellNum());
        if (ancho>COLUMNAS.size()) throw invalido("La hoja contiene columnas adicionales.");
        var valores=new ArrayList<String>();
        for (int i=0;i<ancho;i++) {
            Cell c=fila.getCell(i);
            if (c==null) valores.add("");
            else if (c.getCellType()==CellType.FORMULA || c.getHyperlink()!=null) {
                throw invalido("Los encabezados no admiten fórmulas ni vínculos.");
            } else if (c.getCellType()==CellType.NUMERIC && DateUtil.isCellDateFormatted(c)) {
                valores.add(c.getLocalDateTimeCellValue().toLocalDate().toString());
            } else if (c.getCellType()==CellType.NUMERIC && List.of(6,7,8,15).contains(i)) {
                valores.add(BigDecimal.valueOf(c.getNumericCellValue()).stripTrailingZeros().toPlainString());
            } else valores.add(formato.formatCellValue(c));
        }
        return valores;
    }

    private void validarColumnas(List<String> columnas) {
        if (!COLUMNAS.equals(columnas.stream().map(String::strip).toList())) {
            throw invalido("Encabezados esperados: "+String.join(", ",COLUMNAS));
        }
    }

    private void agregar(List<Fila> filas,int numero,List<String> datos) {
        if (filas.size()>=LIMITE) throw invalido("El máximo es de 2000 registros.");
        if (datos.size()!=COLUMNAS.size()) {
            filas.add(new Fila(numero,null,null,List.of("Cantidad incorrecta de columnas.")));
            return;
        }
        var v=datos.stream().map(String::strip).toList();
        Producto producto=null;
        try {
            boolean activo=switch(v.get(13).toLowerCase(Locale.ROOT)) {
                case "", "true", "1", "sí", "si" -> true;
                case "false", "0", "no" -> false;
                default -> throw invalido("activo debe ser verdadero o falso (true/false).");
            };
            producto=InventarioServicio.validar(new Producto(v.get(0),v.get(1),v.get(2),v.get(3),
                    TipoProducto.valueOf(v.get(4).toUpperCase(Locale.ROOT)),v.get(5),decimal(v.get(6)),decimal(v.get(7)),
                    Integer.valueOf(v.get(8)),fecha(v.get(9)),fecha(v.get(10)),fecha(v.get(11)),fecha(v.get(12)),activo));
            int cantidad=v.get(15).isEmpty() ? 0 : Integer.parseInt(v.get(15));
            if (cantidad<0 || (cantidad>0 && (v.get(14).isEmpty() || !activo))) {
                throw invalido("La cantidad debe ser no negativa. El saldo inicial exige lote y producto activo.");
            }
            if (!activo && !v.get(14).isEmpty()) throw invalido("El producto inactivo no puede tener lote inicial.");
            var lote=v.get(14).isEmpty() ? null : InventarioServicio.validarLote(new RegistrarLote(
                    v.get(14),producto.precioCompra(),producto.fechaIngreso(),producto.fechaVence(),cantidad,null));
            filas.add(new Fila(numero,producto,lote,List.of()));
        } catch (org.springframework.web.server.ResponseStatusException error) {
            filas.add(new Fila(numero,producto,null,List.of(error.getReason())));
        } catch (IllegalArgumentException | java.time.DateTimeException error) {
            filas.add(new Fila(numero,producto,null,List.of("Verifica tipo, fechas ISO (AAAA-MM-DD), decimales con punto y cantidades enteras.")));
        }
    }

    private BigDecimal decimal(String valor) {
        if (!valor.matches("[+-]?[0-9]{1,12}(\\.[0-9]{1,2})?")) throw invalido("Precio inválido.");
        return new BigDecimal(valor);
    }
    private LocalDate fecha(String valor) { return valor.isEmpty() ? null : LocalDate.parse(valor); }
}
