
package com.calculadora.matematicas;

import java.util.ArrayList;
import java.util.List;

public class ProcedimientoIntegral {

    public List<String> generarPasos(
            String funcion,
            String variable,
            boolean definida,
            double limiteInferior,
            double limiteSuperior) {

        List<String> pasos = new ArrayList<>();

        String f = funcion.trim();
        String v = variable;

        pasos.add("1. Función original:");
        pasos.add("   f(" + v + ") = " + f);

        pasos.add("");
        pasos.add("2. Identificar la operación:");
        pasos.add(definida
                ? "   Se calculará una integral definida."
                : "   Se calculará una integral indefinida.");

        if (definida) {
            pasos.add("");
            pasos.add("3. Establecer los límites:");
            pasos.add("   Límite inferior: " + limiteInferior);
            pasos.add("   Límite superior: " + limiteSuperior);

            pasos.add("");
            pasos.add("4. Aplicar las reglas de integración:");
            pasos.add("   Se busca una antiderivada de la función.");

            pasos.add("");
            pasos.add("5. Evaluar en los límites:");
            pasos.add("   Se calcula F(" + limiteSuperior + ")"
                    + " - F(" + limiteInferior + ").");

        } else {
            pasos.add("");
            pasos.add("3. Aplicar las reglas de integración:");
            pasos.add("   Se busca una antiderivada de la función.");

            pasos.add("");
            pasos.add("4. Agregar la constante de integración:");
            pasos.add("   La constante C representa las posibles"
                    + " antiderivadas.");
        }

        pasos.add("");
        pasos.add("Nota: estos pasos describen el proceso general."
                + " Las reglas específicas se incorporarán"
                + " progresivamente.");

        return pasos;
    }
}
