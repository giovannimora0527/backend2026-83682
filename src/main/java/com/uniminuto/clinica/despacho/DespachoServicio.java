package com.uniminuto.clinica.despacho;

import static com.uniminuto.clinica.calendario.AgendaServicio.*;
import static com.uniminuto.clinica.despacho.DespachoDatos.*;
import static com.uniminuto.clinica.despacho.DespachoDatos.Estado.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongFunction;

import com.uniminuto.clinica.despacho.DespachoRepositorio.Cabecera;
import com.uniminuto.clinica.despacho.DespachoRepositorio.Preparacion;
import com.uniminuto.clinica.inventario.InventarioDatos.Lote;
import com.uniminuto.clinica.inventario.InventarioServicio;
import com.uniminuto.clinica.service.IdentidadService.Rol;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
class DespachoServicio {
    private static final Comparator<Seleccion> ORDEN = Comparator.comparingLong(Seleccion::medicamento)
            .thenComparingLong(Seleccion::lote);
    private final DespachoRepositorio datos;
    private final InventarioServicio inventario;
    private final ObjectMapper json;

    DespachoServicio(DespachoRepositorio datos,InventarioServicio inventario,ObjectMapper json) {
        this.datos=datos;
        this.inventario=inventario;
        this.json=json;
    }

    List<Formula> buscar(Long id,String propietario,String mascota,LocalDate fecha,Estado estado,int pagina) {
        usuario();
        return datos.buscar(id,propietario,mascota,fecha,estado,pagina(pagina));
    }

    @Transactional(readOnly=true)
    Detalle consultar(long id) {
        usuario();
        Formula f=datos.formula(id,false);
        var medicamentos=datos.prescripciones(f).stream().map(p->{
            var lotes=inventario.lotes(p.medicamento(),true).stream().map(LoteDisponible::de).toList();
            return new Medicamento(p,lotes,lotes.stream().mapToLong(LoteDisponible::cantidad).sum());
        }).toList();
        Plan preparacion=f.estado()==EN_PREPARACION ? datos.ultima(id).map(this::plan).orElse(null) : null;
        return new Detalle(f,medicamentos,preparacion,datos.entregas(id,null,null,null,0).stream().map(this::entrega).toList(),
                datos.historial(id));
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Plan preparar(long id,Preparar solicitud) {
        long usuario=usuario();
        if(solicitud==null || solicitud.clave()==null) throw invalido("Indica una clave de preparación.");
        var seleccion=normalizar(solicitud.lotes());
        String clave=solicitud.clave().toString();
        Formula f=datos.formula(id,true);
        var anterior=datos.porClave(clave).orElse(null);
        if(anterior!=null) {
            if(anterior.formula()!=id || anterior.usuario()!=usuario || !seleccion.equals(leerPlan(anterior))) {
                throw conflicto("La clave de preparación ya se utilizó.");
            }
            return plan(anterior);
        }
        abierta(f);
        verificar(f,seleccion,p->inventario.lotes(p,true));
        datos.cambiar(f,EN_PREPARACION,usuario,ahora(),null);
        try {
            long entrega=datos.preparar(f,usuario,clave,json.writeValueAsString(seleccion),ahora());
            return new Plan(entrega,seleccion,null);
        } catch(DuplicateKeyException e) {
            throw conflicto("La clave de preparación ya se utilizó.");
        }
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Entrega confirmar(long id) {
        long usuario=usuario();
        var previa=datos.entrega(id);
        Formula f=datos.formula(previa.formula(),true);
        var e=datos.entrega(id);
        if(e.confirmada()!=null) return consultarEntrega(id);
        if(f.estado()!=EN_PREPARACION || datos.ultima(f.id()).orElseThrow().id()!=id) {
            throw conflicto("La preparación cambió. Actualiza la fórmula antes de confirmar.");
        }
        var seleccion=leerPlan(e);
        var pendientes=verificar(f,seleccion,inventario::lotesBloqueadosParaDespacho);
        for(var lote:seleccion) {
            long movimiento=inventario.movimientoDespacho(lote.medicamento(),lote.lote(),lote.cantidad(),
                    usuario,f.id(),id,false).id();
            datos.detalle(e,lote,movimiento);
        }
        Estado estado=pendientes.isEmpty()?DESPACHADO:DESPACHO_PARCIAL;
        datos.confirmar(id,usuario,ahora(),estado,json.writeValueAsString(pendientes));
        datos.cambiar(f,estado,usuario,ahora(),null);
        return consultarEntrega(id);
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Entrega revertir(long id,Motivo solicitud) {
        long usuario=usuario();
        var previa=datos.entrega(id);
        var f=datos.formula(previa.formula(),true);
        var e=datos.entrega(id);
        if(e.confirmada()==null) throw conflicto("La entrega aún no se ha confirmado.");
        if(e.revertida()!=null) return consultarEntrega(id);
        datos.verificarReversionFinanciera(id);
        String motivo=motivo(solicitud);
        for(var linea:datos.lineas(id)) {
            long movimiento=inventario.movimientoDespacho(linea.medicamento(),linea.lote(),linea.cantidad(),
                    usuario,f.id(),id,true).id();
            datos.revertirLinea(id,linea.lote(),movimiento);
        }
        datos.revertir(id,usuario,ahora(),motivo);
        boolean parcial=datos.prescripciones(f).stream().anyMatch(p->p.entregada()>0);
        datos.cambiar(f,parcial?DESPACHO_PARCIAL:PENDIENTE_DESPACHO,usuario,ahora(),motivo);
        return consultarEntrega(id);
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Detalle cambiar(long id,Estado nuevo,Motivo solicitud) {
        long usuario=usuario();
        var f=datos.formula(id,true);
        abierta(f);
        String motivo=motivo(solicitud);
        var prescripciones=datos.prescripciones(f);
        if(nuevo==CANCELADO && prescripciones.stream().anyMatch(p->p.entregada()>0)) {
            throw conflicto("Revierte las entregas antes de cancelar el despacho.");
        }
        if(nuevo==PENDIENTE_DISPONIBILIDAD && prescripciones.stream().noneMatch(p->
                inventario.lotes(p.medicamento(),true).stream().mapToLong(Lote::cantidad).sum()<p.pendiente())) {
            throw conflicto("Hay existencias para completar la cantidad pendiente.");
        }
        datos.cambiar(f,nuevo,usuario,ahora(),motivo);
        return consultar(id);
    }

    List<Entrega> historial(LocalDate desde,LocalDate hasta,int pagina) {
        usuario();
        if(desde!=null && hasta!=null && desde.isAfter(hasta)) throw invalido("Revisa el rango de fechas.");
        if(hasta!=null && hasta.getYear()>9998) throw invalido("Fecha final inválida.");
        return datos.entregas(null,null,desde,hasta==null?null:hasta.plusDays(1),pagina(pagina)).stream()
                .map(this::entrega).toList();
    }

    Entrega consultarEntrega(long id) {
        usuario();
        return datos.entregas(null,id,null,null,0).stream().findFirst().map(this::entrega)
                .orElseThrow(()->noExiste("Entrega confirmada"));
    }

    Formula origen(Entrega e) { return datos.formula(e.formula(),false); }

    private Entrega entrega(Cabecera c) {
        List<String> pendiente=json.readValue(c.pendiente(),new TypeReference<List<String>>(){});
        return new Entrega(c.id(),c.formula(),c.responsable(),c.fecha(),c.estado(),c.revertida(),c.reversor(),c.motivo(),
                datos.lineas(c.id()),pendiente);
    }

    private Plan plan(Preparacion p) {
        return new Plan(p.id(),leerPlan(p),p.confirmada()==null?null:p.usuario());
    }

    private List<Seleccion> leerPlan(Preparacion p) {
        return json.readValue(p.plan(),new TypeReference<List<Seleccion>>(){});
    }

    private static List<Seleccion> normalizar(List<Seleccion> lotes) {
        if(lotes==null || lotes.isEmpty() || lotes.size()>500) throw invalido("Selecciona entre 1 y 500 lotes.");
        Set<Long> ids=new HashSet<>();
        for(var l:lotes) {
            if(l==null || l.medicamento()<=0 || l.lote()<=0 || l.cantidad()<=0 || !ids.add(l.lote())) {
                throw invalido("Selecciona lotes distintos y cantidades enteras positivas.");
            }
        }
        return lotes.stream().sorted(ORDEN).toList();
    }

    private List<String> verificar(Formula f,List<Seleccion> seleccion,LongFunction<List<Lote>> lotes) {
        Map<Long,List<Seleccion>> porMedicamento=new java.util.TreeMap<>();
        for(var s:seleccion) porMedicamento.computeIfAbsent(s.medicamento(),key->new ArrayList<>()).add(s);
        List<String> pendientes=new ArrayList<>();
        for(var p:datos.prescripciones(f)) {
            var actual=porMedicamento.remove(p.medicamento());
            long cantidad=actual==null?0:actual.stream().mapToLong(Seleccion::cantidad).sum();
            if(cantidad>p.pendiente()) throw conflicto("La cantidad de "+p.nombre()+" supera lo pendiente.");
            if(actual!=null) {
                var esperado=asignar(p.medicamento(),cantidad,lotes.apply(p.medicamento()));
                if(!actual.equals(esperado)) {
                    throw conflicto("Los lotes de "+p.nombre()+" cambiaron o incumplen FEFO. Actualiza la selección.");
                }
            }
            if(cantidad<p.pendiente()) pendientes.add(p.nombre()+": "+(p.pendiente()-cantidad));
        }
        if(!porMedicamento.isEmpty()) throw conflicto("El medicamento no pertenece a la fórmula.");
        return pendientes;
    }

    static List<Seleccion> asignar(long medicamento,long unidades,List<Lote> lotes) {
        List<Seleccion> seleccion=new ArrayList<>();
        for(var l:lotes) {
            if(unidades==0) break;
            if(!l.disponible()) continue;
            int cantidad=(int)Math.min(unidades,l.cantidad());
            seleccion.add(new Seleccion(medicamento,l.id(),cantidad));
            unidades-=cantidad;
        }
        if(unidades!=0) throw conflicto("No hay suficientes existencias en lotes vigentes.");
        return seleccion.stream().sorted(ORDEN).toList();
    }

    private static void abierta(Formula f) {
        if(f.estado()==DESPACHADO || f.estado()==CANCELADO) throw conflicto("El despacho está cerrado.");
    }

    private static String motivo(Motivo solicitud) {
        String motivo=solicitud==null || solicitud.motivo()==null?"":solicitud.motivo().strip();
        if(motivo.isEmpty() || motivo.length()>255) throw invalido("Indica un motivo de hasta 255 caracteres.");
        return motivo;
    }

    private static int pagina(int pagina) {
        if(pagina<0 || pagina>10000) throw invalido("Página inválida.");
        return pagina;
    }

    private static long usuario() {
        var identidad=identidad();
        if(identidad.rol()!=Rol.DESPACHO_MEDICO) throw prohibido("Esta operación requiere el perfil Despacho Médico.");
        return identidad.usuarioId();
    }
}
