package com.uniminuto.clinica.atencion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ClinicaDatos {
    private ClinicaDatos() {}
    public record Especialidad(Long id,String codigo,String nombre,String descripcion,Boolean activo) {}
    public record Servicio(long id,long especialidadId,String nombre) {}
    public record Tarifa(Long id,BigDecimal valor,BigDecimal iva,BigDecimal impuesto,LocalDate desde,
            LocalDate hasta,Long usuarioId,LocalDateTime fecha) {}
    public record CatalogoServicio(Servicio servicio,Tarifa tarifa) {}
    public record Paciente(long citaId,int medicoId,int mascotaId,int clienteId,String estado,
            LocalDateTime inicio,String medico,String registro,String mascota,String propietario) {}
    public record Antecedente(LocalDateTime fecha,String medico,String descripcion) {}
    public record Cabecera(long id,long citaId,long historiaId,long especialidadId,String especialidad,
            String motivo,String sintomas,String diagnostico,String observaciones,String recomendaciones,
            LocalDateTime creada,LocalDateTime actualizada,LocalDateTime finalizada,int version) {}
    public record ServicioSolicitado(Long servicioId,Integer cantidad) {}
    public record GuardarAtencion(Integer version,String motivo,String sintomas,String diagnostico,String observaciones,
            String recomendaciones,List<ServicioSolicitado> servicios) {}
    public record LineaServicio(long servicioId,String nombre,int cantidad, BigDecimal valor,BigDecimal iva,BigDecimal impuesto) {}
    public record Detalle(Long medicamentoId,String nombre,String dosis,String frecuencia,String duracion,
            Integer cantidad,String via,String indicaciones,BigDecimal precioEstimado) {}
    public record EdicionFormula(Integer revision,List<Detalle> medicamentos) {}
    public record Formula(long id,long citaId,String paciente,String estado,int revision,
            LocalDateTime creada,LocalDateTime confirmada,List<Detalle> medicamentos) {}
    public record Atencion(Paciente cita,Cabecera atencion,List<Antecedente> antecedentes,
            List<LineaServicio> servicios,List<CatalogoServicio> catalogo,List<Formula> formulas) {}
    public record Totales(BigDecimal servicios,BigDecimal medicamentos,BigDecimal subtotal,
            BigDecimal impuestos,BigDecimal total) {}
}
