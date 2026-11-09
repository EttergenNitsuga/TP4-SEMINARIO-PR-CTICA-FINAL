package ar.edu.hornero;

/**
 * Excepción no verificada: el usuario pidió algo que las reglas del negocio no permiten
 * (cabaña ocupada, monto inválido, estado incorrecto, etc.).
 */
public class ReglaNegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
