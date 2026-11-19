package com.uniminuto.clinica.calendario;

import com.uniminuto.clinica.calendario.AgendaDatos.Evento;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class AgendaExcel {
    private static final List<String> TITULOS=List.of("Fecha","Inicio","Finalización","Médico","Mascota","Propietario","Motivo","Estado");

    byte[] generar(List<Evento> citas) throws IOException {
        try (var libro=new XSSFWorkbook();var salida=new ByteArrayOutputStream()) {
            var hoja=libro.createSheet("Citas médicas");
            var cabecera=hoja.createRow(0);
            var estilo=libro.createCellStyle();
            var fuente=libro.createFont();
            fuente.setBold(true);
            estilo.setFont(fuente);
            var fecha=libro.createCellStyle();
            fecha.setDataFormat(libro.createDataFormat().getFormat("dd/mm/yyyy"));
            var hora=libro.createCellStyle();
            hora.setDataFormat(libro.createDataFormat().getFormat("hh:mm"));
            for (int i=0;i<TITULOS.size();i++) {
                var celda=cabecera.createCell(i);
                celda.setCellValue(TITULOS.get(i));
                celda.setCellStyle(estilo);
                hoja.setColumnWidth(i,(i==6 ? 52 : i>=3 && i<=5 ? 26 : 16)*256);
            }
            for (Evento cita:citas) {
                var fila=hoja.createRow(hoja.getLastRowNum()+1);
                var dia=fila.createCell(0);
                dia.setCellValue(cita.inicio().toLocalDate());
                dia.setCellStyle(fecha);
                var inicio=fila.createCell(1);
                inicio.setCellValue(cita.inicio().toLocalTime().toNanoOfDay()/86_400_000_000_000d);
                inicio.setCellStyle(hora);
                var fin=fila.createCell(2);
                fin.setCellValue(cita.fin().toLocalTime().toNanoOfDay()/86_400_000_000_000d);
                fin.setCellStyle(hora);
                fila.createCell(3).setCellValue(cita.medico());
                fila.createCell(4).setCellValue(cita.mascota());
                fila.createCell(5).setCellValue(cita.propietario());
                String motivo=cita.motivo();
                fila.createCell(6).setCellValue(motivo==null ? "" : motivo.substring(0,Math.min(32767,motivo.length())));
                fila.createCell(7).setCellValue(cita.estado());
            }
            hoja.createFreezePane(0,1);
            hoja.setAutoFilter(new CellRangeAddress(0,hoja.getLastRowNum(),0,TITULOS.size()-1));
            libro.write(salida);
            return salida.toByteArray();
        }
    }
}
