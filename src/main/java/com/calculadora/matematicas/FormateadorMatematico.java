
package com.calculadora.matematicas;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mejora la presentación de expresiones algebraicas.
 *
 * Ordena términos polinómicos de mayor a menor exponente
 * sin modificar los cálculos realizados por Symja.
 *
 * Las expresiones que no reconoce con seguridad se conservan
 * en su orden original.
 */
public final class FormateadorMatematico {

    private FormateadorMatematico() {
        // Clase de utilidades: no necesita instancias.
    }

    /**
     * Formatea una expresión respecto a la variable indicada.
     *
     * Ejemplo:
     * 2*x - 3*x^2 + x^4 -> x^4 - 3*x^2 + 2*x
     */
    public static String formatear(
            String expresion, String variable) {

        if (expresion == null || expresion.isBlank()) {
            return expresion;
        }

        if (!"x".equals(variable) && !"y".equals(variable)) {
            return expresion;
        }

        String limpia = expresion.trim();

        // No alterar funciones ni expresiones que no podamos
        // clasificar con seguridad.
        if (!esPolinomioCompatible(limpia, variable)) {
            return limpia;
        }

        List<Termino> terminos = separarTerminos(limpia);

        if (terminos.size() < 2) {
            return limpia;
        }

        // Orden descendente por grado. En empates se conserva
        // el orden original.
        terminos.sort(
                Comparator.comparingInt(Termino::grado)
                        .reversed()
                        .thenComparingInt(Termino::posicion)
        );

        return reconstruir(terminos);
    }

    private static boolean esPolinomioCompatible(
            String expresion, String variable) {

        // Solo permite constantes numéricas, x/y, operaciones
        // aritméticas y exponentes enteros no negativos.
        // Si encuentra funciones, paréntesis u otros símbolos,
        // devuelve la expresión original sin reorganizarla.
        String permitida = "[0-9xyXY+*/^().\\s-]+";

        if (!expresion.matches(permitida)) {
            return false;
        }

        if (expresion.contains("(") || expresion.contains(")")) {
            return false;
        }

        // No reorganizar polinomios que incluyan ambas variables:
        // no se define aquí un orden multivariable.
        String otra = "x".equals(variable) ? "y" : "x";

        return !expresion.toLowerCase().contains(otra);
    }

    private static List<Termino> separarTerminos(
            String expresion) {

        List<Termino> terminos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        int posicion = 0;

        for (int i = 0; i < expresion.length(); i++) {
            char c = expresion.charAt(i);

            if ((c == '+' || c == '-')
                    && i > 0
                    && !esOperadorUnario(expresion, i)) {

                agregarTermino(terminos, actual.toString(), posicion++);
                actual.setLength(0);
            }

            actual.append(c);
        }

        if (!actual.isEmpty()) {
            agregarTermino(terminos, actual.toString(), posicion);
        }

        return terminos;
    }

    private static boolean esOperadorUnario(
            String expresion, int indice) {

        char anterior = expresion.charAt(indice - 1);

        return anterior == '+'
                || anterior == '-'
                || anterior == '*'
                || anterior == '/'
                || anterior == '^';
    }

    private static void agregarTermino(
            List<Termino> terminos,
            String texto,
            int posicion) {

        String limpio = texto.trim();

        if (limpio.isEmpty() || "+".equals(limpio)
                || "-".equals(limpio)) {
            return;
        }

        int grado = calcularGrado(limpio);

        terminos.add(new Termino(limpio, grado, posicion));
    }

    private static int calcularGrado(String termino) {
        Pattern patron = Pattern.compile(
                "([xy])(?:\\^(\\d+))?",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = patron.matcher(termino);
        int grado = 0;

        while (matcher.find()) {
            int exponente = matcher.group(2) == null
                    ? 1
                    : Integer.parseInt(matcher.group(2));

            grado += exponente;
        }

        return grado;
    }

    private static String reconstruir(List<Termino> terminos) {
        StringBuilder resultado = new StringBuilder();

        for (Termino termino : terminos) {
            String texto = termino.texto();

            if (resultado.isEmpty()) {
                resultado.append(texto);
            } else if (texto.startsWith("-")) {
                resultado.append(" - ")
                        .append(texto.substring(1).trim());
            } else {
                resultado.append(" + ").append(texto);
            }
        }

        return resultado.toString();
    }

    private record Termino(
            String texto, int grado, int posicion) {
    }
}
