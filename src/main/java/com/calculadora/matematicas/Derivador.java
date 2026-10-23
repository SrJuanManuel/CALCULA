
package com.calculadora.matematicas;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

public class Derivador {

    private final ExprEvaluator evaluador;

    public Derivador() {
        evaluador = new ExprEvaluator();
    }

    public String derivar(String funcion, String variable) {
        validarFuncion(funcion);
        validarVariable(variable);

        String expresion = normalizarFuncion(funcion);

        IExpr resultado = evaluador.eval(
                "D(" + expresion + ", " + variable + ")"
        );

        if (resultado == null
                || resultado.toString().startsWith("D(")) {
            throw new IllegalArgumentException(
                    "No se pudo calcular la derivada de esta función."
            );
        }

        resultado = ordenarResultado(resultado, variable);

        return FormateadorMatematico.formatear(
        resultado.toString(), variable);
    }

    private IExpr ordenarResultado(
            IExpr resultado, String variable) {

        String expresion = resultado.toString();

        IExpr expandido = evaluador.eval(
                "Expand(" + expresion + ")"
        );

        IExpr agrupado = evaluador.eval(
                "Collect(" + expandido + ", " + variable + ")"
        );

        if (agrupado == null
                || agrupado.toString().startsWith("Collect(")) {
            return expandido;
        }

        return agrupado;
    }

    private void validarFuncion(String funcion) {
        if (funcion == null || funcion.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Ingresa una función matemática."
            );
        }

        String expresion = normalizarFuncion(funcion);

        if (!expresion.matches(
                "[0-9a-zA-Z_+*/^().,\\s-]+")) {
            throw new IllegalArgumentException(
                    "La función contiene caracteres no admitidos."
            );
        }
    }

    private void validarVariable(String variable) {
        if (!"x".equals(variable) && !"y".equals(variable)) {
            throw new IllegalArgumentException(
                    "La variable debe ser x o y."
            );
        }
    }

    private String normalizarFuncion(String funcion) {
        return funcion.trim()
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-");
    }
}
