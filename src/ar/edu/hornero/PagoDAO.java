package ar.edu.hornero;

import java.math.BigDecimal;
import java.sql.Connection;

/** Contrato de acceso a datos de pagos (patrón DAO). */
public interface PagoDAO {
    /** @return el id generado por la base. */
    int insertar(Connection cn, int idReserva, int idUsuario, BigDecimal monto,
                 String tipo, String metodo) throws PersistenciaException;

    /** @return la suma de los pagos de la reserva (cero si no hay). */
    BigDecimal totalPagado(Connection cn, int idReserva) throws PersistenciaException;
}
