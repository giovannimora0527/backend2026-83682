package com.uniminuto.clinica.atencion;

import static com.uniminuto.clinica.atencion.ClinicaDatos.*;
import static com.uniminuto.clinica.atencion.TarifasServicio.texto;
import static com.uniminuto.clinica.calendario.AgendaServicio.*;

import com.uniminuto.clinica.inventario.InventarioDatos.Ficha;
import com.uniminuto.clinica.inventario.InventarioServicio;
import com.uniminuto.clinica.repository.CitaRepository;
import com.uniminuto.clinica.service.AlcanceMedico;
import com.uniminuto.clinica.service.CitaService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
class AtencionServicio {
    private final ClinicaRepositorio repo;
    private final CitaRepository citas;
    private final CitaService agenda;
    private final AlcanceMedico alcance;
    private final InventarioServicio inventario;
    private final JsonMapper json;

    AtencionServicio(ClinicaRepositorio repo,CitaRepository citas,CitaService agenda,AlcanceMedico alcance,
                     InventarioServicio inventario,JsonMapper json) {
        this.repo=repo; this.citas=citas; this.agenda=agenda; this.alcance=alcance; this.inventario=inventario; this.json=json;
    }

    private int medico() {
        Integer id=alcance.medicoId();
        if(id==null) throw prohibido("Esta operación requiere un médico vinculado.");
        return id;
    }

    private Paciente paciente(long id,boolean bloquear) {
        int profesional=medico();
        if(bloquear) citas.bloquearPorId(id).orElseThrow(()->noExiste("Cita"));
        Paciente p=repo.paciente(id);
        if(p.medicoId()!=profesional) throw prohibido("La cita no está asignada a este médico.");
        return p;
    }

    @Transactional(readOnly=true)
    Atencion consultar(long id) {
        Paciente p=paciente(id,false);
        var cab=repo.atencion(id).orElse(null);
        var lineas=cab==null ? List.<LineaServicio>of() : repo.lineas(cab.id());
        var catalogo=cab==null || cab.finalizada()!=null || !repo.especialidad(cab.especialidadId(),false).activo()
                ? List.<CatalogoServicio>of() : repo.servicios(cab.especialidadId()).stream()
                    .map(s->new CatalogoServicio(s,repo.tarifaVigente(s.id(),p.inicio().toLocalDate()).orElse(null)))
                    .toList();
        return new Atencion(p,cab,repo.antecedentes(p.mascotaId(),id),lineas,catalogo,repo.formulas(id,p.medicoId()));
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Atencion abrir(long id) {
        Paciente p=paciente(id,true);
        if(repo.atencion(id).isPresent()) return consultar(id);
        if(Set.of("cancelada","finalizada").contains(p.estado())) throw conflicto("Esta cita no admite una nueva atención.");
        Long esp=repo.uno("SELECT especializacion_id FROM medico WHERE id=? AND activo=1",
                (r,i)->r.getLong(1),"Médico activo",p.medicoId());
        var especialidad=repo.especialidad(esp,false);
        long historia=repo.historia(p.mascotaId(),ahora());
        var instante=ahora();
        long clave=repo.agregar("""
                INSERT INTO atencion_veterinaria(cita_id,medico_id,historia_id,especializacion_id,especialidad,motivo,creada,actualizada)
                SELECT id,medico_id,?,?,?,motivo,?,? FROM cita WHERE id=?
                """,historia,esp,especialidad.nombre(),instante,instante,id);
        auditar(clave,"APERTURA",Map.of("historiaId",historia));
        return consultar(id);
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Atencion guardar(long id,GuardarAtencion form) {
        Paciente p=paciente(id,true);
        Cabecera c=editable(id,p);
        if(form==null || !Objects.equals(form.version(),c.version())) throw conflicto("La atención cambió. Recarga antes de guardar.");
        String motivo=texto(form.motivo(),5000,false,"motivo");
        String sintomas=texto(form.sintomas(),5000,false,"síntomas");
        String diagnostico=texto(form.diagnostico(),5000,false,"diagnóstico");
        String observaciones=texto(form.observaciones(),5000,false,"observaciones");
        String recomendaciones=texto(form.recomendaciones(),5000,false,"recomendaciones");
        if(form.servicios()==null || form.servicios().size()>50) throw invalido("Indica hasta 50 servicios.");
        var ids=new HashSet<Long>();
        var antes=repo.lineas(c.id());
        repo.ejecutar("UPDATE atencion_servicio SET activo=0 WHERE atencion_id=? AND activo=1",c.id());
        for(var item:form.servicios()) {
            if(item==null || item.servicioId()==null || item.servicioId()<=0 || item.cantidad()==null
                    || item.cantidad()<1 || item.cantidad()>100 || !ids.add(item.servicioId())) {
                throw invalido("Servicio repetido o cantidad fuera de 1 a 100.");
            }
            if(repo.restaurarLinea(c.id(),item.servicioId(),item.cantidad())) continue;
            registrarServicio(c,item,p.inicio().toLocalDate());
        }
        var instante=ahora();
        repo.ejecutar("""
                UPDATE atencion_veterinaria SET motivo=?,sintomas=?,diagnostico=?,observaciones=?,recomendaciones=?,
                actualizada=?,version=version+1 WHERE id=?
                """,motivo,sintomas,diagnostico,observaciones,recomendaciones,instante,c.id());
        var actual=consultar(id);
        auditar(c.id(),"EDICION",Map.of("anterior",c,"serviciosAnteriores",antes,
                "actual",actual.atencion(),"servicios",actual.servicios()));
        return actual;
    }

    private void registrarServicio(Cabecera c,ServicioSolicitado item,LocalDate fecha) {
        var servicio=repo.servicio(item.servicioId(),false);
        if(servicio.especialidadId()!=c.especialidadId() || !repo.especialidad(c.especialidadId(),false).activo()) {
            throw invalido("El servicio no corresponde a una especialidad activa de esta atención.");
        }
        var t=repo.tarifaVigente(servicio.id(),fecha)
                .orElseThrow(()->conflicto("El servicio "+servicio.nombre()+" no tiene tarifa vigente en la fecha de la cita."));
        repo.ejecutar("""
                INSERT INTO atencion_servicio(atencion_id,servicio_id,tarifa_id,nombre,cantidad,activo,valor,iva,impuesto)
                VALUES(?,?,?,?,?,1,?,?,?)
                """,c.id(),servicio.id(),t.id(),servicio.nombre(),item.cantidad(),t.valor(),t.iva(),t.impuesto());
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Atencion finalizar(long id,int version) {
        Paciente p=paciente(id,true);
        Cabecera c=editable(id,p);
        if(version!=c.version()) throw conflicto("La atención cambió. Recarga antes de finalizar.");
        texto(c.motivo(),5000,true,"motivo"); texto(c.sintomas(),5000,true,"síntomas");
        texto(c.diagnostico(),5000,true,"diagnóstico");
        if(repo.lineas(c.id()).isEmpty()) throw invalido("Agrega al menos un servicio realizado.");
        if(repo.formulas(id,p.medicoId()).stream().anyMatch(f->f.estado().equals("BORRADOR"))) {
            throw conflicto("Confirma o anula las fórmulas en borrador antes de finalizar.");
        }
        var instante=ahora();
        repo.ejecutar("""
                UPDATE atencion_veterinaria SET finalizada=?,actualizada=?,version=version+1 WHERE id=?
                """,instante,instante,c.id());
        if(!p.estado().equals("atendida")) agenda.cambiarAgenda(id,new com.uniminuto.clinica.calendario.AgendaDatos.Cambio("atendida",null,null));
        agenda.cambiarAgenda(id,new com.uniminuto.clinica.calendario.AgendaDatos.Cambio("finalizada",null,null));
        auditar(c.id(),"FINALIZACION",Map.of("version",version+1,"fecha",instante));
        return consultar(id);
    }

    List<Formula> formulas() { return repo.formulas(null,medico()); }

    Formula formula(long id) {
        paciente(repo.citaDeFormula(id),false);
        return repo.formula(id);
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Formula guardarFormula(long cita,Long formulaId,EdicionFormula form) {
        Paciente p=paciente(cita,true);
        Cabecera c=editable(cita,p);
        if(form==null || form.medicamentos()==null || form.medicamentos().isEmpty() || form.medicamentos().size()>50) {
            throw invalido("Agrega de 1 a 50 medicamentos a la fórmula.");
        }
        int revision=1;
        long id;
        if(formulaId==null) {
            id=repo.agregar("INSERT INTO formula_clinica(atencion_id,estado,revision,creada) VALUES(?,'BORRADOR',1,?)",
                    c.id(),ahora());
        } else {
            Formula existente=repo.formula(formulaId);
            if(existente.citaId()!=cita) throw prohibido("La fórmula no pertenece a esta cita.");
            verificarBorrador(existente,form.revision());
            id=formulaId;
            revision=existente.revision()+1;
            repo.ejecutar("UPDATE formula_clinica SET revision=? WHERE id=?",revision,id);
        }
        var catalogo=catalogoMedicamentos();
        var ids=new HashSet<Long>();
        for(Detalle d:form.medicamentos()) {
            if(d==null || d.medicamentoId()==null || !ids.add(d.medicamentoId()) || d.cantidad()==null || d.cantidad()<1) {
                throw invalido("Cada medicamento requiere cantidad positiva y aparece una sola vez.");
            }
            Ficha ficha=catalogo.get(d.medicamentoId());
            if(ficha==null || !ficha.producto().activo()
                    || ficha.producto().tipo()!=com.uniminuto.clinica.inventario.InventarioDatos.TipoProducto.MEDICAMENTO) {
                throw invalido("Selecciona un medicamento activo.");
            }
            repo.ejecutar("""
                    INSERT INTO formula_clinica_detalle(formula_id,revision,medicamento_id,nombre,dosis,frecuencia,duracion,
                        cantidad,via,indicaciones,precio_estimado) VALUES(?,?,?,?,?,?,?,?,?,?,?)
                    """,id,revision,d.medicamentoId(),ficha.producto().nombre(),texto(d.dosis(),100,true,"dosis"),
                    texto(d.frecuencia(),100,true,"frecuencia"),texto(d.duracion(),100,true,"duración"),d.cantidad(),
                    texto(d.via(),100,true,"vía"),texto(d.indicaciones(),500,false,"indicaciones"),ficha.producto().precioVenta());
        }
        auditar(c.id(),"FORMULA_BORRADOR",Map.of("formulaId",id,"revision",revision));
        return repo.formula(id);
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    Formula resolverFormula(long id,int revision,boolean confirmar) {
        medico();
        long cita=repo.citaDeFormula(id);
        Paciente p=paciente(cita,true);
        Cabecera c=editable(cita,p);
        Formula f=repo.formula(id);
        verificarBorrador(f,revision);
        if(confirmar) {
            var catalogo=catalogoMedicamentos();
            for(Detalle d:f.medicamentos()) {
                Ficha ficha=catalogo.get(d.medicamentoId());
                if(ficha==null || ficha.disponible()<d.cantidad()) {
                    throw conflicto("Disponibilidad insuficiente para "+d.nombre()+".");
                }
                repo.ejecutar("""
                        UPDATE formula_clinica_detalle SET precio_estimado=?
                        WHERE formula_id=? AND revision=? AND medicamento_id=?
                        """,ficha.producto().precioVenta(),id,revision,d.medicamentoId());
            }
        }
        repo.ejecutar("UPDATE formula_clinica SET estado=?,confirmada=? WHERE id=?",
                confirmar?"CONFIRMADA":"ANULADA",confirmar?ahora():null,id);
        auditar(c.id(),confirmar?"FORMULA_CONFIRMADA":"FORMULA_ANULADA",Map.of("formulaId",id,"revision",revision));
        return repo.formula(id);
    }

    private static void verificarBorrador(Formula f,Integer revision) {
        if(!f.estado().equals("BORRADOR") || !Objects.equals(f.revision(),revision)) {
            throw conflicto("La fórmula cambió o dejó de ser un borrador.");
        }
    }

    private Map<Long,Ficha> catalogoMedicamentos() {
        return inventario.catalogo().stream().collect(Collectors.toMap(Ficha::id,Function.identity()));
    }

    private Cabecera editable(long id,Paciente p) {
        Cabecera c=repo.atencion(id).orElseThrow(()->noExiste("Atención"));
        if(c.finalizada()!=null || Set.of("cancelada","finalizada").contains(p.estado())) throw conflicto("La atención está cerrada.");
        return c;
    }

    private void auditar(long id,String accion,Object datos) {
        repo.ejecutar("INSERT INTO atencion_historial(atencion_id,usuario_id,fecha,accion,detalle) VALUES(?,?,?,?,?)",
                id,identidad().usuarioId(),ahora(),accion,json.writeValueAsString(datos));
    }

    static Totales totales(Atencion a) {
        BigDecimal servicios=BigDecimal.ZERO, medicamentos=BigDecimal.ZERO, impuestos=BigDecimal.ZERO;
        for(var item:a.servicios()) {
            BigDecimal base=item.valor().multiply(BigDecimal.valueOf(item.cantidad()));
            servicios=servicios.add(base);
            impuestos=impuestos.add(base.multiply(item.iva()).movePointLeft(2).setScale(2,RoundingMode.HALF_UP))
                    .add(base.multiply(item.impuesto()).movePointLeft(2).setScale(2,RoundingMode.HALF_UP));
        }
        for(var f:a.formulas()) if(f.estado().equals("CONFIRMADA")) {
            for(var d:f.medicamentos()) medicamentos=medicamentos.add(d.precioEstimado().multiply(BigDecimal.valueOf(d.cantidad())));
        }
        BigDecimal subtotal=servicios.add(medicamentos);
        return new Totales(servicios,medicamentos,subtotal,impuestos,subtotal.add(impuestos));
    }
}
