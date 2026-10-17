-- Migración manual independiente para el esquema REAL observado en MySQL.
-- NO ejecutar sin respaldo y revisión del esquema en el entorno destino.
-- No copia ni modifica filas: fecha_emision queda NULL en fórmulas antiguas porque
-- la fecha de emisión original nunca fue persistida. La API conserva su significado
-- previo devolviendo fecha_creacion_registro como fecha de presentación para esas filas.
--
-- Precondiciones verificadas solo por lectura en la base local al preparar este archivo:
-- formula_medica contiene 5 filas; no hay citas/medicamentos huérfanos;
-- historia_medica contiene 1 fila sin mascota huérfana. Revalidar antes de aplicar.
-- Se encontró una cita histórica cuyo cliente difiere del propietario de la mascota;
-- este script conserva ambos datos y no intenta corregir esa relación.

ALTER TABLE formula_medica
    MODIFY COLUMN cita_id BIGINT UNSIGNED NULL,
    MODIFY COLUMN medicamento_id BIGINT UNSIGNED NOT NULL,
    ADD COLUMN historia_medica_id BIGINT UNSIGNED NULL AFTER cita_id,
    ADD COLUMN medico_id BIGINT UNSIGNED NULL AFTER historia_medica_id,
    ADD COLUMN fecha_emision DATE NULL AFTER indicaciones,
    ADD KEY idx_formula_historia_medica (historia_medica_id),
    ADD KEY idx_formula_medico (medico_id),
    ADD CONSTRAINT fk_formula_cita
        FOREIGN KEY (cita_id) REFERENCES cita (id),
    ADD CONSTRAINT fk_formula_historia_medica
        FOREIGN KEY (historia_medica_id) REFERENCES historia_medica (id),
    ADD CONSTRAINT fk_formula_medico
        FOREIGN KEY (medico_id) REFERENCES medico (id),
    ADD CONSTRAINT fk_formula_medicamento
        FOREIGN KEY (medicamento_id) REFERENCES medicamento (id),
    ADD CONSTRAINT chk_formula_asociacion_clinica
        CHECK (cita_id IS NOT NULL OR historia_medica_id IS NOT NULL);
