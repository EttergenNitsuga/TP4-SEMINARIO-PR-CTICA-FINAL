package ar.edu.hornero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reserva {
    public enum Estado { CONFIRMADA, EN_CURSO, FINALIZADA, CANCELADA }

    private int idReserva;
    private final int idCliente;
    private final int idCabana;
    private final int idUsuario;
    private final LocalDate fechaIngreso;
    private final LocalDate fechaEgreso;
    private final int cantHuespedes;
    private final BigDecimal tarifaAplicada;
    private Estado estado;

    public Reserva(int idReserva, int idCliente, int idCabana, int idUsuario, LocalDate fechaIngreso,
                   LocalDate fechaEgreso, int cantHuespedes, BigDecimal tarifaAplicada, Estado estado) {
        this.idReserva = idReserva;
        this.idCliente = idCliente;
        this.idCabana = idCabana;
        this.idUsuario = idUsuario;
        this.fechaIngreso = fechaIngreso;
        this.fechaEgreso = fechaEgreso;
        this.cantHuespedes = cantHuespedes;
        this.tarifaAplicada = tarifaAplicada;
        this.estado = estado;
    }

    public long getNoches() { return ChronoUnit.DAYS.between(fechaIngreso, fechaEgreso); }
    public BigDecimal getMontoTotal() { return tarifaAplicada.multiply(BigDecimal.valueOf(getNoches())); }

    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int id) { this.idReserva = id; }
    public int getIdCliente() { return idCliente; }
    public int getIdCabana() { return idCabana; }
    public int getIdUsuario() { return idUsuario; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public LocalDate getFechaEgreso() { return fechaEgreso; }
    public int getCantHuespedes() { return cantHuespedes; }
    public BigDecimal getTarifaAplicada() { return tarifaAplicada; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
}
