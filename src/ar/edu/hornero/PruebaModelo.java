package ar.edu.hornero;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Prueba del modelo POO, las reglas, los arreglos y los archivos sin conexión a la base de datos. */
public class PruebaModelo {
    public static void main(String[] args) throws Exception {
        // 1) Abstracción, herencia y polimorfismo: referencia de tipo Persona, objeto Cliente
        Cliente cliente = new Cliente("María", "González", "30111222", "3543-501111", "maria.gonzalez@mail.com");
        Persona p = cliente;
        System.out.println("[1] " + p.obtenerDetalles());

        // 2) Estructura de datos (List) + ordenamiento (Comparable / Collections.sort)
        List<Cabana> lista = new ArrayList<>();
        lista.add(new Cabana(4, 4, 6, new BigDecimal("95000.00")));
        lista.add(new Cabana(1, 1, 2, new BigDecimal("45000.00")));
        Cabana cab3 = new Cabana(3, 3, 4, new BigDecimal("70000.00"));
        lista.add(cab3);
        Collections.sort(lista);
        System.out.println("[2] Cabañas ordenadas por tarifa:");
        lista.forEach(c -> System.out.println("     - id " + c.getIdCabana() + ": " + c));

        // 3) Cálculo del monto total en la entidad Reserva (3 noches x $70.000)
        Reserva r = new Reserva(1, 1, 3, 2, LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 12),
                4, new BigDecimal("70000.00"), Reserva.Estado.CONFIRMADA);
        System.out.println("[3] Noches: " + r.getNoches() + " | Monto total: $" + r.getMontoTotal());

        // 4) Excepción propia: egreso anterior al ingreso
        try {
            ReservaController.validarFechas(LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 10));
        } catch (ReglaNegocioException ex) {
            System.out.println("[4] ReglaNegocioException: " + ex.getMessage());
        }

        // 5) Reporte con arreglos int[12] y conteo por estado, sobre una lista de reservas
        List<Reserva> reservas = new ArrayList<>();
        reservas.add(r);
        reservas.add(new Reserva(2, 1, 1, 2, LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 3),
                2, new BigDecimal("45000.00"), Reserva.Estado.FINALIZADA));
        reservas.add(new Reserva(3, 1, 4, 2, LocalDate.of(2026, 12, 30), LocalDate.of(2027, 1, 2),
                5, new BigDecimal("95000.00"), Reserva.Estado.CONFIRMADA));
        reservas.add(new Reserva(4, 1, 1, 2, LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 23),
                2, new BigDecimal("45000.00"), Reserva.Estado.CANCELADA));
        ReporteOcupacion rep = ReporteOcupacion.calcular(2026, reservas);
        System.out.println("[5] Noches reservadas por mes en 2026 (meses sin ocupación omitidos):");
        int[] noches = rep.getNochesPorMes();
        for (int m = 0; m < noches.length; m++) {
            if (noches[m] > 0) System.out.println("     - " + ReporteOcupacion.MESES[m] + ": " + noches[m]);
        }
        System.out.println("     Total: " + rep.getTotalNoches()
                + " | Mes de mayor ocupación: " + ReporteOcupacion.MESES[rep.mesDeMayorOcupacion()]);
        StringBuilder sb = new StringBuilder("     Reservas por estado:");
        Reserva.Estado[] estados = rep.getEstados();
        int[] cant = rep.getReservasPorEstado();
        for (int e = 0; e < estados.length; e++) sb.append(' ').append(estados[e]).append('=').append(cant[e]);
        System.out.println(sb);

        // 6) Archivos: CSV, comprobante y bitácora en un directorio temporal, y su lectura posterior
        Path tmp = Files.createTempDirectory("hornero_prueba");
        GestorArchivos gestor = new GestorArchivos(tmp);
        Path csv = gestor.exportarReporteCsv(rep);
        Path txt = gestor.escribirComprobante(r, cliente, cab3);
        gestor.registrarOperacion(2, "REGISTRAR_RESERVA", "id=1 cabana=3 2026-10-09 a 2026-10-12");
        gestor.registrarOperacion(2, "CHECK_IN", "reserva=1");
        System.out.println("[6] Archivos generados: " + tmp.relativize(csv).toString().replace('\\', '/')
                + ", " + tmp.relativize(txt).toString().replace('\\', '/') + ", logs/operaciones.log");
        System.out.println("    Contenido de " + csv.getFileName() + ":");
        Files.readAllLines(csv).forEach(l -> System.out.println("     " + l));
        System.out.println("    Contenido de " + txt.getFileName() + ":");
        Files.readAllLines(txt).forEach(l -> System.out.println("     " + l));
        System.out.println("    Bitácora leída (últimas 5 líneas):");
        gestor.leerUltimasOperaciones(5).forEach(l -> System.out.println("     " + l));

        // 7) Excepción de persistencia: detección de violación UNIQUE a partir del SQLSTATE 23000
        PersistenciaException pe = new PersistenciaException("No se pudo registrar el cliente.",
                new SQLException("Duplicate entry", "23000"));
        System.out.println("[7] PersistenciaException: SQLSTATE=" + pe.getSqlState()
                + " | violación de restricción: " + pe.esViolacionDeRestriccion());
    }
}
