-- ============================================================================
-- Cabañas El Hornero - 02_datos_prueba.sql
-- Datos de demostración con los que se reproducen los casos de la Tabla 9 del
-- informe TP4. Ejecutar DESPUÉS de 01_crear_tablas.sql, sobre una base nueva y
-- vacía (son INSERT simples: si ya hay filas con esos ids, MySQL dará error y
-- no pisará nada).
-- Para volver a este estado después de probar, usar 03_reiniciar_demo.sql.
-- ============================================================================
USE cabanas_el_hornero;

-- Usuarios. El prototipo usa id_usuario = 2 (recepcionista). La contraseña es
-- un valor de relleno: la autenticación todavía no se implementó en Java.
INSERT INTO usuario (id_usuario, nombre_usuario, contrasena_hash, rol, activo) VALUES
  (1, 'admin1', 'HASH_DEMO_NO_USAR', 'ADMINISTRADOR', 1),
  (2, 'recep1', 'HASH_DEMO_NO_USAR', 'RECEPCIONISTA', 1);

-- Clientes. El documento 30111222 es el de los casos 3 y 11.
INSERT INTO cliente (id_cliente, nombre, apellido, documento, telefono, email) VALUES
  (1, 'María',  'González', '30111222', '3543-501111', 'maria.gonzalez@example.com'),
  (2, 'Carlos', 'Ruiz',     '28555444', '3543-502222', 'carlos.ruiz@example.com');

-- Cabañas. La 1 admite 2 huéspedes (caso 12) y cuesta $45.000 (caso 3).
INSERT INTO cabana (id_cabana, numero, capacidad, tarifa_noche, estado) VALUES
  (1, 1, 2, 45000.00, 'DISPONIBLE'),
  (2, 2, 4, 60000.00, 'DISPONIBLE'),
  (3, 3, 4, 70000.00, 'DISPONIBLE'),
  (4, 4, 6, 95000.00, 'DISPONIBLE');

-- Reservas.
--  1: cabaña 2, 9 al 12/10/2026, 3 noches a $70.000 = $210.000, CONFIRMADA.
--     La tarifa aplicada histórica es $70.000/noche aunque la tarifa vigente de
--     la cabaña 2 sea $60.000; el total de la reserva usa tarifa_aplicada.
--  2: cabaña 4, 10 al 13/10/2026: deja libres solo las cabañas 1 y 3 el 10/10 (caso 1).
--  3: cabaña 1, septiembre, FINALIZADA y pagada completa (caso 6).
INSERT INTO reserva (id_reserva, id_cliente, id_cabana, id_usuario, fecha_ingreso, fecha_egreso,
                     cant_huespedes, tarifa_aplicada, estado, fecha_alta) VALUES
  (1, 1, 2, 2, '2026-10-09', '2026-10-12', 4, 70000.00, 'CONFIRMADA', '2026-09-20 10:00:00'),
  (2, 2, 4, 2, '2026-10-10', '2026-10-13', 5, 95000.00, 'CONFIRMADA', '2026-09-22 11:30:00'),
  (3, 1, 1, 2, '2026-09-01', '2026-09-04', 2, 45000.00, 'FINALIZADA', '2026-08-15 09:00:00');

-- Pagos. Reserva 1: seña de $70.000, queda saldo de $140.000 (caso 8).
INSERT INTO pago (id_pago, id_reserva, id_usuario, fecha_hora, monto, tipo, metodo_pago) VALUES
  (1, 1, 2, '2026-09-20 10:05:00',  70000.00, 'SENA', 'EFECTIVO'),
  (2, 3, 2, '2026-09-01 13:45:00', 135000.00, 'PAGO', 'TRANSFERENCIA');

-- Estadía de la reserva finalizada.
INSERT INTO estadia (id_estadia, id_reserva, fecha_hora_checkin, fecha_hora_checkout) VALUES
  (1, 3, '2026-09-01 14:00:00', '2026-09-04 10:00:00');
