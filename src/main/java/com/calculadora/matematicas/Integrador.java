
package com.calculadora.matematicas;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

public class Integrador {

    private final ExprEvaluator evaluador;

    public Integrador() {
        evaluador = new ExprEvaluator();
    }

    public String integrarIndefinida(
            String funcion, String variable) {

        validarFuncion(funcion);
        validarVariable(variable);

        String expresion = normalizarFuncion(funcion);

        IExpr resultado = evaluador.eval(
                "Integrate(" + expresion + ", " + variable + ")"
        );

        comprobarResultado(resultado, "integral indefinida");

        resultado = ordenarResultado(resultado, variable);

        String resultadoFormateado =
        FormateadorMatematico.formatear(
                resultado.toString(), variable);

        return resultadoFormateado + " + C";
    }

    public String integrarDefinida(
            String funcion,
            String variable,
            double limiteInferior,
            double limiteSuperior) {

        validarFuncion(funcion);
        validarVariable(variable);

        if (!Double.isFinite(limiteInferior)
                || !Double.isFinite(limiteSuperior)) {
            throw new IllegalArgumentException(
                    "Los límites deben ser números finitos."
            );
        }

        if (limiteInferior >= limiteSuperior) {
            throw new IllegalArgumentException(
                    "El límite inferior debe ser menor que el superior."
            );
        }

        String expresion = normalizarFuncion(funcion);

        String operacion = "Integrate("
                + expresion + ", {"
                + variable + ", "
                + limiteInferior + ", "
                + limiteSuperior + "})";

        IExpr resultado = evaluador.eval(operacion);

        comprobarResultado(resultado, "integral definida");

        return resultado.toString();
    }

    private IExpr ordenarResultado(
            IExpr resultado, String variable) {

        String expresion = resultado.toString();

        IExpr expandido = evaluador.eval(
                "Expand(" + expresion + ")"
        );

        comprobarResultado(expandido, "integral indefinida");

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

    private void comprobarResultado(
            IExpr resultado, String operacion) {

        if (resultado == null
                || resultado.toString().startsWith("Integrate(")) {
            throw new IllegalArgumentException(
                    "No se pudo resolver la " + operacion
                    + ". Verifica la función o prueba otra expresión."
            );
        }
    }
}
