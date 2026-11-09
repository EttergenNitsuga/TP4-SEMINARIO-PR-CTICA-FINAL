package ar.edu.hornero;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Persistencia en archivos: reporte CSV, comprobante de reserva TXT y bitácora de operaciones. */
public class GestorArchivos {
    private static final DateTimeFormatter MARCA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path dirBase;

    public GestorArchivos() { this(Paths.get("salida")); }

    public GestorArchivos(Path dirBase) { this.dirBase = dirBase; }

    /** Exporta el reporte a un CSV delimitado por punto y coma (compatible con Excel en Argentina). */
    public Path exportarReporteCsv(ReporteOcupacion rep) throws ArchivoException {
        Path destino = dirBase.resolve("reportes").resolve("ocupacion_" + rep.getAnio() + ".csv");
        try {
            Files.createDirectories(destino.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
                w.write("mes;noches_reservadas");
                w.newLine();
                int[] noches = rep.getNochesPorMes();
                for (int m = 0; m < noches.length; m++) {
                    w.write(ReporteOcupacion.MESES[m] + ";" + noches[m]);
                    w.newLine();
                }
                w.write("TOTAL;" + rep.getTotalNoches());
                w.newLine();
                w.newLine();
                w.write("estado_reserva;cantidad");
                w.newLine();
                Reserva.Estado[] estados = rep.getEstados();
                int[] cant = rep.getReservasPorEstado();
                for (int e = 0; e < estados.length; e++) {
                    w.write(estados[e] + ";" + cant[e]);
                    w.newLine();
                }
            }
        } catch (IOException e) {
            throw new ArchivoException("No se pudo escribir el reporte en " + destino + ".", e);
        }
        return destino;
    }

    /** Emite un comprobante TXT para la reserva. */
    public Path escribirComprobante(Reserva r, Cliente cliente, Cabana cabana) throws ArchivoException {
        Path destino = dirBase.resolve("comprobantes").resolve("reserva_" + r.getIdReserva() + ".txt");
        try {
            Files.createDirectories(destino.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
                String[] lineas = {
                    "CABAÑAS EL HORNERO - COMPROBANTE DE RESERVA",
                    "--------------------------------------------",
                    "Reserva N°     : " + r.getIdReserva(),
                    "Estado         : " + r.getEstado(),
                    cliente.obtenerDetalles(),
                    "Alojamiento    : " + cabana,
                    "Ingreso        : " + r.getFechaIngreso(),
                    "Egreso         : " + r.getFechaEgreso(),
                    "Noches         : " + r.getNoches(),
                    "Huéspedes      : " + r.getCantHuespedes(),
                    "Tarifa x noche : $" + r.getTarifaAplicada(),
                    "MONTO TOTAL    : $" + r.getMontoTotal(),
                    "Emitido        : " + LocalDateTime.now().format(MARCA)
                };
                for (String linea : lineas) {
                    w.write(linea);
                    w.newLine();
                }
            }
        } catch (IOException e) {
            throw new ArchivoException("No se pudo escribir el comprobante en " + destino + ".", e);
        }
        return destino;
    }

    /** Agrega una línea a la bitácora (modo APPEND: no pisa lo anterior). */
    public void registrarOperacion(int idUsuario, String operacion, String detalle) throws ArchivoException {
        Path log = dirBase.resolve("logs").resolve("operaciones.log");
        try {
            Files.createDirectories(log.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(log, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                w.write(LocalDateTime.now().format(MARCA) + " | usuario=" + idUsuario
                        + " | " + operacion + " | " + detalle);
                w.newLine();
            }
        } catch (IOException e) {
            throw new ArchivoException("No se pudo escribir en la bitácora " + log + ".", e);
        }
    }

    /** Lee la bitácora y devuelve, en una lista, sus últimas n líneas. */
    public List<String> leerUltimasOperaciones(int n) throws ArchivoException {
        Path log = dirBase.resolve("logs").resolve("operaciones.log");
        List<String> ultimas = new ArrayList<>();
        if (!Files.exists(log)) return ultimas;
        try (BufferedReader br = Files.newBufferedReader(log, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                ultimas.add(linea);
                if (ultimas.size() > n) ultimas.remove(0);
            }
        } catch (IOException e) {
            throw new ArchivoException("No se pudo leer la bitácora " + log + ".", e);
        }
        return ultimas;
    }
}
