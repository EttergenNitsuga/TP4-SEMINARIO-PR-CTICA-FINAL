package ar.edu.hornero;

public abstract class Persona {
    protected String nombre;
    protected String apellido;
    protected String documento;

    public Persona(String nombre, String apellido, String documento) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
    }

    public abstract String obtenerDetalles();

    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
}
