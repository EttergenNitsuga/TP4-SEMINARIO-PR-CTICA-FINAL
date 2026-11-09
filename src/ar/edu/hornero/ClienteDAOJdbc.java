package ar.edu.hornero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Implementación JDBC de {@link ClienteDAO}. */
public class ClienteDAOJdbc implements ClienteDAO {

    @Override
    public int insertar(Connection cn, Cliente c) throws PersistenciaException {
        String sql = "INSERT INTO cliente (nombre, apellido, documento, telefono, email) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getDocumento());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getEmail());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo registrar el cliente.", e);
        }
    }

    @Override
    public int buscarIdPorDocumento(Connection cn, String documento) throws PersistenciaException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT id_cliente FROM cliente WHERE documento = ?")) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo buscar el cliente por documento.", e);
        }
    }

    @Override
    public Cliente buscarPorId(Connection cn, int idCliente) throws PersistenciaException {
        String sql = "SELECT nombre, apellido, documento, telefono, email FROM cliente WHERE id_cliente = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Cliente(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo buscar el cliente.", e);
        }
    }
}
