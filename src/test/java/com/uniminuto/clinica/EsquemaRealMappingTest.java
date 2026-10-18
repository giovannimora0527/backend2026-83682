package com.uniminuto.clinica;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Garantiza que ninguna entidad JPA mapee tablas o columnas que no existen en la base MySQL
 * real "clinica". La lista corresponde al esquema existente (SHOW CREATE TABLE, 2026-10-17);
 * si una entidad nueva necesita otra columna, esta prueba falla en lugar de fallar en producción.
 */
@SpringBootTest
@ActiveProfiles("test")
class EsquemaRealMappingTest {

    private static final Map<String, Set<String>> ESQUEMA_REAL = Map.ofEntries(
            Map.entry("anotacion_historia", Set.of("id", "historia_id", "medico_id", "fecha", "descripcion")),
            Map.entry("cita", Set.of("id", "cliente_id", "mascota_id", "medico_id", "fecha_hora", "estado", "motivo")),
            Map.entry("cliente", Set.of("cliente_id", "usuario_id", "tipo_documento", "numero_documento", "nombres",
                    "apellidos", "fecha_nacimiento", "genero", "telefono", "direccion", "activo")),
            Map.entry("especializacion", Set.of("id", "nombre", "descripcion", "codigo_especializacion")),
            Map.entry("formula_medica", Set.of("id", "cita_id", "medicamento_id", "dosis", "indicaciones",
                    "fecha_creacion_registro", "fecha_actualizacion_registro")),
            Map.entry("historia_medica", Set.of("id", "paciente_id", "fecha_creacion")),
            Map.entry("mascota", Set.of("mascota_id", "nombre_mascota", "edad", "raza_id", "fecha_registro",
                    "fecha_modificacion", "cliente_id")),
            Map.entry("medicamento", Set.of("id", "nombre", "descripcion", "presentacion", "fecha_compra", "fecha_vence",
                    "fecha_creacion_registro", "fecha_modificacion_registro")),
            Map.entry("medico", Set.of("id", "tipo_documento", "numero_documento", "nombres", "apellidos", "telefono",
                    "registro_profesional", "especializacion_id")),
            Map.entry("raza", Set.of("raza_id", "nombre", "especie", "fecha_creacion", "fecha_modificacion")),
            Map.entry("session", Set.of("session_id", "user_id", "token", "fecha_ini_sesion", "fecha_expiracion")),
            Map.entry("usuario", Set.of("id", "username", "password_hash", "rol", "fecha_creacion", "activo", "email")));

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void todasLasEntidadesUsanSoloTablasYColumnasExistentes() {
        List<String> errores = new ArrayList<>();
        Set<String> tablasMapeadas = new TreeSet<>();
        var metamodel = entityManagerFactory.unwrap(SessionFactoryImplementor.class).getMappingMetamodel();
        metamodel.forEachEntityDescriptor(persister -> {
            String entidad = persister.getEntityName();
            persister.getIdentifierMapping().forEachSelectable((i, columna) ->
                    revisar(entidad, columna.getContainingTableExpression(), columna.getSelectionExpression(),
                            errores, tablasMapeadas));
            persister.forEachSelectable((i, columna) ->
                    revisar(entidad, columna.getContainingTableExpression(), columna.getSelectionExpression(),
                            errores, tablasMapeadas));
        });
        assertTrue(errores.isEmpty(), "Mapeos fuera del esquema real:\n" + String.join("\n", errores));
        assertTrue(tablasMapeadas.containsAll(Set.of("cita", "cliente", "formula_medica", "historia_medica",
                "anotacion_historia", "mascota", "medico", "raza", "usuario")), "Tablas mapeadas: " + tablasMapeadas);
    }

    private void revisar(String entidad, String tabla, String columna, List<String> errores, Set<String> tablas) {
        String t = tabla.toLowerCase(Locale.ROOT).replace("`", "").replace("\"", "");
        String c = columna.toLowerCase(Locale.ROOT).replace("`", "").replace("\"", "");
        tablas.add(t);
        Set<String> columnas = ESQUEMA_REAL.get(t);
        if (columnas == null) errores.add(entidad + ": la tabla '" + t + "' no existe");
        else if (!columnas.contains(c)) errores.add(entidad + ": la columna '" + t + "." + c + "' no existe");
    }
}
