-- Esquema con el que partia el proyecto (estructura real de la base `clinica` antes del examen final).
-- MySQL 8. Ejecutar sobre una base vacia; en una base existente no cambia nada.
-- Orden: 01_esquema_base.sql, 02_migracion_examen_final.sql, 03_datos_iniciales.sql
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `anotacion_historia` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `historia_id` int NOT NULL,
  `medico_id` int NOT NULL,
  `fecha` datetime DEFAULT CURRENT_TIMESTAMP,
  `descripcion` text NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `cita` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `cliente_id` int NOT NULL,
  `mascota_id` int NOT NULL,
  `medico_id` int NOT NULL,
  `fecha_hora` datetime NOT NULL,
  `estado` varchar(20) NOT NULL,
  `motivo` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `cliente` (
  `cliente_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `usuario_id` int DEFAULT NULL,
  `tipo_documento` varchar(10) NOT NULL,
  `numero_documento` varchar(20) NOT NULL,
  `nombres` varchar(100) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `fecha_nacimiento` date NOT NULL,
  `genero` char(1) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `direccion` text,
  `activo` tinyint(1) NOT NULL,
  PRIMARY KEY (`cliente_id`),
  UNIQUE KEY `id` (`cliente_id`),
  UNIQUE KEY `numero_documento` (`numero_documento`),
  UNIQUE KEY `usuario_id` (`usuario_id`),
  KEY `idx_paciente_documento` (`numero_documento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `especializacion` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text,
  `codigo_especializacion` varchar(10) NOT NULL DEFAULT '""',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `nombre` (`nombre`),
  UNIQUE KEY `codigo_especializacion_UNIQUE` (`codigo_especializacion`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `formula_medica` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `cita_id` int NOT NULL,
  `medicamento_id` int NOT NULL,
  `dosis` text NOT NULL,
  `indicaciones` text,
  `fecha_creacion_registro` datetime NOT NULL,
  `fecha_actualizacion_registro` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `historia_medica` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `paciente_id` int NOT NULL,
  `fecha_creacion` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `mascota` (
  `mascota_id` int NOT NULL AUTO_INCREMENT,
  `nombre_mascota` varchar(100) NOT NULL,
  `edad` int NOT NULL,
  `raza_id` int NOT NULL,
  `fecha_registro` datetime NOT NULL,
  `fecha_modificacion` datetime DEFAULT NULL,
  `cliente_id` int NOT NULL,
  PRIMARY KEY (`mascota_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `medicamento` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text,
  `presentacion` varchar(100) DEFAULT NULL,
  `fecha_compra` date NOT NULL,
  `fecha_vence` date NOT NULL,
  `fecha_creacion_registro` datetime NOT NULL,
  `fecha_modificacion_registro` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `medico` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `tipo_documento` varchar(10) NOT NULL,
  `numero_documento` varchar(20) NOT NULL,
  `nombres` varchar(100) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `registro_profesional` varchar(50) NOT NULL,
  `especializacion_id` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `numero_documento` (`numero_documento`),
  UNIQUE KEY `registro_profesional` (`registro_profesional`),
  KEY `idx_medico_documento` (`numero_documento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `raza` (
  `raza_id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `especie` varchar(100) NOT NULL,
  `fecha_creacion` datetime NOT NULL,
  `fecha_modificacion` datetime DEFAULT NULL,
  PRIMARY KEY (`raza_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `session` (
  `session_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `token` varchar(500) NOT NULL,
  `fecha_ini_sesion` datetime DEFAULT NULL,
  `fecha_expiracion` datetime DEFAULT NULL,
  PRIMARY KEY (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `usuario` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` text,
  `rol` varchar(30) NOT NULL,
  `fecha_creacion` datetime DEFAULT CURRENT_TIMESTAMP,
  `activo` tinyint(1) DEFAULT '1',
  `email` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
