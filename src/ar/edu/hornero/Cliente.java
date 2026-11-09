package ar.edu.hornero;

public class Cliente extends Persona {
    private String telefono;
    private String email;

    public Cliente(String nombre, String apellido, String documento, String telefono, String email) {
        super(nombre, apellido, documento);
        this.telefono = telefono;
        this.email = email;
    }

    @Override
    public String obtenerDetalles() {
        return "Cliente: " + nombre + " " + apellido + " | DNI: " + documento + " | Tel: " + telefono;
    }

    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
}
