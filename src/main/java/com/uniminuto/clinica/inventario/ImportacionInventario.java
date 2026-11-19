package com.uniminuto.clinica.inventario;

import static com.uniminuto.clinica.inventario.InventarioDatos.*;

import java.text.Normalizer;
import java.util.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ImportacionInventario {
    private final InventarioRepositorio datos;
    private final InventarioServicio inventario;
    private final ArchivoInventario lector;
    private final JsonMapper json;

    public ImportacionInventario(InventarioRepositorio datos, InventarioServicio inventario,
            ArchivoInventario lector, JsonMapper json) {
        this.datos=datos;
        this.inventario=inventario;
        this.lector=lector;
        this.json=json;
    }

    @Transactional
    public Importacion previsualizar(MultipartFile archivo,long usuario) {
        var filas=detectarDuplicados(lector.leer(archivo));
        String nombre=archivo.getOriginalFilename().replace('\\','/');
        nombre=InventarioServicio.texto(nombre.substring(nombre.lastIndexOf('/')+1),200,true,"archivo");
        long id=datos.guardarCarga(usuario,nombre,json.writeValueAsString(filas),rechazadas(filas),InventarioServicio.ahora());
        return detalle(id);
    }

    private List<Fila> detectarDuplicados(List<Fila> filas) {
        var codigos=new HashSet<String>();
        var nombres=new HashSet<String>();
        return filas.stream().map(fila -> {
            if (!fila.errores().isEmpty()) return fila;
            Producto p=fila.producto();
            String nombre=Normalizer.normalize(p.nombre(),Normalizer.Form.NFD).replaceAll("\\p{M}","")
                    .toLowerCase(Locale.ROOT);
            if (codigos.contains(p.codigo()) || nombres.contains(nombre) || datos.duplicado(p)) {
                return error(fila,"El código o nombre ya existe en el archivo o en el catálogo.");
            }
            codigos.add(p.codigo());
            nombres.add(nombre);
            return fila;
        }).toList();
    }

    public List<Resumen> historial() { return datos.cargas(); }

    public Importacion detalle(long id) { return convertir(datos.carga(id,false)); }

    @Transactional
    public Importacion confirmar(long id,long usuario) {
        Importacion carga=convertir(datos.carga(id,true));
        if (carga.resumen().usuarioId()!=usuario) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Solo el administrador que preparó la carga puede confirmarla.");
        }
        if (carga.resumen().fechaConfirmacion()!=null) return carga;
        List<Fila> resultado=new ArrayList<>();
        int importados=0;
        for (Fila fila:carga.filas()) {
            if (!fila.errores().isEmpty()) {
                resultado.add(fila);
                continue;
            }
            try {
                var lote=fila.lote();
                if (lote!=null) lote=new RegistrarLote(lote.numero(),lote.costo(),lote.fechaIngreso(),
                        lote.fechaVence(),lote.cantidad(),"IMPORTACION:"+id+":"+fila.numero());
                inventario.importar(fila.producto(),lote,usuario);
                importados++;
                resultado.add(fila);
            } catch (DuplicateKeyException duplicado) {
                // El primer INSERT de esta fila chocó con el índice único. No se creó su lote.
                resultado.add(error(fila,"El código o nombre fue registrado después de la vista previa."));
            }
        }
        datos.confirmarCarga(id,json.writeValueAsString(resultado),importados,rechazadas(resultado),InventarioServicio.ahora());
        return detalle(id);
    }

    private Importacion convertir(InventarioRepositorio.Carga carga) {
        return new Importacion(carga.resumen(),json.readValue(carga.filas(),new TypeReference<List<Fila>>() {}));
    }

    private int rechazadas(List<Fila> filas) { return (int)filas.stream().filter(f->!f.errores().isEmpty()).count(); }

    private Fila error(Fila fila,String mensaje) {
        return new Fila(fila.numero(),fila.producto(),fila.lote(),List.of(mensaje));
    }
}
