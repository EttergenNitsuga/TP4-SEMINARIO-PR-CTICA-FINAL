package ar.edu.hornero;

import java.sql.SQLException;

/**
 * Excepción verificada de la capa de acceso a datos. Envuelve la SQLException de JDBC
 * para que las capas superiores no dependan de java.sql y reciban un mensaje claro.
 */
public class PersistenciaException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String sqlState;

    public PersistenciaException(String mensaje, SQLException causa) {
        super(mensaje, causa);
        this.sqlState = (causa != null) ? causa.getSQLState() : null;
    }

    public String getSqlState() { return sqlState; }

    /** SQLSTATE 23xxx = violación de restricción de integridad (UNIQUE, FOREIGN KEY, CHECK). */
    public boolean esViolacionDeRestriccion() {
        return sqlState != null && sqlState.startsWith("23");
    }
}
