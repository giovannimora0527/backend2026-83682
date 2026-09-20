-- =====================================================================
-- Script de creacion de las tablas usadas en el Primer Parcial.
-- Base de datos: clinica
--
-- IMPORTANTE: este script refleja el modelo asumido por las entidades
-- JPA de este proyecto. Si el docente entrega un script oficial con
-- otros nombres de tabla o de columna, ese script manda y hay que
-- ajustar las entidades (@Table y @Column) para que coincidan.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS clinica
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE clinica;

-- ---------------------------------------------------------------------
-- Requerimiento 1: inventario de formulas medicas.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS formula_medica (
    formula_medica_id BIGINT       NOT NULL AUTO_INCREMENT,
    descripcion       VARCHAR(255) NOT NULL,
    fecha_creacion    DATETIME     NOT NULL,
    PRIMARY KEY (formula_medica_id),
    KEY idx_formula_medica_fecha_creacion (fecha_creacion)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Requerimientos 2, 3 y 4: citas de la clinica.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cita (
    cita_id    BIGINT       NOT NULL AUTO_INCREMENT,
    fecha_cita DATETIME     NOT NULL,
    motivo     VARCHAR(255) NULL,
    PRIMARY KEY (cita_id),
    KEY idx_cita_fecha_cita (fecha_cita)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Requerimiento 5: historia medica y sus anotaciones.
-- Relacion 1:N -> una historia medica tiene muchas anotaciones.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS historia_medica (
    historia_medica_id  BIGINT       NOT NULL AUTO_INCREMENT,
    descripcion_general VARCHAR(255) NULL,
    PRIMARY KEY (historia_medica_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS anotacion_historia (
    anotacion_historia_id BIGINT       NOT NULL AUTO_INCREMENT,
    historia_medica_id    BIGINT       NOT NULL,
    fecha_anotacion       DATETIME     NOT NULL,
    detalle               VARCHAR(255) NOT NULL,
    PRIMARY KEY (anotacion_historia_id),
    KEY idx_anotacion_fecha (fecha_anotacion),
    CONSTRAINT fk_anotacion_historia_medica
        FOREIGN KEY (historia_medica_id)
        REFERENCES historia_medica (historia_medica_id)
) ENGINE = InnoDB;

-- =====================================================================
-- Datos de prueba minimos para poder demostrar los servicios.
-- =====================================================================

INSERT INTO formula_medica (descripcion, fecha_creacion) VALUES
    ('Amoxicilina 250mg cada 12 horas por 7 dias', '2026-01-10 08:30:00'),
    ('Antiparasitario oral dosis unica',           '2026-02-14 10:15:00'),
    ('Meloxicam 1.5mg/ml, 0.1ml/kg por 3 dias',    '2026-03-02 16:45:00'),
    ('Shampoo medicado dos veces por semana',      '2026-03-20 09:00:00');

INSERT INTO cita (fecha_cita, motivo) VALUES
    ('2026-01-05 09:00:00', 'Vacunacion anual'),
    ('2026-02-11 14:30:00', 'Control post operatorio'),
    ('2026-03-08 11:00:00', 'Consulta por cojera'),
    ('2026-04-01 08:00:00', 'Limpieza dental');

INSERT INTO historia_medica (descripcion_general) VALUES
    ('Historia clinica de Firulais - canino mestizo'),
    ('Historia clinica de Michi - felino comun europeo');

INSERT INTO anotacion_historia (historia_medica_id, fecha_anotacion, detalle) VALUES
    (1, '2026-01-05 09:20:00', 'Se aplica vacuna polivalente, paciente estable'),
    (1, '2026-02-11 14:50:00', 'Retiro de puntos sin complicaciones'),
    (2, '2026-03-08 11:25:00', 'Se observa inflamacion leve en pata trasera'),
    (2, '2026-04-01 08:30:00', 'Profilaxis dental realizada bajo sedacion');
