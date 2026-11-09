-- ============================================================================
-- Cabañas El Hornero - 03_reiniciar_demo.sql
-- Vuelve la BASE DE DEMOSTRACIÓN al estado de 02_datos_prueba.sql después de
-- haber probado el menú. BORRA lo creado desde el programa (clientes con id
-- mayor a 2, reservas con id mayor a 3, sus pagos y estadías).
-- No ejecutar sobre una base con datos reales. Ejecutar como administrador.
-- La bitacora se guarda en salida/logs/operaciones.log (archivo externo a MySQL):
-- para repetir el caso 17 desde cero, eliminar ese archivo manualmente desde Windows.
-- ============================================================================
USE cabanas_el_hornero;

DELETE FROM pago    WHERE id_pago    > 2;
DELETE FROM estadia WHERE id_estadia > 1;
DELETE FROM reserva WHERE id_reserva > 3;
DELETE FROM cliente WHERE id_cliente > 2;

UPDATE reserva SET estado = 'CONFIRMADA' WHERE id_reserva IN (1, 2);
UPDATE reserva SET estado = 'FINALIZADA' WHERE id_reserva = 3;
UPDATE cabana  SET estado = 'DISPONIBLE';
