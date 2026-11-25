package com.uniminuto.clinica.cita;

import com.uniminuto.clinica.exception.ConflictException;
import com.uniminuto.clinica.seguridad.SesionUsuario;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DisponibilidadTest {

    private static LocalDateTime h(String hora) {
        return LocalDateTime.parse("2026-11-24T" + hora);
    }

    @Test
    void restaLasCitasOcupadasDeLaFranja() {
        List<LocalDateTime[]> libres = CitaService.restar(h("08:00"), h("12:00"), List.of(
                new LocalDateTime[]{h("09:00"), h("09:30")}, new LocalDateTime[]{h("10:00"), h("11:00")}));
        assertEquals(3, libres.size());
        assertEquals(h("08:00"), libres.get(0)[0]);
        assertEquals(h("09:00"), libres.get(0)[1]);
        assertEquals(h("09:30"), libres.get(1)[0]);
        assertEquals(h("11:00"), libres.get(2)[0]);
        assertEquals(h("12:00"), libres.get(2)[1]);
    }

    @Test
    void manejaIntervalosSuperpuestosYFueraDeLaFranja() {
        List<LocalDateTime[]> libres = CitaService.restar(h("08:00"), h("12:00"), List.of(
                new LocalDateTime[]{h("07:00"), h("08:30")}, new LocalDateTime[]{h("08:15"), h("09:00")},
                new LocalDateTime[]{h("13:00"), h("14:00")}));
        assertEquals(1, libres.size());
        assertEquals(h("09:00"), libres.get(0)[0]);
    }

    @Test
    void transicionesDeEstadoPermitidas() {
        SesionUsuario recepcion = new SesionUsuario(1, 3, "recepcion", 2, "SECRETARIA", null, java.util.Set.of("CITA_GESTIONAR"));
        SesionUsuario admin = new SesionUsuario(1, 2, "admin", 1, "ADMINISTRADOR", null, java.util.Set.of("CITA_GESTIONAR"));
        assertDoesNotThrow(() -> CitaService.validarTransicion("pendiente", "confirmada", recepcion));
        assertDoesNotThrow(() -> CitaService.validarTransicion("confirmada", "cancelada", recepcion));
        assertDoesNotThrow(() -> CitaService.validarTransicion("atendida", "finalizada", recepcion));
        assertDoesNotThrow(() -> CitaService.validarTransicion("confirmada", "atendida", admin));
        assertThrows(ConflictException.class, () -> CitaService.validarTransicion("confirmada", "atendida", recepcion));
        assertThrows(ConflictException.class, () -> CitaService.validarTransicion("cancelada", "pendiente", admin));
        assertThrows(ConflictException.class, () -> CitaService.validarTransicion("finalizada", "atendida", admin));
    }
}
