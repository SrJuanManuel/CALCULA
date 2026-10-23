package com.calculadora.matematicas;

import org.matheclipse.core.eval.ExprEvaluator;

/**
 * Precalienta el motor simbólico (Symja).
 *
 * La primera operación de Symja tarda bastante porque carga sus reglas. Si
 * ocurre cuando el usuario pulsa "Resolver", la ventana parece congelada. Aquí
 * se hace en un hilo aparte mientras se muestra la pantalla de bienvenida.
 */
public final class MotorSimbolico {

    private static boolean iniciado;

    private MotorSimbolico() {
    }

    public static synchronized void precalentar() {
        if (iniciado) {
            return;
        }
        iniciado = true;

        Thread hilo = new Thread(() -> {
            try {
                ExprEvaluator evaluador = new ExprEvaluator();
                evaluador.eval("Integrate(x^2, x)");
                evaluador.eval("D(Sin(x), x)");
                evaluador.eval("N(Sqrt(2))");
            } catch (Throwable ex) {
                // Es solo una optimización: si falla, se carga al usarlo.
            }
        }, "calcula-precalentamiento");
        hilo.setDaemon(true);
        hilo.setPriority(Thread.NORM_PRIORITY - 1);
        hilo.start();
    }
}
