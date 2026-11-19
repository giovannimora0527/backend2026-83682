-- Aplicar una sola vez. Ejecutar en modo batch sin --force.
USE clinica;
CREATE TEMPORARY TABLE v003_precondicion (ok TINYINT NOT NULL CHECK (ok = 1));
INSERT INTO v003_precondicion VALUES (
    (SELECT COUNT(*) FROM information_schema.columns
      WHERE table_schema=DATABASE() AND table_name IN ('medico','usuario','cita')
        AND column_name='id' AND column_type='bigint') = 3
    AND NOT EXISTS (SELECT 1 FROM information_schema.columns
      WHERE table_schema=DATABASE() AND ((table_name='cita' AND column_name='fecha_fin')
      OR (table_name='medico' AND column_name='activo')))
    AND NOT EXISTS (SELECT 1 FROM information_schema.tables
      WHERE table_schema=DATABASE() AND table_name IN ('medico_horario','medico_ausencia','cita_historial'))
);
ALTER TABLE medico ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
-- Las citas previas conservan sus fechas. Si fecha_fin es NULL, la API usa 30 minutos.
ALTER TABLE cita
    ADD COLUMN fecha_fin DATETIME(6) NULL,
    ADD CONSTRAINT ck_cita_intervalo CHECK (fecha_fin IS NULL OR fecha_fin > fecha_hora),
    ADD INDEX ix_cita_medico_inicio (medico_id, fecha_hora),
    ADD INDEX ix_cita_mascota_inicio (mascota_id, fecha_hora);

CREATE TABLE medico_horario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medico_id BIGINT NOT NULL,
    dia TINYINT NOT NULL,
    inicio TIME NOT NULL,
    fin TIME NOT NULL,
    CONSTRAINT fk_horario_medico FOREIGN KEY (medico_id) REFERENCES medico(id),
    CONSTRAINT ck_horario_dia CHECK (dia BETWEEN 1 AND 7),
    CONSTRAINT ck_horario_intervalo CHECK (inicio >= '00:00:00' AND fin < '24:00:00' AND fin > inicio),
    UNIQUE KEY uq_medico_franja (medico_id,dia,inicio)
) ENGINE=InnoDB;

CREATE TABLE medico_ausencia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medico_id BIGINT NOT NULL,
    inicio DATETIME(6) NOT NULL,
    fin DATETIME(6) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    CONSTRAINT fk_ausencia_medico FOREIGN KEY (medico_id) REFERENCES medico(id),
    CONSTRAINT ck_ausencia_intervalo CHECK (fin > inicio),
    INDEX ix_ausencia_medico_inicio (medico_id,inicio)
) ENGINE=InnoDB;

CREATE TABLE cita_historial (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cita_id BIGINT NOT NULL,
    usuario_id BIGINT NULL,
    fecha DATETIME(6) NOT NULL,
    anterior JSON NULL,
    actual JSON NOT NULL,
    CONSTRAINT fk_historial_cita FOREIGN KEY (cita_id) REFERENCES cita(id),
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    INDEX ix_historial_cita (cita_id,id)
) ENGINE=InnoDB;
