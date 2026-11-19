-- MySQL dump 10.13  Distrib 8.4.11, for Linux (x86_64)
--
-- ------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `anotacion_historia`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `anotacion_historia` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` text NOT NULL,
  `fecha` datetime(6) DEFAULT NULL,
  `historia_id` int NOT NULL,
  `medico_id` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `atencion_historial`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `atencion_historial` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `atencion_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `accion` varchar(30) NOT NULL,
  `detalle` json NOT NULL,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  KEY `ix_atencion_historial` (`atencion_id`,`id`),
  CONSTRAINT `atencion_historial_ibfk_1` FOREIGN KEY (`atencion_id`) REFERENCES `atencion_veterinaria` (`id`),
  CONSTRAINT `atencion_historial_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `atencion_servicio`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `atencion_servicio` (
  `atencion_id` bigint NOT NULL,
  `servicio_id` bigint NOT NULL,
  `tarifa_id` bigint NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `cantidad` int NOT NULL,
  `activo` tinyint(1) NOT NULL,
  `valor` decimal(14,2) NOT NULL,
  `iva` decimal(5,2) NOT NULL,
  `impuesto` decimal(5,2) NOT NULL,
  PRIMARY KEY (`atencion_id`,`servicio_id`),
  KEY `servicio_id` (`servicio_id`),
  KEY `tarifa_id` (`tarifa_id`),
  CONSTRAINT `atencion_servicio_ibfk_1` FOREIGN KEY (`atencion_id`) REFERENCES `atencion_veterinaria` (`id`),
  CONSTRAINT `atencion_servicio_ibfk_2` FOREIGN KEY (`servicio_id`) REFERENCES `servicio_veterinario` (`id`),
  CONSTRAINT `atencion_servicio_ibfk_3` FOREIGN KEY (`tarifa_id`) REFERENCES `tarifa_veterinaria` (`id`),
  CONSTRAINT `atencion_servicio_chk_1` CHECK ((`cantidad` between 1 and 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `atencion_veterinaria`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `atencion_veterinaria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cita_id` bigint NOT NULL,
  `medico_id` bigint NOT NULL,
  `historia_id` bigint NOT NULL,
  `especializacion_id` bigint NOT NULL,
  `especialidad` varchar(100) NOT NULL,
  `motivo` text,
  `sintomas` text,
  `diagnostico` text,
  `observaciones` text,
  `recomendaciones` text,
  `creada` datetime(6) NOT NULL,
  `actualizada` datetime(6) NOT NULL,
  `finalizada` datetime(6) DEFAULT NULL,
  `version` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `cita_id` (`cita_id`),
  KEY `medico_id` (`medico_id`),
  KEY `historia_id` (`historia_id`),
  KEY `especializacion_id` (`especializacion_id`),
  CONSTRAINT `atencion_veterinaria_ibfk_1` FOREIGN KEY (`cita_id`) REFERENCES `cita` (`id`),
  CONSTRAINT `atencion_veterinaria_ibfk_2` FOREIGN KEY (`medico_id`) REFERENCES `medico` (`id`),
  CONSTRAINT `atencion_veterinaria_ibfk_3` FOREIGN KEY (`historia_id`) REFERENCES `historia_medica` (`id`),
  CONSTRAINT `atencion_veterinaria_ibfk_4` FOREIGN KEY (`especializacion_id`) REFERENCES `especializacion` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cita`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cita` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cliente_id` int NOT NULL,
  `estado` varchar(20) NOT NULL,
  `fecha_hora` datetime(6) NOT NULL,
  `mascota_id` int NOT NULL,
  `medico_id` int NOT NULL,
  `motivo` text,
  `fecha_fin` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_cita_medico_inicio` (`medico_id`,`fecha_hora`),
  KEY `ix_cita_mascota_inicio` (`mascota_id`,`fecha_hora`),
  CONSTRAINT `ck_cita_intervalo` CHECK (((`fecha_fin` is null) or (`fecha_fin` > `fecha_hora`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cita_historial`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cita_historial` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cita_id` bigint NOT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `fecha` datetime(6) NOT NULL,
  `anterior` json DEFAULT NULL,
  `actual` json NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_historial_usuario` (`usuario_id`),
  KEY `ix_historial_cita` (`cita_id`,`id`),
  CONSTRAINT `fk_historial_cita` FOREIGN KEY (`cita_id`) REFERENCES `cita` (`id`),
  CONSTRAINT `fk_historial_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cliente`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cliente` (
  `cliente_id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `apellidos` varchar(100) NOT NULL,
  `direccion` text,
  `fecha_nacimiento` date NOT NULL,
  `genero` varchar(1) DEFAULT NULL,
  `nombres` varchar(100) NOT NULL,
  `numero_documento` varchar(20) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `tipo_documento` varchar(10) NOT NULL,
  `usuario_id` int DEFAULT NULL,
  PRIMARY KEY (`cliente_id`),
  UNIQUE KEY `UKhx9w6tpma79yxjbk83xfiw7fo` (`numero_documento`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `despacho_detalle`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `despacho_detalle` (
  `entrega_id` bigint NOT NULL,
  `formula_id` bigint NOT NULL,
  `revision` int NOT NULL,
  `medicamento_id` bigint NOT NULL,
  `lote_id` bigint NOT NULL,
  `cantidad` int NOT NULL,
  `movimiento_id` bigint NOT NULL,
  `reversion_movimiento_id` bigint DEFAULT NULL,
  PRIMARY KEY (`entrega_id`,`lote_id`),
  UNIQUE KEY `movimiento_id` (`movimiento_id`),
  UNIQUE KEY `reversion_movimiento_id` (`reversion_movimiento_id`),
  KEY `entrega_id` (`entrega_id`,`formula_id`,`revision`),
  KEY `formula_id` (`formula_id`,`revision`,`medicamento_id`),
  KEY `lote_id` (`lote_id`),
  CONSTRAINT `despacho_detalle_ibfk_1` FOREIGN KEY (`entrega_id`, `formula_id`, `revision`) REFERENCES `despacho_entrega` (`id`, `formula_id`, `revision`),
  CONSTRAINT `despacho_detalle_ibfk_2` FOREIGN KEY (`formula_id`, `revision`, `medicamento_id`) REFERENCES `formula_clinica_detalle` (`formula_id`, `revision`, `medicamento_id`),
  CONSTRAINT `despacho_detalle_ibfk_3` FOREIGN KEY (`lote_id`) REFERENCES `inventario_lote` (`id`),
  CONSTRAINT `despacho_detalle_ibfk_4` FOREIGN KEY (`movimiento_id`) REFERENCES `inventario_movimiento` (`id`),
  CONSTRAINT `despacho_detalle_ibfk_5` FOREIGN KEY (`reversion_movimiento_id`) REFERENCES `inventario_movimiento` (`id`),
  CONSTRAINT `despacho_detalle_chk_1` CHECK ((`cantidad` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `despacho_entrega`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `despacho_entrega` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `formula_id` bigint NOT NULL,
  `revision` int NOT NULL,
  `usuario_id` bigint NOT NULL,
  `clave` char(36) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
  `plan` json NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `confirmada` datetime(6) DEFAULT NULL,
  `estado_resultante` varchar(26) DEFAULT NULL,
  `pendiente` json DEFAULT NULL,
  `revertida` datetime(6) DEFAULT NULL,
  `reversor_id` bigint DEFAULT NULL,
  `motivo_reversion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `clave` (`clave`),
  UNIQUE KEY `id` (`id`,`formula_id`,`revision`),
  KEY `usuario_id` (`usuario_id`),
  KEY `reversor_id` (`reversor_id`),
  KEY `ix_entrega_formula` (`formula_id`,`confirmada`,`id`),
  CONSTRAINT `despacho_entrega_ibfk_1` FOREIGN KEY (`formula_id`) REFERENCES `despacho_formula` (`formula_id`),
  CONSTRAINT `despacho_entrega_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `despacho_entrega_ibfk_3` FOREIGN KEY (`reversor_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `despacho_estado`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `despacho_estado` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `formula_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `anterior` varchar(26) NOT NULL,
  `nuevo` varchar(26) NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  KEY `ix_despacho_estado` (`formula_id`,`id`),
  CONSTRAINT `despacho_estado_ibfk_1` FOREIGN KEY (`formula_id`) REFERENCES `despacho_formula` (`formula_id`),
  CONSTRAINT `despacho_estado_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `despacho_formula`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `despacho_formula` (
  `formula_id` bigint NOT NULL,
  `estado` varchar(26) NOT NULL,
  PRIMARY KEY (`formula_id`),
  CONSTRAINT `despacho_formula_ibfk_1` FOREIGN KEY (`formula_id`) REFERENCES `formula_clinica` (`id`),
  CONSTRAINT `despacho_formula_chk_1` CHECK ((`estado` in (_utf8mb4'PENDIENTE_DESPACHO',_utf8mb4'EN_PREPARACION',_utf8mb4'DESPACHO_PARCIAL',_utf8mb4'DESPACHADO',_utf8mb4'PENDIENTE_DISPONIBILIDAD',_utf8mb4'CANCELADO')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `especializacion`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `especializacion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo_especializacion` varchar(10) NOT NULL,
  `descripcion` text,
  `nombre` varchar(100) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKko985q1iji558pox6yh3hw9lj` (`codigo_especializacion`),
  UNIQUE KEY `UKkgcsm6v96b022hk4on3g6dyg8` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `factura`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factura` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `numero` varchar(40) NOT NULL,
  `atencion_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `clave` char(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `huella` binary(32) NOT NULL,
  `emitida` datetime(6) NOT NULL,
  `clinica` varchar(160) NOT NULL,
  `propietario` varchar(255) NOT NULL,
  `mascota` varchar(160) NOT NULL,
  `veterinario` varchar(255) NOT NULL,
  `especialidad` varchar(100) NOT NULL,
  `descuento_pct` decimal(5,2) NOT NULL,
  `motivo_descuento` varchar(255) DEFAULT NULL,
  `subtotal` decimal(19,2) NOT NULL,
  `descuento` decimal(19,2) NOT NULL,
  `iva` decimal(19,2) NOT NULL,
  `otros_impuestos` decimal(19,2) NOT NULL,
  `total` decimal(19,2) NOT NULL,
  `pagado` decimal(19,2) NOT NULL DEFAULT '0.00',
  `estado` varchar(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero` (`numero`),
  UNIQUE KEY `clave` (`clave`),
  KEY `usuario_id` (`usuario_id`),
  KEY `ix_factura_atencion` (`atencion_id`,`id`),
  KEY `ix_factura_fecha` (`emitida`,`id`),
  KEY `ix_factura_estado_fecha` (`estado`,`emitida`,`id`),
  CONSTRAINT `factura_ibfk_1` FOREIGN KEY (`atencion_id`) REFERENCES `atencion_veterinaria` (`id`),
  CONSTRAINT `factura_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `factura_chk_1` CHECK ((`descuento_pct` between 0 and 100)),
  CONSTRAINT `factura_chk_2` CHECK (((`descuento_pct` = 0) or (`motivo_descuento` is not null))),
  CONSTRAINT `factura_chk_3` CHECK (((`subtotal` >= `descuento`) and (`descuento` >= 0) and (`iva` >= 0) and (`otros_impuestos` >= 0))),
  CONSTRAINT `factura_chk_4` CHECK (((`total` = (((`subtotal` - `descuento`) + `iva`) + `otros_impuestos`)) and (`pagado` between 0 and `total`))),
  CONSTRAINT `factura_chk_5` CHECK ((((`estado` = _utf8mb4'PENDIENTE_PAGO') and (`pagado` = 0) and (`total` > 0)) or ((`estado` = _utf8mb4'PAGO_PARCIAL') and (`pagado` > 0) and (`pagado` < `total`)) or ((`estado` = _utf8mb4'PAGADA') and (`pagado` = `total`)) or ((`estado` = _utf8mb4'ANULADA') and (`pagado` = 0))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `factura_detalle`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factura_detalle` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `factura_id` bigint NOT NULL,
  `atencion_servicio_id` bigint DEFAULT NULL,
  `servicio_id` bigint DEFAULT NULL,
  `despacho_movimiento_id` bigint DEFAULT NULL,
  `concepto` varchar(255) NOT NULL,
  `cantidad` int NOT NULL,
  `precio_unitario` decimal(14,2) NOT NULL,
  `subtotal` decimal(19,2) NOT NULL,
  `descuento` decimal(19,2) NOT NULL,
  `iva_pct` decimal(5,2) NOT NULL,
  `otros_pct` decimal(5,2) NOT NULL,
  `iva` decimal(19,2) NOT NULL,
  `otros_impuestos` decimal(19,2) NOT NULL,
  `total` decimal(19,2) NOT NULL,
  `vigente` tinyint(1) NOT NULL DEFAULT '1',
  `servicio_vigente` bigint GENERATED ALWAYS AS ((case when `vigente` then `servicio_id` end)) STORED,
  `atencion_vigente` bigint GENERATED ALWAYS AS ((case when `vigente` then `atencion_servicio_id` end)) STORED,
  `despacho_vigente` bigint GENERATED ALWAYS AS ((case when `vigente` then `despacho_movimiento_id` end)) STORED,
  PRIMARY KEY (`id`),
  UNIQUE KEY `atencion_vigente` (`atencion_vigente`,`servicio_vigente`),
  UNIQUE KEY `despacho_vigente` (`despacho_vigente`),
  KEY `factura_id` (`factura_id`),
  KEY `atencion_servicio_id` (`atencion_servicio_id`,`servicio_id`),
  KEY `despacho_movimiento_id` (`despacho_movimiento_id`),
  CONSTRAINT `factura_detalle_ibfk_1` FOREIGN KEY (`factura_id`) REFERENCES `factura` (`id`),
  CONSTRAINT `factura_detalle_ibfk_2` FOREIGN KEY (`atencion_servicio_id`, `servicio_id`) REFERENCES `atencion_servicio` (`atencion_id`, `servicio_id`),
  CONSTRAINT `factura_detalle_ibfk_3` FOREIGN KEY (`despacho_movimiento_id`) REFERENCES `despacho_detalle` (`movimiento_id`),
  CONSTRAINT `factura_detalle_chk_1` CHECK ((((`atencion_servicio_id` is not null) and (`servicio_id` is not null) and (`despacho_movimiento_id` is null)) or ((`atencion_servicio_id` is null) and (`servicio_id` is null) and (`despacho_movimiento_id` is not null)))),
  CONSTRAINT `factura_detalle_chk_2` CHECK (((`cantidad` > 0) and (`precio_unitario` >= 0) and (`subtotal` = (`cantidad` * `precio_unitario`)))),
  CONSTRAINT `factura_detalle_chk_3` CHECK (((`iva_pct` >= 0) and (`otros_pct` >= 0) and ((`iva_pct` + `otros_pct`) <= 100))),
  CONSTRAINT `factura_detalle_chk_4` CHECK (((`subtotal` >= `descuento`) and (`descuento` >= 0) and (`iva` >= 0) and (`otros_impuestos` >= 0))),
  CONSTRAINT `factura_detalle_chk_5` CHECK ((`total` = (((`subtotal` - `descuento`) + `iva`) + `otros_impuestos`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `factura_estado`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factura_estado` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `factura_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `anterior` varchar(16) DEFAULT NULL,
  `nuevo` varchar(16) NOT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `fecha` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `usuario_id` (`usuario_id`),
  KEY `ix_factura_estado_historial` (`factura_id`,`id`),
  CONSTRAINT `factura_estado_ibfk_1` FOREIGN KEY (`factura_id`) REFERENCES `factura` (`id`),
  CONSTRAINT `factura_estado_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `factura_estado_chk_1` CHECK ((`nuevo` in (_utf8mb4'PENDIENTE_PAGO',_utf8mb4'PAGO_PARCIAL',_utf8mb4'PAGADA',_utf8mb4'ANULADA'))),
  CONSTRAINT `factura_estado_chk_2` CHECK (((`nuevo` <> _utf8mb4'ANULADA') or (`motivo` is not null)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `factura_pago`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `factura_pago` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `factura_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `clave` char(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `importe` decimal(19,2) NOT NULL,
  `metodo` varchar(32) NOT NULL,
  `referencia` varchar(100) DEFAULT NULL,
  `estado` varchar(16) NOT NULL DEFAULT 'REGISTRADO',
  PRIMARY KEY (`id`),
  UNIQUE KEY `factura_id` (`factura_id`,`clave`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `factura_pago_ibfk_1` FOREIGN KEY (`factura_id`) REFERENCES `factura` (`id`),
  CONSTRAINT `factura_pago_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `factura_pago_chk_1` CHECK (((`importe` > 0) and (`estado` = _utf8mb4'REGISTRADO')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `formula_clinica`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `formula_clinica` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `atencion_id` bigint NOT NULL,
  `estado` varchar(16) NOT NULL,
  `revision` int NOT NULL,
  `creada` datetime(6) NOT NULL,
  `confirmada` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_formula_atencion` (`atencion_id`,`id`),
  CONSTRAINT `formula_clinica_ibfk_1` FOREIGN KEY (`atencion_id`) REFERENCES `atencion_veterinaria` (`id`),
  CONSTRAINT `formula_clinica_chk_1` CHECK (((`estado` in (_utf8mb4'BORRADOR',_utf8mb4'CONFIRMADA',_utf8mb4'ANULADA')) and (`revision` >= 1)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `formula_clinica_detalle`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `formula_clinica_detalle` (
  `formula_id` bigint NOT NULL,
  `revision` int NOT NULL,
  `medicamento_id` bigint NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `dosis` varchar(100) NOT NULL,
  `frecuencia` varchar(100) NOT NULL,
  `duracion` varchar(100) NOT NULL,
  `cantidad` int NOT NULL,
  `via` varchar(100) NOT NULL,
  `indicaciones` varchar(500) DEFAULT NULL,
  `precio_estimado` decimal(14,2) NOT NULL,
  PRIMARY KEY (`formula_id`,`revision`,`medicamento_id`),
  KEY `medicamento_id` (`medicamento_id`),
  CONSTRAINT `formula_clinica_detalle_ibfk_1` FOREIGN KEY (`formula_id`) REFERENCES `formula_clinica` (`id`),
  CONSTRAINT `formula_clinica_detalle_ibfk_2` FOREIGN KEY (`medicamento_id`) REFERENCES `medicamento` (`id`),
  CONSTRAINT `formula_clinica_detalle_chk_1` CHECK (((`cantidad` > 0) and (`precio_estimado` >= 0)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `formula_medica`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `formula_medica` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cita_id` int NOT NULL,
  `dosis` varchar(255) NOT NULL,
  `duracion_tratamiento` varchar(100) DEFAULT NULL,
  `fecha_actualizacion_registro` datetime(6) DEFAULT NULL,
  `fecha_creacion_registro` datetime(6) NOT NULL,
  `fecha_emision` datetime(6) DEFAULT NULL,
  `frecuencia` varchar(100) DEFAULT NULL,
  `indicaciones` varchar(255) DEFAULT NULL,
  `medicamento_id` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `historia_medica`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `historia_medica` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `paciente_id` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventario_importacion`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventario_importacion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `archivo` varchar(200) NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `fecha_confirmacion` datetime(6) DEFAULT NULL,
  `filas` json NOT NULL,
  `importados` int NOT NULL DEFAULT '0',
  `rechazados` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_importacion_usuario` (`usuario_id`),
  CONSTRAINT `fk_importacion_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventario_lote`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventario_lote` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `medicamento_id` bigint NOT NULL,
  `numero` varchar(60) NOT NULL,
  `costo` decimal(14,2) NOT NULL,
  `fecha_ingreso` date NOT NULL,
  `fecha_vence` date DEFAULT NULL,
  `cantidad` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_inventario_lote` (`medicamento_id`,`numero`),
  CONSTRAINT `fk_lote_medicamento` FOREIGN KEY (`medicamento_id`) REFERENCES `medicamento` (`id`),
  CONSTRAINT `ck_lote_cantidad` CHECK ((`cantidad` >= 0)),
  CONSTRAINT `ck_lote_costo` CHECK ((`costo` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventario_movimiento`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventario_movimiento` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `lote_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `tipo` varchar(10) NOT NULL,
  `cantidad` int NOT NULL,
  `saldo` int NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `motivo` varchar(255) NOT NULL,
  `referencia` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_movimiento_usuario` (`usuario_id`),
  KEY `ix_movimiento_lote` (`lote_id`,`id`),
  CONSTRAINT `fk_movimiento_lote` FOREIGN KEY (`lote_id`) REFERENCES `inventario_lote` (`id`),
  CONSTRAINT `fk_movimiento_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `ck_movimiento_saldo` CHECK ((`saldo` >= 0)),
  CONSTRAINT `ck_movimiento_tipo` CHECK ((((`tipo` in (_utf8mb4'ENTRADA',_utf8mb4'SALIDA')) and (`cantidad` > 0)) or ((`tipo` = _utf8mb4'AJUSTE') and (`cantidad` <> 0))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mascota`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mascota` (
  `mascota_id` int NOT NULL AUTO_INCREMENT,
  `cliente_id` int NOT NULL,
  `edad` int NOT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `nombre_mascota` varchar(255) NOT NULL,
  `raza_id` int NOT NULL,
  PRIMARY KEY (`mascota_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medicamento`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medicamento` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `descripcion` text,
  `fecha_compra` date NOT NULL,
  `fecha_creacion_registro` datetime(6) NOT NULL,
  `fecha_modificacion_registro` datetime(6) DEFAULT NULL,
  `fecha_vence` date DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `presentacion` varchar(100) DEFAULT NULL,
  `codigo` char(8) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
  `tipo` varchar(20) NOT NULL DEFAULT 'MEDICAMENTO',
  `invima` varchar(100) DEFAULT NULL,
  `precio_compra` decimal(14,2) NOT NULL DEFAULT '0.00',
  `precio_venta` decimal(14,2) NOT NULL DEFAULT '0.00',
  `stock_minimo` int NOT NULL DEFAULT '0',
  `fecha_ingreso` date NOT NULL,
  `fecha_proximo_pedido` date DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfy4qp46a0asx66i0igibhi1n8` (`nombre`),
  UNIQUE KEY `uq_medicamento_codigo` (`codigo`),
  CONSTRAINT `ck_medicamento_minimo` CHECK ((`stock_minimo` >= 0)),
  CONSTRAINT `ck_medicamento_precios` CHECK (((`precio_compra` >= 0) and (`precio_venta` >= 0))),
  CONSTRAINT `ck_medicamento_tipo` CHECK ((`tipo` in (_utf8mb4'MEDICAMENTO',_utf8mb4'PRODUCTO',_utf8mb4'UTENSILIO')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medico`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medico` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `apellidos` varchar(100) NOT NULL,
  `especializacion_id` int NOT NULL,
  `nombres` varchar(100) NOT NULL,
  `numero_documento` varchar(20) NOT NULL,
  `registro_profesional` varchar(50) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `tipo_documento` varchar(10) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK70rt8elxd41jmtof1ti3tr7ie` (`numero_documento`),
  UNIQUE KEY `UKd0cahrkn79q0i29g2b6kgak35` (`registro_profesional`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medico_ausencia`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medico_ausencia` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `medico_id` bigint NOT NULL,
  `inicio` datetime(6) NOT NULL,
  `fin` datetime(6) NOT NULL,
  `motivo` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ix_ausencia_medico_inicio` (`medico_id`,`inicio`),
  CONSTRAINT `fk_ausencia_medico` FOREIGN KEY (`medico_id`) REFERENCES `medico` (`id`),
  CONSTRAINT `ck_ausencia_intervalo` CHECK ((`fin` > `inicio`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medico_horario`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medico_horario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `medico_id` bigint NOT NULL,
  `dia` tinyint NOT NULL,
  `inicio` time NOT NULL,
  `fin` time NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_medico_franja` (`medico_id`,`dia`,`inicio`),
  CONSTRAINT `fk_horario_medico` FOREIGN KEY (`medico_id`) REFERENCES `medico` (`id`),
  CONSTRAINT `ck_horario_dia` CHECK ((`dia` between 1 and 7)),
  CONSTRAINT `ck_horario_intervalo` CHECK (((`inicio` >= _utf8mb4'00:00:00') and (`fin` < _utf8mb4'24:00:00') and (`fin` > `inicio`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu` (
  `menu_id` int NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `code` varchar(100) NOT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `icon` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `url` longtext NOT NULL,
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `perfil`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `perfil` (
  `perfil_id` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(255) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`perfil_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `perfil_menu`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `perfil_menu` (
  `perfil_id` int NOT NULL,
  `menu_id` int NOT NULL,
  PRIMARY KEY (`perfil_id`,`menu_id`),
  KEY `menu_id` (`menu_id`),
  CONSTRAINT `perfil_menu_ibfk_1` FOREIGN KEY (`perfil_id`) REFERENCES `perfil` (`perfil_id`),
  CONSTRAINT `perfil_menu_ibfk_2` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `raza`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `raza` (
  `raza_id` int NOT NULL AUTO_INCREMENT,
  `especie` varchar(255) DEFAULT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `nombre` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`raza_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `servicio_veterinario`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `servicio_veterinario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `especializacion_id` bigint NOT NULL,
  `nombre` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_servicio_especialidad` (`especializacion_id`,`nombre`),
  CONSTRAINT `servicio_veterinario_ibfk_1` FOREIGN KEY (`especializacion_id`) REFERENCES `especializacion` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tarifa_veterinaria`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tarifa_veterinaria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `servicio_id` bigint NOT NULL,
  `valor` decimal(14,2) NOT NULL,
  `iva` decimal(5,2) NOT NULL,
  `impuesto` decimal(5,2) NOT NULL,
  `desde` date NOT NULL,
  `hasta` date DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  `fecha` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tarifa_vigencia` (`servicio_id`,`desde`),
  KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `tarifa_veterinaria_ibfk_1` FOREIGN KEY (`servicio_id`) REFERENCES `servicio_veterinario` (`id`),
  CONSTRAINT `tarifa_veterinaria_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `tarifa_veterinaria_chk_1` CHECK (((`valor` >= 0) and (`iva` between 0 and 100) and (`impuesto` between 0 and 100) and ((`iva` + `impuesto`) <= 100))),
  CONSTRAINT `tarifa_veterinaria_chk_2` CHECK (((`hasta` is null) or (`hasta` >= `desde`)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `password_hash` varchar(255) DEFAULT NULL,
  `perfil_id` int DEFAULT NULL,
  `rol` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_usuario_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario_medico`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario_medico` (
  `usuario_id` bigint NOT NULL,
  `medico_id` bigint NOT NULL,
  PRIMARY KEY (`usuario_id`),
  UNIQUE KEY `uq_usuario_medico_medico` (`medico_id`),
  CONSTRAINT `fk_usuario_medico_medico` FOREIGN KEY (`medico_id`) REFERENCES `medico` (`id`),
  CONSTRAINT `fk_usuario_medico_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

