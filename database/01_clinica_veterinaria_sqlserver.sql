/* =====================================================================
   SEGUNDO PARCIAL DE PROGRAMACIÓN WEB - Centro veterinario
   Base de datos: clinica_veterinaria  (Microsoft SQL Server 2017+)

   Adaptación del dump MySQL "clinica" (MySQL 8.0) a SQL Server:
     - AUTO_INCREMENT  -> IDENTITY(1,1)
     - tinyint(1)      -> BIT
     - text            -> NVARCHAR(n) / NVARCHAR(MAX)
     - datetime        -> DATETIME2(0)
     - medico          -> veterinario (el parcial habla de "veterinario")
     - historia_medica -> historia_clinica (ahora con cliente, mascota, cita,
                          veterinario, fecha, motivo, diagnóstico, estado)
     - formula_medica  -> ahora ligada a historia + mascota + veterinario y con
                          dosis, frecuencia y duración como valores numéricos
   Integridad ("no debe existir información huérfana"):
     - Todas las relaciones tienen FOREIGN KEY sin cascada (NO ACTION).
     - FK compuestas garantizan a nivel de BD que:
         * la mascota de una cita pertenece al cliente de la cita
         * la mascota de una historia es la misma de su cita
         * la mascota de una fórmula es la misma de su historia
     - Las fórmulas no se borran: se inactivan (columna activo).
   No se copiaron las tablas usuario/session del dump original (no hacen
   parte del parcial y contenían credenciales y un token de sesión).
   Se corrigió el nombre de especialización 'Cardiología 2' -> 'Cardiología'
   y se actualizó la fecha de vencimiento de los medicamentos de prueba.
   ===================================================================== */

IF DB_ID(N'clinica_veterinaria') IS NULL
    CREATE DATABASE clinica_veterinaria;
GO

USE clinica_veterinaria;
GO

/* ---------------------------------------------------------------------
   1. Limpieza (orden inverso a las dependencias) -> el script es re-ejecutable
   --------------------------------------------------------------------- */
IF OBJECT_ID(N'dbo.formula_medica',     N'U') IS NOT NULL DROP TABLE dbo.formula_medica;
IF OBJECT_ID(N'dbo.anotacion_historia', N'U') IS NOT NULL DROP TABLE dbo.anotacion_historia;
IF OBJECT_ID(N'dbo.historia_clinica',   N'U') IS NOT NULL DROP TABLE dbo.historia_clinica;
IF OBJECT_ID(N'dbo.cita',               N'U') IS NOT NULL DROP TABLE dbo.cita;
IF OBJECT_ID(N'dbo.mascota',            N'U') IS NOT NULL DROP TABLE dbo.mascota;
IF OBJECT_ID(N'dbo.veterinario',        N'U') IS NOT NULL DROP TABLE dbo.veterinario;
IF OBJECT_ID(N'dbo.medicamento',        N'U') IS NOT NULL DROP TABLE dbo.medicamento;
IF OBJECT_ID(N'dbo.especializacion',    N'U') IS NOT NULL DROP TABLE dbo.especializacion;
IF OBJECT_ID(N'dbo.raza',               N'U') IS NOT NULL DROP TABLE dbo.raza;
IF OBJECT_ID(N'dbo.cliente',            N'U') IS NOT NULL DROP TABLE dbo.cliente;
GO

/* ---------------------------------------------------------------------
   2. Tablas
   --------------------------------------------------------------------- */
CREATE TABLE dbo.cliente (
    cliente_id        BIGINT        IDENTITY(1,1) NOT NULL,
    tipo_documento    NVARCHAR(10)  NOT NULL,
    numero_documento  NVARCHAR(20)  NOT NULL,
    nombres           NVARCHAR(100) NOT NULL,
    apellidos         NVARCHAR(100) NOT NULL,
    fecha_nacimiento  DATE          NOT NULL,
    genero            CHAR(1)       NULL,
    telefono          NVARCHAR(20)  NULL,
    direccion         NVARCHAR(300) NULL,
    activo            BIT           NOT NULL CONSTRAINT DF_cliente_activo DEFAULT 1,
    CONSTRAINT PK_cliente           PRIMARY KEY (cliente_id),
    CONSTRAINT UQ_cliente_documento UNIQUE (numero_documento),
    CONSTRAINT CK_cliente_genero    CHECK (genero IN ('M', 'F'))
);
GO

CREATE TABLE dbo.raza (
    raza_id            BIGINT        IDENTITY(1,1) NOT NULL,
    nombre             NVARCHAR(100) NOT NULL,
    especie            NVARCHAR(100) NOT NULL,
    fecha_creacion     DATETIME2(0)  NOT NULL CONSTRAINT DF_raza_creacion DEFAULT SYSDATETIME(),
    fecha_modificacion DATETIME2(0)  NULL,
    CONSTRAINT PK_raza        PRIMARY KEY (raza_id),
    CONSTRAINT UQ_raza_nombre UNIQUE (nombre)
);
GO

CREATE TABLE dbo.mascota (
    mascota_id         BIGINT        IDENTITY(1,1) NOT NULL,
    nombre_mascota     NVARCHAR(100) NOT NULL,
    edad               INT           NOT NULL,
    raza_id            BIGINT        NOT NULL,
    cliente_id         BIGINT        NOT NULL,
    fecha_registro     DATETIME2(0)  NOT NULL CONSTRAINT DF_mascota_registro DEFAULT SYSDATETIME(),
    fecha_modificacion DATETIME2(0)  NULL,
    activo             BIT           NOT NULL CONSTRAINT DF_mascota_activo DEFAULT 1,
    CONSTRAINT PK_mascota         PRIMARY KEY (mascota_id),
    CONSTRAINT UQ_mascota_cliente UNIQUE (mascota_id, cliente_id),   -- soporte de las FK compuestas
    CONSTRAINT CK_mascota_edad    CHECK (edad >= 0),
    CONSTRAINT FK_mascota_raza    FOREIGN KEY (raza_id)    REFERENCES dbo.raza (raza_id),
    CONSTRAINT FK_mascota_cliente FOREIGN KEY (cliente_id) REFERENCES dbo.cliente (cliente_id)
);
GO

CREATE TABLE dbo.especializacion (
    id                     BIGINT        IDENTITY(1,1) NOT NULL,
    nombre                 NVARCHAR(100) NOT NULL,
    descripcion            NVARCHAR(500) NULL,
    codigo_especializacion NVARCHAR(10)  NOT NULL,
    CONSTRAINT PK_especializacion        PRIMARY KEY (id),
    CONSTRAINT UQ_especializacion_nombre UNIQUE (nombre),
    CONSTRAINT UQ_especializacion_codigo UNIQUE (codigo_especializacion)
);
GO

CREATE TABLE dbo.veterinario (
    id                   BIGINT        IDENTITY(1,1) NOT NULL,
    tipo_documento       NVARCHAR(10)  NOT NULL,
    numero_documento     NVARCHAR(20)  NOT NULL,
    nombres              NVARCHAR(100) NOT NULL,
    apellidos            NVARCHAR(100) NOT NULL,
    telefono             NVARCHAR(20)  NULL,
    registro_profesional NVARCHAR(50)  NOT NULL,
    especializacion_id   BIGINT        NOT NULL,
    activo               BIT           NOT NULL CONSTRAINT DF_veterinario_activo DEFAULT 1,
    CONSTRAINT PK_veterinario           PRIMARY KEY (id),
    CONSTRAINT UQ_veterinario_documento UNIQUE (numero_documento),
    CONSTRAINT UQ_veterinario_registro  UNIQUE (registro_profesional),
    CONSTRAINT FK_veterinario_especializacion FOREIGN KEY (especializacion_id) REFERENCES dbo.especializacion (id)
);
GO

CREATE TABLE dbo.medicamento (
    id                          BIGINT        IDENTITY(1,1) NOT NULL,
    nombre                      NVARCHAR(100) NOT NULL,
    descripcion                 NVARCHAR(500) NULL,
    presentacion                NVARCHAR(100) NULL,
    fecha_compra                DATE          NOT NULL,
    fecha_vence                 DATE          NOT NULL,
    fecha_creacion_registro     DATETIME2(0)  NOT NULL CONSTRAINT DF_medicamento_creacion DEFAULT SYSDATETIME(),
    fecha_modificacion_registro DATETIME2(0)  NULL,
    CONSTRAINT PK_medicamento        PRIMARY KEY (id),
    CONSTRAINT UQ_medicamento_nombre UNIQUE (nombre),
    CONSTRAINT CK_medicamento_fechas CHECK (fecha_vence >= fecha_compra)
);
GO

CREATE TABLE dbo.cita (
    id                 BIGINT        IDENTITY(1,1) NOT NULL,
    cliente_id         BIGINT        NOT NULL,
    mascota_id         BIGINT        NOT NULL,
    veterinario_id     BIGINT        NOT NULL,
    fecha_hora         DATETIME2(0)  NOT NULL,
    estado             NVARCHAR(20)  NOT NULL CONSTRAINT DF_cita_estado DEFAULT N'PENDIENTE',
    motivo             NVARCHAR(500) NOT NULL,
    fecha_creacion     DATETIME2(0)  NOT NULL CONSTRAINT DF_cita_creacion DEFAULT SYSDATETIME(),
    fecha_modificacion DATETIME2(0)  NULL,
    CONSTRAINT PK_cita         PRIMARY KEY (id),
    CONSTRAINT UQ_cita_mascota UNIQUE (id, mascota_id),               -- soporte de la FK compuesta de historia
    CONSTRAINT CK_cita_estado  CHECK (estado IN (N'PENDIENTE', N'CONFIRMADA', N'ATENDIDA', N'CANCELADA', N'PROGRAMADA')),
    CONSTRAINT FK_cita_cliente FOREIGN KEY (cliente_id)     REFERENCES dbo.cliente (cliente_id),
    CONSTRAINT FK_cita_vet     FOREIGN KEY (veterinario_id) REFERENCES dbo.veterinario (id),
    CONSTRAINT FK_cita_mascota_del_cliente FOREIGN KEY (mascota_id, cliente_id)
        REFERENCES dbo.mascota (mascota_id, cliente_id)               -- la mascota DEBE ser del cliente
);
CREATE INDEX IX_cita_vet_fecha     ON dbo.cita (veterinario_id, fecha_hora);
CREATE INDEX IX_cita_mascota_fecha ON dbo.cita (mascota_id, fecha_hora);
GO

CREATE TABLE dbo.historia_clinica (
    id             BIGINT         IDENTITY(1,1) NOT NULL,
    cliente_id     BIGINT         NOT NULL,
    mascota_id     BIGINT         NOT NULL,
    cita_id        BIGINT         NOT NULL,
    veterinario_id BIGINT         NOT NULL,
    fecha_hora     DATETIME2(0)   NOT NULL,
    motivo         NVARCHAR(500)  NOT NULL,
    diagnostico    NVARCHAR(2000) NOT NULL,
    observaciones  NVARCHAR(2000) NULL,
    estado         NVARCHAR(20)   NOT NULL CONSTRAINT DF_historia_estado DEFAULT N'ABIERTA',
    fecha_creacion DATETIME2(0)   NOT NULL CONSTRAINT DF_historia_creacion DEFAULT SYSDATETIME(),
    CONSTRAINT PK_historia          PRIMARY KEY (id),
    CONSTRAINT UQ_historia_cita     UNIQUE (cita_id),                  -- una cita genera una sola historia
    CONSTRAINT UQ_historia_mascota  UNIQUE (id, mascota_id),           -- soporte de la FK compuesta de fórmula
    CONSTRAINT CK_historia_estado   CHECK (estado IN (N'ABIERTA', N'CERRADA')),
    CONSTRAINT FK_historia_cliente  FOREIGN KEY (cliente_id)     REFERENCES dbo.cliente (cliente_id),
    CONSTRAINT FK_historia_vet      FOREIGN KEY (veterinario_id) REFERENCES dbo.veterinario (id),
    CONSTRAINT FK_historia_mascota_del_cliente FOREIGN KEY (mascota_id, cliente_id)
        REFERENCES dbo.mascota (mascota_id, cliente_id),
    CONSTRAINT FK_historia_cita_de_la_mascota FOREIGN KEY (cita_id, mascota_id)
        REFERENCES dbo.cita (id, mascota_id)                           -- la cita DEBE ser de esa mascota
);
CREATE INDEX IX_historia_mascota_fecha ON dbo.historia_clinica (mascota_id, fecha_hora);
GO

CREATE TABLE dbo.anotacion_historia (
    id                    BIGINT         IDENTITY(1,1) NOT NULL,
    historia_id           BIGINT         NOT NULL,
    veterinario_id        BIGINT         NOT NULL,
    fecha_hora            DATETIME2(0)   NOT NULL CONSTRAINT DF_anotacion_fecha DEFAULT SYSDATETIME(),
    descripcion           NVARCHAR(2000) NOT NULL,
    observacion           NVARCHAR(2000) NULL,
    diagnostico_evolucion NVARCHAR(2000) NULL,
    CONSTRAINT PK_anotacion         PRIMARY KEY (id),
    CONSTRAINT FK_anotacion_historia FOREIGN KEY (historia_id)    REFERENCES dbo.historia_clinica (id),
    CONSTRAINT FK_anotacion_vet      FOREIGN KEY (veterinario_id) REFERENCES dbo.veterinario (id)
);
CREATE INDEX IX_anotacion_historia_fecha ON dbo.anotacion_historia (historia_id, fecha_hora DESC);
GO

CREATE TABLE dbo.formula_medica (
    id                  BIGINT         IDENTITY(1,1) NOT NULL,
    historia_id         BIGINT         NOT NULL,
    mascota_id          BIGINT         NOT NULL,
    veterinario_id      BIGINT         NOT NULL,
    medicamento_id      BIGINT         NOT NULL,
    fecha_emision       DATE           NOT NULL,
    dosis_valor         DECIMAL(8,2)   NOT NULL,
    dosis_unidad        NVARCHAR(20)   NOT NULL,
    frecuencia_horas    INT            NOT NULL,
    duracion_dias       INT            NOT NULL,
    indicaciones        NVARCHAR(1000) NULL,
    activo              BIT            NOT NULL CONSTRAINT DF_formula_activo DEFAULT 1,
    fecha_creacion      DATETIME2(0)   NOT NULL CONSTRAINT DF_formula_creacion DEFAULT SYSDATETIME(),
    fecha_actualizacion DATETIME2(0)   NULL,
    CONSTRAINT PK_formula            PRIMARY KEY (id),
    CONSTRAINT CK_formula_dosis      CHECK (dosis_valor > 0),
    CONSTRAINT CK_formula_frecuencia CHECK (frecuencia_horas BETWEEN 1 AND 168),
    CONSTRAINT CK_formula_duracion   CHECK (duracion_dias BETWEEN 1 AND 365),
    CONSTRAINT FK_formula_vet        FOREIGN KEY (veterinario_id) REFERENCES dbo.veterinario (id),
    CONSTRAINT FK_formula_medicamento FOREIGN KEY (medicamento_id) REFERENCES dbo.medicamento (id),
    CONSTRAINT FK_formula_mascota    FOREIGN KEY (mascota_id)     REFERENCES dbo.mascota (mascota_id),
    CONSTRAINT FK_formula_historia_de_la_mascota FOREIGN KEY (historia_id, mascota_id)
        REFERENCES dbo.historia_clinica (id, mascota_id)               -- la historia DEBE ser de esa mascota
);
CREATE INDEX IX_formula_creacion ON dbo.formula_medica (fecha_creacion DESC);
GO

/* ---------------------------------------------------------------------
   3. Datos de prueba (tomados del dump original; las citas, historias,
      anotaciones y fórmulas se regeneran con fechas RELATIVAS a hoy para
      que las pruebas del parcial funcionen cuando se ejecute el script)
   --------------------------------------------------------------------- */
INSERT INTO dbo.cliente (tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento, genero, telefono, direccion, activo) VALUES
(N'CC', N'100000001', N'Juan',     N'Pérez López',       '1990-05-12', 'M', N'3001112233', N'Calle 10 #12-34',        1),
(N'CC', N'100000002', N'María',    N'Gómez Rodríguez',   '1985-08-22', 'F', N'3012223344', N'Carrera 15 #20-10',      1),
(N'TI', N'900000003', N'Andrés',   N'Martínez Díaz',     '2005-03-18', 'M', N'3023334455', N'Av. Boyacá #45-67',      1),
(N'CC', N'100000004', N'Laura',    N'Hernández Ruiz',    '1992-12-01', 'F', N'3034445566', N'Calle 5 #7-89',          1),
(N'CC', N'100000005', N'Carlos',   N'Ramírez Torres',    '1980-07-30', 'M', N'3045556677', N'Carrera 8 #15-20',       1),
(N'CE', N'200000006', N'Ana',      N'Castro Moreno',     '1998-01-25', 'F', N'3056667788', N'Diagonal 12 #25-50',     1),
(N'CC', N'100000007', N'Santiago', N'Moreno Vargas',     '1995-09-14', 'M', N'3067778899', N'Transversal 20 #40-22',  1),
(N'CC', N'100000008', N'Paula',    N'Ríos Fernández',    '1987-11-10', 'F', N'3078889900', N'Calle 22 #18-45',        1),
(N'TI', N'900000009', N'Felipe',   N'Gutiérrez Méndez',  '2006-06-06', 'M', N'3089990011', N'Carrera 50 #60-12',      1),
(N'CC', N'100000010', N'Diana',    N'Salazar Peña',      '1993-04-21', 'F', N'3090001122', N'Av. Caracas #100-50',    1);
GO

INSERT INTO dbo.raza (nombre, especie) VALUES (N'Gato', N'Felino'), (N'Perro', N'Canino');
GO

INSERT INTO dbo.mascota (nombre_mascota, edad, raza_id, cliente_id, fecha_registro, fecha_modificacion) VALUES
(N'Max',    3, 1,  1, '2026-08-01 08:15:00', '2026-08-15 10:20:00'),
(N'Luna',   2, 2,  2, '2026-08-01 09:30:00', '2026-08-16 11:00:00'),
(N'Rocky',  5, 1,  3, '2026-08-02 10:00:00', NULL),
(N'Bella',  4, 2,  4, '2026-08-02 11:20:00', '2026-08-17 09:15:00'),
(N'Toby',   1, 1,  5, '2026-08-03 08:45:00', NULL),
(N'Nala',   6, 2,  6, '2026-08-03 14:10:00', '2026-08-18 14:30:00'),
(N'Coco',   3, 1,  7, '2026-08-04 09:00:00', NULL),
(N'Simba',  2, 2,  8, '2026-08-04 15:30:00', '2026-08-19 16:00:00'),
(N'Milo',   7, 1,  9, '2026-08-05 10:15:00', NULL),
(N'Maya',   4, 2, 10, '2026-08-05 16:00:00', '2026-08-20 10:00:00'),
(N'Thor',   5, 1,  1, '2026-08-06 08:30:00', NULL),
(N'Kira',   2, 2,  2, '2026-08-06 13:45:00', '2026-08-21 13:20:00'),
(N'Bruno',  8, 1,  3, '2026-08-07 09:20:00', NULL),
(N'Canela', 3, 2,  4, '2026-08-07 14:30:00', '2026-08-22 15:10:00'),
(N'Zeus',   6, 1,  5, '2026-08-08 10:45:00', NULL),
(N'Sasha',  1, 2,  6, '2026-08-08 16:15:00', '2026-08-23 11:45:00'),
(N'Duke',   4, 1,  7, '2026-08-09 08:00:00', NULL),
(N'Mia',    5, 2,  8, '2026-08-09 11:30:00', '2026-08-24 09:30:00'),
(N'Oliver', 2, 1,  9, '2026-08-10 13:00:00', NULL),
(N'Lola',   7, 2, 10, '2026-08-10 15:45:00', '2026-08-25 14:00:00');
GO

INSERT INTO dbo.especializacion (nombre, descripcion, codigo_especializacion) VALUES
(N'Cardiología',         N'Especialización médica centrada en el diagnóstico y tratamiento de enfermedades del corazón y otras enfermedades asociadas.', N'COD_100'),
(N'Neurología',          N'Se ocupa de los trastornos del sistema nervioso central y periférico.',                    N'COD_02'),
(N'Pediatría',           N'Atención médica especializada en niños y adolescentes.',                                   N'COD_03'),
(N'Oncología',           N'Estudio y tratamiento de diferentes tipos de cáncer.',                                     N'COD_04'),
(N'Dermatología',        N'Diagnóstico y tratamiento de enfermedades de la piel.',                                    N'COD_05'),
(N'Ginecología',         N'Enfocada en el sistema reproductor femenino.',                                             N'COD_06'),
(N'Urología',            N'Tratamiento del sistema urinario y el sistema reproductor masculino.',                     N'COD_07'),
(N'Psiquiatría',         N'Estudio y tratamiento de enfermedades mentales y emocionales.',                            N'COD_08'),
(N'Oftalmología',        N'Especialización en la salud ocular y la visión.',                                          N'COD_09'),
(N'Otorrinolaringología',N'Se encarga de enfermedades de oído, nariz y garganta.',                                    N'COD_10'),
(N'Traumatología',       N'Tratamiento de lesiones y enfermedades del sistema musculoesquelético.',                   N'COD_11'),
(N'Anestesiología',      N'Responsable de la anestesia y control del dolor durante procedimientos quirúrgicos.',      N'COD_12'),
(N'Reumatología',        N'Estudia enfermedades autoinmunes y musculoesqueléticas.',                                  N'COD_13'),
(N'Endocrinología',      N'Especialización en glándulas y trastornos hormonales.',                                    N'COD_14'),
(N'Gastroenterología',   N'Diagnóstico y tratamiento de enfermedades del aparato digestivo.',                         N'COD_15');
GO

INSERT INTO dbo.veterinario (tipo_documento, numero_documento, nombres, apellidos, telefono, registro_profesional, especializacion_id) VALUES
(N'CC', N'1001001001', N'Juan',          N'Pérez Gómez',      N'3001112233', N'RP-1001',        13),
(N'CC', N'1001001002', N'María',         N'Rodríguez López',  N'3001112234', N'RP-1002',         2),
(N'CC', N'1001001003', N'Carlos',        N'Martínez Ruiz',    N'3001112235', N'RP-1003',         3),
(N'CC', N'1001001004', N'Laura',         N'Fernández Díaz',   N'3001112236', N'RP-1004',         4),
(N'CC', N'1001001005', N'Andrés',        N'Ramírez Soto',     N'3001112237', N'RP-1005',         5),
(N'CC', N'1001001006', N'Diana',         N'Gómez Torres',     N'3001112238', N'RP-1006',         6),
(N'CC', N'1001001007', N'Ricardo',       N'Jiménez Vargas',   N'3001112239', N'RP-1007',         7),
(N'CC', N'1001001008', N'Patricia',      N'Hernández Ríos',   N'3001112240', N'RP-1008',         8),
(N'CC', N'1001001009', N'Santiago',      N'Morales Castro',   N'3001112241', N'RP-1009',         9),
(N'CC', N'1001001010', N'Natalia',       N'García Mendoza',   N'3001112242', N'RP-1010',         9),
(N'CC', N'88274388',   N'Giovanni',      N'Mora',             N'5711322',    N'1234656789100',   8),
(N'CC', N'12345678',   N'Edson Arantes', N'Do nascimento',    N'3207729661', N'RP-10011',       12),
(N'CC', N'12345123',   N'Giovanni',      N'Mora Jaimes',      N'3207729661', N'001RP1234',      12);
GO

INSERT INTO dbo.medicamento (nombre, descripcion, presentacion, fecha_compra, fecha_vence) VALUES
(N'Paracetamol',           N'Analgésico y antipirético',            N'Tabletas 500mg',  '2024-06-01', '2028-06-01'),
(N'Paracetamol Inyectable',N'Analgésico y antipirético',            N'Tabletas 1200mg', '2024-06-01', '2028-06-01'),
(N'Paracetamol Solución',  N'Analgésico y antipirético',            N'Tabletas 800mg',  '2024-06-01', '2028-06-01'),
(N'Amoxicilina',           N'Antibiótico de amplio espectro',       N'Suspensión 250mg/5ml', '2026-01-15', '2028-01-15'),
(N'Meloxicam',             N'Antiinflamatorio no esteroideo',       N'Tabletas 7.5mg',  '2026-01-15', '2028-03-01');
GO

/* ---- Citas, historias, anotaciones y fórmulas (fechas relativas a hoy) ---- */
DECLARE @base DATETIME2(0) = CAST(CAST(SYSDATETIME() AS DATE) AS DATETIME2(0));   -- hoy 00:00

-- Cada cita respeta: la mascota pertenece al cliente (lo exige la FK compuesta).
INSERT INTO dbo.cita (cliente_id, mascota_id, veterinario_id, fecha_hora, estado, motivo) VALUES
( 1,  1, 3, DATEADD(MINUTE,  540, DATEADD(DAY, -10, @base)), N'ATENDIDA',   N'Consulta general'),         -- cita 1  (con historia)
( 2,  2, 3, DATEADD(MINUTE,  870, DATEADD(DAY,  -9, @base)), N'ATENDIDA',   N'Consulta odontológica'),    -- cita 2  (con historia)
( 3,  3, 2, DATEADD(MINUTE, 1110, DATEADD(DAY,  -8, @base)), N'ATENDIDA',   N'Consulta general'),         -- cita 3  (con historia)
( 4,  4, 5, DATEADD(MINUTE,  600, DATEADD(DAY,  -5, @base)), N'ATENDIDA',   N'Control de piel'),          -- cita 4  (SIN historia: sirve para probar)
( 5,  5, 1, DATEADD(MINUTE,  660, DATEADD(DAY,  -3, @base)), N'ATENDIDA',   N'Vacunación'),               -- cita 5  (SIN historia: sirve para probar)
( 1, 11, 3, DATEADD(MINUTE,  540, DATEADD(DAY,   2, @base)), N'PENDIENTE',  N'Revisión de rutina'),       -- cita 6
( 2, 12, 3, DATEADD(MINUTE,  870, DATEADD(DAY,   2, @base)), N'CONFIRMADA', N'Control de peso'),          -- cita 7
( 6,  6, 4, DATEADD(MINUTE,  600, DATEADD(DAY,   3, @base)), N'PROGRAMADA', N'Desparasitación'),         -- cita 8
( 7,  7, 2, DATEADD(MINUTE,  960, DATEADD(DAY,   4, @base)), N'PENDIENTE',  N'Consulta general'),         -- cita 9
( 8,  8, 5, DATEADD(MINUTE,  900, DATEADD(DAY,  -1, @base)), N'CANCELADA',  N'Control'),                  -- cita 10 (cancelada)
( 9,  9, 6, DATEADD(MINUTE,  510, DATEADD(DAY,   5, @base)), N'PENDIENTE',  N'Consulta general'),         -- cita 11
(10, 10, 7, DATEADD(MINUTE,  690, DATEADD(DAY,   6, @base)), N'CONFIRMADA', N'Vacunación anual');         -- cita 12

-- Historias de las citas 1, 2 y 3 (ORDER BY garantiza ids 1, 2, 3)
INSERT INTO dbo.historia_clinica (cliente_id, mascota_id, cita_id, veterinario_id, fecha_hora, motivo, diagnostico, observaciones, estado)
SELECT c.cliente_id, c.mascota_id, c.id, c.veterinario_id, c.fecha_hora, c.motivo, d.diagnostico, d.observaciones, d.estado
FROM dbo.cita AS c
JOIN (VALUES
    (1, N'Gastritis leve',          N'Dieta blanda durante 3 días',          N'ABIERTA'),
    (2, N'Sarro dental grado 2',    N'Se recomienda limpieza dental',        N'ABIERTA'),
    (3, N'Control sin hallazgos',   N'Peso estable, vacunas al día',         N'CERRADA')
) AS d (cita_id, diagnostico, observaciones, estado) ON d.cita_id = c.id
ORDER BY c.id;

INSERT INTO dbo.anotacion_historia (historia_id, veterinario_id, fecha_hora, descripcion, observacion, diagnostico_evolucion) VALUES
(1, 3, DATEADD(MINUTE,  600, DATEADD(DAY, -9, @base)), N'Control de evolución a las 24 horas',               N'Mascota con apetito recuperado',          N'Evolución favorable'),
(1, 3, DATEADD(MINUTE,  570, DATEADD(DAY, -7, @base)), N'Revisión final del tratamiento',                    N'Sin vómito en los últimos 3 días',        N'Recuperación completa'),
(2, 3, DATEADD(MINUTE,  900, DATEADD(DAY, -8, @base)), N'Seguimiento posterior a la consulta odontológica',   N'Se programa limpieza dental',             N'Sarro dental en seguimiento');

INSERT INTO dbo.formula_medica (historia_id, mascota_id, veterinario_id, medicamento_id, fecha_emision, dosis_valor, dosis_unidad, frecuencia_horas, duracion_dias, indicaciones, activo, fecha_creacion) VALUES
(1, 1, 3, 1, CAST(DATEADD(DAY, -10, @base) AS DATE), 250, N'mg', 12, 7, N'Administrar después de las comidas',              1, DATEADD(MINUTE, 600, DATEADD(DAY, -10, @base))),
(2, 2, 3, 4, CAST(DATEADD(DAY,  -9, @base) AS DATE), 250, N'mg',  8, 5, N'Agitar antes de usar y dar con abundante agua',  1, DATEADD(MINUTE, 900, DATEADD(DAY,  -9, @base))),
(3, 3, 2, 5, CAST(DATEADD(DAY,  -8, @base) AS DATE),   1, N'ml', 24, 3, N'Administrar con el alimento',                      1, DATEADD(MINUTE, 1140, DATEADD(DAY, -8, @base)));
GO

/* ---------------------------------------------------------------------
   4. Verificación rápida
   --------------------------------------------------------------------- */
SELECT 'cliente' AS tabla, COUNT(*) AS filas FROM dbo.cliente
UNION ALL SELECT 'mascota',           COUNT(*) FROM dbo.mascota
UNION ALL SELECT 'veterinario',       COUNT(*) FROM dbo.veterinario
UNION ALL SELECT 'medicamento',       COUNT(*) FROM dbo.medicamento
UNION ALL SELECT 'cita',              COUNT(*) FROM dbo.cita
UNION ALL SELECT 'historia_clinica',  COUNT(*) FROM dbo.historia_clinica
UNION ALL SELECT 'anotacion_historia',COUNT(*) FROM dbo.anotacion_historia
UNION ALL SELECT 'formula_medica',    COUNT(*) FROM dbo.formula_medica;
GO
