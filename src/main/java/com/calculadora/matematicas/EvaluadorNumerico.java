package com.calculadora.matematicas;

import java.util.function.DoubleUnaryOperator;

/**
 * Evaluador numérico rápido para graficar.
 *
 * Convierte una expresión de una variable (por ejemplo "3*x^2 + Sin(x)") en
 * una función de Java que se puede llamar miles de veces sin pasar por el
 * motor simbólico, que es mucho más lento porque interpreta el texto en cada
 * llamada.
 *
 * Solo entiende lo habitual: números, la variable, Pi, E, + - * / ^,
 * paréntesis, multiplicación implícita (2x, 3(x+1)) y las funciones Sin, Cos,
 * Tan, Cot, Sec, Csc, ArcSin, ArcCos, ArcTan, Sinh, Cosh, Tanh, Sqrt, Exp, Abs,
 * Log (natural, o Log(base, valor)) y Ln. Si aparece algo distinto,
 * {@link #compilar} devuelve null y quien llama usa el motor simbólico.
 */
public final class EvaluadorNumerico {

    private EvaluadorNumerico() {
    }

    /** Nodo del árbol de la expresión. */
    private interface Nodo {
        double evaluar(double v);
    }

    /** Se lanza cuando la expresión usa algo que este evaluador no soporta. */
    private static final class NoSoportado extends RuntimeException {

        private static final long serialVersionUID = 1L;

        NoSoportado() {
            super(null, null, false, false);
        }
    }

    /**
     * @return la función compilada, o null si la expresión no se puede
     * evaluar con este evaluador (conviene entonces usar Symja)
     */
    public static DoubleUnaryOperator compilar(String expresion, String variable) {
        if (expresion == null || variable == null || variable.isEmpty()) {
            return null;
        }
        try {
            Analizador analizador = new Analizador(expresion, variable);
            Nodo raiz = analizador.analizar();
            return raiz::evaluar;
        } catch (NoSoportado | StackOverflowError ex) {
            return null;
        }
    }

    private static final class Analizador {

        private final String texto;
        private final String variable;
        private int pos;

        Analizador(String texto, String variable) {
            this.texto = texto;
            this.variable = variable;
        }

        Nodo analizar() {
            Nodo n = expresion();
            saltarEspacios();
            if (pos != texto.length()) {
                throw new NoSoportado();
            }
            return n;
        }

        // expresion := termino (('+' | '-') termino)*
        private Nodo expresion() {
            Nodo n = termino();
            while (true) {
                saltarEspacios();
                if (consumir('+')) {
                    Nodo a = n;
                    Nodo b = termino();
                    n = v -> a.evaluar(v) + b.evaluar(v);
                } else if (consumir('-')) {
                    Nodo a = n;
                    Nodo b = termino();
                    n = v -> a.evaluar(v) - b.evaluar(v);
                } else {
                    return n;
                }
            }
        }

        // termino := unario (('*' | '/' | implícito) ...)*
        private Nodo termino() {
            Nodo n = unario();
            while (true) {
                saltarEspacios();
                if (consumir('*')) {
                    Nodo a = n;
                    Nodo b = unario();
                    n = v -> a.evaluar(v) * b.evaluar(v);
                } else if (consumir('/')) {
                    Nodo a = n;
                    Nodo b = unario();
                    n = v -> a.evaluar(v) / b.evaluar(v);
                } else if (iniciaFactor()) {
                    Nodo a = n;
                    Nodo b = potencia();
                    n = v -> a.evaluar(v) * b.evaluar(v);
                } else {
                    return n;
                }
            }
        }

        private Nodo unario() {
            saltarEspacios();
            if (consumir('-')) {
                Nodo a = unario();
                return v -> -a.evaluar(v);
            }
            if (consumir('+')) {
                return unario();
            }
            return potencia();
        }

        // potencia := atomo ('^' unario)?   (asociativa por la derecha)
        private Nodo potencia() {
            Nodo base = atomo();
            saltarEspacios();
            if (consumir('^')) {
                Nodo exponente = unario();
                return v -> Math.pow(base.evaluar(v), exponente.evaluar(v));
            }
            return base;
        }

        private Nodo atomo() {
            saltarEspacios();
            if (pos >= texto.length()) {
                throw new NoSoportado();
            }
            char c = texto.charAt(pos);

            if (Character.isDigit(c) || c == '.') {
                return numero();
            }
            if (c == '(') {
                pos++;
                Nodo n = expresion();
                saltarEspacios();
                if (!consumir(')')) {
                    throw new NoSoportado();
                }
                return n;
            }
            if (Character.isLetter(c) || c == '_') {
                return identificador();
            }
            throw new NoSoportado();
        }

        private Nodo numero() {
            int inicio = pos;
            while (pos < texto.length()
                    && (Character.isDigit(texto.charAt(pos))
                    || texto.charAt(pos) == '.')) {
                pos++;
            }
            try {
                double valor = Double.parseDouble(texto.substring(inicio, pos));
                return v -> valor;
            } catch (NumberFormatException ex) {
                throw new NoSoportado();
            }
        }

        private Nodo identificador() {
            int inicio = pos;
            while (pos < texto.length()
                    && (Character.isLetterOrDigit(texto.charAt(pos))
                    || texto.charAt(pos) == '_')) {
                pos++;
            }
            String nombre = texto.substring(inicio, pos);

            // ¿Llamada a función?
            int guardado = pos;
            saltarEspacios();
            if (pos < texto.length() && texto.charAt(pos) == '(') {
                Nodo f = funcion(nombre);
                if (f != null) {
                    return f;
                }
                throw new NoSoportado();
            }
            pos = guardado;

            if (nombre.equals(variable)) {
                return v -> v;
            }
            switch (nombre) {
                case "Pi":
                    return v -> Math.PI;
                case "E":
                    return v -> Math.E;
                default:
                    // Otra letra (la otra variable, un símbolo...): lo resuelve Symja.
                    throw new NoSoportado();
            }
        }

        private Nodo funcion(String nombre) {
            consumir('(');
            Nodo a = expresion();
            Nodo b = null;
            saltarEspacios();
            if (consumir(',')) {
                b = expresion();
                saltarEspacios();
            }
            if (!consumir(')')) {
                throw new NoSoportado();
            }

            String n = nombre.toLowerCase();
            if (b != null) {
                if (n.equals("log")) {
                    // Log(base, valor)
                    Nodo base = a;
                    Nodo valor = b;
                    return v -> Math.log(valor.evaluar(v))
                            / Math.log(base.evaluar(v));
                }
                throw new NoSoportado();
            }

            switch (n) {
                case "sin":
                    return v -> Math.sin(a.evaluar(v));
                case "cos":
                    return v -> Math.cos(a.evaluar(v));
                case "tan":
                    return v -> Math.tan(a.evaluar(v));
                case "cot":
                    return v -> 1.0 / Math.tan(a.evaluar(v));
                case "sec":
                    return v -> 1.0 / Math.cos(a.evaluar(v));
                case "csc":
                    return v -> 1.0 / Math.sin(a.evaluar(v));
                case "arcsin":
                    return v -> Math.asin(a.evaluar(v));
                case "arccos":
                    return v -> Math.acos(a.evaluar(v));
                case "arctan":
                    return v -> Math.atan(a.evaluar(v));
                case "sinh":
                    return v -> Math.sinh(a.evaluar(v));
                case "cosh":
                    return v -> Math.cosh(a.evaluar(v));
                case "tanh":
                    return v -> Math.tanh(a.evaluar(v));
                case "sqrt":
                    return v -> Math.sqrt(a.evaluar(v));
                case "exp":
                    return v -> Math.exp(a.evaluar(v));
                case "abs":
                    return v -> Math.abs(a.evaluar(v));
                case "log":
                case "ln":
                    return v -> Math.log(a.evaluar(v));
                default:
                    return null;
            }
        }

        /** ¿Empieza aquí un factor sin operador (multiplicación implícita)? */
        private boolean iniciaFactor() {
            if (pos >= texto.length()) {
                return false;
            }
            char c = texto.charAt(pos);
            return Character.isLetter(c) || Character.isDigit(c)
                    || c == '.' || c == '(' || c == '_';
        }

        private boolean consumir(char esperado) {
            if (pos < texto.length() && texto.charAt(pos) == esperado) {
                pos++;
                return true;
            }
            return false;
        }

        private void saltarEspacios() {
            while (pos < texto.length()
                    && Character.isWhitespace(texto.charAt(pos))) {
                pos++;
            }
        }
    }
}
