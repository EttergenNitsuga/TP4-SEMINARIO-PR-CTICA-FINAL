package ar.edu.hornero;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** Lógica de negocio: reglas, validaciones, transacciones y auditoría. No contiene SQL. */
public class ReservaController {
    private static final String[] TIPOS_PAGO = {"SENA", "PAGO"};
    private static final String[] METODOS_PAGO = {"EFECTIVO", "TRANSFERENCIA", "TARJETA"};

    /** Operación que se ejecuta con una conexión ya abierta. */
    @FunctionalInterface
    private interface Operacion<T> {
        T ejecutar(Connection cn) throws PersistenciaException, SQLException;
    }

    private final ClienteDAO clienteDAO;
    private final ReservaDAO reservaDAO;
    private final PagoDAO pagoDAO;
    private final GestorArchivos archivos;
    private final int idUsuario;

    public ReservaController(int idUsuario) {
        this(idUsuario, new ClienteDAOJdbc(), new ReservaDAOJdbc(), new PagoDAOJdbc(), new GestorArchivos());
    }

    public ReservaController(int idUsuario, ClienteDAO clienteDAO, ReservaDAO reservaDAO,
                             PagoDAO pagoDAO, GestorArchivos archivos) {
        this.idUsuario = idUsuario;
        this.clienteDAO = clienteDAO;
        this.reservaDAO = reservaDAO;
        this.pagoDAO = pagoDAO;
        this.archivos = archivos;
    }

    public static void validarFechas(LocalDate ingreso, LocalDate egreso) {
        if (ingreso == null || egreso == null || !egreso.isAfter(ingreso)) {
            throw new ReglaNegocioException("La fecha de egreso debe ser posterior a la de ingreso.");
        }
    }

    // ---------------------------------------------------------------- infraestructura

    /** Ejecuta una operación con una conexión que se cierra sola. */
    private <T> T consultar(Operacion<T> op) throws PersistenciaException {
        try (Connection cn = ConexionDB.getConexion()) {
            return op.ejecutar(cn);
        } catch (SQLException e) {
            throw new PersistenciaException("Error al cerrar la conexión con la base de datos.", e);
        }
    }

    /** Ejecuta una operación dentro de una transacción: commit si sale bien, rollback si falla. */
    private <T> T enTransaccion(Operacion<T> op) throws PersistenciaException {
        return consultar(cn -> {
            try {
                cn.setAutoCommit(false);
                T resultado = op.ejecutar(cn);
                cn.commit();
                return resultado;
            } catch (PersistenciaException | RuntimeException e) {
                revertir(cn);
                throw e;
            } catch (SQLException e) {
                revertir(cn);
                throw new PersistenciaException("No se pudo completar la transacción.", e);
            }
        });
    }

    private void revertir(Connection cn) {
        try {
            cn.rollback();
        } catch (SQLException e) {
            System.err.println("Advertencia: no se pudo revertir la transacción (" + e.getMessage() + ").");
        }
    }

    /** La bitácora es auxiliar: si falla, solo se informa una advertencia. */
    private void auditar(String operacion, String detalle) {
        try {
            archivos.registrarOperacion(idUsuario, operacion, detalle);
        } catch (ArchivoException e) {
            System.err.println("Advertencia: " + e.getMessage());
        }
    }

    private static boolean esValorPermitido(String[] permitidos, String valor) {
        return Arrays.asList(permitidos).contains(valor);
    }

    // ---------------------------------------------------------------- clientes

    /** @return el id generado del cliente. */
    public int registrarCliente(Cliente cliente) throws PersistenciaException {
        int id;
        try {
            id = consultar(cn -> clienteDAO.insertar(cn, cliente));
        } catch (PersistenciaException e) {
            if (e.esViolacionDeRestriccion()) {
                throw new ReglaNegocioException("Ya existe un cliente con el documento "
                        + cliente.getDocumento() + ".");
            }
            throw e;
        }
        auditar("REGISTRAR_CLIENTE", "id=" + id + " documento=" + cliente.getDocumento());
        return id;
    }

    /** @return el id del cliente o -1 si no existe. */
    public int buscarIdCliente(String documento) throws PersistenciaException {
        return consultar(cn -> clienteDAO.buscarIdPorDocumento(cn, documento));
    }

    // ---------------------------------------------------------------- reservas

    public List<Cabana> consultarDisponibilidad(LocalDate ingreso, LocalDate egreso)
            throws PersistenciaException {
        validarFechas(ingreso, egreso);
        return consultar(cn -> reservaDAO.buscarCabanasLibres(cn, ingreso, egreso));
    }

    public int registrarReserva(int idCliente, int idCabana, LocalDate ingreso, LocalDate egreso,
                                int huespedes) throws PersistenciaException {
        validarFechas(ingreso, egreso);
        int id = enTransaccion(cn -> {
            Cabana cab = reservaDAO.buscarCabanaPorId(cn, idCabana);
            if (cab == null) throw new ReglaNegocioException("La cabaña no existe.");
            if (huespedes < 1 || huespedes > cab.getCapacidad()) {
                throw new ReglaNegocioException("La cabaña " + cab.getNumero() + " admite entre 1 y "
                        + cab.getCapacidad() + " huéspedes.");
            }
            if (reservaDAO.existeSuperposicion(cn, idCabana, ingreso, egreso)) {
                throw new ReglaNegocioException("La cabaña no está disponible para las fechas indicadas.");
            }
            Reserva r = new Reserva(0, idCliente, idCabana, idUsuario, ingreso, egreso,
                    huespedes, cab.getTarifaNoche(), Reserva.Estado.CONFIRMADA);
            return reservaDAO.insertar(cn, r);
        });
        auditar("REGISTRAR_RESERVA", "id=" + id + " cabana=" + idCabana + " " + ingreso + " a " + egreso);
        return id;
    }

    /** Genera el comprobante TXT de una reserva existente. */
    public Path emitirComprobante(int idReserva) throws PersistenciaException, ArchivoException {
        Reserva r = consultar(cn -> reservaDAO.buscarPorId(cn, idReserva));
        if (r == null) throw new ReglaNegocioException("La reserva no existe.");
        Cliente cliente = consultar(cn -> clienteDAO.buscarPorId(cn, r.getIdCliente()));
        Cabana cabana = consultar(cn -> reservaDAO.buscarCabanaPorId(cn, r.getIdCabana()));
        if (cliente == null || cabana == null) {
            throw new ReglaNegocioException("La reserva tiene datos de cliente o cabaña inexistentes.");
        }
        Path destino = archivos.escribirComprobante(r, cliente, cabana);
        auditar("EMITIR_COMPROBANTE", "reserva=" + idReserva + " archivo=" + destino);
        return destino;
    }

    public void cancelarReserva(int idReserva) throws PersistenciaException {
        enTransaccion(cn -> {
            Reserva r = reservaDAO.buscarPorId(cn, idReserva);
            if (r == null) throw new ReglaNegocioException("La reserva no existe.");
            if (r.getEstado() != Reserva.Estado.CONFIRMADA) {
                throw new ReglaNegocioException("Solo pueden cancelarse reservas confirmadas.");
            }
            reservaDAO.actualizarEstado(cn, idReserva, Reserva.Estado.CANCELADA);
            return null;
        });
        auditar("CANCELAR_RESERVA", "reserva=" + idReserva);
    }

    // ---------------------------------------------------------------- pagos

    public int registrarPago(int idReserva, BigDecimal monto, String tipo, String metodo)
            throws PersistenciaException {
        if (monto == null || monto.signum() <= 0) {
            throw new ReglaNegocioException("El monto debe ser mayor a cero.");
        }
        String t = (tipo == null) ? "" : tipo.trim().toUpperCase();
        String m = (metodo == null) ? "" : metodo.trim().toUpperCase();
        if (!esValorPermitido(TIPOS_PAGO, t)) {
            throw new ReglaNegocioException("Tipo de pago inválido. Valores permitidos: "
                    + String.join("/", TIPOS_PAGO) + ".");
        }
        if (!esValorPermitido(METODOS_PAGO, m)) {
            throw new ReglaNegocioException("Método de pago inválido. Valores permitidos: "
                    + String.join("/", METODOS_PAGO) + ".");
        }
        int id = enTransaccion(cn -> {
            Reserva r = reservaDAO.buscarPorId(cn, idReserva);
            if (r == null) throw new ReglaNegocioException("La reserva no existe.");
            if (r.getEstado() == Reserva.Estado.CANCELADA) {
                throw new ReglaNegocioException("La reserva está cancelada.");
            }
            BigDecimal saldo = r.getMontoTotal().subtract(pagoDAO.totalPagado(cn, idReserva));
            if (monto.compareTo(saldo) > 0) {
                throw new ReglaNegocioException("El monto supera el saldo pendiente ($" + saldo + ").");
            }
            return pagoDAO.insertar(cn, idReserva, idUsuario, monto, t, m);
        });
        auditar("REGISTRAR_PAGO", "id=" + id + " reserva=" + idReserva + " monto=" + monto + " " + t + "/" + m);
        return id;
    }

    public BigDecimal calcularSaldo(int idReserva) throws PersistenciaException {
        return consultar(cn -> {
            Reserva r = reservaDAO.buscarPorId(cn, idReserva);
            if (r == null) throw new ReglaNegocioException("La reserva no existe.");
            return r.getMontoTotal().subtract(pagoDAO.totalPagado(cn, idReserva));
        });
    }

    // ---------------------------------------------------------------- check-in / check-out

    public void checkIn(int idReserva) throws PersistenciaException {
        enTransaccion(cn -> {
            Reserva r = reservaDAO.buscarPorId(cn, idReserva);
            if (r == null) throw new ReglaNegocioException("La reserva no existe.");
            if (r.getEstado() != Reserva.Estado.CONFIRMADA) {
                throw new ReglaNegocioException("La reserva no está vigente para check-in.");
            }
            reservaDAO.registrarCheckIn(cn, idReserva);
            reservaDAO.actualizarEstado(cn, idReserva, Reserva.Estado.EN_CURSO);
            reservaDAO.actualizarEstadoCabana(cn, r.getIdCabana(), "OCUPADA");
            return null;
        });
        auditar("CHECK_IN", "reserva=" + idReserva);
    }

    /** @return el saldo pendiente al momento del check-out. */
    public BigDecimal checkOut(int idReserva) throws PersistenciaException {
        BigDecimal saldo = enTransaccion(cn -> {
            Reserva r = reservaDAO.buscarPorId(cn, idReserva);
            if (r == null) throw new ReglaNegocioException("La reserva no existe.");
            if (r.getEstado() != Reserva.Estado.EN_CURSO) {
                throw new ReglaNegocioException("La reserva no está en curso.");
            }
            BigDecimal pendiente = r.getMontoTotal().subtract(pagoDAO.totalPagado(cn, idReserva));
            reservaDAO.registrarCheckOut(cn, idReserva);
            reservaDAO.actualizarEstado(cn, idReserva, Reserva.Estado.FINALIZADA);
            reservaDAO.actualizarEstadoCabana(cn, r.getIdCabana(), "PENDIENTE_LIMPIEZA");
            return pendiente;
        });
        auditar("CHECK_OUT", "reserva=" + idReserva + " saldo=" + saldo);
        return saldo;
    }

    // ---------------------------------------------------------------- reportes y bitácora

    public ReporteOcupacion generarReporte(int anio) throws PersistenciaException {
        if (anio < 2000 || anio > 2100) {
            throw new ReglaNegocioException("Año fuera de rango (2000-2100).");
        }
        List<Reserva> reservas = consultar(cn -> reservaDAO.listarPorAnio(cn, anio));
        return ReporteOcupacion.calcular(anio, reservas);
    }

    public Path exportarReporte(ReporteOcupacion rep) throws ArchivoException {
        Path destino = archivos.exportarReporteCsv(rep);
        auditar("EXPORTAR_REPORTE", "anio=" + rep.getAnio() + " archivo=" + destino);
        return destino;
    }

    public List<String> ultimasOperaciones(int n) throws ArchivoException {
        return archivos.leerUltimasOperaciones(n);
    }
}
