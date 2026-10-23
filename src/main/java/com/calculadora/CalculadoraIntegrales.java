package com.calculadora;

import com.calculadora.matematicas.MotorSimbolico;
import com.calculadora.vista.PantallaBienvenida;
import javax.swing.SwingUtilities;

public class CalculadoraIntegrales {

    public static void main(String[] args) {
        // El motor de cálculo se carga mientras se ve la bienvenida.
        MotorSimbolico.precalentar();

        SwingUtilities.invokeLater(() -> {
            PantallaBienvenida bienvenida =
                    new PantallaBienvenida();

            bienvenida.mostrar();
        });
    }
}
