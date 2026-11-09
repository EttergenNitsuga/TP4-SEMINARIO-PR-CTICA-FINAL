package ar.edu.hornero;

import java.time.LocalDate;
import java.util.List;

/** Reporte anual de ocupación: noches por mes (int[12]) y reservas por estado (arreglo por enum). */
public class ReporteOcupacion {
    public static final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

    private final int anio;
    private final int[] nochesPorMes = new int[12];
    private final Reserva.Estado[] estados = Reserva.Estado.values();
    private final int[] reservasPorEstado = new int[estados.length];

    private ReporteOcupacion(int anio) { this.anio = anio; }

    /** Calcula el reporte a partir de la lista (ArrayList) de reservas que tocan el año. */
    public static ReporteOcupacion calcular(int anio, List<Reserva> reservas) {
        ReporteOcupacion rep = new ReporteOcupacion(anio);
        for (Reserva r : reservas) {
            rep.reservasPorEstado[r.getEstado().ordinal()]++;
            if (r.getEstado() == Reserva.Estado.CANCELADA) continue; // no ocupa la cabaña
            for (LocalDate d = r.getFechaIngreso(); d.isBefore(r.getFechaEgreso()); d = d.plusDays(1)) {
                if (d.getYear() == anio) rep.nochesPorMes[d.getMonthValue() - 1]++;
            }
        }
        return rep;
    }

    public int getAnio() { return anio; }
    public int[] getNochesPorMes() { return nochesPorMes.clone(); }
    public Reserva.Estado[] getEstados() { return estados.clone(); }
    public int[] getReservasPorEstado() { return reservasPorEstado.clone(); }

    public int getTotalNoches() {
        int total = 0;
        for (int n : nochesPorMes) total += n;
        return total;
    }

    /** @return índice (0-11) del mes con más noches, o -1 si no hay ocupación. */
    public int mesDeMayorOcupacion() {
        int mejor = -1;
        int max = 0;
        for (int m = 0; m < nochesPorMes.length; m++) {
            if (nochesPorMes[m] > max) {
                max = nochesPorMes[m];
                mejor = m;
            }
        }
        return mejor;
    }
}
