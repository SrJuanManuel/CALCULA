package com.calculadora.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistroOperacion implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String tipo;
    private final String funcion;
    private final String variable;
    private final String resultado;
    private final String detalles;
    private final LocalDateTime fechaHora;

    public RegistroOperacion(String tipo, String funcion, String variable,
            String resultado, String detalles) {
        this.tipo = tipo;
        this.funcion = funcion;
        this.variable = variable;
        this.resultado = resultado;
        this.detalles = detalles;
        this.fechaHora = LocalDateTime.now();
    }

    public String getTipo() { return tipo; }
    public String getFuncion() { return funcion; }
    public String getVariable() { return variable; }
    public String getResultado() { return resultado; }
    public String getDetalles() { return detalles; }
    public LocalDateTime getFechaHora() { return fechaHora; }

    public String getFechaFormateada() {
        return fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    @Override
    public String toString() {
        return "[" + tipo + "] " + funcion + " respecto a " + variable
                + "  |  " + getFechaFormateada();
    }
}
