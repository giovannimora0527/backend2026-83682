-- MySQL Docker, una sola ejecución en modo batch sin --force.
USE clinica;
CREATE TEMPORARY TABLE v004_precondicion (ok TINYINT NOT NULL CHECK (ok=1));
INSERT INTO v004_precondicion VALUES (
  (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE()
    AND table_name IN ('cita','medico','especializacion','historia_medica','medicamento','usuario')
    AND column_name='id' AND column_type='bigint')=6
  AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE()
    AND table_name='especializacion' AND column_name='activo')
  AND NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE()
    AND table_name IN ('servicio_veterinario','tarifa_veterinaria','atencion_veterinaria','atencion_servicio',
    'atencion_historial','formula_clinica','formula_clinica_detalle'))
);
ALTER TABLE especializacion ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
CREATE TABLE servicio_veterinario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, especializacion_id BIGINT NOT NULL, nombre VARCHAR(100) NOT NULL,
    UNIQUE KEY uq_servicio_especialidad (especializacion_id,nombre),
    FOREIGN KEY (especializacion_id) REFERENCES especializacion(id)
) ENGINE=InnoDB;
CREATE TABLE tarifa_veterinaria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, servicio_id BIGINT NOT NULL,
    valor DECIMAL(14,2) NOT NULL, iva DECIMAL(5,2) NOT NULL, impuesto DECIMAL(5,2) NOT NULL,
    desde DATE NOT NULL, hasta DATE NULL, usuario_id BIGINT NOT NULL, fecha DATETIME(6) NOT NULL,
    UNIQUE KEY uq_tarifa_vigencia (servicio_id,desde),
    CHECK (valor>=0 AND iva BETWEEN 0 AND 100 AND impuesto BETWEEN 0 AND 100 AND iva+impuesto<=100),
    CHECK (hasta IS NULL OR hasta>=desde),
    FOREIGN KEY (servicio_id) REFERENCES servicio_veterinario(id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;
CREATE TABLE atencion_veterinaria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, cita_id BIGINT NOT NULL UNIQUE, medico_id BIGINT NOT NULL,
    historia_id BIGINT NOT NULL, especializacion_id BIGINT NOT NULL, especialidad VARCHAR(100) NOT NULL,
    motivo TEXT, sintomas TEXT, diagnostico TEXT, observaciones TEXT, recomendaciones TEXT,
    creada DATETIME(6) NOT NULL, actualizada DATETIME(6) NOT NULL, finalizada DATETIME(6) NULL,
    version INT NOT NULL DEFAULT 0,
    FOREIGN KEY (cita_id) REFERENCES cita(id), FOREIGN KEY (medico_id) REFERENCES medico(id),
    FOREIGN KEY (historia_id) REFERENCES historia_medica(id),
    FOREIGN KEY (especializacion_id) REFERENCES especializacion(id)
) ENGINE=InnoDB;
CREATE TABLE atencion_servicio (
    atencion_id BIGINT NOT NULL, servicio_id BIGINT NOT NULL, tarifa_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL, cantidad INT NOT NULL, activo BOOLEAN NOT NULL,
    valor DECIMAL(14,2) NOT NULL, iva DECIMAL(5,2) NOT NULL, impuesto DECIMAL(5,2) NOT NULL,
    PRIMARY KEY(atencion_id,servicio_id), CHECK (cantidad BETWEEN 1 AND 100),
    FOREIGN KEY (atencion_id) REFERENCES atencion_veterinaria(id),
    FOREIGN KEY (servicio_id) REFERENCES servicio_veterinario(id),
    FOREIGN KEY (tarifa_id) REFERENCES tarifa_veterinaria(id)
) ENGINE=InnoDB;
CREATE TABLE atencion_historial (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, atencion_id BIGINT NOT NULL, usuario_id BIGINT NOT NULL,
    fecha DATETIME(6) NOT NULL, accion VARCHAR(30) NOT NULL, detalle JSON NOT NULL,
    FOREIGN KEY (atencion_id) REFERENCES atencion_veterinaria(id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    INDEX ix_atencion_historial(atencion_id,id)
) ENGINE=InnoDB;
-- Las fórmulas monomedicamento anteriores conservan tabla, identificadores y endpoints.
CREATE TABLE formula_clinica (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, atencion_id BIGINT NOT NULL,
    estado VARCHAR(16) NOT NULL, revision INT NOT NULL, creada DATETIME(6) NOT NULL, confirmada DATETIME(6),
    CHECK (estado IN ('BORRADOR','CONFIRMADA','ANULADA') AND revision>=1),
    FOREIGN KEY (atencion_id) REFERENCES atencion_veterinaria(id),
    INDEX ix_formula_atencion(atencion_id,id)
) ENGINE=InnoDB;
CREATE TABLE formula_clinica_detalle (
    formula_id BIGINT NOT NULL, revision INT NOT NULL, medicamento_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL, dosis VARCHAR(100) NOT NULL, frecuencia VARCHAR(100) NOT NULL,
    duracion VARCHAR(100) NOT NULL, cantidad INT NOT NULL, via VARCHAR(100) NOT NULL,
    indicaciones VARCHAR(500), precio_estimado DECIMAL(14,2) NOT NULL,
    PRIMARY KEY(formula_id,revision,medicamento_id), CHECK(cantidad>0 AND precio_estimado>=0),
    FOREIGN KEY (formula_id) REFERENCES formula_clinica(id),
    FOREIGN KEY (medicamento_id) REFERENCES medicamento(id)
) ENGINE=InnoDB;
