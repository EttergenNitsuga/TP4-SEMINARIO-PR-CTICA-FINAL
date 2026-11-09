package ar.edu.hornero;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Único punto de creación de conexiones JDBC a MySQL. */
public class ConexionDB {
    private static final String HOST = System.getenv().getOrDefault("HORNERO_DB_HOST", "localhost");
    private static final String URL  = "jdbc:mysql://" + HOST + ":3306/cabanas_el_hornero"
            + "?useSSL=true&serverTimezone=America/Argentina/Cordoba&characterEncoding=UTF-8";
    private static final String USER = System.getenv().getOrDefault("HORNERO_DB_USER", "hornero_app");
    private static final String PASS = System.getenv().getOrDefault("HORNERO_DB_PASS", "");

    private ConexionDB() {}

    public static Connection getConexion() throws PersistenciaException {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar a la base de datos MySQL.", e);
        }
    }
}
