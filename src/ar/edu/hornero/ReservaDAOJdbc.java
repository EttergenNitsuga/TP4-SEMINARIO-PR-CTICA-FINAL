package ar.edu.hornero;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Implementación JDBC de {@link ReservaDAO}: única clase que conoce el SQL de reservas. */
public class ReservaDAOJdbc implements ReservaDAO {

    @Override
    public List<Cabana> buscarCabanasLibres(Connection cn, LocalDate ingreso, LocalDate egreso)
            throws PersistenciaException {
        String sql = "SELECT c.id_cabana, c.numero, c.capacidad, c.tarifa_noche FROM cabana c "
                + "WHERE c.estado <> 'MANTENIMIENTO' AND NOT EXISTS ("
                + " SELECT 1 FROM reserva r WHERE r.id_cabana = c.id_cabana"
                + " AND r.estado IN ('CONFIRMADA','EN_CURSO')"
                + " AND r.fecha_ingreso < ? AND r.fecha_egreso > ?) ORDER BY c.numero";
        List<Cabana> res = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(egreso));
            ps.setDate(2, Date.valueOf(ingreso));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    res.add(new Cabana(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getBigDecimal(4)));
                }
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar la disponibilidad de cabañas.", e);
        }
        return res;
    }

    @Override
    public boolean existeSuperposicion(Connection cn, int idCabana, LocalDate ingreso, LocalDate egreso)
            throws PersistenciaException {
        String sql = "SELECT COUNT(*) FROM reserva WHERE id_cabana = ? AND estado IN ('CONFIRMADA','EN_CURSO') "
                + "AND fecha_ingreso < ? AND fecha_egreso > ? FOR UPDATE";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCabana);
            ps.setDate(2, Date.valueOf(egreso));
            ps.setDate(3, Date.valueOf(ingreso));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo verificar la superposición de reservas.", e);
        }
    }

    @Override
    public int insertar(Connection cn, Reserva r) throws PersistenciaException {
        String sql = "INSERT INTO reserva (id_cliente, id_cabana, id_usuario, fecha_ingreso, fecha_egreso, "
                + "cant_huespedes, tarifa_aplicada, estado) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getIdCliente());
            ps.setInt(2, r.getIdCabana());
            ps.setInt(3, r.getIdUsuario());
            ps.setDate(4, Date.valueOf(r.getFechaIngreso()));
            ps.setDate(5, Date.valueOf(r.getFechaEgreso()));
            ps.setInt(6, r.getCantHuespedes());
            ps.setBigDecimal(7, r.getTarifaAplicada());
            ps.setString(8, r.getEstado().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo registrar la reserva.", e);
        }
    }

    @Override
    public Reserva buscarPorId(Connection cn, int idReserva) throws PersistenciaException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM reserva WHERE id_reserva = ?")) {
            ps.setInt(1, idReserva);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo buscar la reserva.", e);
        }
    }

    @Override
    public List<Reserva> listarPorAnio(Connection cn, int anio) throws PersistenciaException {
        String sql = "SELECT * FROM reserva WHERE fecha_ingreso <= ? AND fecha_egreso > ? "
                + "ORDER BY fecha_ingreso";
        List<Reserva> res = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.of(anio, 12, 31)));
            ps.setDate(2, Date.valueOf(LocalDate.of(anio, 1, 1)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) res.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron listar las reservas del año " + anio + ".", e);
        }
        return res;
    }

    @Override
    public Cabana buscarCabanaPorId(Connection cn, int idCabana) throws PersistenciaException {
        String sql = "SELECT id_cabana, numero, capacidad, tarifa_noche FROM cabana WHERE id_cabana = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCabana);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Cabana(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getBigDecimal(4));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo buscar la cabaña.", e);
        }
    }

    @Override
    public void actualizarEstado(Connection cn, int idReserva, Reserva.Estado estado) throws PersistenciaException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE reserva SET estado = ? WHERE id_reserva = ?")) {
            ps.setString(1, estado.name());
            ps.setInt(2, idReserva);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo actualizar el estado de la reserva.", e);
        }
    }

    @Override
    public void actualizarEstadoCabana(Connection cn, int idCabana, String estado) throws PersistenciaException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE cabana SET estado = ? WHERE id_cabana = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idCabana);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo actualizar el estado de la cabaña.", e);
        }
    }

    @Override
    public void registrarCheckIn(Connection cn, int idReserva) throws PersistenciaException {
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO estadia (id_reserva, fecha_hora_checkin) VALUES (?, NOW())")) {
            ps.setInt(1, idReserva);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo registrar el check-in.", e);
        }
    }

    @Override
    public void registrarCheckOut(Connection cn, int idReserva) throws PersistenciaException {
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE estadia SET fecha_hora_checkout = NOW() WHERE id_reserva = ?")) {
            ps.setInt(1, idReserva);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo registrar el check-out.", e);
        }
    }

    private Reserva mapear(ResultSet rs) throws SQLException {
        BigDecimal tarifa = rs.getBigDecimal("tarifa_aplicada");
        return new Reserva(rs.getInt("id_reserva"), rs.getInt("id_cliente"), rs.getInt("id_cabana"),
                rs.getInt("id_usuario"), rs.getDate("fecha_ingreso").toLocalDate(),
                rs.getDate("fecha_egreso").toLocalDate(), rs.getInt("cant_huespedes"),
                tarifa, Reserva.Estado.valueOf(rs.getString("estado")));
    }
}
