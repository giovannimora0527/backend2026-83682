package com.uniminuto.clinica.serviceimpl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Reversible, versioned storage format for clinical fields in descripcion TEXT. */
final class HistoriaClinicaNarrativeCodec {
    private static final String MARKER = "[[CLINICA_HISTORIA_V1]]\n";
    private static final String NULL_VALUE = "~";

    private HistoriaClinicaNarrativeCodec() { }

    /** Stores three URL-safe Base64 UTF-8 fields, one per line; '~' represents null. */
    static String encode(String motivoConsulta, String diagnostico, String observaciones) {
        return MARKER + encodeField(motivoConsulta) + "\n" + encodeField(diagnostico)
                + "\n" + encodeField(observaciones);
    }

    /** Plain legacy descriptions remain whole and are never parsed as the versioned format. */
    static Narrative decode(String storedDescription) {
        if (storedDescription == null || !storedDescription.startsWith(MARKER)) {
            return Narrative.legacy(storedDescription);
        }
        String[] fields = storedDescription.substring(MARKER.length()).split("\\n", -1);
        if (fields.length != 3) return Narrative.legacy(storedDescription);
        try {
            String motivo = decodeField(fields[0]);
            String diagnostico = decodeField(fields[1]);
            if (motivo == null || diagnostico == null) return Narrative.legacy(storedDescription);
            return new Narrative(motivo, diagnostico, decodeField(fields[2]), true);
        } catch (IllegalArgumentException ex) {
            // Preserve malformed tagged values verbatim instead of dropping their content.
            return Narrative.legacy(storedDescription);
        }
    }

    /** Keeps the existing annotation API human-readable while the database stores encoded fields. */
    static String display(String storedDescription) {
        Narrative narrative = decode(storedDescription);
        if (!narrative.structured()) return storedDescription;
        StringBuilder display = new StringBuilder("Motivo: ").append(narrative.motivoConsulta())
                .append("\nDiagnostico: ").append(narrative.diagnostico());
        if (narrative.observaciones() != null) {
            display.append("\nObservaciones: ").append(narrative.observaciones());
        }
        return display.toString();
    }

    private static String encodeField(String value) {
        if (value == null) return NULL_VALUE;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeField(String value) {
        if (NULL_VALUE.equals(value)) return null;
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    record Narrative(String motivoConsulta, String diagnostico, String observaciones, boolean structured) {
        private static Narrative legacy(String value) {
            return new Narrative(value, null, null, false);
        }
    }
}
