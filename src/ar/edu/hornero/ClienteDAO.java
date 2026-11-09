package ar.edu.hornero;

import java.sql.Connection;

/** Contrato de acceso a datos de clientes (patrón DAO). */
public interface ClienteDAO {
    /** @return el id generado por la base. */
    int insertar(Connection cn, Cliente cliente) throws PersistenciaException;

    /** @return el id del cliente o -1 si no existe. */
    int buscarIdPorDocumento(Connection cn, String documento) throws PersistenciaException;

    /** @return el cliente o null si no existe. */
    Cliente buscarPorId(Connection cn, int idCliente) throws PersistenciaException;
}
