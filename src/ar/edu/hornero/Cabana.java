package ar.edu.hornero;

import java.math.BigDecimal;

public class Cabana implements Comparable<Cabana> {
    private final int idCabana;
    private final int numero;
    private final int capacidad;
    private final BigDecimal tarifaNoche;

    public Cabana(int idCabana, int numero, int capacidad, BigDecimal tarifaNoche) {
        this.idCabana = idCabana;
        this.numero = numero;
        this.capacidad = capacidad;
        this.tarifaNoche = tarifaNoche;
    }

    public int getIdCabana() { return idCabana; }
    public int getNumero() { return numero; }
    public int getCapacidad() { return capacidad; }
    public BigDecimal getTarifaNoche() { return tarifaNoche; }

    @Override 
    public String toString() {
        return "Cabaña " + numero + " (cap. " + capacidad + ", $" + tarifaNoche + "/noche)";
    }

    @Override
    public int compareTo(Cabana otra) {
        return this.tarifaNoche.compareTo(otra.tarifaNoche);
    }
}
