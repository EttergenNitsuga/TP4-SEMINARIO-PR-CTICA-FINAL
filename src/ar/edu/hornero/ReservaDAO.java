package ar.edu.hornero;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

/** Contrato de acceso a datos de reservas, cabañas y estadías (patrón DAO). */
public interface ReservaDAO {
    List<Cabana> buscarCabanasLibres(Connection cn, LocalDate ingreso, LocalDate egreso)
            throws PersistenciaException;

    boolean existeSuperposicion(Connection cn, int idCabana, LocalDate ingreso, LocalDate egreso)
            throws PersistenciaException;

    int insertar(Connection cn, Reserva reserva) throws PersistenciaException;

    /** @return la reserva o null si no existe. */
    Reserva buscarPorId(Connection cn, int idReserva) throws PersistenciaException;

    /** Reservas (de cualquier estado) cuyo período toca el año indicado. */
    List<Reserva> listarPorAnio(Connection cn, int anio) throws PersistenciaException;

    /** @return la cabaña o null si no existe. */
    Cabana buscarCabanaPorId(Connection cn, int idCabana) throws PersistenciaException;

    void actualizarEstado(Connection cn, int idReserva, Reserva.Estado estado) throws PersistenciaException;

    void actualizarEstadoCabana(Connection cn, int idCabana, String estado) throws PersistenciaException;

    void registrarCheckIn(Connection cn, int idReserva) throws PersistenciaException;

    void registrarCheckOut(Connection cn, int idReserva) throws PersistenciaException;
}
