package ar.edu.hornero;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static final int ID_USUARIO_DEMO = 2; // recepcionista 'recep1' cargado en 02_datos_prueba.sql

    /** Opciones del menú: se recorren con un for para imprimirlas. */
    private static final String[] OPCIONES_MENU = {
        "1) Registrar cliente", "2) Consultar disponibilidad", "3) Registrar reserva",
        "4) Cancelar reserva", "5) Registrar pago", "6) Ver saldo",
        "7) Check-in", "8) Check-out", "9) Reporte de ocupación (CSV)",
        "10) Ver bitácora de operaciones", "0) Salir"
    };

    public static void main(String[] args) {
        ReservaController ctl = new ReservaController(ID_USUARIO_DEMO);
        Scanner in = new Scanner(System.in);
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== CABAÑAS EL HORNERO - Gestión de Reservas ===");
            for (int k = 0; k < OPCIONES_MENU.length; k++) {
                System.out.println(OPCIONES_MENU[k]);
            }
            System.out.print("Opción: ");
            try {
                String op = in.nextLine().trim();
                switch (op) {
                    case "1" -> {
                        System.out.print("Nombre: "); String n = in.nextLine();
                        System.out.print("Apellido: "); String a = in.nextLine();
                        System.out.print("Documento: "); String d = in.nextLine();
                        System.out.print("Teléfono: "); String t = in.nextLine();
                        System.out.print("E-mail: "); String e = in.nextLine();

                        // Creación de objeto Cliente (encapsulamiento y herencia)
                        Cliente nuevoCliente = new Cliente(n, a, d, t, e);
                        int idGen = ctl.registrarCliente(nuevoCliente);
                        System.out.println("Cliente registrado con id " + idGen);
                        System.out.println(nuevoCliente.obtenerDetalles()); // Llamada polimórfica
                    }
                    case "2" -> {
                        LocalDate i = pedirFecha(in, "Ingreso (AAAA-MM-DD): ");
                        LocalDate e = pedirFecha(in, "Egreso  (AAAA-MM-DD): ");
                        List<Cabana> libres = ctl.consultarDisponibilidad(i, e);
                        if (libres.isEmpty()) {
                            System.out.println("No hay cabañas disponibles para ese período.");
                        } else {
                            // Algoritmo de ordenación sobre la estructura de datos (List)
                            Collections.sort(libres);
                            System.out.println("\n-- Cabañas Disponibles (Ordenadas por tarifa) --");
                            libres.forEach(c -> System.out.println(" - id " + c.getIdCabana() + ": " + c));
                        }
                    }
                    case "3" -> {
                        System.out.print("Documento del cliente: ");
                        int idCli = ctl.buscarIdCliente(in.nextLine().trim());
                        if (idCli < 0) {
                            System.out.println("Cliente inexistente. Regístrelo primero (opción 1).");
                            break;
                        }
                        int idCab = pedirEntero(in, "Id de cabaña: ");
                        LocalDate i = pedirFecha(in, "Ingreso (AAAA-MM-DD): ");
                        LocalDate e = pedirFecha(in, "Egreso  (AAAA-MM-DD): ");
                        int h = pedirEntero(in, "Cantidad de huéspedes: ");
                        int idRes = ctl.registrarReserva(idCli, idCab, i, e, h);
                        System.out.println("Reserva registrada con id " + idRes);
                        try {
                            Path comprobante = ctl.emitirComprobante(idRes);
                            System.out.println("Comprobante generado: " + comprobante);
                        } catch (ArchivoException | PersistenciaException ex) {
                            // La reserva ya está confirmada: el comprobante es secundario.
                            System.out.println("Aviso: reserva registrada, pero no se pudo emitir "
                                    + "el comprobante (" + ex.getMessage() + ").");
                        }
                    }
                    case "4" -> {
                        ctl.cancelarReserva(pedirEntero(in, "Id de reserva: "));
                        System.out.println("Reserva cancelada.");
                    }
                    case "5" -> {
                        int id = pedirEntero(in, "Id de reserva: ");
                        System.out.print("Monto: "); BigDecimal m = new BigDecimal(in.nextLine().trim());
                        System.out.print("Tipo (SENA/PAGO): "); String tipo = in.nextLine();
                        System.out.print("Método (EFECTIVO/TRANSFERENCIA/TARJETA): "); String met = in.nextLine();
                        System.out.println("Pago registrado con id " + ctl.registrarPago(id, m, tipo, met));
                    }
                    case "6" -> System.out.println("Saldo pendiente: $"
                            + ctl.calcularSaldo(pedirEntero(in, "Id de reserva: ")));
                    case "7" -> {
                        ctl.checkIn(pedirEntero(in, "Id de reserva: "));
                        System.out.println("Check-in registrado.");
                    }
                    case "8" -> {
                        BigDecimal s = ctl.checkOut(pedirEntero(in, "Id de reserva: "));
                        System.out.println("Check-out registrado."
                                + (s.signum() > 0 ? " ATENCIÓN: saldo pendiente $" + s : ""));
                    }
                    case "9" -> {
                        ReporteOcupacion rep = ctl.generarReporte(pedirEntero(in, "Año del reporte (AAAA): "));
                        mostrarReporte(rep);
                        System.out.println("Reporte exportado a: " + ctl.exportarReporte(rep));
                    }
                    case "10" -> {
                        List<String> lineas = ctl.ultimasOperaciones(10);
                        if (lineas.isEmpty()) System.out.println("La bitácora todavía no tiene operaciones.");
                        else lineas.forEach(System.out::println);
                    }
                    case "0" -> salir = true;
                    default -> System.out.println("Opción inválida.");
                }
            } catch (ReglaNegocioException ex) {
                System.out.println("Aviso: " + ex.getMessage());
            } catch (PersistenciaException ex) {
                System.out.println("Error de base de datos: " + ex.getMessage());
            } catch (ArchivoException ex) {
                System.out.println("Error de archivo: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                System.out.println("Aviso: se esperaba un número válido (" + ex.getMessage() + ").");
            } catch (DateTimeParseException ex) {
                System.out.println("Aviso: fecha con formato inválido, use AAAA-MM-DD.");
            } catch (NoSuchElementException ex) {
                salir = true; // fin de la entrada estándar
            } catch (Exception ex) {
                System.out.println("Error inesperado: " + ex.getMessage());
            }
        }
    }

    /** Muestra el reporte recorriendo los arreglos del objeto ReporteOcupacion. */
    private static void mostrarReporte(ReporteOcupacion rep) {
        int[] noches = rep.getNochesPorMes();
        System.out.println("\n-- Ocupación " + rep.getAnio() + " (noches reservadas por mes) --");
        for (int m = 0; m < noches.length; m++) {
            String barra = "#".repeat(Math.min(noches[m], 40));
            System.out.printf("%-11s %4d %s%n", ReporteOcupacion.MESES[m], noches[m], barra);
        }
        System.out.println("Total de noches: " + rep.getTotalNoches());
        int mejor = rep.mesDeMayorOcupacion();
        if (mejor >= 0) System.out.println("Mes de mayor ocupación: " + ReporteOcupacion.MESES[mejor]);
        System.out.println("-- Reservas por estado --");
        Reserva.Estado[] estados = rep.getEstados();
        int[] cantidades = rep.getReservasPorEstado();
        for (int e = 0; e < estados.length; e++) {
            System.out.printf("%-11s %4d%n", estados[e], cantidades[e]);
        }
    }

    private static LocalDate pedirFecha(Scanner in, String msg) {
        System.out.print(msg);
        return LocalDate.parse(in.nextLine().trim());
    }

    private static int pedirEntero(Scanner in, String msg) {
        System.out.print(msg);
        return Integer.parseInt(in.nextLine().trim());
    }
}
