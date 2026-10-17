-- Migración manual para los módulos del parcial.
-- Revisar contra SHOW CREATE TABLE de la instalación antes de aplicarla.
-- Esta ruta no se ejecuta automáticamente: el proyecto todavía no configura Flyway.

CREATE TABLE IF NOT EXISTS veterinario (
    veterinario_id BIGINT NOT NULL AUTO_INCREMENT,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    activo BOOLEAN NULL,
    PRIMARY KEY (veterinario_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cita (
    cita_id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    mascota_id INT NOT NULL,
    veterinario_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    motivo_consulta VARCHAR(500) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    PRIMARY KEY (cita_id),
    KEY idx_cita_fecha_hora (fecha, hora),
    KEY idx_cita_veterinario_fecha (veterinario_id, fecha),
    CONSTRAINT fk_cita_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(cliente_id),
    CONSTRAINT fk_cita_mascota FOREIGN KEY (mascota_id) REFERENCES mascota(mascota_id),
    CONSTRAINT fk_cita_veterinario FOREIGN KEY (veterinario_id) REFERENCES veterinario(veterinario_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS historia_clinica (
    historia_clinica_id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    mascota_id INT NOT NULL,
    cita_id BIGINT NULL,
    veterinario_id BIGINT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    motivo_consulta VARCHAR(500) NOT NULL,
    diagnostico TEXT NOT NULL,
    observaciones TEXT NULL,
    PRIMARY KEY (historia_clinica_id),
    UNIQUE KEY uk_historia_cita (cita_id),
    KEY idx_historia_fecha (fecha_hora),
    CONSTRAINT fk_historia_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(cliente_id),
    CONSTRAINT fk_historia_mascota FOREIGN KEY (mascota_id) REFERENCES mascota(mascota_id),
    CONSTRAINT fk_historia_cita FOREIGN KEY (cita_id) REFERENCES cita(cita_id),
    CONSTRAINT fk_historia_veterinario FOREIGN KEY (veterinario_id) REFERENCES veterinario(veterinario_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS anotacion (
    anotacion_id BIGINT NOT NULL AUTO_INCREMENT,
    historia_clinica_id BIGINT NOT NULL,
    veterinario_id BIGINT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    descripcion VARCHAR(1000) NOT NULL,
    observacion TEXT NULL,
    diagnostico_evolucion TEXT NULL,
    PRIMARY KEY (anotacion_id),
    KEY idx_anotacion_fecha (fecha_hora),
    CONSTRAINT fk_anotacion_historia FOREIGN KEY (historia_clinica_id) REFERENCES historia_clinica(historia_clinica_id),
    CONSTRAINT fk_anotacion_veterinario FOREIGN KEY (veterinario_id) REFERENCES veterinario(veterinario_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS formula_medica (
    formula_medica_id BIGINT NOT NULL AUTO_INCREMENT,
    mascota_id INT NOT NULL,
    historia_clinica_id BIGINT NULL,
    veterinario_id BIGINT NOT NULL,
    fecha_emision DATE NOT NULL,
    medicamento VARCHAR(200) NOT NULL,
    dosis DECIMAL(12,3) NOT NULL,
    frecuencia VARCHAR(120) NOT NULL,
    duracion_dias INT NOT NULL,
    indicaciones TEXT NULL,
    fecha_creacion DATETIME NOT NULL,
    PRIMARY KEY (formula_medica_id),
    KEY idx_formula_fecha (fecha_creacion),
    CONSTRAINT fk_formula_mascota FOREIGN KEY (mascota_id) REFERENCES mascota(mascota_id),
    CONSTRAINT fk_formula_historia FOREIGN KEY (historia_clinica_id) REFERENCES historia_clinica(historia_clinica_id),
    CONSTRAINT fk_formula_veterinario FOREIGN KEY (veterinario_id) REFERENCES veterinario(veterinario_id),
    CONSTRAINT chk_formula_dosis CHECK (dosis > 0),
    CONSTRAINT chk_formula_duracion CHECK (duracion_dias > 0)
) ENGINE=InnoDB;
