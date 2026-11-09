package ar.edu.hornero;

import java.io.IOException;

/** Excepción verificada de la capa de archivos. Envuelve la IOException original. */
public class ArchivoException extends Exception {
    private static final long serialVersionUID = 1L;

    public ArchivoException(String mensaje, IOException causa) {
        super(mensaje, causa);
    }
}
