package com.uniminuto.clinica.atencion;

import static com.uniminuto.clinica.atencion.ClinicaDatos.*;
import static com.uniminuto.clinica.calendario.AgendaServicio.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
class TarifasServicio {
    private final ClinicaRepositorio repo;

    TarifasServicio(ClinicaRepositorio repo) { this.repo=repo; }

    List<Especialidad> especialidades(){ return repo.especialidades(); }

    @Transactional
    Especialidad guardar(Long id,Especialidad formulario) {
        String codigo=texto(formulario.codigo(),10,true,"código").toUpperCase(Locale.ROOT);
        String nombre=texto(formulario.nombre(),100,true,"nombre");
        String descripcion=texto(formulario.descripcion(),4000,false,"descripción");
        boolean activo=!Boolean.FALSE.equals(formulario.activo());
        try {
            if (id==null) {
                id=repo.agregar("INSERT INTO especializacion(codigo_especializacion,nombre,descripcion,activo) VALUES(?,?,?,?)",
                        codigo,nombre,descripcion,activo);
            } else {
                repo.especialidad(id,true);
                repo.ejecutar("UPDATE especializacion SET codigo_especializacion=?,nombre=?,descripcion=?,activo=? WHERE id=?",
                        codigo,nombre,descripcion,activo,id);
            }
        } catch(DuplicateKeyException e) {
            throw conflicto("Ya existe una especialidad con ese código o nombre.");
        }
        return repo.especialidad(id,false);
    }

    List<Servicio> servicios(long id) { repo.especialidad(id,false); return repo.servicios(id); }

    @Transactional
    Servicio agregarServicio(long especialidad,String nombre) {
        if (!repo.especialidad(especialidad,true).activo()) throw conflicto("La especialidad está inactiva.");
        try {
            long id=repo.agregar("INSERT INTO servicio_veterinario(especializacion_id,nombre) VALUES(?,?)",
                    especialidad,texto(nombre,100,true,"servicio"));
            return repo.servicio(id,false);
        } catch(DuplicateKeyException e) {
            throw conflicto("Ya existe ese servicio en la especialidad.");
        }
    }

    List<Tarifa> historial(long id) { repo.servicio(id,false); return repo.tarifas(id); }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Tarifa agregarTarifa(long servicio,Tarifa t) {
        Servicio item=repo.servicio(servicio,true);
        if (!repo.especialidad(item.especialidadId(),false).activo()) throw conflicto("La especialidad está inactiva.");
        validar(t);
        LocalDate fecha=ahora().toLocalDate();
        var anteriores=repo.tarifas(servicio);
        if (!anteriores.isEmpty()) {
            Tarifa ultima=anteriores.getFirst();
            if (!t.desde().isAfter(ultima.desde()) || t.desde().isBefore(fecha)) {
                throw conflicto("La nueva tarifa debe empezar después de la última fecha de inicio y desde hoy en adelante.");
            }
            if (ultima.hasta()==null || !ultima.hasta().isBefore(t.desde())) {
                repo.ejecutar("UPDATE tarifa_veterinaria SET hasta=? WHERE id=?",t.desde().minusDays(1),ultima.id());
            }
        }
        long id=repo.agregar("""
                INSERT INTO tarifa_veterinaria(servicio_id,valor,iva,impuesto,desde,hasta,usuario_id,fecha)
                VALUES(?,?,?,?,?,?,?,?)
                """,servicio,t.valor(),t.iva(),t.impuesto(),t.desde(),t.hasta(),identidad().usuarioId(),ahora());
        return repo.tarifas(servicio).stream().filter(tarifa->tarifa.id()==id).findFirst().orElseThrow();
    }

    static void validar(Tarifa t) {
        if (t==null || t.desde()==null || t.desde().getYear()<1000 || t.desde().getYear()>9999
                || t.hasta()!=null && (t.hasta().isBefore(t.desde()) || t.hasta().getYear()>9999)) {
            throw invalido("La vigencia no es válida.");
        }
        decimal(t.valor(),new BigDecimal("999999999999.99"),"valor");
        decimal(t.iva(),BigDecimal.valueOf(100),"IVA");
        decimal(t.impuesto(),BigDecimal.valueOf(100),"impuesto");
        if (t.iva().add(t.impuesto()).compareTo(BigDecimal.valueOf(100))>0) throw invalido("Los impuestos superan el 100 %.");
    }

    private static void decimal(BigDecimal n,BigDecimal max,String campo) {
        if(n==null || n.signum()<0 || n.compareTo(max)>0 || n.stripTrailingZeros().scale()>2) {
            throw invalido("Valor inválido para "+campo+". Usa un importe no negativo de hasta dos decimales.");
        }
    }

    static String texto(String valor,int limite,boolean requerido,String campo) {
        String limpio=valor==null ? "" : valor.strip();
        if (limpio.length()>limite || requerido && limpio.isBlank()) throw invalido("Campo inválido: "+campo+".");
        return limpio.isBlank() ? null : limpio;
    }
}
