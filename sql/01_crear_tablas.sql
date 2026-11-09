-- ============================================================================
-- Cabañas El Hornero - 01_crear_tablas.sql
-- Esquema alineado con la Figura 1 del informe TP4 (diagrama entidad-relación):
-- tipos, longitudes, ENUM, claves foráneas, restricciones CHECK/UNIQUE e índice
-- compuesto idx_reserva_cabana_fechas. Motor InnoDB, juego de caracteres utf8mb4.
-- Requiere MySQL 8.0.16 o superior (para que los CHECK se apliquen).
-- Pensado para una instalación NUEVA. No lo uses para pisar una base con datos reales.
-- ============================================================================
CREATE DATABASE IF NOT EXISTS cabanas_el_hornero
  DEFAULT CHARACTER SET utf8mb4;
USE cabanas_el_hornero;

CREATE TABLE IF NOT EXISTS usuario (
  id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
  nombre_usuario  VARCHAR(50)  NOT NULL UNIQUE,
  contrasena_hash VARCHAR(255) NOT NULL,
  rol             ENUM('ADMINISTRADOR','RECEPCIONISTA','MANTENIMIENTO') NOT NULL,
  activo          TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cliente (
  id_cliente INT AUTO_INCREMENT PRIMARY KEY,
  nombre     VARCHAR(60)  NOT NULL,
  apellido   VARCHAR(60)  NOT NULL,
  documento  VARCHAR(20)  NOT NULL UNIQUE,
  telefono   VARCHAR(30)  NOT NULL,
  email      VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cabana (
  id_cabana    INT AUTO_INCREMENT PRIMARY KEY,
  numero       INT NOT NULL UNIQUE,
  capacidad    INT NOT NULL,
  tarifa_noche DECIMAL(10,2) NOT NULL,
  estado       ENUM('DISPONIBLE','OCUPADA','PENDIENTE_LIMPIEZA','MANTENIMIENTO')
               NOT NULL DEFAULT 'DISPONIBLE',
  CONSTRAINT chk_cabana_capacidad CHECK (capacidad > 0),
  CONSTRAINT chk_cabana_tarifa    CHECK (tarifa_noche > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reserva (
  id_reserva      INT AUTO_INCREMENT PRIMARY KEY,
  id_cliente      INT NOT NULL,
  id_cabana       INT NOT NULL,
  id_usuario      INT NOT NULL,
  fecha_ingreso   DATE NOT NULL,
  fecha_egreso    DATE NOT NULL,
  cant_huespedes  INT  NOT NULL,
  tarifa_aplicada DECIMAL(10,2) NOT NULL,
  estado          ENUM('CONFIRMADA','EN_CURSO','FINALIZADA','CANCELADA')
                  NOT NULL DEFAULT 'CONFIRMADA',
  fecha_alta      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
  CONSTRAINT fk_reserva_cabana  FOREIGN KEY (id_cabana)  REFERENCES cabana(id_cabana),
  CONSTRAINT fk_reserva_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  CONSTRAINT chk_reserva_fechas    CHECK (fecha_egreso > fecha_ingreso),
  CONSTRAINT chk_reserva_huespedes CHECK (cant_huespedes > 0),
  CONSTRAINT chk_reserva_tarifa    CHECK (tarifa_aplicada > 0),
  INDEX idx_reserva_cabana_fechas (id_cabana, fecha_ingreso, fecha_egreso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pago (
  id_pago     INT AUTO_INCREMENT PRIMARY KEY,
  id_reserva  INT NOT NULL,
  id_usuario  INT NOT NULL,
  fecha_hora  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  monto       DECIMAL(10,2) NOT NULL,
  tipo        ENUM('SENA','PAGO') NOT NULL,
  metodo_pago ENUM('EFECTIVO','TRANSFERENCIA','TARJETA') NOT NULL,
  CONSTRAINT fk_pago_reserva FOREIGN KEY (id_reserva) REFERENCES reserva(id_reserva),
  CONSTRAINT fk_pago_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
  CONSTRAINT chk_pago_monto  CHECK (monto > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS estadia (
  id_estadia          INT AUTO_INCREMENT PRIMARY KEY,
  id_reserva          INT NOT NULL UNIQUE,
  fecha_hora_checkin  DATETIME NULL,
  fecha_hora_checkout DATETIME NULL,
  CONSTRAINT fk_estadia_reserva FOREIGN KEY (id_reserva) REFERENCES reserva(id_reserva),
  CONSTRAINT chk_estadia_fechas CHECK (fecha_hora_checkout IS NULL
                                       OR fecha_hora_checkout >= fecha_hora_checkin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS log_operacion (
  id_log     INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  operacion  VARCHAR(50)  NOT NULL,
  detalle    VARCHAR(255) NULL,
  CONSTRAINT fk_log_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
