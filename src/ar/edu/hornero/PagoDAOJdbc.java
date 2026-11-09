package ar.edu.hornero;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Implementación JDBC de {@link PagoDAO}. */
public class PagoDAOJdbc implements PagoDAO {

    @Override
    public int insertar(Connection cn, int idReserva, int idUsuario, BigDecimal monto,
                        String tipo, String metodo) throws PersistenciaException {
        String sql = "INSERT INTO pago (id_reserva, id_usuario, monto, tipo, metodo_pago) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idReserva);
            ps.setInt(2, idUsuario);
            ps.setBigDecimal(3, monto);
            ps.setString(4, tipo);
            ps.setString(5, metodo);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo registrar el pago.", e);
        }
    }

    @Override
    public BigDecimal totalPagado(Connection cn, int idReserva) throws PersistenciaException {
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT COALESCE(SUM(monto),0) FROM pago WHERE id_reserva = ?")) {
            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo calcular el total pagado.", e);
        }
    }
}
